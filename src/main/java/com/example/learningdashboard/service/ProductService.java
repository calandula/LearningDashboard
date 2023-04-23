package com.example.learningdashboard.service;

import com.example.learningdashboard.dtos.ProductDto;
import com.example.learningdashboard.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<ProductDto> getAllProducts() {
        List<ProductDto> products = productRepository.findAll();
        return new ArrayList<>(products);
    }

    public ProductDto getProductById(String productId) {
        ProductDto product = productRepository.findById(productId);
        return product;
    }

    public ProductDto createProduct(ProductDto product) {
        return productRepository.save(product);
    }

    public ProductDto updateProduct(String productId, ProductDto productDto) {
        return productDto;
    }

    public void deleteProduct(String productId) {
    }
}
