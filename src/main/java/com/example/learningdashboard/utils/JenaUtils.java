package com.example.learningdashboard.utils;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JenaUtils {
    public static String parseId(String id) {
        String uuidString = id.substring(id.lastIndexOf("#") + 1);
        try {
            UUID uuid = UUID.fromString(uuidString);
            return uuid.toString();
        } catch (IllegalArgumentException e) {
            return uuidString;
        }
    }

    public static String getPropertyString(Resource resource, String propertyName) {
        Statement statement = resource.getProperty(ResourceFactory.createProperty(propertyName));
        if (statement != null) {
            return statement.getString();
        }
        return null;
    }

    public static List<String> getPropertyList(Resource resource, String propertyName) {
        List<String> values = new ArrayList<>();
        StmtIterator iterator = resource.listProperties(ResourceFactory.createProperty(propertyName));
        while (iterator.hasNext()) {
            Statement statement = iterator.next();
            values.add(statement.getObject().asResource().getLocalName());
        }
        return values;
    }

    public static boolean getPropertyBoolean(Resource resource, String propertyURI) {
        Statement statement = resource.getProperty(ResourceFactory.createProperty(propertyURI));
        if (statement != null) {
            return statement.getBoolean();
        }
        return false;
    }
}
