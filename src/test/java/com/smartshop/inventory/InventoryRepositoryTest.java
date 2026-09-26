package com.smartshop.inventory;

import com.smartshop.category.Category;
import com.smartshop.category.CategoryRepository;
import com.smartshop.product.Product;
import com.smartshop.product.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class InventoryRepositoryTest {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void savesAndFindsByProductId() {
        Category category = categoryRepository.save(new Category("Kitchen", "Kitchenware", true));
        LocalDateTime now = LocalDateTime.now();
        Product product = productRepository.save(
                new Product("Blender", "500W", new BigDecimal("39.90"), true, now, now, category));

        inventoryRepository.save(new Inventory(100, 10, now, product));

        Optional<Inventory> found = inventoryRepository.findByProductId(product.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getQuantity()).isEqualTo(100);
        assertThat(found.get().getReservedQuantity()).isEqualTo(10);
    }

    @Test
    void rejectsNegativeQuantity() {
        Category category = categoryRepository.save(new Category("Garden", "Garden tools", true));
        LocalDateTime now = LocalDateTime.now();
        Product product = productRepository.save(
                new Product("Shovel", "Steel", new BigDecimal("12.00"), true, now, now, category));

        assertThat(catchQuantityViolation(product, now)).isTrue();
    }

    private boolean catchQuantityViolation(Product product, LocalDateTime now) {
        try {
            new Inventory(-1, 0, now, product);
            return false;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }
}