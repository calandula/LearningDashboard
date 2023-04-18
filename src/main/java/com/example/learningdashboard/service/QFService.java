package com.example.learningdashboard.service;

import com.example.learningdashboard.dtos.QFDto;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class QFService {

    @Autowired
    private String prefixes;

    @Autowired
    private Dataset dataset;

    @Autowired
    private String namespace;

    public QFDto createQF(QFDto qf) {
        String qfId = UUID.randomUUID().toString();
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
            return null;
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }

    public List<QFDto> getAllQFs() {
        return null;
    }

    public QFDto getQFById(String qfId) {
        return null;
    }
}
