package com.example.learningdashboard.controller;

import com.example.learningdashboard.dtos.MetricItemDto;
import com.example.learningdashboard.service.MetricItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/metrics")
public class MetricItemController {

    @Autowired
    private MetricItemService metricService;

    @PostMapping
    public ResponseEntity<MetricItemDto> createMetric(@RequestBody MetricItemDto metric) {
        MetricItemDto savedMetric = metricService.createMetric(metric);
        return new ResponseEntity<>(savedMetric, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<MetricItemDto>> getAllMetrics() {
        List<MetricItemDto> metrics = metricService.getAllMetrics();
        return new ResponseEntity<>(metrics, HttpStatus.OK);
    }

    @GetMapping("{id}")
    public ResponseEntity<MetricItemDto> getMetricById(@PathVariable("id") String metricId) {
        MetricItemDto metric = metricService.getMetricById(metricId);
        return new ResponseEntity<>(metric, HttpStatus.OK);
    }

    @GetMapping("/qfitem/{id}")
    public ResponseEntity<List<MetricItemDto>> getMetricsByQFItem(@PathVariable("id") String qfItemId) {
        List<MetricItemDto> metric = metricService.getMetricByQFItem(qfItemId);
        return new ResponseEntity<>(metric, HttpStatus.OK);
    }

    @PutMapping("{id}")
    public ResponseEntity<MetricItemDto> updateMetric(@PathVariable("id") String metricId, @RequestBody MetricItemDto metric) {
        MetricItemDto updatedMetric = metricService.updateMetric(metricId, metric);
        return new ResponseEntity<>(updatedMetric, HttpStatus.OK);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteMetric(@PathVariable("id") String metricId) {
        metricService.deleteMetric(metricId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
