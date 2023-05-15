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
public class ProfileDto {
    private String id;
    @NotBlank
    private String name;
    private String description;
    private String qualityLevel = "All";
    @NotEmpty
    private ArrayList<String> allowedProjects;
    private ArrayList<String> allowedStrategicIndicators;
    private String detailedStrategicIndicatorsView = "Radar";
    private String detailedFactorsView = "Radar";
    private String metricsView = "Gauge";
    private String qualityModelsView = "Graph";
}
