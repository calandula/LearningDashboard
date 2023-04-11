package com.example.learningdashboard.controller;

import com.example.learningdashboard.dtos.PostProductDto;
import com.example.learningdashboard.model.Product;
import com.example.learningdashboard.service.ProductService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class MyController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping("/products")
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @PostMapping("/products")
    public PostProductDto createProduct(@RequestBody PostProductDto postProductDto) {
        Product product = convertToEntity(postProductDto);
        Product productCreated = productService.createProduct(product);
        return convertToDto(productCreated);
    }

    private PostProductDto convertToDto(Product product) {
        PostProductDto postDto = modelMapper.map(product, PostProductDto.class);
        return postDto;
    }

    private Product convertToEntity(PostProductDto postDto) {
        Product product = modelMapper.map(postDto, Product.class);
        return product;
    }

}
