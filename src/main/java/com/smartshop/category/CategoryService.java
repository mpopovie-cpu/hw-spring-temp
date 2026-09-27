package com.smartshop.category;

import com.smartshop.exception.BusinessException;
import com.smartshop.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public Category createCategory(String name, String description, boolean active) {
        if (categoryRepository.existsByName(name)) {
            throw new BusinessException("Category name already exists: " + name);
        }
        Category category = new Category(name, description, active);
        return categoryRepository.save(category);
    }

    @Transactional(readOnly = true)
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: id=" + id));
    }

    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Category> getActiveCategories() {
        return categoryRepository.findByActiveTrue();
    }

    @Transactional
    public Category updateCategory(Long id, String name, String description) {
        Category category = getCategoryById(id);
        if (!category.getName().equals(name) && categoryRepository.existsByName(name)) {
            throw new BusinessException("Category name already exists: " + name);
        }
        category.setName(name);
        category.setDescription(description);
        return categoryRepository.save(category);
    }

    @Transactional
    public void deactivateCategory(Long id) {
        Category category = getCategoryById(id);
        // не удаляем, только деактивируем
        category.setActive(false);
        categoryRepository.save(category);
    }
}