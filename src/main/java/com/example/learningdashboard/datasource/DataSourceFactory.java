package com.example.learningdashboard.datasource;

import org.springframework.stereotype.Component;

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
