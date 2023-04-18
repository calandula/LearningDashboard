package com.example.learningdashboard.service;

import com.example.learningdashboard.dtos.CategoryDto;
import com.example.learningdashboard.dtos.IterationDto;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class IterationService {

    @Autowired
    private String prefixes;

    @Autowired
    private Dataset dataset;

    @Autowired
    private String namespace;

    public IterationDto createIteration(IterationDto iteration) {
        String iterationId = UUID.randomUUID().toString();
        String iterationURI = namespace + iterationId;
        Resource iterationResource = ResourceFactory.createResource(iterationURI);
        Resource iterationClass = ResourceFactory.createResource(namespace + "Iteration");
        dataset.begin(ReadWrite.WRITE);
        try {
            List<Resource> projectResources = iteration.getAssociatedProjects().stream()
                    .map(projectId -> ResourceFactory.createResource(namespace + projectId))
                    .filter(projectResource -> dataset.getDefaultModel().containsResource(projectResource))
                    .toList();
            if (projectResources.size() != iteration.getAssociatedProjects().size()) {
                throw new IllegalArgumentException("One or more project IDs do not exist in the dataset.");
            }
            dataset.getDefaultModel()
                    .add(iterationResource, RDF.type, iterationClass)
                    .add(iterationResource, ResourceFactory.createProperty(namespace + "iterationName"),
                            ResourceFactory.createPlainLiteral(iteration.getName()))
                    .add(iterationResource, ResourceFactory.createProperty(namespace + "iterationSubject"),
                            ResourceFactory.createPlainLiteral(iteration.getSubject()))
                    .add(iterationResource, ResourceFactory.createProperty(namespace + "iterationFrom"),
                            ResourceFactory.createTypedLiteral(iteration.getFrom()))
                    .add(iterationResource, ResourceFactory.createProperty(namespace + "iterationTo"),
                            ResourceFactory.createTypedLiteral(iteration.getTo()));


            projectResources.forEach(projectResource ->
                    dataset.getDefaultModel().add(iterationResource,
                            ResourceFactory.createProperty(namespace + "associatedProject"),
                            projectResource));

            dataset.commit();
            return null;
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }

    public List<IterationDto> getAllIterations() {
        List<IterationDto> iterations = new ArrayList<>();
        dataset.begin(ReadWrite.READ);
        try {
            dataset.getDefaultModel().listResourcesWithProperty(RDF.type, ResourceFactory.createResource(namespace + "Iteration"))
                    .forEachRemaining(iterationResource -> {
                        IterationDto iteration = new IterationDto();
                        iteration.setName(iterationResource.getProperty(ResourceFactory.createProperty(namespace + "iterationName")).getString());
                        iteration.setSubject(iterationResource.getProperty(ResourceFactory.createProperty(namespace + "iterationSubject")).getString());
                        iteration.setFrom(LocalDate.parse(iterationResource.getProperty(ResourceFactory.createProperty(namespace + "iterationFrom")).getString()));
                        iteration.setTo(LocalDate.parse(iterationResource.getProperty(ResourceFactory.createProperty(namespace + "iterationTo")).getString()));
                        iteration.setAssociatedProjects((ArrayList<String>) iterationResource.listProperties(ResourceFactory.createProperty(namespace + "associatedProject"))
                                .mapWith(Statement::getObject).mapWith(RDFNode::asResource)
                                .mapWith(Resource::getLocalName).toList());
                        iterations.add(iteration);
                    });

            dataset.commit();
            return iterations;
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }

    public IterationDto getIterationById(String iterationId) {
        String iterationURI = namespace + iterationId;
        Resource iterationResource = ResourceFactory.createResource(iterationURI);
        dataset.begin(ReadWrite.READ);
        try {
            Model model = dataset.getDefaultModel();

            if (!model.containsResource(iterationResource)) {
                return null;
            }

            String iterationName = model.getProperty(iterationResource, model.createProperty(namespace + "iterationName"))
                    .getString();
            String iterationSubject = model.getProperty(iterationResource, model.createProperty(namespace + "iterationSubject"))
                    .getString();
            String iterationFrom = model.getProperty(iterationResource, model.createProperty(namespace + "iterationFrom"))
                    .getString();
            String iterationTo = model.getProperty(iterationResource, model.createProperty(namespace + "iterationTo"))
                    .getString();
            List<String> associatedProjects = model.listObjectsOfProperty(iterationResource, model.createProperty(namespace + "associatedProject"))
                    .mapWith(resource -> resource.asResource().getURI().substring(namespace.length()))
                    .toList();

            IterationDto iteration = new IterationDto();
            iteration.setName(iterationName);
            iteration.setSubject(iterationSubject);
            iteration.setFrom(LocalDate.parse(iterationFrom));
            iteration.setTo(LocalDate.parse(iterationTo));
            iteration.setAssociatedProjects((ArrayList<String>) associatedProjects);
            return iteration;
        } finally {
            dataset.end();
        }
    }

    public List<IterationDto> getIterationsByProject(String projectId) {
        String projectURI = namespace + projectId;
        Resource projectResource = ResourceFactory.createResource(projectURI);
        dataset.begin(ReadWrite.READ);
        try {
            Model model = dataset.getDefaultModel();

            List<IterationDto> iterations = new ArrayList<>();

            StmtIterator stmtIterator = model.listStatements(null, model.createProperty(namespace + "associatedProject"), projectResource);
            while (stmtIterator.hasNext()) {
                Resource iterationResource = stmtIterator.next().getSubject();

                String iterationName = model.getProperty(iterationResource, model.createProperty(namespace + "iterationName"))
                        .getString();
                String iterationSubject = model.getProperty(iterationResource, model.createProperty(namespace + "iterationSubject"))
                        .getString();
                String iterationFrom = model.getProperty(iterationResource, model.createProperty(namespace + "iterationFrom"))
                        .getString();
                String iterationTo = model.getProperty(iterationResource, model.createProperty(namespace + "iterationTo"))
                        .getString();
                List<String> associatedProjects = model.listObjectsOfProperty(iterationResource, model.createProperty(namespace + "associatedProject"))
                        .mapWith(resource -> resource.asResource().getURI().substring(namespace.length()))
                        .toList();

                IterationDto iteration = new IterationDto();
                iteration.setName(iterationName);
                iteration.setSubject(iterationSubject);
                iteration.setFrom(LocalDate.parse(iterationFrom));
                iteration.setTo(LocalDate.parse(iterationTo));
                iteration.setAssociatedProjects((ArrayList<String>) associatedProjects);
                iterations.add(iteration);
            }

            return iterations;
        } finally {
            dataset.end();
        }
    }
}
