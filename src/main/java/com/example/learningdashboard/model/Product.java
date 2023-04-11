package com.example.learningdashboard.model;

import org.apache.jena.rdf.model.Resource;

public class Product {
    private String productId;
    private String name;
    private String description;

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    private String logo;

    public Product() {

    }
    public Product(String productId, String name, String description, String logo) {
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.logo = logo;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
