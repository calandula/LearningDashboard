package com.example.learningdashboard.repository;

import com.example.learningdashboard.dtos.ProfileDto;
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
public class ProfileRepository {

    @Autowired
    private String prefixes;

    @Autowired
    private Dataset dataset;

    @Autowired
    private String namespace;

    public ProfileDto save(ProfileDto profile, String profileId) {
        profileId = profileId == null ? UUID.randomUUID().toString() : profileId;
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

            dataset.getDefaultModel()
                    .add(profileResource, RDF.type, profileClass)
                    .add(profileResource, ResourceFactory.createProperty(namespace + "profileName"),
                            ResourceFactory.createPlainLiteral(profile.getName()))
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

            if (profile.getDescription() != null) {
                dataset.getDefaultModel().add(profileResource,
                        ResourceFactory.createProperty(namespace + "profileDescription"),
                        ResourceFactory.createPlainLiteral(profile.getDescription()));
            }

            projectResources.forEach(projectResource ->
                    dataset.getDefaultModel().add(profileResource,
                            ResourceFactory.createProperty(namespace + "allowedProject"),
                            projectResource));

            if (profile.getAllowedStrategicIndicators() != null) {
                List<Resource> siResources = profile.getAllowedStrategicIndicators().stream()
                        .map(siId -> ResourceFactory.createResource(namespace + siId))
                        .filter(siResource -> dataset.getDefaultModel().containsResource(siResource))
                        .toList();
                if (siResources.size() != profile.getAllowedStrategicIndicators().size()) {
                    throw new IllegalArgumentException("One or more SI IDs do not exist in the dataset.");
                }

                siResources.forEach(siResource ->
                        dataset.getDefaultModel().add(profileResource,
                                ResourceFactory.createProperty(namespace + "allowedSI"),
                                siResource));
            }

            dataset.commit();
            profile.setId(profileId);
            return profile;
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }

    public List<ProfileDto> findAll() {
        List<ProfileDto> profiles = new ArrayList<>();
        dataset.begin(ReadWrite.READ);
        try {
            Model model = dataset.getDefaultModel();
            ResIterator profileResources = model.listResourcesWithProperty(RDF.type, ResourceFactory.createResource(namespace + "Profile"));
            while (profileResources.hasNext()) {
                Resource profileResource = profileResources.next();
                ProfileDto profile = new ProfileDto();
                profile.setName(getPropertyString(profileResource, "profileName"));
                profile.setDescription(getPropertyString(profileResource, "profileDescription"));
                profile.setQualityLevel(getPropertyString(profileResource, "profileQualityLevel"));
                profile.setDetailedStrategicIndicatorsView(getPropertyString(profileResource, "profileDetailedSIView"));
                profile.setDetailedFactorsView(getPropertyString(profileResource, "profileDetailedFView"));
                profile.setMetricsView(getPropertyString(profileResource, "profileMetricsView"));
                profile.setQualityModelsView(getPropertyString(profileResource, "profileQMView"));
                profile.setAllowedProjects((ArrayList<String>) getPropertyList(profileResource, "allowedProject"));
                profile.setAllowedStrategicIndicators((ArrayList<String>) getPropertyList(profileResource, "allowedSI"));
                profile.setId(JenaUtils.parseId(profileResource.getURI()));
                profiles.add(profile);
            }
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
            ProfileDto profile = new ProfileDto();
            profile.setName(getPropertyString(profileResource, "profileName"));
            profile.setDescription(getPropertyString(profileResource, "profileDescription"));
            profile.setQualityLevel(getPropertyString(profileResource, "profileQualityLevel"));
            profile.setDetailedStrategicIndicatorsView(getPropertyString(profileResource, "profileDetailedSIView"));
            profile.setDetailedFactorsView(getPropertyString(profileResource, "profileDetailedFView"));
            profile.setMetricsView(getPropertyString(profileResource, "profileMetricsView"));
            profile.setQualityModelsView(getPropertyString(profileResource, "profileQMView"));
            profile.setAllowedProjects((ArrayList<String>) getPropertyList(profileResource, "allowedProject"));
            profile.setAllowedStrategicIndicators((ArrayList<String>) getPropertyList(profileResource, "allowedSI"));
            profile.setId(JenaUtils.parseId(profileResource.getURI()));
            return profile;
        } finally {
            dataset.end();
        }
    }


    public void deleteById(String profileId, boolean update) {
        String profileURI = namespace + profileId;
        Resource profileResource = ResourceFactory.createResource(profileURI);
        dataset.begin(ReadWrite.WRITE);
        try {
            if (update) {
                StmtIterator it = dataset.getDefaultModel().listStatements(profileResource, null, (RDFNode) null);
                while (it.hasNext()) {
                    Statement stmt = it.next();
                    dataset.getDefaultModel().remove(stmt);
                }
            } else {
                dataset.getDefaultModel().removeAll(profileResource, null, (RDFNode) null);
            }
            dataset.commit();
        } catch (Exception e) {
            dataset.abort();
            throw e;
        }
    }
}
