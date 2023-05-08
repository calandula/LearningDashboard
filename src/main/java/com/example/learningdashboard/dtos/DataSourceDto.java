package com.example.learningdashboard.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DataSourceDto {

    private String id;

    @NotBlank
    private String repository;

    @NotBlank
    private String owner;

    @NotBlank
    private String accessToken;
}
