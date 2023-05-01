package com.example.learningdashboard.datasource;

import java.util.Map;

public abstract class DataSource {
    public abstract String getName();
    public abstract boolean supportsObject(String objectName);
    public abstract Object retrieveData(String objectName) throws Exception;
}
