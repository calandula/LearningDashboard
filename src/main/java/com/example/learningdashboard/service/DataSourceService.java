package com.example.learningdashboard.service;

import com.example.learningdashboard.dtos.DataSourceDto;
import com.example.learningdashboard.dtos.GithubDataSourceDto;
import com.example.learningdashboard.dtos.TaigaDataSourceDto;
import com.example.learningdashboard.repository.DataSourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    public GithubDataSourceDto createGithubDataSource(GithubDataSourceDto ds) {
        String repository = ds.getRepository();
        String owner = ds.getOwner();
        String accessToken = ds.getAccessToken();
        GithubDataSourceDto githubDataSource = new GithubDataSourceDto();
        githubDataSource.setRepository(repository);
        githubDataSource.setOwner(owner);
        githubDataSource.setAccessToken(accessToken);
        String generatedId = UUID.randomUUID().toString();
        githubDataSource.setId(generatedId);
        dataSourceRepository.save(githubDataSource, generatedId);

        return githubDataSource;
    }

    public TaigaDataSourceDto createTaigaDataSource(TaigaDataSourceDto ds) {
        String backlogId = ds.getBacklogID();
        TaigaDataSourceDto taigaDataSource = new TaigaDataSourceDto();
        taigaDataSource.setBacklogID(backlogId);
        String accessToken = ds.getAccessToken();
        taigaDataSource.setAccessToken(accessToken);
        String generatedId = UUID.randomUUID().toString();
        taigaDataSource.setId(generatedId);
        dataSourceRepository.save(taigaDataSource, generatedId);

        return taigaDataSource;
    }

    public GithubDataSourceDto updateGithubDataSourceById(String dsId, GithubDataSourceDto ds) {
        GithubDataSourceDto existingDataSource = (GithubDataSourceDto) dataSourceRepository.findById(dsId);
        if (existingDataSource != null) {
            existingDataSource.setRepository(ds.getRepository());
            existingDataSource.setOwner(ds.getOwner());
            dataSourceRepository.save(existingDataSource, dsId);
        }

        return existingDataSource;
    }

    public TaigaDataSourceDto updateTaigaDataSourceById(String dsId, TaigaDataSourceDto ds) {
        TaigaDataSourceDto existingDataSource = (TaigaDataSourceDto) dataSourceRepository.findById(dsId);
        if (existingDataSource != null) {
            existingDataSource.setBacklogID(ds.getBacklogID());
            dataSourceRepository.save(existingDataSource, dsId);
        }
        return existingDataSource;
    }
}

