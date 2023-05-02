package com.example.learningdashboard.datasource;

import com.example.learningdashboard.dtos.DataRetrievalDto;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DataSourceFactory {

    public DataSourceFactory() {

    }

    public DataSource getDataSource(String className, Object... initArgs) {
        if (className.equalsIgnoreCase("github")) {
            if (initArgs.length != 3) {
                throw new IllegalArgumentException("Invalid number of arguments for GithubDataSource initialization");
            }
            String repository = (String) initArgs[0];
            String owner = (String) initArgs[1];
            String accessToken = (String) initArgs[2];
            return new GithubDataSource(new RestTemplateBuilder(), repository, owner, accessToken);
        }
        return null;
    }
}
