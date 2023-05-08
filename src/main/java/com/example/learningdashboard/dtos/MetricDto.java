package com.example.learningdashboard.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MetricDto {

    private String id;

    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @NotBlank
    private String category;

    @NotNull
    private float threshold;

    @NotNull
    private float weight;

    @NotNull
    private float value;
}
