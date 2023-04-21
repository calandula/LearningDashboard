package com.example.learningdashboard.repository;

import com.example.learningdashboard.dtos.IterationDto;
import com.example.learningdashboard.dtos.ProfileDto;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class ProfileRepository {

    @Autowired
    private String prefixes;

    @Autowired
    private Dataset dataset;

    @Autowired
    private String namespace;

    public ProfileDto save(ProfileDto profile) {
        String profileId = UUID.randomUUID().toString();
        String profileURI = namespace + profileId;
        Resource profileResource = ResourceFactory.createResource(profileURI);
        Resource profileClass = ResourceFactory.createResource(namespace + "Profile");
        dataset.begin(ReadWrite.WRITE);
        try {
            List<Resource> projectResources = profile.getAllowedProjects().stream()
                    .map(projectId -> ResourceFactory.createResource(namespace + projectId))
                    .filter(projectResource -> dataset.getDefaultModel().containsResource(projectResource))
                    .toList();
            if (projectResources.size() != profile.getAllowedProjects().size()) {
                throw new IllegalArgumentException("One or more project IDs do not exist in the dataset.");
            }

            List<Resource> siResources = profile.getAllowedStrategicIndicators().stream()
                    .map(siId -> ResourceFactory.createResource(namespace + siId))
                    .filter(siResource -> dataset.getDefaultModel().containsResource(siResource))
                    .toList();
            if (projectResources.size() != profile.getAllowedStrategicIndicators().size()) {
                throw new IllegalArgumentException("One or more SI IDs do not exist in the dataset.");
            }

            dataset.getDefaultModel()
                    .add(profileResource, RDF.type, profileClass)
                    .add(profileResource, ResourceFactory.createProperty(namespace + "profileName"),
                            ResourceFactory.createPlainLiteral(profile.getName()))
                    .add(profileResource, ResourceFactory.createProperty(namespace + "profileDescription"),
                            ResourceFactory.createPlainLiteral(profile.getDescription()))
                    .add(profileResource, ResourceFactory.createProperty(namespace + "profileQualityLevel"),
                            ResourceFactory.createTypedLiteral(profile.getQualityLevel()))
                    .add(profileResource, ResourceFactory.createProperty(namespace + "profileDetailedSIView"),
                            ResourceFactory.createTypedLiteral(profile.getDetailedStrategicIndicatorsView()))
                    .add(profileResource, ResourceFactory.createProperty(namespace + "profileDetailedFView"),
                            ResourceFactory.createTypedLiteral(profile.getDetailedFactorsView()))
                    .add(profileResource, ResourceFactory.createProperty(namespace + "profileMetricsView"),
                            ResourceFactory.createTypedLiteral(profile.getMetricsView()))
                    .add(profileResource, ResourceFactory.createProperty(namespace + "profileQMView"),
                            ResourceFactory.createTypedLiteral(profile.getQualityModelsView()));



            projectResources.forEach(projectResource ->
                    dataset.getDefaultModel().add(profileResource,
                            ResourceFactory.createProperty(namespace + "allowedProject"),
                            projectResource));

            siResources.forEach(siResource ->
                    dataset.getDefaultModel().add(profileResource,
                            ResourceFactory.createProperty(namespace + "allowedSI"),
                            siResource));

            dataset.commit();
            return null;
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }

    public List<ProfileDto> findAll() {
        List<ProfileDto> profiles = new ArrayList<>();
        dataset.begin(ReadWrite.READ);
        try {
            dataset.getDefaultModel().listResourcesWithProperty(RDF.type, ResourceFactory.createResource(namespace + "Profile"))
                    .forEachRemaining(profileResource -> {
                        ProfileDto profile = new ProfileDto();
                        profile.setName(profileResource.getProperty(ResourceFactory.createProperty(namespace + "profileName")).getString());
                        profile.setDescription(profileResource.getProperty(ResourceFactory.createProperty(namespace + "profileDescription")).getString());
                        profile.setQualityLevel(profileResource.getProperty(ResourceFactory.createProperty(namespace + "profileQualityLevel")).getString());
                        profile.setDetailedStrategicIndicatorsView(profileResource.getProperty(ResourceFactory.createProperty(namespace + "profileDetailedSIView")).getString());
                        profile.setDetailedFactorsView(profileResource.getProperty(ResourceFactory.createProperty(namespace + "profileDetailedFView")).getString());
                        profile.setMetricsView(profileResource.getProperty(ResourceFactory.createProperty(namespace + "profileMetricsView")).getString());
                        profile.setQualityModelsView(profileResource.getProperty(ResourceFactory.createProperty(namespace + "profileQMView")).getString());
                        profile.setAllowedProjects((ArrayList<String>) profileResource.listProperties(ResourceFactory.createProperty(namespace + "allowedProject"))
                                .mapWith(Statement::getObject).mapWith(RDFNode::asResource)
                                .mapWith(Resource::getLocalName).toList());
                        profile.setAllowedStrategicIndicators((ArrayList<String>) profileResource.listProperties(ResourceFactory.createProperty(namespace + "allowedSI"))
                                .mapWith(Statement::getObject).mapWith(RDFNode::asResource)
                                .mapWith(Resource::getLocalName).toList());
                        profiles.add(profile);
                    });

            dataset.commit();
            return profiles;
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }

    public ProfileDto findById(String profileId) {
        String profileURI = namespace + profileId;
        Resource profileResource = ResourceFactory.createResource(profileURI);
        dataset.begin(ReadWrite.READ);
        try {
            Model model = dataset.getDefaultModel();

            if (!model.containsResource(profileResource)) {
                return null;
            }

            String profileName = model.getProperty(profileResource, model.createProperty(namespace + "profileName"))
                    .getString();
            String profileDescription = model.getProperty(profileResource, model.createProperty(namespace + "profileDescription"))
                    .getString();
            String profileQualityLevel = model.getProperty(profileResource, model.createProperty(namespace + "profileQualityLevel"))
                    .getString();
            String profileDetailedSIView = model.getProperty(profileResource, model.createProperty(namespace + "profileDetailedSIView"))
                    .getString();
            String profileDetailedFView = model.getProperty(profileResource, model.createProperty(namespace + "profileDetailedFView"))
                    .getString();
            String profileMetricsView = model.getProperty(profileResource, model.createProperty(namespace + "profileMetricsView"))
                    .getString();
            String profileQMView = model.getProperty(profileResource, model.createProperty(namespace + "profileQMView"))
                    .getString();
            List<String> allowedProjects = model.listObjectsOfProperty(profileResource, model.createProperty(namespace + "allowedProject"))
                    .mapWith(resource -> resource.asResource().getURI().substring(namespace.length()))
                    .toList();
            List<String> allowedSIs = model.listObjectsOfProperty(profileResource, model.createProperty(namespace + "allowedSI"))
                    .mapWith(resource -> resource.asResource().getURI().substring(namespace.length()))
                    .toList();

            ProfileDto profile = new ProfileDto();
            profile.setName(profileName);
            profile.setDescription(profileDescription);
            profile.setQualityLevel(profileQualityLevel);
            profile.setDetailedStrategicIndicatorsView(profileDetailedSIView);
            profile.setDetailedFactorsView(profileDetailedFView);
            profile.setMetricsView(profileMetricsView);
            profile.setQualityModelsView(profileQMView);
            profile.setAllowedProjects((ArrayList<String>) allowedProjects);
            profile.setAllowedStrategicIndicators((ArrayList<String>) allowedSIs);
            return profile;
        } finally {
            dataset.end();
        }
    }
}
