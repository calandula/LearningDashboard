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
public class SIDto {

    private String id;
    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @NotBlank
    private String assessmentModel;

    @NotEmpty
    private ArrayList<String> qualityFactors;
}
