package com.example.learningdashboard.repository;

import com.example.learningdashboard.dtos.QFItemDto;
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
public class QFItemRepository {

    @Autowired
    private String prefixes;

    @Autowired
    private Dataset dataset;

    @Autowired
    private String namespace;

    public QFItemDto save(QFItemDto qfItem) {
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
                            ResourceFactory.createTypedLiteral(0.0))
                    .add(qfItemResource, ResourceFactory.createProperty(namespace + "QFItemWeight"),
                            ResourceFactory.createTypedLiteral(qfItem.getWeight()));


            String qfClassURI = namespace + "QF";
            Resource qfClass = ResourceFactory.createResource(qfClassURI);
            Resource sourceQF = ResourceFactory.createResource(qfItem.getSourceQF());
            if (dataset.getDefaultModel().contains(sourceQF, RDF.type, qfClass)) {
                dataset.getDefaultModel().add(qfItemResource,
                        ResourceFactory.createProperty(namespace + "sourceQF"),
                        sourceQF);
            } else {
                throw new IllegalArgumentException("The source SI does not exist in the dataset.");
            }

            String categoryClassURI = namespace + "Category";
            Resource categoryClass = ResourceFactory.createResource(categoryClassURI);
            Resource sourceCategory = ResourceFactory.createResource(qfItem.getCategory());
            if (dataset.getDefaultModel().contains(sourceCategory, RDF.type, categoryClass)) {
                dataset.getDefaultModel().add(qfItemResource,
                        ResourceFactory.createProperty(namespace + "QFItemCategory"),
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

    public List<QFItemDto> findAll() {
        List<QFItemDto> qfItems = new ArrayList<>();
        dataset.begin(ReadWrite.READ);
        try {
            dataset.getDefaultModel().listResourcesWithProperty(RDF.type, ResourceFactory.createResource(namespace + "QFItem"))
                    .forEachRemaining(qfItemResource -> {
                        QFItemDto qfItem = new QFItemDto();
                        qfItem.setThreshold(Float.parseFloat(qfItemResource.getProperty(ResourceFactory.createProperty(namespace + "QFItemThreshold")).getString()));
                        qfItem.setValue(Float.parseFloat(qfItemResource.getProperty(ResourceFactory.createProperty(namespace + "QFItemValue")).getString()));
                        qfItem.setWeight(Float.parseFloat(qfItemResource.getProperty(ResourceFactory.createProperty(namespace + "QFItemWeight")).getString()));
                        qfItem.setSourceQF(qfItemResource.getProperty(ResourceFactory.createProperty(namespace + "sourceQF")).getString());
                        qfItem.setCategory(qfItemResource.getProperty(ResourceFactory.createProperty(namespace + "QFItemCategory")).getString());
                        qfItem.setMetrics((ArrayList<String>) qfItemResource.listProperties(ResourceFactory.createProperty(namespace + "hasMetric"))
                                .mapWith(Statement::getObject).mapWith(RDFNode::asResource)
                                .mapWith(Resource::getLocalName).toList());
                        qfItems.add(qfItem);
                    });

            dataset.commit();
            return qfItems;
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }

    public QFItemDto findById(String qfItemId) {
        String qfItemURI = namespace + qfItemId;
        Resource qfItemResource = ResourceFactory.createResource(qfItemURI);
        dataset.begin(ReadWrite.READ);
        try {
            Model model = dataset.getDefaultModel();

            if (!model.containsResource(qfItemResource)) {
                return null;
            }

            String QFItemThreshold = model.getProperty(qfItemResource, model.createProperty(namespace + "QFItemThreshold"))
                    .getString();
            String QFItemValue = model.getProperty(qfItemResource, model.createProperty(namespace + "QFItemValue"))
                    .getString();
            String QFItemWeight = model.getProperty(qfItemResource, model.createProperty(namespace + "QFItemWeight"))
                    .getString();
            String sourceQF = model.getProperty(qfItemResource, model.createProperty(namespace + "sourceQF"))
                    .getString();
            String QFItemCategory = model.getProperty(qfItemResource, model.createProperty(namespace + "QFItemCategory"))
                    .getString();
            List<String> Metrics = model.listObjectsOfProperty(qfItemResource, model.createProperty(namespace + "hasMetric"))
                    .mapWith(resource -> resource.asResource().getURI().substring(namespace.length()))
                    .toList();

            QFItemDto qfItem = new QFItemDto();
            qfItem.setThreshold(Float.parseFloat(QFItemThreshold));
            qfItem.setValue(Float.parseFloat(QFItemValue));
            qfItem.setWeight(Float.parseFloat(QFItemWeight));
            qfItem.setSourceQF(sourceQF);
            qfItem.setCategory(QFItemCategory);
            qfItem.setMetrics((ArrayList<String>) Metrics);
            return qfItem;
        } finally {
            dataset.end();
        }
    }
}

