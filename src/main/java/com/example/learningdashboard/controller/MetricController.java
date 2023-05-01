package com.example.learningdashboard.controller;

import com.example.learningdashboard.dtos.CategoryDto;
import com.example.learningdashboard.dtos.MetricDto;
import com.example.learningdashboard.service.CategoryService;
import com.example.learningdashboard.service.MetricService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/metrics")
public class MetricController {

    @Autowired
    private MetricService metricService;

    @PostMapping
    public ResponseEntity<MetricDto> createMetric(@RequestBody MetricDto metric) {
        MetricDto savedMetric = metricService.createMetric(metric);
        return new ResponseEntity<>(savedMetric, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<MetricDto>> getAllMetrics() {
        List<MetricDto> metrics = metricService.getAllMetrics();
        return new ResponseEntity<>(metrics, HttpStatus.OK);
    }

    @GetMapping("{id}")
    public ResponseEntity<MetricDto> getMetricById(@PathVariable("id") String metricId) {
        MetricDto metric = metricService.getMetricById(metricId);
        return new ResponseEntity<>(metric, HttpStatus.OK);
    }

    @GetMapping("/item/{id}")
    public ResponseEntity<List<MetricDto>> getMetricsByQFItem(@PathVariable("id") String qfItemId) {
        List<MetricDto> metric = metricService.getMetricByQFItem(qfItemId);
        return new ResponseEntity<>(metric, HttpStatus.OK);
    }

    @PutMapping("{id}")
    public ResponseEntity<MetricDto> updateMetric(@PathVariable("id") String metricId, @RequestBody MetricDto metric) {
        MetricDto updatedMetric = metricService.updateMetric(metricId, metric);
        return new ResponseEntity<>(updatedMetric, HttpStatus.OK);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteMetric(@PathVariable("id") String metricId) {
        metricService.deleteMetric(metricId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
