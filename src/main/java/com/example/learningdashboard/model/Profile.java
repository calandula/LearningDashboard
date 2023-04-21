package com.example.learningdashboard.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Profile {

    private String name;

    private String description;

    private String qualityLevel;

    private ArrayList<String> allowedProjects;

    private ArrayList<String> allowedStrategicIndicators;

    private String detailedStrategicIndicatorsView;

    private String detailedFactorsView;

    private String metricsView;

    private String qualityModelsView;
}
