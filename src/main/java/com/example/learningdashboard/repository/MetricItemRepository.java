package com.example.learningdashboard.repository;

import com.example.learningdashboard.dtos.MetricItemDto;
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
public class MetricItemRepository {

    @Autowired
    private String prefixes;

    @Autowired
    private Dataset dataset;

    @Autowired
    private String namespace;

    public MetricItemDto save(MetricItemDto metric, String metricId) {
        metricId = metricId == null ? UUID.randomUUID().toString() : metricId;
        String metricURI = namespace + metricId;
        Resource metricResource = ResourceFactory.createResource(metricURI);
        Resource metricClass = ResourceFactory.createResource(namespace + "Metric");
        dataset.begin(ReadWrite.WRITE);
        try {

            dataset.getDefaultModel()
                    .add(metricResource, RDF.type, metricClass)
                    .add(metricResource, ResourceFactory.createProperty(namespace + "metricName"),
                            ResourceFactory.createPlainLiteral(metric.getName()))
                    .add(metricResource, ResourceFactory.createProperty(namespace + "metricDescription"),
                            ResourceFactory.createPlainLiteral(metric.getDescription()))
                    .add(metricResource, ResourceFactory.createProperty(namespace + "metricThreshold"),
                            ResourceFactory.createTypedLiteral(metric.getThreshold()))
                    .add(metricResource, ResourceFactory.createProperty(namespace + "metricValue"),
                            ResourceFactory.createTypedLiteral(metric.getValue()));


            String categoryClassURI = namespace + "Category";
            Resource categoryClass = ResourceFactory.createResource(categoryClassURI);
            Resource sourceCategory = ResourceFactory.createResource(namespace + metric.getCategory());
            if (dataset.getDefaultModel().contains(sourceCategory, RDF.type, categoryClass)) {
                dataset.getDefaultModel().add(metricResource,
                        ResourceFactory.createProperty(namespace + "metricCategory"),
                        sourceCategory);
            } else {
                throw new IllegalArgumentException("The category does not exist in the dataset.");
            }

            dataset.commit();
            metric.setId(metricId);
            return metric;
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }

    public List<MetricItemDto> findAll() {
        List<MetricItemDto> metrics = new ArrayList<>();
        dataset.begin(ReadWrite.READ);
        try {
            dataset.getDefaultModel().listResourcesWithProperty(RDF.type, ResourceFactory.createResource(namespace + "Metric"))
                    .forEachRemaining(metricResource -> {
                        MetricItemDto metric = new MetricItemDto();
                        metric.setName(metricResource.getProperty(ResourceFactory.createProperty(namespace + "metricName")).getString());
                        metric.setDescription(metricResource.getProperty(ResourceFactory.createProperty(namespace + "metricDescription")).getString());
                        metric.setValue(Float.parseFloat(metricResource.getProperty(ResourceFactory.createProperty(namespace + "metricValue")).getString()));
                        metric.setThreshold(Float.parseFloat(metricResource.getProperty(ResourceFactory.createProperty(namespace + "metricThreshold")).getString()));
                        metric.setCategory(metricResource.getPropertyResourceValue(ResourceFactory.createProperty(namespace + "metricCategory")).getURI());
                        metric.setId(JenaUtils.parseId(metricResource.getURI()));
                        metrics.add(metric);
                    });

            dataset.commit();
            return metrics;
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }

    public MetricItemDto findById(String metricId) {
        String metricURI = namespace + metricId;
        Resource metricResource = ResourceFactory.createResource(metricURI);
        dataset.begin(ReadWrite.READ);
        try {
            Model model = dataset.getDefaultModel();

            if (!model.containsResource(metricResource)) {
                return null;
            }

            String metricName = model.getProperty(metricResource, model.createProperty(namespace + "metricName"))
                    .getString();
            String metricDescription = model.getProperty(metricResource, model.createProperty(namespace + "metricDescription"))
                    .getString();
            String metricValue = model.getProperty(metricResource, model.createProperty(namespace + "metricValue"))
                    .getString();
            String metricThreshold = model.getProperty(metricResource, model.createProperty(namespace + "metricThreshold"))
                    .getString();
            String metricWeight = model.getProperty(metricResource, model.createProperty(namespace + "metricWeight"))
                    .getString();
            String metricCategory = metricResource.getPropertyResourceValue(model.createProperty(namespace + "metricCategory")).getURI();

            MetricItemDto metric = new MetricItemDto();
            metric.setName(metricName);
            metric.setDescription(metricDescription);
            metric.setCategory(metricCategory);
            metric.setValue(Float.parseFloat(metricValue));
            metric.setThreshold(Float.parseFloat(metricThreshold));
            metric.setId(JenaUtils.parseId(metricResource.getURI()));
            return metric;
        } finally {
            dataset.end();
        }
    }

    public List<MetricItemDto> findByQFItem(String qfItemId) {
        String qfItemURI = namespace + qfItemId;
        Resource qfItemResource = ResourceFactory.createResource(qfItemURI);
        List<MetricItemDto> metrics = new ArrayList<>();
        dataset.begin(ReadWrite.READ);
        try {
            Model model = dataset.getDefaultModel();

            if (!model.containsResource(qfItemResource)) {
                throw new IllegalArgumentException("QFItem with ID " + qfItemId + " does not exist in the dataset.");
            }

            StmtIterator it = model.listStatements(qfItemResource, ResourceFactory.createProperty(namespace + "hasMetric"), (RDFNode) null);
            while (it.hasNext()) {
                Statement stmt = it.next();
                RDFNode metricNode = stmt.getObject();
                if (metricNode.isResource()) {
                    Resource metricResource = metricNode.asResource();

                    MetricItemDto metric = new MetricItemDto();
                    metric.setName(String.valueOf(Float.parseFloat(metricResource.getProperty(ResourceFactory.createProperty(namespace + "metricName")).getString())));
                    metric.setDescription(String.valueOf(Float.parseFloat(metricResource.getProperty(ResourceFactory.createProperty(namespace + "metricDescription")).getString())));
                    metric.setCategory(metricResource.getPropertyResourceValue(ResourceFactory.createProperty(namespace + "metricCategory")).getURI());
                    metric.setValue(Float.parseFloat(metricResource.getProperty(ResourceFactory.createProperty(namespace + "metricValue")).getObject().asResource().getURI().substring(namespace.length())));
                    metric.setThreshold(Float.parseFloat(metricResource.getProperty(ResourceFactory.createProperty(namespace + "metricThreshold")).getObject().asResource().getURI().substring(namespace.length())));
                    metric.setId(JenaUtils.parseId(metricResource.getURI()));
                    metrics.add(metric);
                }
            }

            dataset.commit();
            return metrics;
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }

    public void deleteById(String metricId, boolean update) {
        String metricURI = namespace + metricId;
        Resource metricResource = ResourceFactory.createResource(metricURI);
        dataset.begin(ReadWrite.WRITE);
        try {
            if (update) {
                StmtIterator it = dataset.getDefaultModel().listStatements(metricResource, null, (RDFNode) null);
                while (it.hasNext()) {
                    Statement stmt = it.next();
                    dataset.getDefaultModel().remove(stmt);
                }
            }
            else {
                dataset.getDefaultModel().removeAll(metricResource, null, (RDFNode) null);
            }
            dataset.commit();
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }

    public void updateValue(float newValue, String metricId) {
        String metricURI = namespace + metricId;
        Resource metricResource = ResourceFactory.createResource(metricURI);

        dataset.begin(ReadWrite.WRITE);
        try {
            Model model = dataset.getDefaultModel();
            if (!model.containsResource(metricResource)) {
                throw new IllegalArgumentException("Metric with ID " + metricId + " does not exist in the dataset.");
            }

            model.removeAll(metricResource, ResourceFactory.createProperty(namespace + "metricValue"), null);
            model.add(metricResource, ResourceFactory.createProperty(namespace + "metricValue"),
                    ResourceFactory.createTypedLiteral(newValue));

            List<Resource> qfItemResourcesToUpdate = new ArrayList<>();
            StmtIterator weightIter = model.listStatements(null, ResourceFactory.createProperty(namespace + "hasMetric"), metricResource);
            while (weightIter.hasNext()) {
                Statement weightStmt = weightIter.next();
                Resource weightResource = weightStmt.getSubject();

                StmtIterator qfItemIter = model.listStatements(null, ResourceFactory.createProperty(namespace + "hasWeightedMetric"), weightResource);
                while (qfItemIter.hasNext()) {
                    Statement qfItemStmt = qfItemIter.next();
                    Resource qfItemResource = qfItemStmt.getSubject();
                    qfItemResourcesToUpdate.add(qfItemResource);
                }
            }

            for (Resource qfItemResource : qfItemResourcesToUpdate) {
                float qfItemValue = computeQFItemValue(qfItemResource);

                model.removeAll(qfItemResource, ResourceFactory.createProperty(namespace + "QFItemValue"), null);
                model.add(qfItemResource, ResourceFactory.createProperty(namespace + "QFItemValue"),
                        ResourceFactory.createTypedLiteral(qfItemValue));
            }

            for (Resource qfItemResource : qfItemResourcesToUpdate) {
                List<Resource> siItemResourcesToUpdate = new ArrayList<>();
                StmtIterator SIItemWeightIter = model.listStatements(null, ResourceFactory.createProperty(namespace + "hasQFItem"), qfItemResource);
                while (SIItemWeightIter.hasNext()) {
                    Statement SIItemWeightStmt = SIItemWeightIter.next();
                    Resource SIItemWeightResource = SIItemWeightStmt.getSubject();

                    StmtIterator SIItemIter = model.listStatements(null, ResourceFactory.createProperty(namespace + "hasWeightedQFItem"), SIItemWeightResource);
                    while (SIItemIter.hasNext()) {
                        Statement SIItemStmt = SIItemIter.next();
                        Resource SIItemResource = SIItemStmt.getSubject();
                        siItemResourcesToUpdate.add(SIItemResource);
                    }
                }

                for (Resource siItemResource : siItemResourcesToUpdate) {
                    float siItemValue = computeSIItemValue(siItemResource);

                    model.removeAll(siItemResource, ResourceFactory.createProperty(namespace + "SIItemValue"), null);
                    model.add(siItemResource, ResourceFactory.createProperty(namespace + "SIItemValue"),
                            ResourceFactory.createTypedLiteral(siItemValue));
                }
            }

            dataset.commit();
        } catch (Exception e) {
            dataset.abort();
            throw e;
        } finally {
            dataset.end();
        }
    }

    private float computeQFItemValue(Resource qfItemResource) {
        float qfItemValue = 0.0f;
        StmtIterator stmtIter = qfItemResource.listProperties(ResourceFactory.createProperty(namespace + "hasWeightedMetric"));
        while (stmtIter.hasNext()) {
            Statement stmt = stmtIter.next();
            Resource weightedMetricResource = stmt.getObject().asResource();
            Resource metricResource = weightedMetricResource.getProperty(ResourceFactory.createProperty(namespace + "hasMetric")).getObject().asResource();
            float metricValue = metricResource.getProperty(ResourceFactory.createProperty(namespace + "metricValue")).getFloat();
            float metricWeight = weightedMetricResource.getProperty(ResourceFactory.createProperty(namespace + "weightValue")).getFloat();
            qfItemValue += metricValue * metricWeight;
        }
        return qfItemValue;
    }

    private float computeSIItemValue(Resource siItemResource) {
        float siItemValue = 0.0f;
        StmtIterator stmtIter = siItemResource.listProperties(ResourceFactory.createProperty(namespace + "hasWeightedQFItem"));
        while (stmtIter.hasNext()) {
            Statement stmt = stmtIter.next();
            Resource weightedQFItemResource = stmt.getObject().asResource();
            Resource qfItemResource = weightedQFItemResource.getProperty(ResourceFactory.createProperty(namespace + "hasQFItem")).getObject().asResource();
            float qfItemValue = qfItemResource.getProperty(ResourceFactory.createProperty(namespace + "QFItemValue")).getFloat();
            float qfItemWeight = weightedQFItemResource.getProperty(ResourceFactory.createProperty(namespace + "weightValue")).getFloat();
            siItemValue += qfItemValue * qfItemWeight;
        }
        return siItemValue;
    }
}
