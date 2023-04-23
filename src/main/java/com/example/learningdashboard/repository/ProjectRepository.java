package com.example.learningdashboard.repository;

import com.example.learningdashboard.dtos.ProjectDto;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class ProjectRepository {

    @Autowired
    private String prefixes;

    @Autowired
    private Dataset dataset;

    @Autowired
    private String namespace;

    public ProjectDto save(ProjectDto project) {
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

    public List<ProjectDto> findAll() {
        List<ProjectDto> projects = new ArrayList<>();
        dataset.begin(ReadWrite.READ);
        try {
            dataset.getDefaultModel().listResourcesWithProperty(RDF.type, ResourceFactory.createResource(namespace + "Project"))
                    .forEachRemaining(projectResource -> {
                        ProjectDto project = new ProjectDto();
                        project.setName(projectResource.getProperty(ResourceFactory.createProperty(namespace + "projectName")).getString());
                        project.setDescription(projectResource.getProperty(ResourceFactory.createProperty(namespace + "projectDescription")).getString());
                        project.setBacklogID(projectResource.getProperty(ResourceFactory.createProperty(namespace + "projectBacklogID")).getString());
                        project.setTaigaURL(projectResource.getProperty(ResourceFactory.createProperty(namespace + "projectTaigaURL")).getString());
                        project.setGithubURL(projectResource.getProperty(ResourceFactory.createProperty(namespace + "projectGithubURL")).getString());
                        project.setGlobal(Boolean.parseBoolean(projectResource.getProperty(ResourceFactory.createProperty(namespace + "projectIsGlobal")).getString()));
                        project.setLogo(projectResource.getProperty(ResourceFactory.createProperty(namespace + "projectLogo")).getString());

                        project.setHierarchyItems((ArrayList<String>) projectResource.listProperties(ResourceFactory.createProperty(namespace + "hasHI"))
                                .mapWith(Statement::getObject).mapWith(RDFNode::asResource)
                                .mapWith(Resource::getLocalName).toList());
                        projects.add(project);
                    });

            dataset.commit();
            return projects;
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }

    public ProjectDto findById(String projectId) {
        String projectURI = namespace + projectId;
        Resource projectResource = ResourceFactory.createResource(projectURI);
        dataset.begin(ReadWrite.READ);
        try {
            Model model = dataset.getDefaultModel();

            if (!model.containsResource(projectResource)) {
                return null;
            }

            String projectName = model.getProperty(projectResource, model.createProperty(namespace + "projectName"))
                    .getString();
            String projectDescription = model.getProperty(projectResource, model.createProperty(namespace + "projectDescription"))
                    .getString();
            String projectBacklogID = model.getProperty(projectResource, model.createProperty(namespace + "projectBacklogID"))
                    .getString();
            String projectTaigaURL = model.getProperty(projectResource, model.createProperty(namespace + "projectTaigaURL"))
                    .getString();
            String projectGithubURL = model.getProperty(projectResource, model.createProperty(namespace + "projectGithubURL"))
                    .getString();
            String projectIsGlobal = model.getProperty(projectResource, model.createProperty(namespace + "projectIsGlobal"))
                    .getString();
            String projectLogo = model.getProperty(projectResource, model.createProperty(namespace + "projectLogo"))
                    .getString();


            List<String> HIs = model.listObjectsOfProperty(projectResource, model.createProperty(namespace + "hasHi"))
                    .mapWith(resource -> resource.asResource().getURI().substring(namespace.length()))
                    .toList();

            ProjectDto project = new ProjectDto();
            project.setName(projectName);
            project.setDescription(projectDescription);
            project.setBacklogID(projectBacklogID);
            project.setTaigaURL(projectTaigaURL);
            project.setGithubURL(projectGithubURL);
            project.setGlobal(Boolean.parseBoolean(projectIsGlobal));
            project.setLogo(projectLogo);
            project.setHierarchyItems((ArrayList<String>) HIs);

            return project;
        } finally {
            dataset.end();
        }
    }

}
