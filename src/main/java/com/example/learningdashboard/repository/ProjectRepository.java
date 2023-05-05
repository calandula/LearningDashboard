package com.example.learningdashboard.repository;

import com.example.learningdashboard.dtos.ProjectDto;
import com.example.learningdashboard.utils.JenaUtils;
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

    public ProjectDto save(ProjectDto project, String projectId) {
        String projectURI = projectId == null ? namespace + UUID.randomUUID().toString() : projectId;
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

            List<Resource> dsResources = project.getDataSources().stream()
                    .map(dsId -> ResourceFactory.createResource(namespace + dsId))
                    .filter(dsResource -> dataset.getDefaultModel().containsResource(dsResource))
                    .toList();
            if (dsResources.size() != project.getDataSources().size()) {
                throw new IllegalArgumentException("One or more datasources IDs do not exist in the dataset.");
            }

            List<Resource> studentResources = project.getStudents().stream()
                    .map(studentId -> ResourceFactory.createResource(namespace + studentId))
                    .filter(studentResource -> dataset.getDefaultModel().containsResource(studentResource))
                    .toList();
            if (studentResources.size() != project.getStudents().size()) {
                throw new IllegalArgumentException("One or more Student IDs do not exist in the dataset.");
            }

            dataset.getDefaultModel()
                    .add(projectResource, RDF.type, projectClass)
                    .add(projectResource, ResourceFactory.createProperty(namespace + "projectName"),
                            ResourceFactory.createPlainLiteral(project.getName()))
                    .add(projectResource, ResourceFactory.createProperty(namespace + "projectDescription"),
                            ResourceFactory.createPlainLiteral(project.getDescription()))
                    .add(projectResource, ResourceFactory.createProperty(namespace + "projectIsGlobal"),
                            ResourceFactory.createTypedLiteral(project.isGlobal()))
                    .add(projectResource, ResourceFactory.createProperty(namespace + "projectLogo"),
                            ResourceFactory.createPlainLiteral(project.getLogo()));


            hiResources.forEach(hiResource ->
                    dataset.getDefaultModel().add(projectResource,
                            ResourceFactory.createProperty(namespace + "hasHI"),
                            hiResource));
            dsResources.forEach(dsResource ->
                    dataset.getDefaultModel().add(projectResource,
                            ResourceFactory.createProperty(namespace + "hasDataSource"),
                            dsResource));
            studentResources.forEach(studentResource ->
                    dataset.getDefaultModel().add(projectResource,
                            ResourceFactory.createProperty(namespace + "hasStudent"),
                            studentResource));

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
                        project.setGlobal(Boolean.parseBoolean(projectResource.getProperty(ResourceFactory.createProperty(namespace + "projectIsGlobal")).getString()));
                        project.setLogo(projectResource.getProperty(ResourceFactory.createProperty(namespace + "projectLogo")).getString());

                        project.setHierarchyItems((ArrayList<String>) projectResource.listProperties(ResourceFactory.createProperty(namespace + "hasHI"))
                                .mapWith(Statement::getObject).mapWith(RDFNode::asResource)
                                .mapWith(Resource::getLocalName).toList());
                        project.setDataSources((ArrayList<String>) projectResource.listProperties(ResourceFactory.createProperty(namespace + "hasDataSource"))
                                .mapWith(Statement::getObject).mapWith(RDFNode::asResource)
                                .mapWith(Resource::getLocalName).toList());
                        project.setStudents((ArrayList<String>) projectResource.listProperties(ResourceFactory.createProperty(namespace + "hasStudent"))
                                .mapWith(Statement::getObject).mapWith(RDFNode::asResource)
                                .mapWith(Resource::getLocalName).toList());
                        project.setId(JenaUtils.parseId(projectResource.getURI()));
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
            List<String> dss = model.listObjectsOfProperty(projectResource, model.createProperty(namespace + "hasDataSource"))
                    .mapWith(resource -> resource.asResource().getURI().substring(namespace.length()))
                    .toList();
            List<String> students = model.listObjectsOfProperty(projectResource, model.createProperty(namespace + "hasStudent"))
                    .mapWith(resource -> resource.asResource().getURI().substring(namespace.length()))
                    .toList();

            ProjectDto project = new ProjectDto();
            project.setName(projectName);
            project.setDescription(projectDescription);
            project.setGlobal(Boolean.parseBoolean(projectIsGlobal));
            project.setLogo(projectLogo);
            project.setHierarchyItems((ArrayList<String>) HIs);
            project.setDataSources((ArrayList<String>) dss);
            project.setStudents((ArrayList<String>) students);
            project.setId(JenaUtils.parseId(projectResource.getURI()));

            return project;
        } finally {
            dataset.end();
        }
    }

    public void deleteById(String projectId, boolean update) {
        String projectURI = namespace + projectId;
        Resource projectResource = ResourceFactory.createResource(projectURI);
        dataset.begin(ReadWrite.WRITE);
        try {
            if (update) {
                StmtIterator it = dataset.getDefaultModel().listStatements(projectResource, null, (RDFNode) null);
                while (it.hasNext()) {
                    Statement stmt = it.next();
                    dataset.getDefaultModel().remove(stmt);
                }
            }
            else {
                dataset.getDefaultModel().removeAll(projectResource, null, (RDFNode) null);
            }
            dataset.commit();
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }
}
