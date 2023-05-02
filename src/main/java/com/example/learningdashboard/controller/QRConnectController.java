package com.example.learningdashboard.controller;

import com.example.learningdashboard.datasource.DataSource;
import com.example.learningdashboard.datasource.DataSourceFactory;
import com.example.learningdashboard.datasource.GithubDataSource;
import com.example.learningdashboard.dtos.DataRetrievalDto;
import org.apache.jena.query.Dataset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("api/qrconnect")
public class QRConnectController {

    private final DataSourceFactory dataSourceFactory;

    @Autowired
    private Dataset dataset;

    @Autowired
    private String namespace;

    public QRConnectController(DataSourceFactory dataSourceFactory) {
        this.dataSourceFactory = dataSourceFactory;
    }

    @GetMapping("/retrieve")
    public ResponseEntity<Object> retrieveData(@RequestBody DataRetrievalDto request) {
        String className = getDataSourceClass(request.getDsId());
        DataSource dataSource = dataSourceFactory.getDataSource(className, "LearningDashboard", "calandula", "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");
        if (dataSource == null) {
            return ResponseEntity.badRequest().body("Invalid data source name");
        }

        String objectName = request.getObjectName();
        if (!dataSource.supportsObject(objectName)) {
            return ResponseEntity.badRequest().body("Invalid object name for the selected data source");
        }

        try {
            Object data = dataSource.retrieveData(objectName);
            return ResponseEntity.ok().body(1);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    private String getDataSourceClass(String dsId) {
        return "github";
    }
}
