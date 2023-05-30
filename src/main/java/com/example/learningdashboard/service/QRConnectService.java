package com.example.learningdashboard.service;

import com.example.learningdashboard.dtos.QRConnectDto;
import com.example.learningdashboard.repository.DataSourceRepository;
import com.example.learningdashboard.repository.GithubEntitiesRepository;
import com.example.learningdashboard.repository.TaigaEntitiesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

@Service
public class QRConnectService {
    @Autowired
    private DataSourceRepository dataSourceRepository;

    @Autowired
    private GithubEntitiesRepository githubEntitiesRepository;

    @Autowired
    private TaigaEntitiesRepository taigaEntitiesRepository;

    public void retrieveData(@RequestBody QRConnectDto request) throws Exception {

            String dataSourceId = request.getDsId();
            String objectName = request.getObjectName();

            String dataSourceClassName = dataSourceRepository.getClass(dataSourceId);
            if (dataSourceClassName == null) {
                throw new Exception("DataSource class cannot be retrieved");
            }

        switch (dataSourceClassName) {
            case "GithubDataSource" -> {
                if (githubEntitiesRepository.supportsObject(objectName)) {
                    githubEntitiesRepository.retrieveData(objectName, dataSourceId);
                } else {
                    throw new Exception("Method not supported");
                }
            }
            case "TaigaDataSource" -> {
                if (taigaEntitiesRepository.supportsObject(objectName)) {
                    taigaEntitiesRepository.retrieveData(objectName, dataSourceId);

                } else {
                    throw new Exception("Method not supported");
                }
            }
        }

    }
}
