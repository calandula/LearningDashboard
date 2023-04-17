package com.example.learningdashboard.controller;

import com.example.learningdashboard.dtos.QFDto;
import com.example.learningdashboard.dtos.SIItemDto;
import com.example.learningdashboard.service.QFService;
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
}
