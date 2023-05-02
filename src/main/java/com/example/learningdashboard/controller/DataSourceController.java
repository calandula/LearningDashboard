package com.example.learningdashboard.controller;

import com.example.learningdashboard.dtos.CategoryItemDto;
import com.example.learningdashboard.dtos.DataSourceDto;
import com.example.learningdashboard.dtos.StudentDto;
import com.example.learningdashboard.service.CategoryItemService;
import com.example.learningdashboard.service.DataSourceService;
import com.example.learningdashboard.service.StudentService;
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

    @PostMapping
    public ResponseEntity<DataSourceDto> createDataSource(@RequestBody DataSourceDto ds) {
        DataSourceDto savedDs = dataSourceService.createDataSource(ds);
        return new ResponseEntity<>(savedDs, HttpStatus.CREATED);
    }

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

    @PutMapping("{id}")
    public ResponseEntity<DataSourceDto> updateDataSource(@PathVariable("id") String dsId, @RequestBody DataSourceDto ds) {
        DataSourceDto updatedDs = dataSourceService.updateDataSourceById(dsId, ds);
        return new ResponseEntity<>(updatedDs, HttpStatus.OK);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteDataSource(@PathVariable("id") String dsId) {
        dataSourceService.deleteDataSourceById(dsId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
