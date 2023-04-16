package com.example.learningdashboard.service;

import com.example.learningdashboard.dtos.ProjectDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService {
    public ProjectDto createProject(ProjectDto project) {
        return project;
    }

    public List<ProjectDto> getAllProjects() {
        return null;
    }

    public ProjectDto getProjectById(String projectId) {
        return null;
    }
}
