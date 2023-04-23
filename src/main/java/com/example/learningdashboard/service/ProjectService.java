package com.example.learningdashboard.service;

import com.example.learningdashboard.dtos.ProjectDto;
import com.example.learningdashboard.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    public List<ProjectDto> getAllProjects() {
        List<ProjectDto> projects = projectRepository.findAll();
        return new ArrayList<>(projects);
    }

    public ProjectDto getProjectById(String productId) {
        ProjectDto project = projectRepository.findById(productId);
        return project;
    }

    public ProjectDto createProject(ProjectDto project) {
        return projectRepository.save(project);
    }

    public void deleteProjectById(String projectId) {
    }

    public ProjectDto updateProjectById(String projectId, ProjectDto project) {
        return project;
    }
}

