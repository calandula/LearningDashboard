package com.example.learningdashboard.service;

import com.example.learningdashboard.dtos.SIDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SIService {
    public SIDto createSI(SIDto si) {
        return si;
    }

    public List<SIDto> getAllSIs() {
        return null;
    }

    public SIDto getSIById(String siId) {
        return null;
    }
}
