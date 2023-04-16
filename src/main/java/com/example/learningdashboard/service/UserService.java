package com.example.learningdashboard.service;

import com.example.learningdashboard.dtos.UserDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    public UserDto createUser(UserDto user) {
        return user;
    }

    public List<UserDto> getAllUsers() {
        return null;
    }

    public UserDto getUserById(String userId) {
        return null;
    }
}
