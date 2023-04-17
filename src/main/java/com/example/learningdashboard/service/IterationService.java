package com.example.learningdashboard.service;

import com.example.learningdashboard.dtos.IterationDto;
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
        return null;
    }

    public IterationDto getIterationById(String iterationId) {
        return null;
    }
}
