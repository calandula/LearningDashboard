package com.example.learningdashboard.service;

import com.example.learningdashboard.dtos.CategoryDto;
import com.example.learningdashboard.dtos.IterationDto;
import com.example.learningdashboard.repository.IterationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        return iterationRepository.save(iteration, null);
    }

    public List<IterationDto> getIterationsByProject(String projectId) {
        return iterationRepository.getIterationsByProject(projectId);
    }

    public IterationDto updateIteration(String iterationId, IterationDto iteration) {
        Optional<IterationDto> optionalIteration = Optional.ofNullable(iterationRepository.findById(iterationId));
        if (optionalIteration.isPresent()) {
            iterationRepository.deleteById(iterationId, true);
            return iterationRepository.save(iteration, iterationId);
        } else {
            return null;
        }
    }

    public void deleteIteration(String iterationId) {
        iterationRepository.deleteById(iterationId, false);
    }
}