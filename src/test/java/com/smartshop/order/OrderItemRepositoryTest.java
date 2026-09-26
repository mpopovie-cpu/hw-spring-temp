package com.smartshop.order;

import com.smartshop.category.Category;
import com.smartshop.category.CategoryRepository;
import com.smartshop.product.Product;
import com.smartshop.product.ProductRepository;
import com.smartshop.user.Role;
import com.smartshop.user.User;
import com.smartshop.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class OrderItemRepositoryTest {

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void savesAndFindsItemsByOrder() {
        Category category = categoryRepository.save(new Category("Office", "Office supplies", true));
        LocalDateTime now = LocalDateTime.now();
        Product product = productRepository.save(
                new Product("Notebook", "A5", new BigDecimal("3.50"), true, now, now, category));
        User user = userRepository.save(
                new User("orderer", "orderer@example.com", "hash", Role.CUSTOMER, true, now));
        Order order = orderRepository.save(new Order(user, OrderStatus.PENDING, new BigDecimal("7.00"), now, now));

        OrderItem item = new OrderItem(product, 2, new BigDecimal("3.50"), new BigDecimal("7.00"));
        order.addItem(item);
        orderRepository.save(order);

        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());

        assertThat(items).hasSize(1);
        assertThat(items.get(0).getPriceAtPurchase()).isEqualByComparingTo("3.50");
    }
}