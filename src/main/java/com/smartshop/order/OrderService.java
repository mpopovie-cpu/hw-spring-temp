package com.smartshop.order;

import com.smartshop.exception.BusinessException;
import com.smartshop.exception.InvalidOrderStateException;
import com.smartshop.exception.ResourceNotFoundException;
import com.smartshop.inventory.InventoryService;
import com.smartshop.product.Product;
import com.smartshop.product.ProductService;
import com.smartshop.user.User;
import com.smartshop.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    // разрешённые переходы статуса
    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(OrderStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(OrderStatus.PENDING, EnumSet.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(OrderStatus.CONFIRMED, EnumSet.of(OrderStatus.PROCESSING, OrderStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(OrderStatus.PROCESSING, EnumSet.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(OrderStatus.SHIPPED, EnumSet.of(OrderStatus.COMPLETED));
        ALLOWED_TRANSITIONS.put(OrderStatus.COMPLETED, EnumSet.noneOf(OrderStatus.class));
        ALLOWED_TRANSITIONS.put(OrderStatus.CANCELLED, EnumSet.noneOf(OrderStatus.class));
    }

    private static final Set<OrderStatus> CANCELLABLE_STATUSES =
            EnumSet.of(OrderStatus.PENDING, OrderStatus.CONFIRMED, OrderStatus.PROCESSING);

    private final OrderRepository orderRepository;
    private final UserService userService;
    private final ProductService productService;
    private final InventoryService inventoryService;

    public OrderService(OrderRepository orderRepository, UserService userService,
                        ProductService productService, InventoryService inventoryService) {
        this.orderRepository = orderRepository;
        this.userService = userService;
        this.productService = productService;
        this.inventoryService = inventoryService;
    }

    @Transactional
    public Order createOrder(Long userId, Map<Long, Integer> productQuantities) {
        if (productQuantities == null || productQuantities.isEmpty()) {
            throw new BusinessException("Order must contain at least one product");
        }

        User user = userService.getUserById(userId);

        LocalDateTime now = LocalDateTime.now();
        Order order = new Order(user, OrderStatus.PENDING, BigDecimal.ZERO, now, now);

        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<Long, Integer> entry : productQuantities.entrySet()) {
            Long productId = entry.getKey();
            Integer quantity = entry.getValue();

            if (quantity == null || quantity <= 0) {
                throw new BusinessException("Quantity must be positive for productId=" + productId);
            }

            Product product = productService.getProductById(productId);
            if (!product.isActive()) {
                throw new BusinessException("Product is not active: id=" + productId);
            }

            // резервируем склад до создания позиции
            inventoryService.reserveStock(productId, quantity);

            BigDecimal priceAtPurchase = product.getPrice();
            BigDecimal subtotal = priceAtPurchase.multiply(BigDecimal.valueOf(quantity));

            OrderItem item = new OrderItem(product, quantity, priceAtPurchase, subtotal);
            order.addItem(item);

            total = total.add(subtotal);
        }

        order.setTotalPrice(total);

        Order saved = orderRepository.save(order);
        log.info("Order created id={} userId={} total={}", saved.getId(), userId, total);
        return saved;
    }

    @Transactional(readOnly = true)
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: id=" + id));
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersByUser(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    @Transactional
    public Order cancelOrder(Long orderId) {
        Order order = getOrderById(orderId);

        if (!CANCELLABLE_STATUSES.contains(order.getStatus())) {
            throw new InvalidOrderStateException("Cannot cancel order in status " + order.getStatus());
        }

        for (OrderItem item : order.getItems()) {
            inventoryService.releaseStock(item.getProduct().getId(), item.getQuantity());
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());

        Order saved = orderRepository.save(order);
        log.info("Order cancelled id={}", orderId);
        return saved;
    }

    @Transactional
    public Order updateStatus(Long orderId, OrderStatus newStatus) {
        if (newStatus == OrderStatus.CANCELLED) {
            // отмена всегда должна освобождать резерв склада
            return cancelOrder(orderId);
        }

        Order order = getOrderById(orderId);
        OrderStatus current = order.getStatus();

        Set<OrderStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(current, EnumSet.noneOf(OrderStatus.class));
        if (!allowed.contains(newStatus)) {
            throw new InvalidOrderStateException("Invalid status transition: " + current + " -> " + newStatus);
        }

        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());

        return orderRepository.save(order);
    }
}