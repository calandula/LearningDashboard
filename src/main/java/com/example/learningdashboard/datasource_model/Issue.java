package com.example.learningdashboard.datasource_model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Issue {
    private String id;
    private String state;
    private String title;
    private String body;
    private String createdBy;
    private String assignedTo;
    private Boolean isLocked;
}
