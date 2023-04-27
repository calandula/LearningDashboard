package com.example.learningdashboard.service;

import com.example.learningdashboard.dtos.CategoryDto;
import com.example.learningdashboard.dtos.ProfileDto;
import com.example.learningdashboard.repository.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        return profileRepository.save(profile, null);
    }

    public ProfileDto updateProfile(String profileId, ProfileDto profile) {
        Optional<ProfileDto> optionalProfile = Optional.ofNullable(profileRepository.findById(profileId));
        if (optionalProfile.isPresent()) {
            profileRepository.deleteById(profileId, true);
            return profileRepository.save(profile, profileId);
        } else {
            return null;
        }
    }

    public void deleteProfile(String profileId) {
        profileRepository.deleteById(profileId, false);
    }
}
