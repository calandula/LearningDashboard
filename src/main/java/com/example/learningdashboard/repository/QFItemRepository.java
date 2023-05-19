package com.example.learningdashboard.repository;

import com.example.learningdashboard.dtos.QFItemDto;
import com.example.learningdashboard.dtos.SIItemDto;
import com.example.learningdashboard.utils.JenaUtils;
import com.example.learningdashboard.utils.SerializablePair;
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

    public QFItemDto save(QFItemDto qfItem, String QFItemId) {
        QFItemId = QFItemId == null ? UUID.randomUUID().toString() : QFItemId;
        String qfItemURI = namespace + QFItemId;
        Resource qfItemResource = ResourceFactory.createResource(qfItemURI);
        Resource qfItemClass = ResourceFactory.createResource(namespace + "QFItem");
        dataset.begin(ReadWrite.WRITE);
        try {
            checkIds(qfItem);

            checkWeight(qfItem);

            createWeights(qfItem, qfItemResource);


            double weightedSum = calculateWeightedSumAndCheckWeight(qfItem);


            dataset.getDefaultModel()
                    .add(qfItemResource, RDF.type, qfItemClass)
                    .add(qfItemResource, ResourceFactory.createProperty(namespace + "QFItemThreshold"),
                            ResourceFactory.createTypedLiteral(qfItem.getThreshold()))
                    .add(qfItemResource, ResourceFactory.createProperty(namespace + "QFItemValue"), ResourceFactory.createTypedLiteral(weightedSum));

            qfItem.setValue((float) weightedSum);

            String qfClassURI = namespace + "QF";
            Resource qfClass = ResourceFactory.createResource(qfClassURI);
            Resource sourceQF = ResourceFactory.createResource(namespace + qfItem.getSourceQF());
            System.out.println(qfItem.getSourceQF());
            System.out.println(qfClassURI);
            if (dataset.getDefaultModel().contains(sourceQF, RDF.type, qfClass)) {
                dataset.getDefaultModel().add(qfItemResource,
                        ResourceFactory.createProperty(namespace + "sourceQF"),
                        sourceQF);
            } else {
                throw new IllegalArgumentException("The source QF does not exist in the dataset.");
            }

            String categoryClassURI = namespace + "Category";
            Resource categoryClass = ResourceFactory.createResource(categoryClassURI);
            Resource sourceCategory = ResourceFactory.createResource(namespace + qfItem.getCategory());
            if (dataset.getDefaultModel().contains(sourceCategory, RDF.type, categoryClass)) {
                dataset.getDefaultModel().add(qfItemResource,
                        ResourceFactory.createProperty(namespace + "QFItemCategory"),
                        sourceCategory);
            } else {
                throw new IllegalArgumentException("The sourceCategory does not exist in the dataset.");
            }


            dataset.commit();
            qfItem.setId(QFItemId);
            return qfItem;
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }

    private void createWeights(QFItemDto qfItem, Resource qfItemResource) {
        for (SerializablePair<String, Float> metricPair : qfItem.getMetricWeights()) {
            String metricId = metricPair.getId();
            float weight = metricPair.getWeight();

            String weightId = UUID.randomUUID().toString();
            String weightURI = namespace + weightId;
            Resource weightResource = ResourceFactory.createResource(weightURI);
            Resource weightClass = ResourceFactory.createResource(namespace + "Weight");
            dataset.getDefaultModel().add(weightResource, RDF.type, weightClass);
            dataset.getDefaultModel().add(weightResource, ResourceFactory.createProperty(namespace + "weightValue"), ResourceFactory.createTypedLiteral(weight));
            dataset.getDefaultModel().add(qfItemResource, ResourceFactory.createProperty(namespace + "hasWeightedMetric"), weightResource);

            Resource metricResource = ResourceFactory.createResource(namespace + metricId);
            dataset.getDefaultModel().add(weightResource, ResourceFactory.createProperty(namespace + "hasMetric"), metricResource);
        }
    }

    private double calculateWeightedSumAndCheckWeight(QFItemDto qfItem) {
        double weightedSum = 0.0;
        double totalWeight = 0.0;

        for (SerializablePair<String, Float> metricTuple : qfItem.getMetricWeights()) {
            String metricId = metricTuple.getId();
            float metricWeight = metricTuple.getWeight();

            Statement valueStmt = dataset.getDefaultModel().getProperty(
                    ResourceFactory.createResource(namespace + metricId),
                    ResourceFactory.createProperty(namespace + "metricValue")
            );
            if (valueStmt == null) {
                throw new IllegalArgumentException("The value of the metric item is missing.");
            }
            RDFNode valueNode = valueStmt.getObject();
            if (!valueNode.isLiteral()) {
                throw new IllegalArgumentException("The value of the metric item must be a literal.");
            }
            double metricValue = valueNode.asLiteral().getDouble();

            weightedSum += metricValue * metricWeight;
            totalWeight += metricWeight;
        }

        if (Math.abs(totalWeight - 1.0) > 0.0001) {
            throw new IllegalArgumentException("The sum of the metric items' weights must be 1.0.");
        }

        return weightedSum;
    }

    private void checkWeight(QFItemDto qfItem) {
        double totalWeight;
        totalWeight = 0.0f;
        for (SerializablePair<String, Float> metricPair : qfItem.getMetricWeights()) {
            totalWeight += metricPair.getWeight();
        }
        if (Math.abs(totalWeight - 1.0) > 0.0001) {
            throw new IllegalArgumentException("The sum of the metric items' weights must be 1.0.");
        }
    }

    private void checkIds(QFItemDto qfItem) {
        List<Resource> metricWeightResources = qfItem.getMetricWeights().stream()
                .map(metricPair -> ResourceFactory.createResource(namespace + metricPair.getId()))
                .filter(metricResource -> dataset.getDefaultModel().containsResource(metricResource))
                .toList();
        if (metricWeightResources.size() != qfItem.getMetricWeights().size()) {
            throw new IllegalArgumentException("One or more metric item IDs do not exist in the dataset.");
        }
    }

    public List<QFItemDto> findAll() {
        List<QFItemDto> qfItems = new ArrayList<>();
        dataset.begin(ReadWrite.READ);
        try {
            Model model = dataset.getDefaultModel();

            ResIterator siItemIter = model.listResourcesWithProperty(RDF.type, ResourceFactory.createResource(namespace + "QFItem"));
            while (siItemIter.hasNext()) {
                Resource qfItemResource = siItemIter.next();

                QFItemDto qfItem = new QFItemDto();
                qfItem.setThreshold(Float.parseFloat(qfItemResource.getProperty(ResourceFactory.createProperty(namespace + "QFItemThreshold")).getString()));
                qfItem.setValue(Float.parseFloat(qfItemResource.getProperty(ResourceFactory.createProperty(namespace + "QFItemValue")).getString()));
                qfItem.setCategory(qfItemResource.getPropertyResourceValue(ResourceFactory.createProperty(namespace + "QFItemCategory")).getURI());
                qfItem.setSourceQF(qfItemResource.getPropertyResourceValue(ResourceFactory.createProperty(namespace + "sourceQF")).getURI());
                qfItem.setId(JenaUtils.parseId(qfItemResource.getURI()));

                List<SerializablePair<String, Float>> metricWeights = new ArrayList<>();
                StmtIterator metricIter = model.listStatements(qfItemResource, ResourceFactory.createProperty(namespace + "hasWeightedMetric"), (RDFNode) null);
                while (metricIter.hasNext()) {
                    Statement stmt = metricIter.next();
                    Resource weightedMetricResource = stmt.getObject().asResource();
                    Resource metricResource = weightedMetricResource.getProperty(ResourceFactory.createProperty(namespace + "hasMetric")).getObject().asResource();
                    float metricWeight = weightedMetricResource.getProperty(ResourceFactory.createProperty(namespace + "weightValue")).getFloat();
                    String metricId = JenaUtils.parseId(metricResource.getURI());
                    metricWeights.add(new SerializablePair<>(metricId, metricWeight));
                }
                qfItem.setMetricWeights((ArrayList<SerializablePair<String, Float>>) metricWeights);

                qfItems.add(qfItem);
            }

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
            String sourceQF = model.getProperty(qfItemResource, model.createProperty(namespace + "sourceQF"))
                    .getString();
            String QFItemCategory = model.getProperty(qfItemResource, model.createProperty(namespace + "QFItemCategory"))
                    .getString();
            List<SerializablePair<String, Float>> metricWeights = new ArrayList<>();
            StmtIterator metricIter = qfItemResource.listProperties(ResourceFactory.createProperty(namespace + "hasWeightedMetric"));
            while (metricIter.hasNext()) {
                Statement stmt = metricIter.next();
                Resource weightedMetricResource = stmt.getObject().asResource();
                Resource metricResource = weightedMetricResource.getProperty(ResourceFactory.createProperty(namespace + "hasMetric")).getObject().asResource();
                float metricWeight = weightedMetricResource.getProperty(ResourceFactory.createProperty(namespace + "weightValue")).getFloat();
                String metricId = JenaUtils.parseId(metricResource.getURI());
                metricWeights.add(new SerializablePair<>(metricId, metricWeight));
            }

            QFItemDto qfItem = new QFItemDto();
            qfItem.setThreshold(Float.parseFloat(QFItemThreshold));
            qfItem.setValue(Float.parseFloat(QFItemValue));
            qfItem.setMetricWeights((ArrayList<SerializablePair<String, Float>>) metricWeights);
            qfItem.setSourceQF(sourceQF);
            qfItem.setCategory(QFItemCategory);
            qfItem.setId(JenaUtils.parseId(qfItemResource.getURI()));
            return qfItem;
        } finally {
            dataset.end();
        }
    }

    public void deleteById(String QFItemId, boolean update) {
        String QFItemURI = namespace + QFItemId;
        Resource categoryItemResource = ResourceFactory.createResource(QFItemURI);
        dataset.begin(ReadWrite.WRITE);
        try {
            if (update) {
                StmtIterator it = dataset.getDefaultModel().listStatements(categoryItemResource, null, (RDFNode) null);
                while (it.hasNext()) {
                    Statement stmt = it.next();
                    dataset.getDefaultModel().remove(stmt);
                }
            }
            else {
                dataset.getDefaultModel().removeAll(categoryItemResource, null, (RDFNode) null);
            }
            dataset.commit();
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }

    public List<QFItemDto> findBySIItem(String siItemId) {
        String siItemURI = namespace + siItemId;
        Resource siItemResource = ResourceFactory.createResource(siItemURI);
        List<QFItemDto> qfItems = new ArrayList<>();
        dataset.begin(ReadWrite.READ);
        try {
            Model model = dataset.getDefaultModel();

            if (!model.containsResource(siItemResource)) {
                throw new IllegalArgumentException("SIItem with ID " + siItemId + " does not exist in the dataset.");
            }

            StmtIterator it = model.listStatements(siItemResource, ResourceFactory.createProperty(namespace + "hasQFI"), (RDFNode) null);
            while (it.hasNext()) {
                Statement stmt = it.next();
                RDFNode qfItemNode = stmt.getObject();
                if (qfItemNode.isResource()) {
                    Resource qfItemResource = qfItemNode.asResource();

                    QFItemDto qfItem = new QFItemDto();
                    ArrayList<SerializablePair<String, Float>> metricWeights = new ArrayList<>();
                    StmtIterator metricIter = qfItemResource.listProperties(ResourceFactory.createProperty(namespace + "hasWeightedMetric"));
                    while (metricIter.hasNext()) {
                        Statement stmt2 = metricIter.next();
                        Resource weightedMetricResource = stmt2.getObject().asResource();
                        Resource metricResource = weightedMetricResource.getProperty(ResourceFactory.createProperty(namespace + "hasMetric")).getObject().asResource();
                        float metricWeight = weightedMetricResource.getProperty(ResourceFactory.createProperty(namespace + "weightValue")).getFloat();
                        String metricId = JenaUtils.parseId(metricResource.getURI());
                        metricWeights.add(new SerializablePair<>(metricId, metricWeight));
                    }
                    qfItem.setThreshold(Float.parseFloat(qfItemResource.getProperty(ResourceFactory.createProperty(namespace + "QFItemThreshold")).getString()));
                    qfItem.setValue(Float.parseFloat(qfItemResource.getProperty(ResourceFactory.createProperty(namespace + "QFItemValue")).getString()));
                    qfItem.setCategory(qfItemResource.getPropertyResourceValue(ResourceFactory.createProperty(namespace + "QFItemCategory")).getURI());
                    qfItem.setSourceQF(qfItemResource.getProperty(ResourceFactory.createProperty(namespace + "sourceQF")).getObject().asResource().getURI().substring(namespace.length()));
                    qfItem.setId(JenaUtils.parseId(qfItemResource.getURI()));
                    qfItem.setMetricWeights(metricWeights);
                    qfItems.add(qfItem);
                }
            }

            dataset.commit();
            return qfItems;
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }

    public List<QFItemDto> findByProject(String projectId) {
        String projectURI = namespace + projectId;
        Resource projectResource = ResourceFactory.createResource(projectURI);
        List<QFItemDto> qfItems = new ArrayList<>();
        dataset.begin(ReadWrite.READ);
        try {
            Model model = dataset.getDefaultModel();

            if (!model.containsResource(projectResource)) {
                throw new IllegalArgumentException("Project with ID " + projectId + " does not exist in the dataset.");
            }

            ResIterator it = model.listResourcesWithProperty(ResourceFactory.createProperty(namespace + "hasHI"), projectResource);
            while (it.hasNext()) {
                Resource siItemResource = it.next();

                StmtIterator it2 = model.listStatements(siItemResource, ResourceFactory.createProperty(namespace + "hasQFI"), (RDFNode) null);
                while (it2.hasNext()) {
                    Statement stmt = it2.next();
                    RDFNode qfItemNode = stmt.getObject();
                    if (qfItemNode.isResource()) {
                        Resource qfItemResource = qfItemNode.asResource();

                        QFItemDto qfItem = new QFItemDto();
                        ArrayList<SerializablePair<String, Float>> metricWeights = new ArrayList<>();
                        StmtIterator metricIter = qfItemResource.listProperties(ResourceFactory.createProperty(namespace + "hasWeightedMetric"));
                        while (metricIter.hasNext()) {
                            Statement stmt2 = metricIter.next();
                            Resource weightedMetricResource = stmt2.getObject().asResource();
                            Resource metricResource = weightedMetricResource.getProperty(ResourceFactory.createProperty(namespace + "hasMetric")).getObject().asResource();
                            float metricWeight = weightedMetricResource.getProperty(ResourceFactory.createProperty(namespace + "Weight")).getFloat();
                            String metricId = JenaUtils.parseId(metricResource.getURI());
                            metricWeights.add(new SerializablePair<>(metricId, metricWeight));
                        }
                        qfItem.setThreshold(Float.parseFloat(qfItemResource.getProperty(ResourceFactory.createProperty(namespace + "QFItemThreshold")).getString()));
                        qfItem.setValue(Float.parseFloat(qfItemResource.getProperty(ResourceFactory.createProperty(namespace + "QFItemValue")).getString()));
                        qfItem.setCategory(qfItemResource.getPropertyResourceValue(ResourceFactory.createProperty(namespace + "QFItemCategory")).getURI());
                        qfItem.setSourceQF(qfItemResource.getProperty(ResourceFactory.createProperty(namespace + "sourceQF")).getObject().asResource().getURI().substring(namespace.length()));
                        qfItem.setId(JenaUtils.parseId(qfItemResource.getURI()));
                        qfItem.setMetricWeights(metricWeights);
                        qfItems.add(qfItem);
                    }
                }
            }

            dataset.commit();
            return qfItems;
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }
}

