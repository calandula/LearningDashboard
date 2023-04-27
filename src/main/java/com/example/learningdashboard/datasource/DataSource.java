package com.example.learningdashboard.datasource;

import java.util.Map;

public interface DataSource {
    String getName();
    boolean supportsObject(String objectName);
    Object retrieveData(String objectName, Map<String, String> apiConfig) throws Exception;
}
