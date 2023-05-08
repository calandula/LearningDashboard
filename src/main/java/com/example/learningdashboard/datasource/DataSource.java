package com.example.learningdashboard.datasource;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public abstract class DataSource {
    public abstract String getName();
    public abstract boolean supportsObject(String objectName);
    public abstract Object retrieveData(String objectName) throws Exception;
}
