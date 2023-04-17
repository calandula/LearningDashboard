package com.example.learningdashboard.service;

import com.example.learningdashboard.dtos.ProductDto;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private String prefixes;

    @Autowired
    private Dataset dataset;

    @Autowired
    private String namespace;

    public ProductDto createProduct(ProductDto product) {
        String productURI = namespace + product.getName().toLowerCase();
        Resource productResource = ResourceFactory.createResource(productURI);
        Resource productClass = ResourceFactory.createResource(namespace + "Product");
        dataset.begin(ReadWrite.WRITE);
        try {
            List<Resource> projectResources = product.getProjects().stream()
                    .map(projectId -> ResourceFactory.createResource(namespace + projectId))
                    .filter(projectResource -> dataset.getDefaultModel().containsResource(projectResource))
                    .collect(Collectors.toList());
            if (projectResources.size() != product.getProjects().size()) {
                throw new IllegalArgumentException("One or more project IDs do not exist in the dataset.");
            }
            dataset.getDefaultModel()
                    .add(productResource, RDF.type, productClass)
                    .add(productResource, ResourceFactory.createProperty(namespace + "productName"),
                            ResourceFactory.createPlainLiteral(product.getName()))
                    .add(productResource, ResourceFactory.createProperty(namespace + "productDescription"),
                            ResourceFactory.createPlainLiteral(product.getDescription()))
                    .add(productResource, ResourceFactory.createProperty(namespace + "productLogo"),
                            ResourceFactory.createPlainLiteral(product.getLogo()));

            projectResources.forEach(projectResource ->
                    dataset.getDefaultModel().add(productResource,
                            ResourceFactory.createProperty(namespace + "hasProject"),
                            projectResource));

            dataset.commit();
            return null;
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }

    public List<ProductDto> getAllProducts() {
        List<ProductDto> products = new ArrayList<>();
        dataset.begin(ReadWrite.READ);
        try {
            dataset.getDefaultModel().listResourcesWithProperty(RDF.type, ResourceFactory.createResource(namespace + "Product"))
                    .forEachRemaining(productResource -> {
                        ProductDto product = new ProductDto();
                        product.setName(productResource.getProperty(ResourceFactory.createProperty(namespace + "productName")).getString());
                        product.setDescription(productResource.getProperty(ResourceFactory.createProperty(namespace + "productDescription")).getString());
                        product.setLogo(productResource.getProperty(ResourceFactory.createProperty(namespace + "productLogo")).getString());
                        product.setProjects((ArrayList<String>) productResource.listProperties(ResourceFactory.createProperty(namespace + "hasProject"))
                                .mapWith(Statement::getObject).mapWith(RDFNode::asResource)
                                .mapWith(Resource::getLocalName).toList());
                        products.add(product);
                    });

            dataset.commit();
            return products;
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }

    public ProductDto getProductById(String id) {
        String productURI = namespace + id.toLowerCase();
        Resource productResource = ResourceFactory.createResource(productURI);
        dataset.begin(ReadWrite.READ);
        try {
            Model model = dataset.getDefaultModel();

            if (!model.containsResource(productResource)) {
                return null;
            }

            String productName = model.getProperty(productResource, model.createProperty(namespace + "productName"))
                    .getString();
            String productDescription = model
                    .getProperty(productResource, model.createProperty(namespace + "productDescription")).getString();
            String productLogo = model.getProperty(productResource, model.createProperty(namespace + "productLogo"))
                    .getString();
            List<String> projectIds = model.listObjectsOfProperty(productResource, model.createProperty(namespace + "isProjectOf"))
                    .mapWith(resource -> resource.asResource().getURI().substring(namespace.length()))
                    .toList();

            ProductDto product = new ProductDto();
            product.setName(productName);
            product.setDescription(productDescription);
            product.setLogo(productLogo);
            product.setProjects((ArrayList<String>) projectIds);
            return product;
        } finally {
            dataset.end();
        }
    }

    public ProductDto updateProduct(String productId, ProductDto productDto) {
        return productDto;
    }

    public void deleteProduct(String productId) {
    }
}
