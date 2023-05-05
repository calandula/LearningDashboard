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
public class ProjectDto {

    private String id;
    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @NotEmpty
    private ArrayList<String> dataSources;

    @NotEmpty
    private ArrayList<String> students;

    private boolean isGlobal = false;

    @NotBlank
    private String logo;

    @NotEmpty
    private ArrayList<String> hierarchyItems;
}
