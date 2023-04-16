package com.example.learningdashboard.service;

import com.example.learningdashboard.dtos.CategoryDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    public CategoryDto createCategory(CategoryDto category) {
        return category;
    }

    public List<CategoryDto> getAllCategories() {
        return null;
    }

    public CategoryDto getCategoryById(String categoryId) {
        return null;
    }
}
