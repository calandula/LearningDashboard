package com.example.learningdashboard.service;

import com.example.learningdashboard.dtos.SIDto;
import com.example.learningdashboard.repository.SIRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SIService {

    @Autowired
    private SIRepository siRepository;

    public List<SIDto> getAllSIs() {
        List<SIDto> sis = siRepository.findAll();
        return new ArrayList<>(sis);
    }

    public SIDto getSIById(String siId) {
        SIDto si = siRepository.findById(siId);
        return si;
    }

    public SIDto createSI(SIDto si) {
        return siRepository.save(si);
    }

    public void deleteSIById(String siId) {
    }

    public SIDto updateSIById(String siId, SIDto si) {
        return si;
    }
}
