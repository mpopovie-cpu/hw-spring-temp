package com.smartshop.product;

import com.smartshop.category.Category;
import com.smartshop.category.CategoryService;
import com.smartshop.exception.BusinessException;
import com.smartshop.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private ProductService productService;

    @Test
    void createsProductSuccessfully() {
        Category category = new Category("Toys", "desc", true);

        when(categoryService.getCategoryById(1L)).thenReturn(category);
        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Product product = productService.createProduct(
                "Lego",
                "desc",
                new BigDecimal("10.00"),
                1L
        );

        assertThat(product.getName()).isEqualTo("Lego");
        assertThat(product.getCategory()).isEqualTo(category);
    }

    @Test
    void rejectsMissingCategory() {
        when(categoryService.getCategoryById(1L))
                .thenThrow(new ResourceNotFoundException("not found"));

        assertThatThrownBy(() ->
                productService.createProduct(
                        "Lego",
                        "desc",
                        new BigDecimal("10.00"),
                        1L
                )
        ).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updatesProductAndChangesUpdatedAtOnly() {
        Category category = new Category("Toys", "desc", true);
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);

        Product product = new Product(
                "Old",
                "desc",
                new BigDecimal("5.00"),
                true,
                createdAt,
                createdAt,
                category
        );

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(categoryService.getCategoryById(1L))
                .thenReturn(category);

        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Product updated = productService.updateProduct(
                1L,
                "New",
                "new desc",
                new BigDecimal("20.00"),
                1L
        );

        assertThat(updated.getName()).isEqualTo("New");
        assertThat(updated.getCreatedAt()).isEqualTo(createdAt);
        assertThat(updated.getUpdatedAt()).isAfter(createdAt);
    }

    @Test
    void deactivatesProduct() {
        Category category = new Category("Toys", "desc", true);
        LocalDateTime now = LocalDateTime.now();

        Product product = new Product(
                "Old",
                "desc",
                new BigDecimal("5.00"),
                true,
                now,
                now,
                category
        );

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        productService.deactivateProduct(1L);

        assertThat(product.isActive()).isFalse();
    }

    @Test
    void throwsWhenProductMissing() {
        when(productRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                productService.getProductById(99L)
        ).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void searchesProductsByName() {
        Category category = new Category("Toys", "desc", true);
        LocalDateTime now = LocalDateTime.now();

        Product product = new Product(
                "Lego Set",
                "desc",
                new BigDecimal("10.00"),
                true,
                now,
                now,
                category
        );

        when(productRepository.findByNameContainingIgnoreCase("lego"))
                .thenReturn(List.of(product));

        List<Product> found = productService.searchProducts("lego");

        assertThat(found).containsExactly(product);
    }

    @Test
    void looksUpProductsByCategory() {
        Category category = new Category("Toys", "desc", true);
        LocalDateTime now = LocalDateTime.now();

        Product product = new Product(
                "Lego Set",
                "desc",
                new BigDecimal("10.00"),
                true,
                now,
                now,
                category
        );

        when(productRepository.findByCategoryId(1L))
                .thenReturn(List.of(product));

        List<Product> found = productService.getProductsByCategory(1L);

        assertThat(found).containsExactly(product);
    }

    @Test
    void rejectsNonPositivePrice() {
        assertThatThrownBy(() ->
                productService.createProduct(
                        "Lego",
                        "desc",
                        BigDecimal.ZERO,
                        1L
                )
        ).isInstanceOf(BusinessException.class);
    }
}