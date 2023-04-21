package com.example.learningdashboard.model;

import java.util.ArrayList;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SI {

    private String name;

    private String description;

    private String assessmentModel;

    private ArrayList<String> qualityFactors;
}
