package com.example.learningdashboard.repository;

import com.example.learningdashboard.dtos.QFDto;
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
public class QFRepository {

    @Autowired
    private String prefixes;

    @Autowired
    private Dataset dataset;

    @Autowired
    private String namespace;

    public QFDto save(QFDto qf, String qfId) {
        qfId = qfId == null ? UUID.randomUUID().toString() : qfId;
        String qfURI = namespace + qfId;
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
            qf.setId(qfId);
            return qf;
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
                        qf.setId(JenaUtils.parseId(qfResource.getURI()));
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
            qf.setId(JenaUtils.parseId(qfResource.getURI()));
            return qf;
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
            } else {
                dataset.getDefaultModel().removeAll(qfResource, null, (RDFNode) null);
            }
            dataset.commit();
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }
}
