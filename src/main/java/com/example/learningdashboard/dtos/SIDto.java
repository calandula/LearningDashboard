package com.example.learningdashboard.dtos;

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

    private String name;

    private String description;

    private String assessmentModel;

    private ArrayList<String> qualityFactors;
}
