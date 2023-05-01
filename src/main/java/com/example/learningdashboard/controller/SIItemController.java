package com.example.learningdashboard.controller;

import com.example.learningdashboard.dtos.SIItemDto;
import com.example.learningdashboard.service.SIItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/siis")
public class SIItemController {

    @Autowired
    private SIItemService siItemService;

    @PostMapping
    public ResponseEntity<SIItemDto> createSIItem(@RequestBody SIItemDto siItem) {
        SIItemDto savedSIItem = siItemService.createSIItem(siItem);
        return new ResponseEntity<>(savedSIItem, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<SIItemDto>> getAllSIItems() {
        List<SIItemDto> siItems = siItemService.getAllSIItems();
        return new ResponseEntity<>(siItems, HttpStatus.OK);
    }

    @GetMapping("{id}")
    public ResponseEntity<SIItemDto> getSIItemById(@PathVariable("id") String siItemId) {
        SIItemDto siItem = siItemService.getSIItemById(siItemId);
        return new ResponseEntity<>(siItem, HttpStatus.OK);
    }

    @GetMapping("/project/{id}")
    public ResponseEntity<List<SIItemDto>> getSIItemByProject(@PathVariable("id") String projectId) {
        List<SIItemDto> siItems = siItemService.getSIItemByProject(projectId);
        return new ResponseEntity<>(siItems, HttpStatus.OK);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteSIItem(@PathVariable("id") String siItemId) {
        siItemService.deleteSIItemById(siItemId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("{id}")
    public ResponseEntity<SIItemDto> updateSIItem(@PathVariable("id") String siItemId, @RequestBody SIItemDto siItem) {
        SIItemDto updatedSIItem = siItemService.updateSIItemById(siItemId, siItem);
        return new ResponseEntity<>(updatedSIItem, HttpStatus.OK);
    }


}
