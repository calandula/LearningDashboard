package com.example.learningdashboard.service;

import com.example.learningdashboard.dtos.UserDto;
import com.example.learningdashboard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public UserDto createUser(UserDto user) {
        return userRepository.save(user, null);
    }

    public List<UserDto> getAllUsers() {
        return userRepository.findAll();
    }

    public UserDto getUserById(String userId) {
        Optional<UserDto> optionalUser = Optional.ofNullable(userRepository.findById(userId));
        return optionalUser.orElse(null);
    }

    public UserDto updateUser(String userId, UserDto user) {
        Optional<UserDto> optionalUser = Optional.ofNullable(userRepository.findById(userId));
        if (optionalUser.isPresent()) {
            userRepository.deleteById(userId, true);
            return userRepository.save(user, userId);
        } else {
            return null;
        }
    }

    public void deleteUser(String userId) {
        userRepository.deleteById(userId, false);
    }
}
