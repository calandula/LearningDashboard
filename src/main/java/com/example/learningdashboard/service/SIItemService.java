package com.example.learningdashboard.service;

import com.example.learningdashboard.dtos.SIItemDto;
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
public class SIItemService {

    @Autowired
    private String prefixes;

    @Autowired
    private Dataset dataset;

    @Autowired
    private String namespace;

    public SIItemDto createSIItem(SIItemDto siItem) {
        String siItemId = UUID.randomUUID().toString();
        String siItemURI = namespace + siItemId;
        Resource siItemResource = ResourceFactory.createResource(siItemURI);
        Resource siItemClass = ResourceFactory.createResource(namespace + "SIItem");
        dataset.begin(ReadWrite.WRITE);
        try {
            List<Resource> qfItemResources = siItem.getQfItems().stream()
                    .map(qfItemId -> ResourceFactory.createResource(namespace + qfItemId))
                    .filter(qfItemResource -> dataset.getDefaultModel().containsResource(qfItemResource))
                    .toList();
            if (qfItemResources.size() != siItem.getQfItems().size()) {
                throw new IllegalArgumentException("One or more quality factors items IDs do not exist in the dataset.");
            }

            dataset.getDefaultModel()
                    .add(siItemResource, RDF.type, siItemClass)
                    .add(siItemResource, ResourceFactory.createProperty(namespace + "SIItemThreshold"),
                            ResourceFactory.createTypedLiteral(siItem.getThreshold()))
                    .add(siItemResource, ResourceFactory.createProperty(namespace + "SIItemValue"),
                            ResourceFactory.createTypedLiteral(0.0));


            String siClassURI = namespace + "SI";
            Resource siClass = ResourceFactory.createResource(siClassURI);
            Resource sourceSI = ResourceFactory.createResource(siItem.getSourceSI());
            if (dataset.getDefaultModel().contains(sourceSI, RDF.type, siClass)) {
                dataset.getDefaultModel().add(siItemResource,
                        ResourceFactory.createProperty(namespace + "sourceSI"),
                        sourceSI);
            } else {
                throw new IllegalArgumentException("The source SI does not exist in the dataset.");
            }

            String categoryClassURI = namespace + "Category";
            Resource categoryClass = ResourceFactory.createResource(categoryClassURI);
            Resource sourceCategory = ResourceFactory.createResource(siItem.getCategory());
            if (dataset.getDefaultModel().contains(sourceCategory, RDF.type, categoryClass)) {
                dataset.getDefaultModel().add(siItemResource,
                        ResourceFactory.createProperty(namespace + "SIItemCategory"),
                        sourceCategory);
            } else {
                throw new IllegalArgumentException("The sourceCategory does not exist in the dataset.");
            }

            qfItemResources.forEach(qfItemResource ->
                    dataset.getDefaultModel().add(siItemResource,
                            ResourceFactory.createProperty(namespace + "hasQFI"),
                            qfItemResource));


            dataset.commit();
            return null;
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }

    public List<SIItemDto> getAllSIItems() {
        return null;
    }

    public SIItemDto getSIItemById(String siItemId) {
        return null;
    }
}
