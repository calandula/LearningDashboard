package com.example.learningdashboard.controller;

import com.example.learningdashboard.dtos.QFDto;
import com.example.learningdashboard.service.QFService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/qfs")
public class QFController {

    @Autowired
    private QFService qfService;

    @PostMapping
    public ResponseEntity<QFDto> createQF(@RequestBody QFDto qf) {
        QFDto savedQF = qfService.createQF(qf);
        return new ResponseEntity<>(savedQF, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<QFDto>> getAllQFs() {
        List<QFDto> qfs = qfService.getAllQFs();
        return new ResponseEntity<>(qfs, HttpStatus.OK);
    }

    @GetMapping("{id}")
    public ResponseEntity<QFDto> getQFById(@PathVariable("id") String qfId) {
        QFDto qf = qfService.getQFById(qfId);
        return new ResponseEntity<>(qf, HttpStatus.OK);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteQF(@PathVariable("id") String qfId) {
        qfService.deleteQFById(qfId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("{id}")
    public ResponseEntity<QFDto> updateQF(@PathVariable("id") String qfId, @RequestBody QFDto qf) {
        QFDto updatedQF = qfService.updateQFById(qfId, qf);
        return new ResponseEntity<>(updatedQF, HttpStatus.OK);
    }
}
