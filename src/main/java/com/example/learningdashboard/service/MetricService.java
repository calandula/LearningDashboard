package com.example.learningdashboard.service;

import com.example.learningdashboard.dtos.CategoryDto;
import com.example.learningdashboard.dtos.MetricDto;
import com.example.learningdashboard.repository.MetricRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MetricService {

    @Autowired
    private MetricRepository metricRepository;

    public MetricDto createMetric(MetricDto metric) {
        return metricRepository.save(metric, null);
    }

    public List<MetricDto> getAllMetrics() {
        return metricRepository.findAll();
    }

    public MetricDto getMetricById(String metricId) {
        Optional<MetricDto> optionalMetric = Optional.ofNullable(metricRepository.findById(metricId));
        return optionalMetric.orElse(null);
    }

    public List<MetricDto> getMetricByQFItem(String qfItemId) {
        return metricRepository.findByQFItem(qfItemId);
    }

    public MetricDto updateMetric(String metricId, MetricDto metric) {
        Optional<MetricDto> optionalMetric = Optional.ofNullable(metricRepository.findById(metricId));
        if (optionalMetric.isPresent()) {
            metricRepository.deleteById(metricId, true);
            return metricRepository.save(metric, metricId);
        } else {
            return null;
        }
    }

    public void deleteMetric(String metricId) {
        metricRepository.deleteById(metricId, false);
    }
}
