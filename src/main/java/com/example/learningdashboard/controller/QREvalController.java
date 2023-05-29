package com.example.learningdashboard.controller;


import com.example.learningdashboard.dtos.QREvalDto;
import com.example.learningdashboard.repository.DataSourceRepository;
import com.example.learningdashboard.repository.GithubEntitiesRepository;
import com.example.learningdashboard.repository.MetricItemRepository;
import com.example.learningdashboard.repository.TaigaEntitiesRepository;
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
    private MetricItemRepository metricRepository;

    @Autowired
    private DataSourceRepository dataSourceRepository;

    @Autowired
    private GithubEntitiesRepository githubEntitiesRepository;

    @Autowired
    private TaigaEntitiesRepository taigaEntitiesRepository;

    @GetMapping("/eval")
    public ResponseEntity<Object> retrieveData(@RequestBody QREvalDto request) {
        String dataSourceId = request.getDsId();
        String method = request.getMethod();
        String metricId = request.getMetricId();
        String target = request.getTarget();

        String dataSourceClassName = dataSourceRepository.getClass(dataSourceId);
        if (dataSourceClassName == null) {
            return ResponseEntity.badRequest().body("Invalid data source ID");
        }

        float newValue = 0.0f;

        System.out.println(dataSourceClassName);
        switch (dataSourceClassName) {
            case "GithubDataSource":
                if (githubEntitiesRepository.supportsMethod(method)) {
                    newValue = githubEntitiesRepository.computeMetric(dataSourceId, method, target);
                }
            case "TaigaDataSource":
                if (taigaEntitiesRepository.supportsMethod(method)) {
                    newValue = taigaEntitiesRepository.computeMetric(dataSourceId, method, target);
                }
        }

        metricRepository.updateValue(newValue, metricId);

        return ResponseEntity.ok().body("Metric computed and quality model updated");
    }
}

