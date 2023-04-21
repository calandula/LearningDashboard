package com.example.learningdashboard.service;

import com.example.learningdashboard.dtos.CategoryDto;
import com.example.learningdashboard.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public CategoryDto createCategory(CategoryDto category) {
        return categoryRepository.save(category);
    }

    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll();
    }

    public CategoryDto getCategoryById(String categoryId) {
        Optional<CategoryDto> optionalCategory = Optional.ofNullable(categoryRepository.findById(categoryId));
        return optionalCategory.orElse(null);
    }

    public CategoryDto getCategoryByItem(String itemId) {
        return categoryRepository.findByItem(itemId);
    }
}

