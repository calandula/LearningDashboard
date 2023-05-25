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

    @Autowired
    private Dataset dataset;

    @Autowired
    private String namespace;

    @Autowired
    private DataSourceRepository dataSourceRepository;

    @Autowired
    private GithubEntitiesRepository githubEntitiesRepository;

    @Autowired
    private TaigaEntitiesRepository taigaEntitiesRepository;


    @GetMapping("/connect")
    public ResponseEntity<Object> retrieveData(@RequestBody QRConnectDto request) throws IOException {
        String dataSourceId = request.getDsId();
        String objectName = request.getObjectName();

        String dataSourceClassName = dataSourceRepository.getClass(dataSourceId);
        if (dataSourceClassName == null) {
            return ResponseEntity.badRequest().body("Invalid data source ID");
        }

        switch (dataSourceClassName) {
            case "GithubDataSource":
                if (githubEntitiesRepository.supportsObject(objectName)) {
                    try {
                        githubEntitiesRepository.retrieveData(objectName, dataSourceId);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return ResponseEntity.ok().body(objectName + " data retrieved successfully from GitHub");
                }
            case "TaigaDataSource":
                if (taigaEntitiesRepository.supportsObject(objectName)) {
                    try {
                        taigaEntitiesRepository.retrieveData(objectName, dataSourceId);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return ResponseEntity.ok().body(objectName + " data retrieved successfully from Taiga");
                }
        }

        return ResponseEntity.ok().body("data could not be retrieved");
        }
    }
