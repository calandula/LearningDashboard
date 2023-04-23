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
public class QFItemDto {

    private String sourceQF;

    private float value;

    private float threshold;

    private String category;

    private float weight;

    private ArrayList<String> metrics;
}
