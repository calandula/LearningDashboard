package com.example.learningdashboard.repository;

import com.example.learningdashboard.dtos.IterationDto;
import com.example.learningdashboard.dtos.QFDto;
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
public class QFRepository {

    @Autowired
    private String prefixes;

    @Autowired
    private Dataset dataset;

    @Autowired
    private String namespace;

    public QFDto save(QFDto qf, String qfId) {
        String qfURI = qfId == null ? namespace + UUID.randomUUID().toString() : qfId;
        Resource qfResource = ResourceFactory.createResource(qfURI);
        Resource qfClass = ResourceFactory.createResource(namespace + "QF");
        dataset.begin(ReadWrite.WRITE);
        try {
            dataset.getDefaultModel()
                    .add(qfResource, RDF.type, qfClass)
                    .add(qfResource, ResourceFactory.createProperty(namespace + "QFName"),
                            ResourceFactory.createPlainLiteral(qf.getName()))
                    .add(qfResource, ResourceFactory.createProperty(namespace + "QFDescription"),
                            ResourceFactory.createPlainLiteral(qf.getDescription()))
                    .add(qfResource, ResourceFactory.createProperty(namespace + "QFDataSource"),
                            ResourceFactory.createPlainLiteral(qf.getDataSource()));


            dataset.commit();
            return null;
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }

    public List<QFDto> findAll() {
        List<QFDto> qfs = new ArrayList<>();
        dataset.begin(ReadWrite.READ);
        try {
            dataset.getDefaultModel().listResourcesWithProperty(RDF.type, ResourceFactory.createResource(namespace + "QF"))
                    .forEachRemaining(qfResource -> {
                        QFDto qf = new QFDto();
                        qf.setName(qfResource.getProperty(ResourceFactory.createProperty(namespace + "QFName")).getString());
                        qf.setDescription(qfResource.getProperty(ResourceFactory.createProperty(namespace + "QFDescription")).getString());
                        qf.setDataSource(qfResource.getProperty(ResourceFactory.createProperty(namespace + "QFDataSource")).getString());
                        qfs.add(qf);
                    });

            dataset.commit();
            return qfs;
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }

    public QFDto findById(String qfId) {
        String qfURI = namespace + qfId;
        Resource qfResource = ResourceFactory.createResource(qfURI);
        dataset.begin(ReadWrite.READ);
        try {
            Model model = dataset.getDefaultModel();

            if (!model.containsResource(qfResource)) {
                return null;
            }

            String QFName = model.getProperty(qfResource, model.createProperty(namespace + "QFName"))
                    .getString();
            String QFDescription = model.getProperty(qfResource, model.createProperty(namespace + "QFDescription"))
                    .getString();
            String QFDataSource = model.getProperty(qfResource, model.createProperty(namespace + "QFDataSource"))
                    .getString();

            QFDto qf = new QFDto();
            qf.setName(QFName);
            qf.setDescription(QFDescription);
            qf.setDataSource(QFDataSource);
            return qf;
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

    public void deleteById(String qfId, boolean update) {
        String qfURI = namespace + qfId;
        Resource qfResource = ResourceFactory.createResource(qfURI);
        dataset.begin(ReadWrite.WRITE);
        try {
            if (update) {
                StmtIterator it = dataset.getDefaultModel().listStatements(qfResource, null, (RDFNode) null);
                while (it.hasNext()) {
                    Statement stmt = it.next();
                    dataset.getDefaultModel().remove(stmt);
                }
            }
            else {
                dataset.getDefaultModel().removeAll(qfResource, null, (RDFNode) null);
            }
            dataset.commit();
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }
}
