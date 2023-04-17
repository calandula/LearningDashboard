package com.example.learningdashboard.service;

import com.example.learningdashboard.dtos.ProjectDto;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    @Autowired
    private String prefixes;

    @Autowired
    private Dataset dataset;

    @Autowired
    private String namespace;

    public ProjectDto createProject(ProjectDto project) {
        String projectId = UUID.randomUUID().toString();
        String projectURI = namespace + projectId;
        Resource projectResource = ResourceFactory.createResource(projectURI);
        Resource projectClass = ResourceFactory.createResource(namespace + "Project");
        dataset.begin(ReadWrite.WRITE);
        try {
            List<Resource> hiResources = project.getHierarchyItems().stream()
                    .map(hiId -> ResourceFactory.createResource(namespace + hiId))
                    .filter(hiResource -> dataset.getDefaultModel().containsResource(hiResource))
                    .toList();
            if (hiResources.size() != project.getHierarchyItems().size()) {
                throw new IllegalArgumentException("One or more strategic indicator Item IDs do not exist in the dataset.");
            }
            dataset.getDefaultModel()
                    .add(projectResource, RDF.type, projectClass)
                    .add(projectResource, ResourceFactory.createProperty(namespace + "projectName"),
                            ResourceFactory.createPlainLiteral(project.getName()))
                    .add(projectResource, ResourceFactory.createProperty(namespace + "projectDescription"),
                            ResourceFactory.createPlainLiteral(project.getDescription()))
                    .add(projectResource, ResourceFactory.createProperty(namespace + "projectBacklogID"),
                            ResourceFactory.createPlainLiteral(project.getBacklogID()))
                    .add(projectResource, ResourceFactory.createProperty(namespace + "projectTaigaURL"),
                        ResourceFactory.createPlainLiteral(project.getTaigaURL()))
                    .add(projectResource, ResourceFactory.createProperty(namespace + "projectGithubURL"),
                            ResourceFactory.createPlainLiteral(project.getGithubURL()))
                    .add(projectResource, ResourceFactory.createProperty(namespace + "projectIsGlobal"),
                            ResourceFactory.createTypedLiteral(project.isGlobal()))
                    .add(projectResource, ResourceFactory.createProperty(namespace + "projectLogo"),
                            ResourceFactory.createPlainLiteral(project.getLogo()));


            hiResources.forEach(hiResource ->
                    dataset.getDefaultModel().add(projectResource,
                            ResourceFactory.createProperty(namespace + "hasHI"),
                            hiResource));

            dataset.commit();
            return null;
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }

    public List<ProjectDto> getAllProjects() {
        return null;
    }

    public ProjectDto getProjectById(String projectId) {
        return null;
    }
}
