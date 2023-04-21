package com.example.learningdashboard.service;

import com.example.learningdashboard.dtos.CategoryItemDto;
import com.example.learningdashboard.repository.CategoryItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryItemService {

    @Autowired
    private CategoryItemRepository categoryItemRepository;

    public CategoryItemDto createCategoryItem(CategoryItemDto categoryItem) {
        return categoryItemRepository.save(categoryItem);
    }

    public List<CategoryItemDto> getAllCategoryItems() {
        return categoryItemRepository.findAll();
    }

    public CategoryItemDto getCategoryItemById(String categoryItemId) {
        Optional<CategoryItemDto> optionalCategoryItem = Optional.ofNullable(categoryItemRepository.findById(categoryItemId));
        return optionalCategoryItem.orElse(null);
    }

    public CategoryItemDto getCategoryItemByCategoryId(String categoryId) {
        return categoryItemRepository.findByCategoryId(categoryId);
    }
}
