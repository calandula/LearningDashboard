package com.example.learningdashboard.service;

import com.example.learningdashboard.dtos.CategoryDto;
import com.example.learningdashboard.dtos.UserDto;
import com.example.learningdashboard.repository.CategoryRepository;
import com.example.learningdashboard.repository.UserRepository;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public UserDto createUser(UserDto user) {
        return userRepository.save(user);
    }

    public List<UserDto> getAllCategories() {
        return userRepository.findAll();
    }

    public UserDto getCategoryById(String categoryId) {
        Optional<UserDto> optionalCategory = Optional.ofNullable(userRepository.findById(categoryId));
        return optionalCategory.orElse(null);
    }
}
