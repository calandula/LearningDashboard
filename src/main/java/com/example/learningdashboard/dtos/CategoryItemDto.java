package com.example.learningdashboard.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryItemDto {

    @NotBlank
    private String type;

    @NotBlank
    private String color;

    @NotBlank
    private int upperThreshold;
}
