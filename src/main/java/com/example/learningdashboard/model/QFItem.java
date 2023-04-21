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
public class QFItem {

    private String sourceQF;

    private float value;

    private float threshold;

    private String category;

    private ArrayList<String> metrics;

}
