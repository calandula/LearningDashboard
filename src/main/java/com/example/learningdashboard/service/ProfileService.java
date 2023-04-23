package com.example.learningdashboard.service;

import com.example.learningdashboard.dtos.ProfileDto;
import com.example.learningdashboard.repository.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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

    public ProfileDto updateProfile(String profileId, ProfileDto profile) {
        return profile;
    }

    public void deleteProfile(String profileId) {
    }
}
