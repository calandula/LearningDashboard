package com.example.learningdashboard.service;

import com.example.learningdashboard.dtos.MetricItemDto;
import com.example.learningdashboard.repository.MetricItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MetricItemService {

    @Autowired
    private MetricItemRepository metricRepository;

    public MetricItemDto createMetric(MetricItemDto metric) {
        return metricRepository.save(metric, null);
    }

    public List<MetricItemDto> getAllMetrics() {
        return metricRepository.findAll();
    }

    public MetricItemDto getMetricById(String metricId) {
        Optional<MetricItemDto> optionalMetric = Optional.ofNullable(metricRepository.findById(metricId));
        return optionalMetric.orElse(null);
    }

    public List<MetricItemDto> getMetricByQFItem(String qfItemId) {
        return metricRepository.findByQFItem(qfItemId);
    }

    public MetricItemDto updateMetric(String metricId, MetricItemDto metric) {
        Optional<MetricItemDto> optionalMetric = Optional.ofNullable(metricRepository.findById(metricId));
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
