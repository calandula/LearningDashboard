package com.example.learningdashboard.datasource;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DataSourceFactory {

    private final Map<String, DataSource> dataSources;

    public DataSourceFactory(List<DataSource> dataSources) {
        this.dataSources = dataSources.stream().collect(Collectors.toMap(DataSource::getName, ds -> ds));
    }

    public DataSource getDataSource(String dataSourceName) {
        return dataSources.get(dataSourceName);
    }
}
