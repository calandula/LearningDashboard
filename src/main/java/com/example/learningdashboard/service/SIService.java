package com.example.learningdashboard.service;

import com.example.learningdashboard.dtos.SIDto;
import com.example.learningdashboard.repository.SIRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        return siRepository.save(si, null);
    }

    public void deleteSIById(String siId) {
        siRepository.deleteById(siId, false);
    }

    public SIDto updateSI(String siId, SIDto si) {
        Optional<SIDto> optionalSI = Optional.ofNullable(siRepository.findById(siId));
        if (optionalSI.isPresent()) {
            siRepository.deleteById(siId, true);
            return siRepository.save(si, siId);
        } else {
            return null;
        }
    }
}
