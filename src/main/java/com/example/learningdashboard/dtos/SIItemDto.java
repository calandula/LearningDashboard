package com.example.learningdashboard.dtos;

import com.example.learningdashboard.utils.Weight;
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

    private String id;

    @NotBlank
    private String sourceSI;

    private float value;


    private float threshold;

    @NotBlank
    private String category;

    @NotEmpty
    private ArrayList<Weight<String, Float>> qfItemWeights;
}
