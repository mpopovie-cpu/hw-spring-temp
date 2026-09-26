package com.smartshop.product;

import com.smartshop.category.Category;
import com.smartshop.category.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void savesProductWithCategory() {
        Category category = categoryRepository.save(new Category("Toys", "Kids toys", true));
        LocalDateTime now = LocalDateTime.now();

        Product product = productRepository.save(
                new Product("Lego Set", "Building blocks", new BigDecimal("49.99"), true, now, now, category));

        assertThat(product.getId()).isNotNull();
        assertThat(product.getCategory().getName()).isEqualTo("Toys");
    }

    @Test
    void findsOnlyActiveProducts() {
        Category category = categoryRepository.save(new Category("Home", "Home goods", true));
        LocalDateTime now = LocalDateTime.now();
        productRepository.save(new Product("Lamp", "Desk lamp", new BigDecimal("19.99"), true, now, now, category));
        productRepository.save(new Product("Old Lamp", "Discontinued", new BigDecimal("9.99"), false, now, now, category));

        List<Product> active = productRepository.findByActiveTrue();

        assertThat(active).extracting(Product::getName).containsExactly("Lamp");
    }

    @Test
    void findsByNameIgnoringCase() {
        Category category = categoryRepository.save(new Category("Sports", "Sport gear", true));
        LocalDateTime now = LocalDateTime.now();
        productRepository.save(new Product("Football", "Ball", new BigDecimal("15.00"), true, now, now, category));

        List<Product> found = productRepository.findByNameContainingIgnoreCase("foot");

        assertThat(found).hasSize(1);
    }

    @Test
    void findsByCategoryId() {
        Category category = categoryRepository.save(new Category("Music", "Instruments", true));
        LocalDateTime now = LocalDateTime.now();
        productRepository.save(new Product("Guitar", "Acoustic", new BigDecimal("120.00"), true, now, now, category));

        List<Product> found = productRepository.findByCategoryId(category.getId());

        assertThat(found).hasSize(1);
    }
}