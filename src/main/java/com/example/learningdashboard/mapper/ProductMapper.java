package com.example.learningdashboard.mapper;

import com.example.learningdashboard.dtos.ProductDto;
import com.example.learningdashboard.model.Product;

import java.util.ArrayList;

public class ProductMapper {

    // Convert User JPA Entity into UserDto
    public static ProductDto mapToProductDto(Product product){
        ProductDto productDto = new ProductDto(
                product.getName(),
                product.getDescription(),
                product.getLogo(),
                new ArrayList<>()
        );
        return productDto;
    }

    // Convert UserDto into User JPA Entity
    public static Product mapToProduct(ProductDto productDto){
        Product product = new Product(
                productDto.getName(),
                productDto.getDescription(),
                productDto.getLogo()
        );
        return product;
    }
}
