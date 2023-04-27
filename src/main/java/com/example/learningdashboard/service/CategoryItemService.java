package com.example.learningdashboard.service;

import com.example.learningdashboard.dtos.CategoryItemDto;
import com.example.learningdashboard.dtos.UserDto;
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
        return categoryItemRepository.save(categoryItem, null);
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

    public CategoryItemDto updateCategoryItem(String categoryItemId, CategoryItemDto categoryItem) {
        Optional<CategoryItemDto> optionalCategoryItem = Optional.ofNullable(categoryItemRepository.findById(categoryItemId));
        if (optionalCategoryItem.isPresent()) {
            categoryItemRepository.deleteById(categoryItemId, true);
            return categoryItemRepository.save(categoryItem, categoryItemId);
        } else {
            return null;
        }
    }

    public void deleteCategoryItem(String categoryItemId) {
        categoryItemRepository.deleteById(categoryItemId, false);
    }
}
