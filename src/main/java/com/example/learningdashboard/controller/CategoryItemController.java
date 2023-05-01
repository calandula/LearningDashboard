package com.example.learningdashboard.controller;

import com.example.learningdashboard.dtos.CategoryDto;
import com.example.learningdashboard.dtos.CategoryItemDto;
import com.example.learningdashboard.service.CategoryItemService;
import com.example.learningdashboard.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/categoryitems")
public class CategoryItemController {

    @Autowired
    private CategoryItemService categoryItemService;

    @PostMapping
    public ResponseEntity<CategoryItemDto> createCategoryItem(@RequestBody CategoryItemDto categoryItem) {
        CategoryItemDto savedCategoryItem = categoryItemService.createCategoryItem(categoryItem);
        return new ResponseEntity<>(savedCategoryItem, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<CategoryItemDto>> getAllCategoryItems() {
        List<CategoryItemDto> categoryItems = categoryItemService.getAllCategoryItems();
        return new ResponseEntity<>(categoryItems, HttpStatus.OK);
    }

    @GetMapping("{id}")
    public ResponseEntity<CategoryItemDto> getCategoryItemById(@PathVariable("id") String categoryItemId) {
        CategoryItemDto categoryItem = categoryItemService.getCategoryItemById(categoryItemId);
        return new ResponseEntity<>(categoryItem, HttpStatus.OK);
    }

    @GetMapping("/category/{id}")
    public ResponseEntity<CategoryItemDto> getCategoryItemByCategory(@PathVariable("id") String categoryId) {
        CategoryItemDto categoryItem = categoryItemService.getCategoryItemByCategoryId(categoryId);
        return new ResponseEntity<>(categoryItem, HttpStatus.OK);
    }

    @PutMapping("{id}")
    public ResponseEntity<CategoryItemDto> updateCategoryItem(@PathVariable("id") String categoryItemId, @RequestBody CategoryItemDto categoryItem) {
        CategoryItemDto updatedCategoryItem = categoryItemService.updateCategoryItem(categoryItemId, categoryItem);
        return new ResponseEntity<>(updatedCategoryItem, HttpStatus.OK);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteCategoryItem(@PathVariable("id") String categoryItemId) {
        categoryItemService.deleteCategoryItem(categoryItemId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
