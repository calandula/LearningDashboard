package com.example.learningdashboard.service;

import com.example.learningdashboard.dtos.CategoryDto;
import com.example.learningdashboard.dtos.ProductDto;
import com.example.learningdashboard.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        return productRepository.save(product, null);
    }

    public ProductDto updateProduct(String productId, ProductDto product) {
        Optional<ProductDto> optionalProduct = Optional.ofNullable(productRepository.findById(productId));
        if (optionalProduct.isPresent()) {
            productRepository.deleteById(productId, true);
            return productRepository.save(product, productId);
        } else {
            return null;
        }
    }

    public void deleteProduct(String productId) {
        productRepository.deleteById(productId, false);
    }
}
