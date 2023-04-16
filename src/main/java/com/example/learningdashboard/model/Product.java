package com.example.learningdashboard.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.jena.rdf.model.Resource;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    private String name;

    private String description;

    private String logo;

}
