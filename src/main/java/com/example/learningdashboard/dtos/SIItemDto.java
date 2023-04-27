package com.example.learningdashboard.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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

    @NotBlank
    private String sourceSI;

    private float value;

    private float threshold;

    @NotBlank
    private String category;

    private ArrayList<String> qfItems;
}
