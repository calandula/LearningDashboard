package com.example.learningdashboard.repository;

import com.example.learningdashboard.dtos.ProductDto;
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

import static com.example.learningdashboard.utils.JenaUtils.getPropertyList;
import static com.example.learningdashboard.utils.JenaUtils.getPropertyString;

@Repository
public class ProductRepository {

    @Autowired
    private String prefixes;

    @Autowired
    private Dataset dataset;

    @Autowired
    private String namespace;

    public ProductDto save(ProductDto product, String productId) {
        productId = productId == null ? UUID.randomUUID().toString() : productId;
        String productURI = namespace + productId;
        Resource productResource = ResourceFactory.createResource(productURI);
        Resource productClass = ResourceFactory.createResource(namespace + "Product");
        dataset.begin(ReadWrite.WRITE);
        try {
            List<Resource> projectResources = new ArrayList<>();
            if (product.getProjects() != null) {
                projectResources = product.getProjects().stream()
                        .map(projectId -> ResourceFactory.createResource(namespace + projectId))
                        .filter(projectResource -> dataset.getDefaultModel().containsResource(projectResource))
                        .toList();
                if (projectResources.size() != product.getProjects().size()) {
                    throw new IllegalArgumentException("One or more project IDs do not exist in the dataset.");
                }
            }

            dataset.getDefaultModel()
                    .add(productResource, RDF.type, productClass)
                    .add(productResource, ResourceFactory.createProperty(namespace + "productName"),
                            ResourceFactory.createPlainLiteral(product.getName()));

            if (product.getDescription() != null) {
                dataset.getDefaultModel()
                        .add(productResource, ResourceFactory.createProperty(namespace + "productDescription"),
                                ResourceFactory.createPlainLiteral(product.getDescription()));
            }

            if (product.getLogo() != null) {
                dataset.getDefaultModel()
                        .add(productResource, ResourceFactory.createProperty(namespace + "productLogo"),
                                ResourceFactory.createPlainLiteral(product.getLogo()));
            }

            projectResources.forEach(projectResource ->
                    dataset.getDefaultModel().add(productResource,
                            ResourceFactory.createProperty(namespace + "hasProject"),
                            projectResource));

            dataset.commit();
            product.setId(productId);
            return product;
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }

    public List<ProductDto> findAll() {
        List<ProductDto> products = new ArrayList<>();
        dataset.begin(ReadWrite.READ);
        try {
            Model model = dataset.getDefaultModel();
            String productNamePropertyURI = namespace + "productName";
            String productDescriptionPropertyURI = namespace + "productDescription";
            String productLogoPropertyURI = namespace + "productLogo";
            String hasProjectPropertyURI = namespace + "hasProject";

            ResIterator iterator = model.listResourcesWithProperty(RDF.type, model.createResource(namespace + "Product"));
            while (iterator.hasNext()) {
                Resource productResource = iterator.next();
                ProductDto product = new ProductDto();
                product.setName(getPropertyString(productResource, productNamePropertyURI));
                product.setDescription(getPropertyString(productResource, productDescriptionPropertyURI));
                product.setLogo(getPropertyString(productResource, productLogoPropertyURI));
                product.setProjects((ArrayList<String>) getPropertyList(productResource, hasProjectPropertyURI));
                product.setId(JenaUtils.parseId(productResource.getURI()));
                products.add(product);
            }

            dataset.commit();
            return products;
        } catch (Exception e) {
            dataset.abort();
            throw e;
        } finally {
            dataset.end();
        }
    }

    public ProductDto findById(String id) {
        String productURI = namespace + id;
        Resource productResource = ResourceFactory.createResource(productURI);
        dataset.begin(ReadWrite.READ);
        try {
            Model model = dataset.getDefaultModel();

            if (!model.containsResource(productResource)) {
                return null;
            }

            String productNamePropertyURI = namespace + "productName";
            String productDescriptionPropertyURI = namespace + "productDescription";
            String productLogoPropertyURI = namespace + "productLogo";
            String hasProjectPropertyURI = namespace + "hasProject";

            ProductDto product = new ProductDto();
            product.setName(getPropertyString(productResource, productNamePropertyURI));
            product.setDescription(getPropertyString(productResource, productDescriptionPropertyURI));
            product.setLogo(getPropertyString(productResource, productLogoPropertyURI));
            product.setProjects((ArrayList<String>) getPropertyList(productResource, hasProjectPropertyURI));
            product.setId(JenaUtils.parseId(productResource.getURI()));

            return product;
        } finally {
            dataset.end();
        }
    }

    public void deleteById(String productId, boolean update) {
        String productURI = namespace + productId;
        Resource productResource = ResourceFactory.createResource(productURI);
        dataset.begin(ReadWrite.WRITE);
        try {
            if (update) {
                StmtIterator it = dataset.getDefaultModel().listStatements(productResource, null, (RDFNode) null);
                while (it.hasNext()) {
                    Statement stmt = it.next();
                    dataset.getDefaultModel().remove(stmt);
                }
            } else {
                dataset.getDefaultModel().removeAll(productResource, null, (RDFNode) null);
            }
            dataset.commit();
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }

}
