package com.example.learningdashboard.service;

import com.example.learningdashboard.dtos.UserDto;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private String prefixes;

    @Autowired
    private Dataset dataset;

    @Autowired
    private String namespace;

    public UserDto createUser(UserDto user) {
        String userId = UUID.randomUUID().toString();
        String userURI = namespace + userId;
        Resource userResource = ResourceFactory.createResource(userURI);
        Resource userClass = ResourceFactory.createResource(namespace + "User");
        dataset.begin(ReadWrite.WRITE);
        try {
            dataset.getDefaultModel()
                    .add(userResource, RDF.type, userClass)
                    .add(userResource, ResourceFactory.createProperty(namespace + "userUsername"),
                            ResourceFactory.createPlainLiteral(user.getUsername()))
                    .add(userResource, ResourceFactory.createProperty(namespace + "userEmail"),
                            ResourceFactory.createPlainLiteral(user.getEmail()))
                    .add(userResource, ResourceFactory.createProperty(namespace + "userAdmin"),
                            ResourceFactory.createTypedLiteral(user.isAdmin()))
                    .add(userResource, ResourceFactory.createProperty(namespace + "userSecurityQuestion"),
                            ResourceFactory.createPlainLiteral(user.getSecurityQuestion()))
                    .add(userResource, ResourceFactory.createProperty(namespace + "userAnswer"),
                            ResourceFactory.createPlainLiteral(user.getAnswer()))
                    .add(userResource, ResourceFactory.createProperty(namespace + "userPassword"),
                            ResourceFactory.createPlainLiteral(user.getPassword()));

            dataset.commit();
            return null;
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }

    public List<UserDto> getAllUsers() {
        return null;
    }

    public UserDto getUserById(String userId) {
        return null;
    }
}
