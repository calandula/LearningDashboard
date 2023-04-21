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
public class SIItem {

    private String sourceSI;

    private float value;

    private float threshold;

    private String category;

    private ArrayList<String> qfItems;
}
