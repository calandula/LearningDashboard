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
public class SIItemDto {

    private String id;

    @NotBlank
    private String sourceSI;

    private float value;

    @NotNull
    private float threshold;

    @NotBlank
    private String category;

    @NotEmpty
    private ArrayList<String> qfItems;
}
