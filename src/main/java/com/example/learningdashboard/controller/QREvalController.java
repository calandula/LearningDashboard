package com.example.learningdashboard.controller;

import com.example.learningdashboard.datasource.DataSource;
import com.example.learningdashboard.datasource.DataSourceFactory;
import com.example.learningdashboard.dtos.DataRetrievalDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class QREvalController {

    private final DataSourceFactory dataSourceFactory;

    public QREvalController(DataSourceFactory dataSourceFactory) {
        this.dataSourceFactory = dataSourceFactory;
    }

    @PostMapping("/retrieve")
    public ResponseEntity<Object> retrieveData(@RequestBody DataRetrievalDto request) {
        DataSource dataSource = dataSourceFactory.getDataSource(request.getDataSourceName());
        if (dataSource == null) {
            return ResponseEntity.badRequest().body("Invalid data source name");
        }

        String objectName = request.getObjectName();
        if (!dataSource.supportsObject(objectName)) {
            return ResponseEntity.badRequest().body("Invalid object name for the selected data source");
        }

        Map<String, String> apiConfig = request.getApiConfig();
        if (apiConfig == null || apiConfig.isEmpty()) {
            return ResponseEntity.badRequest().body("API config cannot be null or empty");
        }

        try {
            Object data = dataSource.retrieveData(objectName, apiConfig);
            return ResponseEntity.ok().body(data);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
