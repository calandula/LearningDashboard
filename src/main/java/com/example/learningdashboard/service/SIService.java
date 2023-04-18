package com.example.learningdashboard.service;

import com.example.learningdashboard.dtos.SIDto;
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
public class SIService {

    @Autowired
    private String prefixes;

    @Autowired
    private Dataset dataset;

    @Autowired
    private String namespace;

    public SIDto createSI(SIDto si) {
        String siId = UUID.randomUUID().toString();
        String siURI = namespace + siId;
        Resource siResource = ResourceFactory.createResource(siURI);
        Resource siClass = ResourceFactory.createResource(namespace + "SI");
        dataset.begin(ReadWrite.WRITE);
        try {
            List<Resource> qfResources = si.getQualityFactors().stream()
                    .map(qfId -> ResourceFactory.createResource(namespace + qfId))
                    .filter(qfResource -> dataset.getDefaultModel().containsResource(qfResource))
                    .toList();
            if (qfResources.size() != si.getQualityFactors().size()) {
                throw new IllegalArgumentException("One or more quality factors IDs do not exist in the dataset.");
            }
            dataset.getDefaultModel()
                    .add(siResource, RDF.type, siClass)
                    .add(siResource, ResourceFactory.createProperty(namespace + "SIName"),
                            ResourceFactory.createPlainLiteral(si.getName()))
                    .add(siResource, ResourceFactory.createProperty(namespace + "SIDescription"),
                            ResourceFactory.createPlainLiteral(si.getDescription()))
                    .add(siResource, ResourceFactory.createProperty(namespace + "SIAssessmentModel"),
                            ResourceFactory.createPlainLiteral(si.getAssessmentModel()));


            qfResources.forEach(qfResource ->
                    dataset.getDefaultModel().add(siResource,
                            ResourceFactory.createProperty(namespace + "hasQF"),
                            qfResource));

            dataset.commit();
            return null;
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }

    public List<SIDto> getAllSIs() {
        return null;
    }

    public SIDto getSIById(String siId) {
        return null;
    }
}
