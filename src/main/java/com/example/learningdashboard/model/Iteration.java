package com.example.learningdashboard.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Iteration {
    private String name;
    private String subject;
    private LocalDate from;
    private LocalDate to;
    private ArrayList<String> associatedProjects;
}
