package com.example.learningdashboard.repository;

import com.example.learningdashboard.dtos.IterationDto;
import com.example.learningdashboard.dtos.SIItemDto;
import com.example.learningdashboard.utils.JenaUtils;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
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
        String siItemURI = siItemId == null ? namespace + UUID.randomUUID().toString() : siItemId;
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

            //check weight

            double totalWeight = 0.0;
            for (String qfItemId : siItem.getQfItems()) {
                Statement weightStmt = dataset.getDefaultModel().getProperty(
                        ResourceFactory.createResource(namespace + qfItemId),
                        ResourceFactory.createProperty(namespace + "QFItemWeight")
                );
                if (weightStmt == null) {
                    throw new IllegalArgumentException("The weight of the quality factor item is missing.");
                }
                RDFNode weightNode = weightStmt.getObject();
                if (!weightNode.isLiteral()) {
                    throw new IllegalArgumentException("The weight of the quality factor item must be a literal.");
                }
                totalWeight += weightNode.asLiteral().getDouble();
            }
            if (Math.abs(totalWeight - 1.0) > 0.0001) {
                throw new IllegalArgumentException("The sum of the quality factors items' weights must be 1.0.");
            }

            //insert

            dataset.getDefaultModel()
                    .add(siItemResource, RDF.type, siItemClass)
                    .add(siItemResource, ResourceFactory.createProperty(namespace + "SIItemThreshold"),
                            ResourceFactory.createTypedLiteral(siItem.getThreshold()));

            //compute Value
            double weightedSum = 0.0;
            for (String qfItemId : siItem.getQfItems()) {
                Statement weightStmt = dataset.getDefaultModel().getProperty(
                        ResourceFactory.createResource(namespace + qfItemId),
                        ResourceFactory.createProperty(namespace + "QFItemWeight")
                );
                Statement valueStmt = dataset.getDefaultModel().getProperty(
                        ResourceFactory.createResource(namespace + qfItemId),
                        ResourceFactory.createProperty(namespace + "QFItemValue")
                );
                if (weightStmt == null || valueStmt == null) {
                    throw new IllegalArgumentException("The weight or value of the quality factor item is missing.");
                }
                RDFNode weightNode = weightStmt.getObject();
                RDFNode valueNode = valueStmt.getObject();
                if (!weightNode.isLiteral() || !valueNode.isLiteral()) {
                    throw new IllegalArgumentException("The weight and value of the quality factor item must be literals.");
                }
                double weight = weightNode.asLiteral().getDouble();
                double value = valueNode.asLiteral().getDouble();
                weightedSum += weight * value;
            }
            dataset.getDefaultModel().add(siItemResource,
                    ResourceFactory.createProperty(namespace + "SIItemValue"),
                    ResourceFactory.createTypedLiteral(weightedSum));

            //--------


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

    public List<SIItemDto> findAll() {
        List<SIItemDto> siItems = new ArrayList<>();
        dataset.begin(ReadWrite.READ);
        try {
            dataset.getDefaultModel().listResourcesWithProperty(RDF.type, ResourceFactory.createResource(namespace + "SIItem"))
                    .forEachRemaining(siItemResource -> {
                        SIItemDto siItem = new SIItemDto();
                        siItem.setThreshold(Float.parseFloat(siItemResource.getProperty(ResourceFactory.createProperty(namespace + "SIItemThreshold")).getString()));
                        siItem.setValue(Float.parseFloat(siItemResource.getProperty(ResourceFactory.createProperty(namespace + "SIItemValue")).getString()));
                        siItem.setCategory(siItemResource.getPropertyResourceValue(ResourceFactory.createProperty(namespace + "SIItemCategory")).getURI());
                        siItem.setSourceSI(siItemResource.getPropertyResourceValue(ResourceFactory.createProperty(namespace + "sourceSI")).getURI());
                        siItem.setQfItems((ArrayList<String>) siItemResource.listProperties(ResourceFactory.createProperty(namespace + "hasQFI"))
                                .mapWith(Statement::getObject).mapWith(RDFNode::asResource)
                                .mapWith(Resource::getLocalName).toList());
                        siItem.setId(JenaUtils.parseId(siItemResource.getURI()));

                        siItems.add(siItem);
                    });

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
            List<String> QFIs = model.listObjectsOfProperty(siItemResource, model.createProperty(namespace + "hasQFI"))
                    .mapWith(resource -> resource.asResource().getURI().substring(namespace.length()))
                    .toList();

            SIItemDto siItem = new SIItemDto();
            siItem.setThreshold(Float.parseFloat(SIItemThreshold));
            siItem.setValue(Float.parseFloat(SIItemValue));
            siItem.setQfItems((ArrayList<String>) QFIs);
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
                    siItem.setThreshold(Float.parseFloat(siItemResource.getProperty(ResourceFactory.createProperty(namespace + "SIItemThreshold")).getString()));
                    siItem.setValue(Float.parseFloat(siItemResource.getProperty(ResourceFactory.createProperty(namespace + "SIItemValue")).getString()));
                    siItem.setCategory(siItemResource.getProperty(ResourceFactory.createProperty(namespace + "SIItemCategory")).getObject().asResource().getURI().substring(namespace.length()));
                    siItem.setSourceSI(siItemResource.getProperty(ResourceFactory.createProperty(namespace + "sourceSI")).getObject().asResource().getURI().substring(namespace.length()));
                    siItem.setQfItems((ArrayList<String>) siItemResource.listProperties(ResourceFactory.createProperty(namespace + "hasQFI"))
                            .mapWith(Statement::getObject).mapWith(RDFNode::asResource)
                            .mapWith(Resource::getLocalName).toList());
                    siItem.setId(JenaUtils.parseId(siItemResource.getURI()));
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
