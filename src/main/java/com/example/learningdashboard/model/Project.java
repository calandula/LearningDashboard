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
public class Project {
    private String name;
    private String description;
    private String backlogID;
    private String taigaURL;
    private String githubURL;
    private boolean isGlobal;
    private String logo;
    private ArrayList<String> hierarchyItems;
}
