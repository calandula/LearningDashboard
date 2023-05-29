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

import static com.example.learningdashboard.utils.JenaUtils.getPropertyList;

@Repository
public class ProjectRepository {

    @Autowired
    private String prefixes;

    @Autowired
    private Dataset dataset;

    @Autowired
    private String namespace;

    public ProjectDto save(ProjectDto project, String projectId) {
        projectId = projectId == null ? UUID.randomUUID().toString() : projectId;
        String projectURI = namespace + projectId;
        Resource projectResource = ResourceFactory.createResource(projectURI);
        Resource projectClass = ResourceFactory.createResource(namespace + "Project");
        dataset.begin(ReadWrite.WRITE);
        try {
            List<Resource> hiResources = new ArrayList<>();
            List<Resource> dsResources = new ArrayList<>();
            List<Resource> studentResources = new ArrayList<>();

            if (project.getSIItems() != null) {
                hiResources = project.getSIItems().stream()
                        .map(hiId -> ResourceFactory.createResource(namespace + hiId))
                        .filter(hiResource -> dataset.getDefaultModel().containsResource(hiResource))
                        .toList();
                if (hiResources.size() != project.getSIItems().size()) {
                    throw new IllegalArgumentException("One or more strategic indicator Item IDs do not exist in the dataset.");
                }
            }

            if (project.getDataSources() != null) {
                dsResources = project.getDataSources().stream()
                        .map(dsId -> ResourceFactory.createResource(namespace + dsId))
                        .filter(dsResource -> dataset.getDefaultModel().containsResource(dsResource))
                        .toList();
                if (dsResources.size() != project.getDataSources().size()) {
                    throw new IllegalArgumentException("One or more datasources IDs do not exist in the dataset.");
                }
            }

            if (project.getStudents() != null) {
                studentResources = project.getStudents().stream()
                        .map(studentId -> ResourceFactory.createResource(namespace + studentId))
                        .filter(studentResource -> dataset.getDefaultModel().containsResource(studentResource))
                        .toList();
                if (studentResources.size() != project.getStudents().size()) {
                    throw new IllegalArgumentException("One or more Student IDs do not exist in the dataset.");
                }
            }

            dataset.getDefaultModel()
                    .add(projectResource, RDF.type, projectClass)
                    .add(projectResource, ResourceFactory.createProperty(namespace + "projectName"),
                            ResourceFactory.createPlainLiteral(project.getName()))
                    .add(projectResource, ResourceFactory.createProperty(namespace + "projectIsGlobal"),
                            ResourceFactory.createTypedLiteral(project.isGlobal()));

            if (project.getDescription() != null) {
                dataset.getDefaultModel()
                        .add(projectResource, ResourceFactory.createProperty(namespace + "projectDescription"),
                                ResourceFactory.createPlainLiteral(project.getDescription()));
            }

            if (project.getLogo() != null) {
                dataset.getDefaultModel()
                        .add(projectResource, ResourceFactory.createProperty(namespace + "projectLogo"),
                                ResourceFactory.createPlainLiteral(project.getLogo()));
            }

            hiResources.forEach(hiResource ->
                    dataset.getDefaultModel().add(projectResource,
                            ResourceFactory.createProperty(namespace + "hasSIItem"),
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
            project.setId(projectId);
            return project;
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
                        project.setName(JenaUtils.getPropertyString(projectResource, namespace + "projectName"));
                        project.setDescription(JenaUtils.getPropertyString(projectResource, namespace + "projectDescription"));
                        project.setGlobal(JenaUtils.getPropertyBoolean(projectResource, namespace + "projectIsGlobal"));
                        project.setLogo(JenaUtils.getPropertyString(projectResource, namespace + "projectLogo"));

                        project.setSIItems((ArrayList<String>) getPropertyList(projectResource, namespace + "hasSIItem"));
                        project.setDataSources((ArrayList<String>) getPropertyList(projectResource, namespace + "hasDataSource"));
                        project.setStudents((ArrayList<String>) getPropertyList(projectResource, namespace + "hasStudent"));
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

            String projectName = JenaUtils.getPropertyString(projectResource, namespace + "projectName");
            String projectDescription = JenaUtils.getPropertyString(projectResource, namespace + "projectDescription");
            boolean projectIsGlobal = JenaUtils.getPropertyBoolean(projectResource, namespace + "projectIsGlobal");
            String projectLogo = JenaUtils.getPropertyString(projectResource, namespace + "projectLogo");

            List<String> HIs = JenaUtils.getPropertyList(projectResource, namespace + "hasSIItem");
            List<String> dss = JenaUtils.getPropertyList(projectResource, namespace + "hasDataSource");
            List<String> students = JenaUtils.getPropertyList(projectResource, namespace + "hasStudent");

            ProjectDto project = new ProjectDto();
            project.setName(projectName);
            project.setDescription(projectDescription);
            project.setGlobal(projectIsGlobal);
            project.setLogo(projectLogo);
            project.setSIItems((ArrayList<String>) HIs);
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
            } else {
                dataset.getDefaultModel().removeAll(projectResource, null, (RDFNode) null);
            }
            dataset.commit();
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }

    public List<ProjectDto> findByProduct(String productId) {
        String productURI = namespace + productId;
        Resource productResource = ResourceFactory.createResource(productURI);
        List<ProjectDto> projects = new ArrayList<>();
        dataset.begin(ReadWrite.READ);
        try {
            Model model = dataset.getDefaultModel();

            if (!model.containsResource(productResource)) {
                throw new IllegalArgumentException("Product with ID " + productId + " does not exist in the dataset.");
            }

            StmtIterator it = model.listStatements(productResource, ResourceFactory.createProperty(namespace + "hasProject"), (RDFNode) null);
            while (it.hasNext()) {
                Statement stmt = it.next();
                RDFNode projectNode = stmt.getObject();
                if (projectNode.isResource()) {
                    Resource projectResource = projectNode.asResource();
                    ProjectDto project = new ProjectDto();
                    project.setName(JenaUtils.getPropertyString(projectResource, namespace + "projectName"));
                    project.setDescription(JenaUtils.getPropertyString(projectResource, namespace + "projectDescription"));
                    project.setGlobal(JenaUtils.getPropertyBoolean(projectResource, namespace + "projectIsGlobal"));
                    project.setLogo(JenaUtils.getPropertyString(projectResource, namespace + "projectLogo"));
                    project.setSIItems((ArrayList<String>) getPropertyList(projectResource, namespace + "hasSIItem"));
                    project.setDataSources((ArrayList<String>) getPropertyList(projectResource, namespace + "hasDataSource"));
                    project.setStudents((ArrayList<String>) getPropertyList(projectResource, namespace + "hasStudent"));
                    project.setId(JenaUtils.parseId(projectResource.getURI()));
                    projects.add(project);
                }
            }

            dataset.commit();
            return projects;
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }
}
