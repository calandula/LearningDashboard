package com.example.learningdashboard.repository;

import com.example.learningdashboard.dtos.CategoryItemDto;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CategoryItemRepository {

    @Autowired
    private Dataset dataset;

    @Autowired
    private String namespace;

    public CategoryItemDto save(CategoryItemDto categoryItem, String categoryItemId) {
        String categoryItemURI = categoryItemId == null ? namespace + UUID.randomUUID().toString() : categoryItemId;
        Resource categoryItemResource = ResourceFactory.createResource(categoryItemURI);
        Resource categoryItemClass = ResourceFactory.createResource(namespace + "CategoryItem");
        dataset.begin(ReadWrite.WRITE);
        try {
            dataset.getDefaultModel()
                    .add(categoryItemResource, RDF.type, categoryItemClass)
                    .add(categoryItemResource, ResourceFactory.createProperty(namespace + "CIType"),
                            ResourceFactory.createPlainLiteral(categoryItem.getType()))
                    .add(categoryItemResource, ResourceFactory.createProperty(namespace + "CIColor"),
                            ResourceFactory.createPlainLiteral(categoryItem.getColor()))
                    .add(categoryItemResource, ResourceFactory.createProperty(namespace + "CIUpperThreshold"),
                            ResourceFactory.createTypedLiteral(categoryItem.getUpperThreshold()));

            dataset.commit();
            return categoryItem;
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }

    public List<CategoryItemDto> findAll() {
        List<CategoryItemDto> categoryItems = new ArrayList<>();
        dataset.begin(ReadWrite.READ);
        try {
            dataset.getDefaultModel().listResourcesWithProperty(RDF.type, ResourceFactory.createResource(namespace + "CategoryItem"))
                    .forEachRemaining(categoryItemResource -> {
                        CategoryItemDto categoryItem = new CategoryItemDto();
                        categoryItem.setType(categoryItemResource.getProperty(ResourceFactory.createProperty(namespace + "CIType")).getString());
                        categoryItem.setColor(categoryItemResource.getProperty(ResourceFactory.createProperty(namespace + "CIColor")).getString());
                        categoryItem.setUpperThreshold(categoryItemResource.getProperty(ResourceFactory.createProperty(namespace + "CIUpperThreshold")).getInt());
                        categoryItems.add(categoryItem);
                    });

            dataset.commit();
            return categoryItems;
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }

    public CategoryItemDto findById(String categoryItemId) {
        String categoryItemURI = namespace + categoryItemId;
        Resource categoryItemResource = ResourceFactory.createResource(categoryItemURI);
        dataset.begin(ReadWrite.READ);
        try {
            Model model = dataset.getDefaultModel();

            if (!model.containsResource(categoryItemResource)) {
                return null;
            }

            String categoryItemType = model.getProperty(categoryItemResource, model.createProperty(namespace + "CIType"))
                    .getString();
            String categoryItemColor = model
                    .getProperty(categoryItemResource, model.createProperty(namespace + "CIColor")).getString();
            int categoryItemUpperThreshold = model.getProperty(categoryItemResource, model.createProperty(namespace + "CIUpperThreshold"))
                    .getInt();

            CategoryItemDto categoryItem = new CategoryItemDto();
            categoryItem.setType(categoryItemType);
            categoryItem.setColor(categoryItemColor);
            categoryItem.setUpperThreshold(categoryItemUpperThreshold);
            return categoryItem;
        } finally {
            dataset.end();
        }
    }

    public CategoryItemDto findByCategoryId(String categoryId) {
        String categoryURI = namespace + categoryId;
        Resource categoryResource = ResourceFactory.createResource(categoryURI);
        dataset.begin(ReadWrite.READ);
        try {
            Model model = dataset.getDefaultModel();

            if (!model.containsResource(categoryResource)) {
                return null;
            }

            StmtIterator stmtIterator = model.listStatements(categoryResource, model.createProperty(namespace + "hasCategoryItem"), (RDFNode)null);
            if(!stmtIterator.hasNext()){
                return null;
            }
            Resource categoryItemResource = stmtIterator.next().getObject().asResource();

            String categoryItemType = model.getProperty(categoryItemResource, model.createProperty(namespace + "CIType"))
                    .getString();
            String categoryItemColor = model
                    .getProperty(categoryItemResource, model.createProperty(namespace + "CIColor")).getString();
            int categoryItemUpperThreshold = model.getProperty(categoryItemResource, model.createProperty(namespace + "CIUpperThreshold"))
                    .getInt();

            CategoryItemDto categoryItem = new CategoryItemDto();
            categoryItem.setType(categoryItemType);
            categoryItem.setColor(categoryItemColor);
            categoryItem.setUpperThreshold(categoryItemUpperThreshold);
            return categoryItem;
        } finally {
            dataset.end();
        }
    }

    public void deleteById(String categoryItemId, boolean update) {
        String categoryItemURI = namespace + categoryItemId;
        Resource categoryItemResource = ResourceFactory.createResource(categoryItemURI);
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
}
