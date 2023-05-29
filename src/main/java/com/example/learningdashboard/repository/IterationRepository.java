package com.example.learningdashboard.repository;

import com.example.learningdashboard.dtos.IterationDto;
import com.example.learningdashboard.utils.JenaUtils;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class IterationRepository {

    @Autowired
    private String prefixes;

    @Autowired
    private Dataset dataset;

    @Autowired
    private String namespace;

    public IterationDto save(IterationDto iteration, String iterationId) {
        iterationId = iterationId == null ? UUID.randomUUID().toString() : iterationId;
        String iterationURI = namespace + iterationId;
        Resource iterationResource = ResourceFactory.createResource(iterationURI);
        Resource iterationClass = ResourceFactory.createResource(namespace + "Iteration");

        dataset.begin(ReadWrite.WRITE);
        try {
            List<String> associatedProjects = iteration.getAssociatedProjects();
            if (associatedProjects == null) {
                associatedProjects = new ArrayList<>();
            }

            Model model = dataset.getDefaultModel();

            boolean allProjectsExist = associatedProjects.stream()
                    .allMatch(projectId -> model.containsResource(ResourceFactory.createResource(namespace + projectId)));

            if (!allProjectsExist) {
                throw new IllegalArgumentException("One or more project IDs do not exist in the dataset.");
            }

            model.add(iterationResource, RDF.type, iterationClass)
                    .add(iterationResource, ResourceFactory.createProperty(namespace + "iterationName"),
                            ResourceFactory.createPlainLiteral(iteration.getName()));
            model.add(iterationResource, ResourceFactory.createProperty(namespace + "iterationFrom"),
                    ResourceFactory.createTypedLiteral(iteration.getFrom().toString()));
            model.add(iterationResource, ResourceFactory.createProperty(namespace + "iterationTo"),
                    ResourceFactory.createTypedLiteral(iteration.getTo().toString()));

            if (iteration.getSubject() != null) {
                model.add(iterationResource, ResourceFactory.createProperty(namespace + "iterationSubject"),
                        ResourceFactory.createPlainLiteral(iteration.getSubject()));
            }

            associatedProjects.forEach(projectId -> {
                Resource projectResource = ResourceFactory.createResource(namespace + projectId);
                model.add(iterationResource, ResourceFactory.createProperty(namespace + "associatedProject"), projectResource);
            });

            dataset.commit();

            iteration.setId(iterationId);
            return iteration;
        } catch (Exception e) {
            dataset.abort();
            throw e;
        } finally {
            dataset.end();
        }
    }

    public List<IterationDto> findAll() {
        List<IterationDto> iterations = new ArrayList<>();
        dataset.begin(ReadWrite.READ);
        try {
            Model model = dataset.getDefaultModel();

            model.listResourcesWithProperty(RDF.type, ResourceFactory.createResource(namespace + "Iteration"))
                    .forEachRemaining(iterationResource -> {
                        IterationDto iteration = new IterationDto();
                        iteration.setName(iterationResource.getProperty(ResourceFactory.createProperty(namespace + "iterationName")).getString());

                        Statement subjectStatement = iterationResource.getProperty(ResourceFactory.createProperty(namespace + "iterationSubject"));
                        if (subjectStatement != null) {
                            iteration.setSubject(subjectStatement.getString());
                        }

                        iteration.setFrom(LocalDate.parse(iterationResource.getProperty(ResourceFactory.createProperty(namespace + "iterationFrom")).getString()));
                        iteration.setTo(LocalDate.parse(iterationResource.getProperty(ResourceFactory.createProperty(namespace + "iterationTo")).getString()));
                        iteration.setAssociatedProjects((ArrayList<String>) iterationResource.listProperties(ResourceFactory.createProperty(namespace + "associatedProject"))
                                .mapWith(Statement::getObject).mapWith(RDFNode::asResource)
                                .mapWith(Resource::getLocalName).toList());
                        iteration.setId(JenaUtils.parseId(iterationResource.getURI()));
                        iterations.add(iteration);
                    });

            dataset.commit();
            return iterations;
        } catch (Exception e) {
            dataset.abort();
            throw e;
        } finally {
            dataset.end();
        }
    }

    public IterationDto findById(String iterationId) {
        String iterationURI = namespace + iterationId;
        Resource iterationResource = ResourceFactory.createResource(iterationURI);
        IterationDto iteration = new IterationDto();
        dataset.begin(ReadWrite.READ);
        try {
            Model model = dataset.getDefaultModel();

            if (!model.containsResource(iterationResource)) {
                throw new IllegalArgumentException("Iteration with ID " + iterationId + " does not exist in the dataset.");
            }

            String iterationName = model.getProperty(iterationResource, model.createProperty(namespace + "iterationName"))
                    .getString();

            Statement subject = model.getProperty(iterationResource, model.createProperty(namespace + "iterationSubject"));
            if (subject.getObject() != null) {
                iteration.setSubject(subject.getObject().toString());
            }

            String iterationFrom = model.getProperty(iterationResource, model.createProperty(namespace + "iterationFrom"))
                    .getString();
            String iterationTo = model.getProperty(iterationResource, model.createProperty(namespace + "iterationTo"))
                    .getString();
            List<String> associatedProjects = model.listObjectsOfProperty(iterationResource, model.createProperty(namespace + "associatedProject"))
                    .mapWith(resource -> resource.asResource().getURI().substring(namespace.length()))
                    .toList();

            iteration.setName(iterationName);
            iteration.setFrom(LocalDate.parse(iterationFrom));
            iteration.setTo(LocalDate.parse(iterationTo));
            iteration.setAssociatedProjects((ArrayList<String>) associatedProjects);
            iteration.setId(JenaUtils.parseId(iterationResource.getURI()));

            return iteration;
        } finally {
            dataset.end();
        }
    }

    public List<IterationDto> getIterationsByProject(String projectId) {
        String projectURI = namespace + projectId;
        Resource projectResource = ResourceFactory.createResource(projectURI);
        IterationDto iteration = new IterationDto();
        dataset.begin(ReadWrite.READ);
        try {
            Model model = dataset.getDefaultModel();

            List<IterationDto> iterations = new ArrayList<>();

            StmtIterator stmtIterator = model.listStatements(null, model.createProperty(namespace + "associatedProject"), projectResource);
            while (stmtIterator.hasNext()) {
                Resource iterationResource = stmtIterator.next().getSubject();

                String iterationName = model.getProperty(iterationResource, model.createProperty(namespace + "iterationName"))
                        .getString();

                Statement subjectStatement = iterationResource.getProperty(ResourceFactory.createProperty(namespace + "iterationSubject"));
                if (subjectStatement != null) {
                    iteration.setSubject(subjectStatement.getString());
                }

                String iterationSubject = model.getProperty(iterationResource, model.createProperty(namespace + "iterationSubject"))
                        .getString();
                String iterationFrom = model.getProperty(iterationResource, model.createProperty(namespace + "iterationFrom"))
                        .getString();
                String iterationTo = model.getProperty(iterationResource, model.createProperty(namespace + "iterationTo"))
                        .getString();
                List<String> associatedProjects = model.listObjectsOfProperty(iterationResource, model.createProperty(namespace + "associatedProject"))
                        .mapWith(resource -> resource.asResource().getURI().substring(namespace.length()))
                        .toList();

                iteration.setName(iterationName);
                iteration.setSubject(iterationSubject);
                iteration.setFrom(LocalDate.parse(iterationFrom));
                iteration.setTo(LocalDate.parse(iterationTo));
                iteration.setAssociatedProjects((ArrayList<String>) associatedProjects);
                iteration.setId(JenaUtils.parseId(iterationResource.getURI()));

                iterations.add(iteration);
            }

            return iterations;
        } finally {
            dataset.end();
        }
    }

    public void deleteById(String iterationId, boolean update) {
        String iterationURI = namespace + iterationId;
        Resource iterationResource = ResourceFactory.createResource(iterationURI);
        dataset.begin(ReadWrite.WRITE);
        try {
            if (update) {
                StmtIterator it = dataset.getDefaultModel().listStatements(iterationResource, null, (RDFNode) null);
                while (it.hasNext()) {
                    Statement stmt = it.next();
                    dataset.getDefaultModel().remove(stmt);
                }
            } else {
                dataset.getDefaultModel().removeAll(iterationResource, null, (RDFNode) null);
            }
            dataset.commit();
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }
}
