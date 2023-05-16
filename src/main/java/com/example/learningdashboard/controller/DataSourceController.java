package com.example.learningdashboard.controller;

import com.example.learningdashboard.dtos.DataSourceDto;
import com.example.learningdashboard.dtos.GithubDataSourceDto;
import com.example.learningdashboard.dtos.TaigaDataSourceDto;
import com.example.learningdashboard.service.DataSourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/datasources")
public class DataSourceController {

    @Autowired
    private DataSourceService dataSourceService;

    @PostMapping("/github")
    public ResponseEntity<GithubDataSourceDto> createGithubDataSource(@RequestBody GithubDataSourceDto ds) {
        GithubDataSourceDto savedDs = dataSourceService.createGithubDataSource(ds);
        return new ResponseEntity<>(savedDs, HttpStatus.CREATED);
    }

    @PostMapping("/taiga")
    public ResponseEntity<TaigaDataSourceDto> createTaigaDataSource(@RequestBody TaigaDataSourceDto ds) {
        TaigaDataSourceDto savedDs = dataSourceService.createTaigaDataSource(ds);
        return new ResponseEntity<>(savedDs, HttpStatus.CREATED);
    }

    // Add similar methods for other data source types

    @GetMapping
    public ResponseEntity<List<DataSourceDto>> getAllDataSources() {
        List<DataSourceDto> dsList = dataSourceService.getAllDataSources();
        return new ResponseEntity<>(dsList, HttpStatus.OK);
    }

    @GetMapping("{id}")
    public ResponseEntity<DataSourceDto> getDataSourceById(@PathVariable("id") String dsId) {
        DataSourceDto ds = dataSourceService.getDataSourceById(dsId);
        return new ResponseEntity<>(ds, HttpStatus.OK);
    }

    @GetMapping("/project/{id}")
    public ResponseEntity<List<DataSourceDto>> getDataSourcesByProject(@PathVariable("id") String projectId) {
        List<DataSourceDto> dsList = dataSourceService.getDataSourcesByProject(projectId);
        return new ResponseEntity<>(dsList, HttpStatus.OK);
    }

    @PutMapping("/github/{id}")
    public ResponseEntity<GithubDataSourceDto> updateGithubDataSource(@PathVariable("id") String dsId, @RequestBody GithubDataSourceDto ds) {
        GithubDataSourceDto updatedDs = dataSourceService.updateGithubDataSourceById(dsId, ds);
        return new ResponseEntity<>(updatedDs, HttpStatus.OK);
    }

    @PutMapping("/taiga/{id}")
    public ResponseEntity<TaigaDataSourceDto> updateTaigaDataSource(@PathVariable("id") String dsId, @RequestBody TaigaDataSourceDto ds) {
        TaigaDataSourceDto updatedDs = dataSourceService.updateTaigaDataSourceById(dsId, ds);
        return new ResponseEntity<>(updatedDs, HttpStatus.OK);
    }

    // Add similar methods for other data source types

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteDataSource(@PathVariable("id") String dsId) {
        dataSourceService.deleteDataSourceById(dsId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
