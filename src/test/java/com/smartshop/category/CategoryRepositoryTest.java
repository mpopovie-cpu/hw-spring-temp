package com.smartshop.category;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void savesAndFindsByName() {
        categoryRepository.save(new Category("Electronics", "Gadgets and devices", true));

        Optional<Category> found = categoryRepository.findByName("Electronics");

        assertThat(found).isPresent();
        assertThat(found.get().isActive()).isTrue();
    }

    @Test
    void findsOnlyActiveCategories() {
        categoryRepository.save(new Category("Books", "Printed books", true));
        categoryRepository.save(new Category("Archived", "Old stuff", false));

        List<Category> active = categoryRepository.findByActiveTrue();

        assertThat(active).extracting(Category::getName).containsExactly("Books");
    }
}