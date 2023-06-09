package com.example.learningdashboard.controller;

import com.example.learningdashboard.dtos.ProjectDto;
import com.example.learningdashboard.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectDto> createProject(@RequestBody ProjectDto project) {
        ProjectDto savedProject = projectService.createProject(project);
        return new ResponseEntity<>(savedProject, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ProjectDto>> getAllProjects() {
        List<ProjectDto> projects = projectService.getAllProjects();
        return new ResponseEntity<>(projects, HttpStatus.OK);
    }

    @GetMapping("{id}")
    public ResponseEntity<ProjectDto> getProjectById(@PathVariable("id") String projectId) {
        ProjectDto project = projectService.getProjectById(projectId);
        return new ResponseEntity<>(project, HttpStatus.OK);
    }

    @GetMapping("/product/{id}")
    public ResponseEntity<List<ProjectDto>> getProjectsByProduct(@PathVariable("id") String productId) {
        List<ProjectDto> projects = projectService.getProjectsByProduct(productId);
        return new ResponseEntity<>(projects, HttpStatus.OK);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable("id") String projectId) {
        projectService.deleteProjectById(projectId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("{id}")
    public ResponseEntity<ProjectDto> updateProject(@PathVariable("id") String projectId, @RequestBody ProjectDto project) {
        ProjectDto updatedProject = projectService.updateProjectById(projectId, project);
        return new ResponseEntity<>(updatedProject, HttpStatus.OK);
    }

}
