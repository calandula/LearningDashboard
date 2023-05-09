package com.example.learningdashboard.datasource;
import com.example.learningdashboard.repository.DataSourceRepository;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DataSourceFactory {

    public DataSourceFactory() {

    }

    public DataSource getDataSource(String dataSourceClassName, String dataSourceId) {
        DataSource dataSource = null;
        switch (dataSourceClassName) {
            case "DataSource", "GitHubDataSource":
                dataSource = new GithubDataSource(dataSourceId);
                break;
            case "TaigaDataSource":
                dataSource = new TaigaDataSource(dataSourceId);
                break;
            default:
                return null;
        }
        return dataSource;
    }
}
