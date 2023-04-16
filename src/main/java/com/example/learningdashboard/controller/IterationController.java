package com.example.learningdashboard.controller;

import com.example.learningdashboard.dtos.IterationDto;
import com.example.learningdashboard.service.IterationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/iterations")
public class IterationController {

    @Autowired
    private IterationService iterationService;

    @PostMapping
    public ResponseEntity<IterationDto> createIteration(@RequestBody IterationDto product) {
        IterationDto savedIteration = iterationService.createIteration(product);
        return new ResponseEntity<>(savedIteration, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<IterationDto>> getAllIterations() {
        List<IterationDto> iterations = iterationService.getAllIterations();
        return new ResponseEntity<>(iterations, HttpStatus.OK);
    }

    @GetMapping("{id}")
    public ResponseEntity<IterationDto> getIterationById(@PathVariable("id") String iterationId) {
        IterationDto iteration = iterationService.getIterationById(iterationId);
        return new ResponseEntity<>(iteration, HttpStatus.OK);
    }
}
