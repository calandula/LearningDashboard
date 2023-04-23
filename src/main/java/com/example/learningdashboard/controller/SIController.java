package com.example.learningdashboard.controller;

import com.example.learningdashboard.dtos.SIDto;
import com.example.learningdashboard.service.SIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/sis")
public class SIController {

    @Autowired
    private SIService siService;

    @PostMapping
    public ResponseEntity<SIDto> createSI(@RequestBody SIDto si) {
        SIDto savedSI = siService.createSI(si);
        return new ResponseEntity<>(savedSI, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<SIDto>> getAllSIs() {
        List<SIDto> sis = siService.getAllSIs();
        return new ResponseEntity<>(sis, HttpStatus.OK);
    }

    @GetMapping("{id}")
    public ResponseEntity<SIDto> getSIById(@PathVariable("id") String siId) {
        SIDto si = siService.getSIById(siId);
        return new ResponseEntity<>(si, HttpStatus.OK);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteSI(@PathVariable("id") String siId) {
        siService.deleteSIById(siId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("{id}")
    public ResponseEntity<SIDto> updateSI(@PathVariable("id") String siId, @RequestBody SIDto si) {
        SIDto updatedSI = siService.updateSIById(siId, si);
        return new ResponseEntity<>(updatedSI, HttpStatus.OK);
    }
}
