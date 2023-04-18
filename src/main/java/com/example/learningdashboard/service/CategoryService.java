package com.example.learningdashboard.service;

import com.example.learningdashboard.dtos.CategoryDto;
import com.example.learningdashboard.dtos.ProductDto;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CategoryService {

    @Autowired
    private String prefixes;

    @Autowired
    private Dataset dataset;

    @Autowired
    private String namespace;

    public CategoryDto createCategory(CategoryDto category) {
        String categoryId = UUID.randomUUID().toString();
        String categoryURI = namespace + categoryId;
        Resource categoryResource = ResourceFactory.createResource(categoryURI);
        Resource categoryClass = ResourceFactory.createResource(namespace + "Category");
        dataset.begin(ReadWrite.WRITE);
        try {
            List<Resource> categoryItemResources = category.getCategoryItems().stream()
                    .map(categoryItemId -> ResourceFactory.createResource(namespace + categoryItemId))
                    .filter(categoryItemResource -> dataset.getDefaultModel().containsResource(categoryItemResource))
                    .toList();
            if (categoryItemResources.size() != category.getCategoryItems().size()) {
                throw new IllegalArgumentException("One or more category item IDs do not exist in the dataset.");
            }
            dataset.getDefaultModel()
                    .add(categoryResource, RDF.type, categoryClass)
                    .add(categoryResource, ResourceFactory.createProperty(namespace + "categoryName"),
                            ResourceFactory.createPlainLiteral(category.getName()));

            categoryItemResources.forEach(projectResource ->
                    dataset.getDefaultModel().add(categoryResource,
                            ResourceFactory.createProperty(namespace + "hasCategoryItem"),
                            projectResource));

            dataset.commit();
            return null;
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }

    public List<CategoryDto> getAllCategories() {
        List<CategoryDto> categories = new ArrayList<>();
        dataset.begin(ReadWrite.READ);
        try {
            dataset.getDefaultModel().listResourcesWithProperty(RDF.type, ResourceFactory.createResource(namespace + "Category"))
                    .forEachRemaining(productResource -> {
                        CategoryDto category = new CategoryDto();
                        category.setName(productResource.getProperty(ResourceFactory.createProperty(namespace + "categoryName")).getString());
                        category.setCategoryItems((ArrayList<String>) productResource.listProperties(ResourceFactory.createProperty(namespace + "hasCategoryItem"))
                                .mapWith(Statement::getObject).mapWith(RDFNode::asResource)
                                .mapWith(Resource::getLocalName).toList());
                        categories.add(category);
                    });

            dataset.commit();
            return categories;
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }

    public CategoryDto getCategoryById(String categoryId) {
        String categoryURI = namespace + categoryId;
        Resource categoryResource = ResourceFactory.createResource(categoryURI);
        dataset.begin(ReadWrite.READ);
        try {
            Model model = dataset.getDefaultModel();

            if (!model.containsResource(categoryResource)) {
                return null;
            }

            String categoryName = model.getProperty(categoryResource, model.createProperty(namespace + "categoryName"))
                    .getString();
            List<String> categoryItems = model.listObjectsOfProperty(categoryResource, model.createProperty(namespace + "hasCategoryItem"))
                    .mapWith(resource -> resource.asResource().getURI().substring(namespace.length()))
                    .toList();

            CategoryDto category = new CategoryDto();
            category.setName(categoryName);
            category.setCategoryItems((ArrayList<String>) categoryItems);
            return category;
        } finally {
            dataset.end();
        }
    }

    public CategoryDto getCategoryByItem(String itemId) {
        String itemURI = namespace + itemId;
        Resource itemResource = ResourceFactory.createResource(itemURI);
        dataset.begin(ReadWrite.READ);
        try {
            Model model = dataset.getDefaultModel();

            if (!model.containsResource(itemResource)) {
                return null;
            }

            StmtIterator stmtIterator = model.listStatements(itemResource, model.createProperty(namespace + "hasCategory"), (RDFNode)null);
            if(!stmtIterator.hasNext()){
                return null;
            }
            Resource categoryResource = stmtIterator.next().getObject().asResource();

            String categoryName = model.getProperty(categoryResource, model.createProperty(namespace + "categoryName"))
                    .getString();
            List<String> categoryItems = model.listObjectsOfProperty(categoryResource, model.createProperty(namespace + "hasCategoryItem"))
                    .mapWith(resource -> resource.asResource().getURI().substring(namespace.length()))
                    .toList();

            CategoryDto category = new CategoryDto();
            category.setName(categoryName);
            category.setCategoryItems((ArrayList<String>) categoryItems);
            return category;
        } finally {
            dataset.end();
        }
    }
}
