package com.smartshop.order;

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
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void savesOrderForUser() {
        User user = userRepository.save(
                new User("buyer", "buyer@example.com", "hash", Role.CUSTOMER, true, LocalDateTime.now()));
        LocalDateTime now = LocalDateTime.now();

        Order order = orderRepository.save(new Order(user, OrderStatus.PENDING, new BigDecimal("100.00"), now, now));

        assertThat(order.getId()).isNotNull();
    }

    @Test
    void findsOrdersByUser() {
        User user = userRepository.save(
                new User("shopper", "shopper@example.com", "hash", Role.CUSTOMER, true, LocalDateTime.now()));
        LocalDateTime now = LocalDateTime.now();
        orderRepository.save(new Order(user, OrderStatus.PENDING, new BigDecimal("50.00"), now, now));

        List<Order> orders = orderRepository.findByUserId(user.getId());

        assertThat(orders).hasSize(1);
    }

    @Test
    void findsOrdersByStatus() {
        User user = userRepository.save(
                new User("statususer", "statususer@example.com", "hash", Role.CUSTOMER, true, LocalDateTime.now()));
        LocalDateTime now = LocalDateTime.now();
        orderRepository.save(new Order(user, OrderStatus.SHIPPED, new BigDecimal("75.00"), now, now));

        List<Order> shipped = orderRepository.findByStatus(OrderStatus.SHIPPED);

        assertThat(shipped).hasSize(1);
    }
}