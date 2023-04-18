package com.example.learningdashboard.service;

import com.example.learningdashboard.dtos.CategoryDto;
import com.example.learningdashboard.dtos.CategoryItemDto;
import com.example.learningdashboard.dtos.ProductDto;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CategoryItemService {

    @Autowired
    private String prefixes;

    @Autowired
    private Dataset dataset;

    @Autowired
    private String namespace;

    public CategoryItemDto createCategoryItem(CategoryItemDto categoryItem) {
        String categoryItemId = UUID.randomUUID().toString();
        String categoryItemURI = namespace + categoryItemId;
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
            return null;
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }

    public List<CategoryItemDto> getAllCategoryItems() {
        List<CategoryItemDto> categoryItems = new ArrayList<>();
        dataset.begin(ReadWrite.READ);
        try {
            dataset.getDefaultModel().listResourcesWithProperty(RDF.type, ResourceFactory.createResource(namespace + "CategoryItem"))
                    .forEachRemaining(productResource -> {
                        CategoryItemDto categoryItem = new CategoryItemDto();
                        categoryItem.setType(productResource.getProperty(ResourceFactory.createProperty(namespace + "CIType")).getString());
                        categoryItem.setColor(productResource.getProperty(ResourceFactory.createProperty(namespace + "CIColor")).getString());
                        categoryItem.setUpperThreshold(productResource.getProperty(ResourceFactory.createProperty(namespace + "CIUpperThreshold")).getInt());
                        categoryItems.add(categoryItem);
                    });

            dataset.commit();
            return categoryItems;
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }

    public CategoryItemDto getCategoryItemById(String categoryItemId) {
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

    public CategoryItemDto getCategoryItemByCategoryId(String categoryId) {
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
}
