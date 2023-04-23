package com.example.learningdashboard.service;

import com.example.learningdashboard.dtos.UserDto;
import com.example.learningdashboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public UserDto createUser(UserDto user) {
        return userRepository.save(user);
    }

    public List<UserDto> getAllUsers() {
        return null;
    }

    public UserDto getUserById(String userId) {
        return null;
    }

    public UserDto updateUser(String userId, UserDto user) {
        return user;
    }

    public void deleteUser(String userId) {
    }
}
