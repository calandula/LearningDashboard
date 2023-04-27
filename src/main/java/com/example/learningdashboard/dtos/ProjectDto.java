package com.example.learningdashboard.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDto {

    @NotBlank
    private String name;

    private String description;

    private ArrayList<String> dataSources;

    private ArrayList<String> students;

    private boolean isGlobal = false;

    private String logo;

    private ArrayList<String> hierarchyItems;
}
