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
public class MetricItemDto {

    private String id;
    @NotBlank
    private String name;
    @NotBlank
    private String description;
    @NotBlank
    private String category;
    private float threshold;
    @NotBlank
    private String method;
    @NotBlank
    private String target;
    @NotBlank
    private String dsId;
    private float value;
}
