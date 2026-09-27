package com.smartshop.product;

import com.smartshop.category.Category;
import com.smartshop.category.CategoryService;
import com.smartshop.exception.BusinessException;
import com.smartshop.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    public ProductService(ProductRepository productRepository, CategoryService categoryService) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
    }

    @Transactional
    public Product createProduct(String name, String description, BigDecimal price, Long categoryId) {
        validateName(name);
        validatePrice(price);
        Category category = categoryService.getCategoryById(categoryId);
        LocalDateTime now = LocalDateTime.now();
        Product product = new Product(name, description, price, true, now, now, category);
        return productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: id=" + id));
    }

    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Product> getActiveProducts() {
        return productRepository.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public List<Product> searchProducts(String query) {
        return productRepository.findByNameContainingIgnoreCase(query);
    }

    @Transactional(readOnly = true)
    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    @Transactional
    public Product updateProduct(Long id, String name, String description, BigDecimal price, Long categoryId) {
        validateName(name);
        validatePrice(price);
        Product product = getProductById(id);
        Category category = categoryService.getCategoryById(categoryId);

        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setCategory(category);
        // createdAt не трогаем, только updatedAt
        product.setUpdatedAt(LocalDateTime.now());

        return productRepository.save(product);
    }

    @Transactional
    public void deactivateProduct(Long id) {
        Product product = getProductById(id);
        product.setActive(false);
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessException("Product name must not be blank");
        }
    }

    private void validatePrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Product price must be positive");
        }
    }
}