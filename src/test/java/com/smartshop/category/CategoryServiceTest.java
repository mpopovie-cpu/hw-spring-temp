package com.smartshop.category;

import com.smartshop.exception.BusinessException;
import com.smartshop.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void createsCategorySuccessfully() {
        when(categoryRepository.existsByName("Books")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Category category = categoryService.createCategory("Books", "Printed books", true);

        assertThat(category.getName()).isEqualTo("Books");
    }

    @Test
    void rejectsDuplicateName() {
        when(categoryRepository.existsByName("Books")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.createCategory("Books", "desc", true))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void getsCategoryById() {
        Category category = new Category("Toys", "desc", true);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        Category found = categoryService.getCategoryById(1L);

        assertThat(found.getName()).isEqualTo("Toys");
    }

    @Test
    void updatesCategory() {
        Category category = new Category("Old", "desc", true);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByName("New")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Category updated = categoryService.updateCategory(1L, "New", "new desc");

        assertThat(updated.getName()).isEqualTo("New");
        assertThat(updated.getDescription()).isEqualTo("new desc");
    }

    @Test
    void deactivatesCategoryWithoutDeletingProducts() {
        Category category = new Category("Active", "desc", true);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        categoryService.deactivateCategory(1L);

        assertThat(category.isActive()).isFalse();
        verify(categoryRepository).save(category);
    }

    @Test
    void throwsWhenCategoryMissing() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getCategoryById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}