package com.example.learningdashboard.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryItemDto {

    private String id;
    @NotBlank
    private String type;

    @NotBlank
    private String color;

    @NotNull
    private int upperThreshold;
}
