package com.example.learningdashboard.controller;


import com.example.learningdashboard.dtos.QREvalDto;
import com.example.learningdashboard.service.QREvalService;
import org.apache.jena.query.Dataset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("api/qreval")
public class QREvalController {

    @Autowired
    private Dataset dataset;

    @Autowired
    private String namespace;

    @Autowired
    private QREvalService qrEvalService;

    @GetMapping("/eval")
    public ResponseEntity<Object> computeMetric(@RequestBody QREvalDto request) {
        try {
            float value = qrEvalService.computeMetric(request);
            return ResponseEntity.ok().body("Metric computed and quality model updated with metric value: " + value);
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e);
        }
    }
}

