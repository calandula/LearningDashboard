package com.example.learningdashboard.repository;

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
public class SIItemRepository {

    @Autowired
    private String prefixes;

    @Autowired
    private Dataset dataset;

    @Autowired
    private String namespace;

    public SIItemDto save(SIItemDto siItem, String siItemId) {
        siItemId = siItemId == null ? UUID.randomUUID().toString() : siItemId;
        String siItemURI = namespace + siItemId;
        Resource siItemResource = ResourceFactory.createResource(siItemURI);
        Resource siItemClass = ResourceFactory.createResource(namespace + "SIItem");
        dataset.begin(ReadWrite.WRITE);
        try {

            //check existence of qfIds
            checkIds(siItem);

            checkWeight(siItem);

            createWeights(siItem, siItemResource);

            double weightedSum = calculateWeightedSumAndCheckWeight(siItem);

            dataset.getDefaultModel().add(siItemResource, ResourceFactory.createProperty(namespace + "SIItemValue"), ResourceFactory.createTypedLiteral(weightedSum));
            siItem.setValue((float) weightedSum);

            dataset.getDefaultModel()
                    .add(siItemResource, RDF.type, siItemClass)
                    .add(siItemResource, ResourceFactory.createProperty(namespace + "SIItemThreshold"),
                            ResourceFactory.createTypedLiteral(siItem.getThreshold()));

            String siClassURI = namespace + "SI";
            Resource siClass = ResourceFactory.createResource(siClassURI);
            Resource sourceSI = ResourceFactory.createResource(namespace + siItem.getSourceSI());
            if (dataset.getDefaultModel().contains(sourceSI, RDF.type, siClass)) {
                dataset.getDefaultModel().add(siItemResource,
                        ResourceFactory.createProperty(namespace + "sourceSI"),
                        sourceSI);
            } else {
                throw new IllegalArgumentException("The source SI does not exist in the dataset.");
            }

            String categoryClassURI = namespace + "Category";
            Resource categoryClass = ResourceFactory.createResource(categoryClassURI);
            Resource sourceCategory = ResourceFactory.createResource(namespace + siItem.getCategory());
            if (dataset.getDefaultModel().contains(sourceCategory, RDF.type, categoryClass)) {
                dataset.getDefaultModel().add(siItemResource,
                        ResourceFactory.createProperty(namespace + "SIItemCategory"),
                        sourceCategory);
            } else {
                throw new IllegalArgumentException("The sourceCategory does not exist in the dataset.");
            }



            dataset.commit();
            siItem.setId(siItemId);
            return siItem;
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }

    private void createWeights(SIItemDto siItem, Resource siItemResource) {
        for (SerializablePair<String, Float> qfItemPair : siItem.getQfItemWeights()) {
            String qfItemId = qfItemPair.getId();
            float weight = qfItemPair.getWeight();

            String weightId = UUID.randomUUID().toString();
            String weightURI = namespace + weightId;
            Resource weightResource = ResourceFactory.createResource(weightURI);
            Resource weightClass = ResourceFactory.createResource(namespace + "Weight");
            dataset.getDefaultModel().add(weightResource, RDF.type, weightClass);
            dataset.getDefaultModel().add(weightResource, ResourceFactory.createProperty(namespace + "weightValue"), ResourceFactory.createTypedLiteral(weight));
            dataset.getDefaultModel().add(siItemResource, ResourceFactory.createProperty(namespace + "hasWeightedQFItem"), weightResource);

            Resource qfItemResource = ResourceFactory.createResource(namespace + qfItemId);
            dataset.getDefaultModel().add(weightResource, ResourceFactory.createProperty(namespace + "hasQFItem"), qfItemResource);
        }
    }

    private double calculateWeightedSumAndCheckWeight(SIItemDto siItem) {
        double weightedSum = 0.0;
        double totalWeight = 0.0;

        for (SerializablePair<String, Float> qfItemTuple : siItem.getQfItemWeights()) {
            String qfItemId = qfItemTuple.getId();
            float qfItemWeight = qfItemTuple.getWeight();

            Statement valueStmt = dataset.getDefaultModel().getProperty(
                    ResourceFactory.createResource(namespace + qfItemId),
                    ResourceFactory.createProperty(namespace + "QFItemValue")
            );
            if (valueStmt == null) {
                throw new IllegalArgumentException("The value of the quality factor item is missing.");
            }
            RDFNode valueNode = valueStmt.getObject();
            if (!valueNode.isLiteral()) {
                throw new IllegalArgumentException("The value of the quality factor item must be a literal.");
            }
            double qfItemValue = valueNode.asLiteral().getDouble();

            weightedSum += qfItemValue * qfItemWeight;
            totalWeight += qfItemWeight;
        }

        if (Math.abs(totalWeight - 1.0) > 0.0001) {
            throw new IllegalArgumentException("The sum of the quality factors items' weights must be 1.0.");
        }

        return weightedSum;
    }

    private void checkWeight(SIItemDto siItem) {
        double totalWeight;
        totalWeight = 0.0f;
        for (SerializablePair<String, Float> qfItemPair : siItem.getQfItemWeights()) {
            totalWeight += qfItemPair.getWeight();
        }
        if (Math.abs(totalWeight - 1.0) > 0.0001) {
            throw new IllegalArgumentException("The sum of the quality factors items' weights must be 1.0.");
        }
    }

    private void checkIds(SIItemDto siItem) {
        List<Resource> qfItemWeightResources = siItem.getQfItemWeights().stream()
                .map(qfItemPair -> ResourceFactory.createResource(namespace + qfItemPair.getId()))
                .filter(qfItemResource -> dataset.getDefaultModel().containsResource(qfItemResource))
                .toList();
        if (qfItemWeightResources.size() != siItem.getQfItemWeights().size()) {
            throw new IllegalArgumentException("One or more quality factor item IDs do not exist in the dataset.");
        }
    }

    public List<SIItemDto> findAll() {
        List<SIItemDto> siItems = new ArrayList<>();
        dataset.begin(ReadWrite.READ);
        try {
            Model model = dataset.getDefaultModel();

            ResIterator siItemIter = model.listResourcesWithProperty(RDF.type, ResourceFactory.createResource(namespace + "SIItem"));
            while (siItemIter.hasNext()) {
                Resource siItemResource = siItemIter.next();

                SIItemDto siItem = new SIItemDto();
                siItem.setThreshold(Float.parseFloat(siItemResource.getProperty(ResourceFactory.createProperty(namespace + "SIItemThreshold")).getString()));
                siItem.setValue(Float.parseFloat(siItemResource.getProperty(ResourceFactory.createProperty(namespace + "SIItemValue")).getString()));
                siItem.setCategory(siItemResource.getPropertyResourceValue(ResourceFactory.createProperty(namespace + "SIItemCategory")).getURI());
                siItem.setSourceSI(siItemResource.getPropertyResourceValue(ResourceFactory.createProperty(namespace + "sourceSI")).getURI());
                siItem.setId(JenaUtils.parseId(siItemResource.getURI()));

                List<SerializablePair<String, Float>> qfItemWeights = new ArrayList<>();
                StmtIterator qfItemIter = siItemResource.listProperties(ResourceFactory.createProperty(namespace + "hasWeightedQFItem"));
                while (qfItemIter.hasNext()) {
                    Statement stmt = qfItemIter.next();
                    Resource weightedQFItemResource = stmt.getObject().asResource();
                    Resource qfItemResource = weightedQFItemResource.getProperty(ResourceFactory.createProperty(namespace + "hasQFItem")).getObject().asResource();
                    float qfItemWeight = weightedQFItemResource.getProperty(ResourceFactory.createProperty(namespace + "weightValue")).getFloat();
                    String qfItemId = JenaUtils.parseId(qfItemResource.getURI());
                    qfItemWeights.add(new SerializablePair<>(qfItemId, qfItemWeight));
                }
                siItem.setQfItemWeights((ArrayList<SerializablePair<String, Float>>) qfItemWeights);

                siItems.add(siItem);
            }

            dataset.commit();
            return siItems;
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }

    public SIItemDto findById(String siItemId) {
        String siItemURI = namespace + siItemId;
        Resource siItemResource = ResourceFactory.createResource(siItemURI);
        dataset.begin(ReadWrite.READ);
        try {
            Model model = dataset.getDefaultModel();

            if (!model.containsResource(siItemResource)) {
                return null;
            }

            String SIItemThreshold = model.getProperty(siItemResource, model.createProperty(namespace + "SIItemThreshold"))
                    .getString();
            String SIItemValue = model.getProperty(siItemResource, model.createProperty(namespace + "SIItemValue"))
                    .getString();
            String SIItemCategory = model.getProperty(siItemResource, model.createProperty(namespace + "SIItemCategory"))
                    .getString();
            String sourceSI = model.getProperty(siItemResource, model.createProperty(namespace + "sourceSI"))
                    .getString();
            ArrayList<SerializablePair<String, Float>> qfItemWeights = new ArrayList<>();
            StmtIterator qfItemIter = siItemResource.listProperties(ResourceFactory.createProperty(namespace + "hasWeightedQFItem"));
            while (qfItemIter.hasNext()) {
                Statement stmt = qfItemIter.next();
                Resource weightedQFItemResource = stmt.getObject().asResource();
                Resource qfItemResource = weightedQFItemResource.getProperty(ResourceFactory.createProperty(namespace + "hasQFItem")).getObject().asResource();
                float qfItemWeight = weightedQFItemResource.getProperty(ResourceFactory.createProperty(namespace + "weightValue")).getFloat();
                String qfItemId = JenaUtils.parseId(qfItemResource.getURI());
                qfItemWeights.add(new SerializablePair<>(qfItemId, qfItemWeight));
            }

            SIItemDto siItem = new SIItemDto();
            siItem.setThreshold(Float.parseFloat(SIItemThreshold));
            siItem.setQfItemWeights((ArrayList<SerializablePair<String, Float>>) qfItemWeights);
            siItem.setValue(Float.parseFloat(SIItemValue));
            siItem.setCategory(SIItemCategory);
            siItem.setSourceSI(sourceSI);
            siItem.setId(JenaUtils.parseId(siItemResource.getURI()));
            return siItem;
        } finally {
            dataset.end();
        }
    }

    public void deleteById(String siItemId, boolean update) {
        String siItemURI = namespace + siItemId;
        Resource siItemResource = ResourceFactory.createResource(siItemURI);
        dataset.begin(ReadWrite.WRITE);
        try {
            if (update) {
                StmtIterator it = dataset.getDefaultModel().listStatements(siItemResource, null, (RDFNode) null);
                while (it.hasNext()) {
                    Statement stmt = it.next();
                    dataset.getDefaultModel().remove(stmt);
                }
            }
            else {
                dataset.getDefaultModel().removeAll(siItemResource, null, (RDFNode) null);
            }
            dataset.commit();
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }

    public List<SIItemDto> findByProject(String projectId) {
        String projectURI = namespace + projectId;
        Resource projectResource = ResourceFactory.createResource(projectURI);
        List<SIItemDto> siItems = new ArrayList<>();
        dataset.begin(ReadWrite.READ);
        try {
            Model model = dataset.getDefaultModel();

            if (!model.containsResource(projectResource)) {
                throw new IllegalArgumentException("Project with ID " + projectId + " does not exist in the dataset.");
            }

            StmtIterator it = model.listStatements(projectResource, ResourceFactory.createProperty(namespace + "hasHI"), (RDFNode) null);
            while (it.hasNext()) {
                Statement stmt = it.next();
                RDFNode siItemNode = stmt.getObject();
                if (siItemNode.isResource()) {
                    Resource siItemResource = siItemNode.asResource();

                    SIItemDto siItem = new SIItemDto();
                    List<SerializablePair<String, Float>> qfItemWeights = new ArrayList<>();
                    StmtIterator qfItemIter = siItemResource.listProperties(ResourceFactory.createProperty(namespace + "hasWeightedQFItem"));
                    while (qfItemIter.hasNext()) {
                        Statement stmt2 = qfItemIter.next();
                        Resource weightedQFItemResource = stmt2.getObject().asResource();
                        Resource qfItemResource = weightedQFItemResource.getProperty(ResourceFactory.createProperty(namespace + "hasQFItem")).getObject().asResource();
                        float qfItemWeight = weightedQFItemResource.getProperty(ResourceFactory.createProperty(namespace + "weightValue")).getFloat();
                        String qfItemId = JenaUtils.parseId(qfItemResource.getURI());
                        qfItemWeights.add(new SerializablePair<>(qfItemId, qfItemWeight));
                    }
                    siItem.setThreshold(Float.parseFloat(siItemResource.getProperty(ResourceFactory.createProperty(namespace + "SIItemThreshold")).getString()));
                    siItem.setValue(Float.parseFloat(siItemResource.getProperty(ResourceFactory.createProperty(namespace + "SIItemValue")).getString()));
                    siItem.setCategory(siItemResource.getProperty(ResourceFactory.createProperty(namespace + "SIItemCategory")).getObject().asResource().getURI().substring(namespace.length()));
                    siItem.setSourceSI(siItemResource.getProperty(ResourceFactory.createProperty(namespace + "sourceSI")).getObject().asResource().getURI().substring(namespace.length()));
                    siItem.setId(JenaUtils.parseId(siItemResource.getURI()));
                    siItem.setQfItemWeights((ArrayList<SerializablePair<String, Float>>) qfItemWeights);
                    siItems.add(siItem);
                }
            }

            dataset.commit();
            return siItems;
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }
}
