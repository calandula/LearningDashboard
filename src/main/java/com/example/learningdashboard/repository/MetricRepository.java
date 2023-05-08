package com.example.learningdashboard.repository;

import com.example.learningdashboard.dtos.CategoryDto;
import com.example.learningdashboard.dtos.MetricDto;
import com.example.learningdashboard.dtos.SIItemDto;
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
public class MetricRepository {

    @Autowired
    private String prefixes;

    @Autowired
    private Dataset dataset;

    @Autowired
    private String namespace;

    public MetricDto save(MetricDto metric, String metricId) {
        String metricURI = metricId == null ? namespace + UUID.randomUUID().toString() : metricId;
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
                            ResourceFactory.createTypedLiteral(metric.getValue()))
                    .add(metricResource, ResourceFactory.createProperty(namespace + "metricWeight"),
                            ResourceFactory.createTypedLiteral(metric.getWeight()));


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
            return null;
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }

    public List<MetricDto> findAll() {
        List<MetricDto> metrics = new ArrayList<>();
        dataset.begin(ReadWrite.READ);
        try {
            dataset.getDefaultModel().listResourcesWithProperty(RDF.type, ResourceFactory.createResource(namespace + "Metric"))
                    .forEachRemaining(metricResource -> {
                        MetricDto metric = new MetricDto();
                        metric.setName(metricResource.getProperty(ResourceFactory.createProperty(namespace + "metricName")).getString());
                        metric.setDescription(metricResource.getProperty(ResourceFactory.createProperty(namespace + "metricDescription")).getString());
                        metric.setValue(Float.parseFloat(metricResource.getProperty(ResourceFactory.createProperty(namespace + "metricValue")).getString()));
                        metric.setWeight(Float.parseFloat(metricResource.getProperty(ResourceFactory.createProperty(namespace + "metricWeight")).getString()));
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

    public MetricDto findById(String metricId) {
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

            MetricDto metric = new MetricDto();
            metric.setName(metricName);
            metric.setDescription(metricDescription);
            metric.setCategory(metricCategory);
            metric.setValue(Float.parseFloat(metricValue));
            metric.setThreshold(Float.parseFloat(metricThreshold));
            metric.setWeight(Float.parseFloat(metricWeight));
            metric.setId(JenaUtils.parseId(metricResource.getURI()));
            return metric;
        } finally {
            dataset.end();
        }
    }

    public List<MetricDto> findByQFItem(String qfItemId) {
        String qfItemURI = namespace + qfItemId;
        Resource qfItemResource = ResourceFactory.createResource(qfItemURI);
        List<MetricDto> metrics = new ArrayList<>();
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

                    MetricDto metric = new MetricDto();
                    metric.setName(String.valueOf(Float.parseFloat(metricResource.getProperty(ResourceFactory.createProperty(namespace + "metricName")).getString())));
                    metric.setDescription(String.valueOf(Float.parseFloat(metricResource.getProperty(ResourceFactory.createProperty(namespace + "metricDescription")).getString())));
                    metric.setCategory(metricResource.getPropertyResourceValue(ResourceFactory.createProperty(namespace + "metricCategory")).getURI());
                    metric.setValue(Float.parseFloat(metricResource.getProperty(ResourceFactory.createProperty(namespace + "metricValue")).getObject().asResource().getURI().substring(namespace.length())));
                    metric.setWeight(Float.parseFloat(metricResource.getProperty(ResourceFactory.createProperty(namespace + "metricWeight")).getObject().asResource().getURI().substring(namespace.length())));
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
}
