package com.example.learningdashboard.datasource;

import com.example.learningdashboard.dtos.QRConnectDto;
import com.example.learningdashboard.repository.DataSourceRepository;
import org.apache.jena.query.Dataset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

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

    @Autowired
    private GithubEntitiesRepository githubEntitiesRepository;

    public QRConnectController(DataSourceFactory dataSourceFactory) {
        this.dataSourceFactory = dataSourceFactory;
    }

    @GetMapping("/connect")
    public ResponseEntity<Object> retrieveData(@RequestBody QRConnectDto request) throws IOException {
        String dataSourceId = request.getDsId();
        String objectName = request.getObjectName();

        String dataSourceClassName = dataSourceRepository.getClass(dataSourceId);
        if (dataSourceClassName == null) {
            return ResponseEntity.badRequest().body("Invalid data source ID");
        }

        /*DataSource dataSource = dataSourceFactory.getDataSource(dataSourceClassName, dataSourceId);

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
        }*/
        switch (dataSourceClassName) {
            case "DataSource", "GitHubDataSource":
                if (githubEntitiesRepository.supportsObject(objectName)) {
                    try {
                        githubEntitiesRepository.retrieveData(objectName, dataSourceId);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return null;
                }
            case "TaigaDataSource":
                githubEntitiesRepository.retrieveData(objectName, dataSourceId);
                return null;
        }

        return ResponseEntity.ok().body("data retrieved successfully");
        }
    }
