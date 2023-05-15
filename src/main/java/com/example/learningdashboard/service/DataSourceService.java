package com.example.learningdashboard.service;

import com.example.learningdashboard.dtos.DataSourceDto;
import com.example.learningdashboard.repository.DataSourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DataSourceService {

    @Autowired
    private DataSourceRepository dataSourceRepository;

    public List<DataSourceDto> getAllDataSources() {
        List<DataSourceDto> dataSourceDtoList = dataSourceRepository.findAll();
        return new ArrayList<>(dataSourceDtoList);
    }

    public DataSourceDto getDataSourceById(String dsId) {
        DataSourceDto ds = dataSourceRepository.findById(dsId);
        return ds;
    }

    public DataSourceDto createDataSource(DataSourceDto ds) {
        return dataSourceRepository.save(ds, null);
    }

    public List<DataSourceDto> getDataSourcesByProject(String projectId) {
        List<DataSourceDto> dataSourceDtoList = dataSourceRepository.findByProject(projectId);
        return new ArrayList<>(dataSourceDtoList);
    }

    public void deleteDataSourceById(String dsId) {
        dataSourceRepository.deleteById(dsId, false);
    }

    public DataSourceDto updateDataSourceById(String dsId, DataSourceDto ds) {
        Optional<DataSourceDto> optionalDs = Optional.ofNullable(dataSourceRepository.findById(dsId));
        if (optionalDs.isPresent()) {
            dataSourceRepository.deleteById(dsId, true);
            return dataSourceRepository.save(ds, dsId);
        } else {
            return null;
        }
    }
}

