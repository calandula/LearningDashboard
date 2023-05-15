package com.example.learningdashboard.repository;

import com.example.learningdashboard.dtos.QFItemDto;
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
            List<Resource> metricResources = qfItem.getMetrics().stream()
                    .map(metricId -> ResourceFactory.createResource(namespace + metricId))
                    .filter(metricResource -> dataset.getDefaultModel().containsResource(metricResource))
                    .toList();
            if (metricResources.size() != qfItem.getMetrics().size()) {
                throw new IllegalArgumentException("One or more metric items IDs do not exist in the dataset.");
            }

            //check weight

            double totalWeight = 0.0;
            for (String metricId : qfItem.getMetrics()) {
                Statement weightStmt = dataset.getDefaultModel().getProperty(
                        ResourceFactory.createResource(namespace + metricId),
                        ResourceFactory.createProperty(namespace + "metricWeight")
                );
                if (weightStmt == null) {
                    throw new IllegalArgumentException("The weight of the metric item is missing.");
                }
                RDFNode weightNode = weightStmt.getObject();
                if (!weightNode.isLiteral()) {
                    throw new IllegalArgumentException("The weight of the metric item must be a literal.");
                }
                totalWeight += weightNode.asLiteral().getDouble();
            }
            if (Math.abs(totalWeight - 1.0) > 0.0001) {
                throw new IllegalArgumentException("The sum of the metric items' weights must be 1.0.");
            }

            //insert

            dataset.getDefaultModel()
                    .add(qfItemResource, RDF.type, qfItemClass)
                    .add(qfItemResource, ResourceFactory.createProperty(namespace + "QFItemThreshold"),
                            ResourceFactory.createTypedLiteral(qfItem.getThreshold()))
                    .add(qfItemResource, ResourceFactory.createProperty(namespace + "QFItemWeight"),
                            ResourceFactory.createTypedLiteral(qfItem.getWeight()));

            //compute Value
            double weightedSum = 0.0;
            for (String metricId : qfItem.getMetrics()) {
                Statement weightStmt = dataset.getDefaultModel().getProperty(
                        ResourceFactory.createResource(namespace + metricId),
                        ResourceFactory.createProperty(namespace + "metricWeight")
                );
                Statement valueStmt = dataset.getDefaultModel().getProperty(
                        ResourceFactory.createResource(namespace + metricId),
                        ResourceFactory.createProperty(namespace + "metricValue")
                );
                if (weightStmt == null || valueStmt == null) {
                    throw new IllegalArgumentException("The weight or value of the metric item is missing.");
                }
                RDFNode weightNode = weightStmt.getObject();
                RDFNode valueNode = valueStmt.getObject();
                if (!weightNode.isLiteral() || !valueNode.isLiteral()) {
                    throw new IllegalArgumentException("The weight and value of the metric item must be literals.");
                }
                double weight = weightNode.asLiteral().getDouble();
                double value = valueNode.asLiteral().getDouble();
                weightedSum += weight * value;
            }
            dataset.getDefaultModel().add(qfItemResource,
                    ResourceFactory.createProperty(namespace + "QFItemValue"),
                    ResourceFactory.createTypedLiteral(weightedSum));
            
            //--------


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

            metricResources.forEach(metricItemResource ->
                    dataset.getDefaultModel().add(qfItemResource,
                            ResourceFactory.createProperty(namespace + "hasMetric"),
                            metricItemResource));


            dataset.commit();
            qfItem.setId(QFItemId);
            return qfItem;
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
                        qfItem.setSourceQF(qfItemResource.getPropertyResourceValue(ResourceFactory.createProperty(namespace + "sourceQF")).getURI());
                        qfItem.setCategory(qfItemResource.getPropertyResourceValue(ResourceFactory.createProperty(namespace + "QFItemCategory")).getURI());
                        qfItem.setMetrics((ArrayList<String>) qfItemResource.listProperties(ResourceFactory.createProperty(namespace + "hasMetric"))
                                .mapWith(Statement::getObject).mapWith(RDFNode::asResource)
                                .mapWith(Resource::getLocalName).toList());
                        qfItem.setId(JenaUtils.parseId(qfItemResource.getURI()));
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
                    qfItem.setThreshold(Float.parseFloat(qfItemResource.getProperty(ResourceFactory.createProperty(namespace + "QFItemThreshold")).getString()));
                    qfItem.setValue(Float.parseFloat(qfItemResource.getProperty(ResourceFactory.createProperty(namespace + "QFItemValue")).getString()));
                    qfItem.setCategory(qfItemResource.getPropertyResourceValue(ResourceFactory.createProperty(namespace + "QFItemCategory")).getURI());
                    qfItem.setWeight(Float.parseFloat(qfItemResource.getProperty(ResourceFactory.createProperty(namespace + "QFItemWeight")).getString()));
                    qfItem.setSourceQF(qfItemResource.getProperty(ResourceFactory.createProperty(namespace + "sourceQF")).getObject().asResource().getURI().substring(namespace.length()));
                    qfItem.setMetrics((ArrayList<String>) qfItemResource.listProperties(ResourceFactory.createProperty(namespace + "hasMetric"))
                            .mapWith(Statement::getObject).mapWith(RDFNode::asResource)
                            .mapWith(Resource::getLocalName).toList());
                    qfItem.setId(JenaUtils.parseId(qfItemResource.getURI()));
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
                        qfItem.setThreshold(Float.parseFloat(qfItemResource.getProperty(ResourceFactory.createProperty(namespace + "QFItemThreshold")).getString()));
                        qfItem.setValue(Float.parseFloat(qfItemResource.getProperty(ResourceFactory.createProperty(namespace + "QFItemValue")).getString()));
                        qfItem.setCategory(qfItemResource.getPropertyResourceValue(ResourceFactory.createProperty(namespace + "QFItemCategory")).getURI());
                        qfItem.setWeight(Float.parseFloat(qfItemResource.getProperty(ResourceFactory.createProperty(namespace + "QFItemWeight")).getString()));
                        qfItem.setSourceQF(qfItemResource.getProperty(ResourceFactory.createProperty(namespace + "sourceQF")).getObject().asResource().getURI().substring(namespace.length()));
                        qfItem.setMetrics((ArrayList<String>) qfItemResource.listProperties(ResourceFactory.createProperty(namespace + "hasMetric"))
                                .mapWith(Statement::getObject).mapWith(RDFNode::asResource)
                                .mapWith(Resource::getLocalName).toList());
                        qfItem.setId(JenaUtils.parseId(qfItemResource.getURI()));
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

