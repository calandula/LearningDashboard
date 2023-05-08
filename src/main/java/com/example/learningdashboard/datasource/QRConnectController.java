package com.example.learningdashboard.datasource;

import com.example.learningdashboard.datasource.DataSource;
import com.example.learningdashboard.datasource.DataSourceFactory;
import com.example.learningdashboard.datasource.GithubDataSource;
import com.example.learningdashboard.dtos.DataRetrievalDto;
import com.example.learningdashboard.repository.DataSourceRepository;
import org.apache.jena.query.Dataset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("api/qrconnect")
public class QRConnectController {

    private final DataSourceFactory dataSourceFactory;

    @Autowired
    private Dataset dataset;

    @Autowired
    private String namespace;

    @Autowired
    private DataSourceRepository dataSourceRepository;

    public QRConnectController(DataSourceFactory dataSourceFactory) {
        this.dataSourceFactory = dataSourceFactory;
    }

    @GetMapping("/retrieve")
    public ResponseEntity<Object> retrieveData(@RequestBody DataRetrievalDto request) {
        String dataSourceId = request.getDsId();
        String objectName = request.getObjectName();

        String dataSourceClassName = dataSourceRepository.getClass(dataSourceId);
        if (dataSourceClassName == null) {
            return ResponseEntity.badRequest().body("Invalid data source ID");
        }

        DataSource dataSource = dataSourceFactory.getDataSource(dataSourceClassName, dataSourceId, dataSourceRepository);

        if (dataSource == null) {
            return ResponseEntity.badRequest().body("Invalid data source class name");
        }

        if (!dataSource.supportsObject(objectName)) {
            return ResponseEntity.badRequest().body("Invalid object name for the selected data source");
        }

        try {
            Object data = dataSource.retrieveData(objectName);
            return ResponseEntity.ok().body("data retrieved successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
