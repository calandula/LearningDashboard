package com.example.learningdashboard.service;

import com.example.learningdashboard.dtos.IterationDto;
import com.example.learningdashboard.dtos.ProfileDto;
import com.example.learningdashboard.repository.IterationRepository;
import com.example.learningdashboard.repository.ProfileRepository;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProfileService {

    @Autowired
    private ProfileRepository profileRepository;

    public List<ProfileDto> getAllProfiles() {
        List<ProfileDto> profiles = profileRepository.findAll();
        return new ArrayList<>(profiles);
    }

    public ProfileDto getProfileById(String profileId) {
        ProfileDto profile = profileRepository.findById(profileId);
        return profile;
    }

    public ProfileDto createProfile(ProfileDto profile) {
        return profileRepository.save(profile);
    }

}
