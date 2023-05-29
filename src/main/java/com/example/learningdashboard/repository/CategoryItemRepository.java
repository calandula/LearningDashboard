package com.example.learningdashboard.repository;

import com.example.learningdashboard.dtos.CategoryItemDto;
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
public class CategoryItemRepository {

    @Autowired
    private Dataset dataset;

    @Autowired
    private String namespace;

    public CategoryItemDto save(CategoryItemDto categoryItem, String categoryItemId) {
        categoryItemId = categoryItemId == null ? UUID.randomUUID().toString() : categoryItemId;
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
            categoryItem.setId(categoryItemId);
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
                        categoryItem.setUpperThreshold(categoryItemResource.getProperty(ResourceFactory.createProperty(namespace + "CIUpperThreshold")).getFloat());
                        categoryItem.setId(JenaUtils.parseId(categoryItemResource.getURI()));
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
            categoryItem.setId(JenaUtils.parseId(categoryItemResource.getURI()));
            return categoryItem;
        } finally {
            dataset.end();
        }
    }

    public List<CategoryItemDto> findByCategoryId(String categoryId) {
        String categoryURI = namespace + categoryId;
        Resource categoryResource = ResourceFactory.createResource(categoryURI);
        List<CategoryItemDto> categoryItemDtos = new ArrayList<>();
        dataset.begin(ReadWrite.READ);
        try {
            Model model = dataset.getDefaultModel();

            if (!model.containsResource(categoryResource)) {
                throw new IllegalArgumentException("Category with ID " + categoryId + " does not exist in the dataset.");
            }

            StmtIterator it = model.listStatements(categoryResource, ResourceFactory.createProperty(namespace + "hasCategoryItem"), (RDFNode) null);
            while (it.hasNext()) {
                Statement stmt = it.next();
                RDFNode categoryItemNode = stmt.getObject();
                if (categoryItemNode.isResource()) {
                    Resource categoryItemResource = categoryItemNode.asResource();
                    CategoryItemDto categoryItem = new CategoryItemDto();
                    categoryItem.setColor(categoryItemResource.getProperty(ResourceFactory.createProperty(namespace + "CIType")).getString());
                    categoryItem.setType(categoryItemResource.getProperty(ResourceFactory.createProperty(namespace + "CIColor")).getString());
                    categoryItem.setUpperThreshold(Float.parseFloat(categoryItemResource.getProperty(ResourceFactory.createProperty(namespace + "CIUpperThreshold")).getString()));
                    categoryItem.setId(JenaUtils.parseId(categoryItemResource.getURI()));
                    categoryItemDtos.add(categoryItem);
                }
            }

            dataset.commit();
            return categoryItemDtos;
        } catch (Exception e) {
            dataset.abort();
            throw e;
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
            } else {
                dataset.getDefaultModel().removeAll(categoryItemResource, null, (RDFNode) null);
            }
            dataset.commit();
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }
}
