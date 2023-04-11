package com.example.learningdashboard.service;

import com.example.learningdashboard.model.Product;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private Model ontModel;

    @Autowired
    private String prefixes;

    @Autowired
    private Dataset dataset;

    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String queryStr = prefixes + "SELECT ?product " +
                "WHERE { ?product rdf:type qrapids-ontology-2:Product }";
        Query query = QueryFactory.create(queryStr);
        dataset.begin(ReadWrite.READ);
        try (QueryExecution qe = QueryExecutionFactory.create(query, dataset)) {
            ResultSet results = qe.execSelect();
            while (results.hasNext()) {
                QuerySolution solution = results.next();
                String productId = solution.get("product").asResource().getLocalName();
                /*String name = solution.get("name").asLiteral().getString();
                String description = solution.get("description").asLiteral().getString();
                String projectId = solution.get("project").asResource().getLocalName();*/
                Product product = new Product(productId, "test", "testing", "testProject");
                products.add(product);
            }
        } finally {
            dataset.end();
        }
        return products;
    }

    public Product getProductById(String productId) {
        String queryStr = prefixes + "SELECT ?name ?description ?project " +
                "WHERE { myproject:" + productId + " rdf:type myproject:Product . " +
                "myproject:" + productId + " myproject:name ?name . " +
                "myproject:" + productId + " myproject:description ?description . " +
                "myproject:" + productId + " myproject:partOfProject ?project . }";
        Query query = QueryFactory.create(queryStr);
        try (QueryExecution qe = QueryExecutionFactory.create(query, ontModel)) {
            ResultSet results = qe.execSelect();
            if (results.hasNext()) {
                QuerySolution solution = results.next();
                String name = solution.get("name").asLiteral().getString();
                String description = solution.get("description").asLiteral().getString();
                String projectId = solution.get("project").asResource().getLocalName();
                return new Product(productId, name, description, projectId);
            } else {
                return null;
            }
        }
    }

    public Product createProduct(Product product) {
        String productURI = "http://www.semanticweb.org/adria/ontologies/2023/3/untitled-ontology-25#Product_" + product.getProductId();
        Resource productResource = ResourceFactory.createResource(productURI);
        Resource productClass = ResourceFactory.createResource("http://www.semanticweb.org/adria/ontologies/2023/1/qrapids-ontology-2#Product");
        Model model = dataset.getDefaultModel();
        dataset.begin(ReadWrite.WRITE); // start a transaction
        try {
            model.add(productResource, RDF.type, productClass);
            model.add(productResource, model.createProperty("http://www.semanticweb.org/adria/ontologies/2023/1/qrapids-ontology-2#productName"), ontModel.createTypedLiteral(product.getName()));
            model.add(productResource, model.createProperty("http://www.semanticweb.org/adria/ontologies/2023/1/qrapids-ontology-2#productDescription"), ontModel.createTypedLiteral(product.getDescription()));
            // Add any other properties that you need to add here
            dataset.commit(); // commit the transaction
        } catch (Exception e) {
            dataset.abort(); // rollback the transaction
            throw e;
        }

        return product;
    }
}
