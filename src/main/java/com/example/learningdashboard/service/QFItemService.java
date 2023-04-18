package com.example.learningdashboard.service;

import com.example.learningdashboard.dtos.QFItemDto;
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
public class QFItemService {

    @Autowired
    private String prefixes;

    @Autowired
    private Dataset dataset;

    @Autowired
    private String namespace;

    public QFItemDto createQF(QFItemDto qfItem) {
        String qfItemId = UUID.randomUUID().toString();
        String qfItemURI = namespace + qfItemId;
        Resource qfItemResource = ResourceFactory.createResource(qfItemURI);
        Resource qfItemClass = ResourceFactory.createResource(namespace + "QFItem");
        dataset.begin(ReadWrite.WRITE);
        try {
            List<Resource> metricResources = qfItem.getMetrics().stream()
                    .map(metricId -> ResourceFactory.createResource(namespace + metricId))
                    .filter(metricResource -> dataset.getDefaultModel().containsResource(metricResource))
                    .toList();
            if (metricResources.size() != qfItem.getMetrics().size()) {
                throw new IllegalArgumentException("One or more metric items IDs do not exist in the dataset.");
            }

            dataset.getDefaultModel()
                    .add(qfItemResource, RDF.type, qfItemClass)
                    .add(qfItemResource, ResourceFactory.createProperty(namespace + "QFItemThreshold"),
                            ResourceFactory.createTypedLiteral(qfItem.getThreshold()))
                    .add(qfItemResource, ResourceFactory.createProperty(namespace + "QFItemValue"),
                            ResourceFactory.createTypedLiteral(0.0));


            String qfClassURI = namespace + "QF";
            Resource qfClass = ResourceFactory.createResource(qfClassURI);
            Resource sourceQF = ResourceFactory.createResource(qfItem.getSourceQF());
            if (dataset.getDefaultModel().contains(sourceQF, RDF.type, qfClass)) {
                dataset.getDefaultModel().add(qfItemResource,
                        ResourceFactory.createProperty(namespace + "sourceSI"),
                        sourceQF);
            } else {
                throw new IllegalArgumentException("The source SI does not exist in the dataset.");
            }

            String categoryClassURI = namespace + "Category";
            Resource categoryClass = ResourceFactory.createResource(categoryClassURI);
            Resource sourceCategory = ResourceFactory.createResource(qfItem.getCategory());
            if (dataset.getDefaultModel().contains(sourceCategory, RDF.type, categoryClass)) {
                dataset.getDefaultModel().add(qfItemResource,
                        ResourceFactory.createProperty(namespace + "SIItemCategory"),
                        sourceCategory);
            } else {
                throw new IllegalArgumentException("The sourceCategory does not exist in the dataset.");
            }

            metricResources.forEach(metricItemResource ->
                    dataset.getDefaultModel().add(qfItemResource,
                            ResourceFactory.createProperty(namespace + "hasMetric"),
                            metricItemResource));


            dataset.commit();
            return null;
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }

    public List<QFItemDto> getAllQFItems() {
        return null;
    }

    public QFItemDto getQFById(String qfItemId) {
        return null;
    }
}
