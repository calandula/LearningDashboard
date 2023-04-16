package com.example.learningdashboard.service;

import com.example.learningdashboard.dtos.ProfileDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProfileService {
    public ProfileDto createProfile(ProfileDto profile) {
        return profile;
    }

    public List<ProfileDto> getAllProfiles() {
        return null;
    }

    public ProfileDto getProfileById(String profileId) {
        return null;
    }
}
