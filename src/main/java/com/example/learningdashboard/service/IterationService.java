package com.example.learningdashboard.service;

import com.example.learningdashboard.dtos.IterationDto;
import com.example.learningdashboard.repository.IterationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class IterationService {

    @Autowired
    private IterationRepository iterationRepository;

    public List<IterationDto> getAllIterations() {
        List<IterationDto> iterations = iterationRepository.findAll();
        return new ArrayList<>(iterations);
    }

    public IterationDto getIterationById(String iterationId) {
        IterationDto iteration = iterationRepository.findById(iterationId);
        return iteration;
    }

    public IterationDto createIteration(IterationDto iteration) {
        return iterationRepository.save(iteration);
    }

    public List<IterationDto> getIterationsByProject(String projectId) {
        return iterationRepository.getIterationsByProject(projectId);
    }

    public IterationDto updateIteration(String iterationId, IterationDto iteration) {
        return iteration;
    }

    public void deleteIteration(String iterationId) {
    }
}