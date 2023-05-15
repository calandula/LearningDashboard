package com.example.learningdashboard.service;

import com.example.learningdashboard.dtos.ProjectDto;
import com.example.learningdashboard.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        return projectRepository.save(project, null);
    }

    public void deleteProjectById(String projectId) {
        projectRepository.deleteById(projectId, false);
    }

    public ProjectDto updateProjectById(String projectId, ProjectDto project) {
        Optional<ProjectDto> optionalProject = Optional.ofNullable(projectRepository.findById(projectId));
        if (optionalProject.isPresent()) {
            projectRepository.deleteById(projectId, true);
            return projectRepository.save(project, projectId);
        } else {
            return null;
        }
    }

    public List<ProjectDto> getProjectsByProduct(String productId) {
        List<ProjectDto> projects = projectRepository.findByProduct(productId);
        return new ArrayList<>(projects);
    }
}

