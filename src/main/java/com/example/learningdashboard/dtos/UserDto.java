package com.example.learningdashboard.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private String id;

    @NotBlank
    private String username;

    @NotBlank
    @Email
    private String email;

    private boolean admin = false;

    @NotBlank
    private String securityQuestion;

    @NotBlank
    private String answer;

    @NotBlank
    private String password;
}
