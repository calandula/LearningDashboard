package com.example.learningdashboard.service;

import com.example.learningdashboard.dtos.IterationDto;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class IterationService {
    public IterationDto createIteration(IterationDto product) {
        return product;
    }

    public List<IterationDto> getAllIterations() {
        return null;
    }

    public IterationDto getIterationById(String iterationId) {
        return null;
    }
}
