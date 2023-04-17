package com.example.learningdashboard.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private String username;

    private String email;

    private boolean admin;

    private String securityQuestion;

    private String answer;

    private String password;
}
