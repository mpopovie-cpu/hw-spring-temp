package com.smartshop.order;

import com.smartshop.category.Category;
import com.smartshop.exception.BusinessException;
import com.smartshop.exception.InsufficientStockException;
import com.smartshop.exception.InvalidOrderStateException;
import com.smartshop.exception.ResourceNotFoundException;
import com.smartshop.inventory.InventoryService;
import com.smartshop.product.Product;
import com.smartshop.product.ProductService;
import com.smartshop.user.Role;
import com.smartshop.user.User;
import com.smartshop.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserService userService;

    @Mock
    private ProductService productService;

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createsOrderSuccessfully() {
        User user = new User(
                "buyer",
                "buyer@example.com",
                "hash",
                Role.CUSTOMER,
                true,
                LocalDateTime.now()
        );

        Product product = activeProduct(1L, "10.00");

        when(userService.getUserById(1L)).thenReturn(user);
        when(productService.getProductById(1L)).thenReturn(product);
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Map<Long, Integer> items = new LinkedHashMap<>();
        items.put(1L, 3);

        Order order = orderService.createOrder(1L, items);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getItems()).hasSize(1);
    }

    @Test
    void computesPriceAtPurchaseFromCurrentProductPrice() {
        User user = new User(
                "buyer",
                "buyer@example.com",
                "hash",
                Role.CUSTOMER,
                true,
                LocalDateTime.now()
        );

        Product product = activeProduct(1L, "19.99");

        when(userService.getUserById(1L)).thenReturn(user);
        when(productService.getProductById(1L)).thenReturn(product);
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Order order = orderService.createOrder(1L, Map.of(1L, 2));

        OrderItem item = order.getItems().get(0);

        assertThat(item.getPriceAtPurchase())
                .isEqualByComparingTo("19.99");
    }

    @Test
    void computesItemSubtotalCorrectly() {
        User user = new User(
                "buyer",
                "buyer@example.com",
                "hash",
                Role.CUSTOMER,
                true,
                LocalDateTime.now()
        );

        Product product = activeProduct(1L, "5.50");

        when(userService.getUserById(1L)).thenReturn(user);
        when(productService.getProductById(1L)).thenReturn(product);
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Order order = orderService.createOrder(1L, Map.of(1L, 4));

        OrderItem item = order.getItems().get(0);

        assertThat(item.getSubtotal())
                .isEqualByComparingTo("22.00");
    }

    @Test
    void computesOrderTotalAcrossItems() {
        User user = new User(
                "buyer",
                "buyer@example.com",
                "hash",
                Role.CUSTOMER,
                true,
                LocalDateTime.now()
        );

        Product product1 = activeProduct(1L, "10.00");
        Product product2 = activeProduct(2L, "3.00");

        when(userService.getUserById(1L)).thenReturn(user);
        when(productService.getProductById(1L)).thenReturn(product1);
        when(productService.getProductById(2L)).thenReturn(product2);
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Map<Long, Integer> items = new LinkedHashMap<>();
        items.put(1L, 2);
        items.put(2L, 1);

        Order order = orderService.createOrder(1L, items);

        assertThat(order.getTotalPrice())
                .isEqualByComparingTo("23.00");
    }

    @Test
    void reservesStockForEveryItem() {
        User user = new User(
                "buyer",
                "buyer@example.com",
                "hash",
                Role.CUSTOMER,
                true,
                LocalDateTime.now()
        );

        Product product = activeProduct(1L, "10.00");

        when(userService.getUserById(1L)).thenReturn(user);
        when(productService.getProductById(1L)).thenReturn(product);
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        orderService.createOrder(1L, Map.of(1L, 3));

        verify(inventoryService).reserveStock(1L, 3);
    }

    @Test
    void rejectsMissingProduct() {
        User user = new User(
                "buyer",
                "buyer@example.com",
                "hash",
                Role.CUSTOMER,
                true,
                LocalDateTime.now()
        );

        when(userService.getUserById(1L)).thenReturn(user);
        when(productService.getProductById(99L))
                .thenThrow(new ResourceNotFoundException("not found"));

        assertThatThrownBy(() ->
                orderService.createOrder(1L, Map.of(99L, 1))
        ).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void rejectsInactiveProduct() {
        User user = new User(
                "buyer",
                "buyer@example.com",
                "hash",
                Role.CUSTOMER,
                true,
                LocalDateTime.now()
        );

        Product inactive = activeProduct(1L, "10.00");
        inactive.setActive(false);

        when(userService.getUserById(1L)).thenReturn(user);
        when(productService.getProductById(1L)).thenReturn(inactive);

        assertThatThrownBy(() ->
                orderService.createOrder(1L, Map.of(1L, 1))
        ).isInstanceOf(BusinessException.class);
    }

    @Test
    void rejectsEmptyOrder() {
        assertThatThrownBy(() ->
                orderService.createOrder(1L, Map.of())
        ).isInstanceOf(BusinessException.class);
    }

    @Test
    void rejectsInvalidQuantity() {
        assertThatThrownBy(() ->
                orderService.createOrder(1L, Map.of(1L, 0))
        ).isInstanceOf(BusinessException.class);
    }

    @Test
    void propagatesInsufficientStock() {
        User user = new User(
                "buyer",
                "buyer@example.com",
                "hash",
                Role.CUSTOMER,
                true,
                LocalDateTime.now()
        );

        Product product = activeProduct(1L, "10.00");

        when(userService.getUserById(1L)).thenReturn(user);
        when(productService.getProductById(1L)).thenReturn(product);

        Mockito.doThrow(new InsufficientStockException("no stock"))
                .when(inventoryService)
                .reserveStock(1L, 5);

        assertThatThrownBy(() ->
                orderService.createOrder(1L, Map.of(1L, 5))
        ).isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void cancellingReleasesStockForEveryItem() {
        Order order = pendingOrderWithItem(1L, 2);

        when(orderRepository.findById(10L))
                .thenReturn(Optional.of(order));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        orderService.cancelOrder(10L);

        verify(inventoryService).releaseStock(1L, 2);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void cannotCancelShippedOrder() {
        Order order = orderWithStatus(OrderStatus.SHIPPED);

        when(orderRepository.findById(10L))
                .thenReturn(Optional.of(order));

        assertThatThrownBy(() ->
                orderService.cancelOrder(10L)
        ).isInstanceOf(InvalidOrderStateException.class);
    }

    @Test
    void cannotCancelCompletedOrder() {
        Order order = orderWithStatus(OrderStatus.COMPLETED);

        when(orderRepository.findById(10L))
                .thenReturn(Optional.of(order));

        assertThatThrownBy(() ->
                orderService.cancelOrder(10L)
        ).isInstanceOf(InvalidOrderStateException.class);
    }

    @Test
    void cannotCancelAlreadyCancelledOrder() {
        Order order = orderWithStatus(OrderStatus.CANCELLED);

        when(orderRepository.findById(10L))
                .thenReturn(Optional.of(order));

        assertThatThrownBy(() ->
                orderService.cancelOrder(10L)
        ).isInstanceOf(InvalidOrderStateException.class);
    }

    @Test
    void allowsValidStatusTransition() {
        Order order = orderWithStatus(OrderStatus.PENDING);

        when(orderRepository.findById(10L))
                .thenReturn(Optional.of(order));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Order updated = orderService.updateStatus(
                10L,
                OrderStatus.CONFIRMED
        );

        assertThat(updated.getStatus())
                .isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void rejectsInvalidStatusTransition() {
        Order order = orderWithStatus(OrderStatus.PENDING);

        when(orderRepository.findById(10L))
                .thenReturn(Optional.of(order));

        assertThatThrownBy(() ->
                orderService.updateStatus(
                        10L,
                        OrderStatus.SHIPPED
                )
        ).isInstanceOf(InvalidOrderStateException.class);
    }

    private Product activeProduct(Long id, String price) {
        Category category = new Category(
                "Category" + id,
                "desc",
                true
        );

        Product product = new Product(
                "Product" + id,
                "desc",
                new BigDecimal(price),
                true,
                LocalDateTime.now(),
                LocalDateTime.now(),
                category
        );

        ReflectionTestUtils.setField(product, "id", id);

        return product;
    }

    private Order orderWithStatus(OrderStatus status) {
        User user = new User(
                "buyer",
                "buyer@example.com",
                "hash",
                Role.CUSTOMER,
                true,
                LocalDateTime.now()
        );

        LocalDateTime now = LocalDateTime.now();

        return new Order(
                user,
                status,
                BigDecimal.ZERO,
                now,
                now
        );
    }

    private Order pendingOrderWithItem(Long productId, int quantity) {
        Order order = orderWithStatus(OrderStatus.PENDING);

        Product product = activeProduct(
                productId,
                "10.00"
        );

        OrderItem item = new OrderItem(
                product,
                quantity,
                new BigDecimal("10.00"),
                new BigDecimal("10.00")
                        .multiply(BigDecimal.valueOf(quantity))
        );

        order.addItem(item);

        return order;
    }
}