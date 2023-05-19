package com.example.learningdashboard.datasource;


import com.example.learningdashboard.dtos.QREvalDto;
import com.example.learningdashboard.repository.DataSourceRepository;
import com.example.learningdashboard.repository.MetricItemRepository;
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

    @GetMapping("/eval")
    public ResponseEntity<Object> retrieveData(@RequestBody QREvalDto request) {
        String dataSourceId = request.getDsId();
        String method = request.getMethod();
        String metricId = request.getMetricId();

        String dataSourceClassName = dataSourceRepository.getClass(dataSourceId);
        if (dataSourceClassName == null) {
            return ResponseEntity.badRequest().body("Invalid data source ID");
        }

        float newValue = 0.0f;

        System.out.println(dataSourceClassName);
        switch (dataSourceClassName) {
            case "DataSource", "GithubDataSource":
                if (githubEntitiesRepository.supportsMethod(method)) {
                    newValue = githubEntitiesRepository.computeMetric(dataSourceId, method);
                }
            case "TaigaDataSource":
                if (githubEntitiesRepository.supportsMethod(method)) {
                    newValue = githubEntitiesRepository.computeMetric(dataSourceId, method);
                }
        }

        metricRepository.updateValue(newValue, metricId);

        return ResponseEntity.ok().body("Metric computed and quality model updated");
    }
}

