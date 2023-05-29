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
public class Commit {
    private String id;
    private String createdBy;
    private String message;
    private Boolean verified;
    private int commentCount;
}
