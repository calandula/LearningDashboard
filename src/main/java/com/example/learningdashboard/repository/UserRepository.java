package com.example.learningdashboard.repository;

import com.example.learningdashboard.dtos.UserDto;
import com.example.learningdashboard.utils.JenaUtils;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class UserRepository {

    @Autowired
    private String prefixes;

    @Autowired
    private Dataset dataset;

    @Autowired
    private String namespace;

    public UserDto save(UserDto user, String userId) {
        String userURI = userId == null ? namespace + UUID.randomUUID().toString() : userId;
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

    public List<UserDto> findAll() {
        List<UserDto> users = new ArrayList<>();
        dataset.begin(ReadWrite.READ);
        try {
            dataset.getDefaultModel().listResourcesWithProperty(RDF.type, ResourceFactory.createResource(namespace + "User"))
                    .forEachRemaining(userResource -> {
                        UserDto user = new UserDto();
                        user.setUsername(userResource.getProperty(ResourceFactory.createProperty(namespace + "userUsername")).getString());
                        user.setEmail(userResource.getProperty(ResourceFactory.createProperty(namespace + "userEmail")).getString());
                        user.setAdmin(Boolean.parseBoolean(userResource.getProperty(ResourceFactory.createProperty(namespace + "userAdmin")).getString()));
                        user.setSecurityQuestion(userResource.getProperty(ResourceFactory.createProperty(namespace + "userSecurityQuestion")).getString());
                        user.setAnswer(userResource.getProperty(ResourceFactory.createProperty(namespace + "userAnswer")).getString());
                        user.setPassword(userResource.getProperty(ResourceFactory.createProperty(namespace + "userPassword")).getString());
                        user.setId(JenaUtils.parseId(userResource.getURI()));
                        users.add(user);
                    });

            dataset.commit();
            return users;
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }

    public UserDto findById(String userId) {
        String userURI = namespace + userId;
        Resource userResource = ResourceFactory.createResource(userURI);
        dataset.begin(ReadWrite.READ);
        try {
            Model model = dataset.getDefaultModel();

            if (!model.containsResource(userResource)) {
                return null;
            }

            String userUsername = model.getProperty(userResource, model.createProperty(namespace + "userUsername"))
                    .getString();
            String userEmail = model.getProperty(userResource, model.createProperty(namespace + "userEmail"))
                    .getString();
            String userAdmin = model.getProperty(userResource, model.createProperty(namespace + "userAdmin"))
                    .getString();
            String userSecurityQuestion = model.getProperty(userResource, model.createProperty(namespace + "userSecurityQuestion"))
                    .getString();
            String userAnswer = model.getProperty(userResource, model.createProperty(namespace + "userAnswer"))
                    .getString();
            String userPassword = model.getProperty(userResource, model.createProperty(namespace + "userPassword"))
                    .getString();

            UserDto user = new UserDto();
            user.setUsername(userUsername);
            user.setEmail(userEmail);
            user.setAdmin(Boolean.parseBoolean(userAdmin));
            user.setSecurityQuestion(userSecurityQuestion);
            user.setAnswer(userAnswer);
            user.setPassword(userPassword);
            user.setId(JenaUtils.parseId(userResource.getURI()));
            return user;
        } finally {
            dataset.end();
        }
    }

    public void deleteById(String userId, boolean update) {
        String userURI = namespace + userId;
        Resource userResource = ResourceFactory.createResource(userURI);
        dataset.begin(ReadWrite.WRITE);
        try {
            if (update) {
                StmtIterator it = dataset.getDefaultModel().listStatements(userResource, null, (RDFNode) null);
                while (it.hasNext()) {
                    Statement stmt = it.next();
                    dataset.getDefaultModel().remove(stmt);
                }
            }
            else {
                dataset.getDefaultModel().removeAll(userResource, null, (RDFNode) null);
            }
            dataset.commit();
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }
}
