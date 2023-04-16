package com.example.learningdashboard.controller;

import com.example.learningdashboard.dtos.QFDto;
import com.example.learningdashboard.dtos.SIDto;
import com.example.learningdashboard.service.QFService;
import com.example.learningdashboard.service.SIService;
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
}
