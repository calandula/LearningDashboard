package com.example.learningdashboard.service;

import com.example.learningdashboard.dtos.MetricItemDto;
import com.example.learningdashboard.dtos.QREvalDto;
import com.example.learningdashboard.repository.DataSourceRepository;
import com.example.learningdashboard.repository.GithubEntitiesRepository;
import com.example.learningdashboard.repository.MetricItemRepository;
import com.example.learningdashboard.repository.TaigaEntitiesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QREvalService {
    @Autowired
    private MetricItemRepository metricRepository;

    @Autowired
    private DataSourceRepository dataSourceRepository;

    @Autowired
    private MetricItemRepository metricItemRepository;

    @Autowired
    private GithubEntitiesRepository githubEntitiesRepository;

    @Autowired
    private TaigaEntitiesRepository taigaEntitiesRepository;

    public Float computeMetric(QREvalDto request) throws Exception {
            MetricItemDto metric = metricItemRepository.findById(request.getMetricId());
            String dsId = metric.getDsId();
            String method = metric.getMethod();
            String target = metric.getTarget();
            String dataSourceClassName = dataSourceRepository.getClass(dsId);
            if (dataSourceClassName == null) {
                throw new Exception("Error: No class name found for this object ID");
            }

            float newValue = 0;

        switch (dataSourceClassName) {
            case "GithubDataSource" -> {
                if (githubEntitiesRepository.supportsMethod(metric.getMethod())) {
                    newValue = githubEntitiesRepository.computeMetric(dsId, method, target);
                } else {
                    throw new Exception("Error: Method not supported by this DataSource");
                }
            }
            case "TaigaDataSource" -> {
                if (taigaEntitiesRepository.supportsMethod(metric.getMethod())) {
                    newValue = taigaEntitiesRepository.computeMetric(dsId, method, target);
                } else {
                    throw new Exception("Error: Method not supported by this DataSource");
                }
            }
        }

            metricRepository.updateValue(newValue, request.getMetricId());

            return newValue;
    }
}
