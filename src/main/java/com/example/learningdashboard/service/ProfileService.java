package com.example.learningdashboard.service;

import com.example.learningdashboard.dtos.ProfileDto;
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
public class ProfileService {

    @Autowired
    private String prefixes;

    @Autowired
    private Dataset dataset;

    @Autowired
    private String namespace;

    public ProfileDto createProfile(ProfileDto profile) {
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
                            ResourceFactory.createProperty(namespace + "associatedProject"),
                            projectResource));

            siResources.forEach(siResource ->
                    dataset.getDefaultModel().add(profileResource,
                            ResourceFactory.createProperty(namespace + "associatedProject"),
                            siResource));

            dataset.commit();
            return null;
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }

    public List<ProfileDto> getAllProfiles() {
        return null;
    }

    public ProfileDto getProfileById(String profileId) {
        return null;
    }
}
