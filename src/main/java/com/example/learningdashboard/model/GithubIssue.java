package com.example.learningdashboard.model;

import java.util.List;

public class GithubIssue {
    private long id;
    private String title;
    private String body;
    private GithubUser user;
    private List<GithubLabel> labels;
    // ... getters and setters
}

