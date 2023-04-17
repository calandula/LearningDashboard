package com.example.learningdashboard.controller;

import com.example.learningdashboard.dtos.QFDto;
import com.example.learningdashboard.dtos.QFItemDto;
import com.example.learningdashboard.service.QFItemService;
import com.example.learningdashboard.service.QFService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/qfis")
public class QFItemController {

    @Autowired
    private QFItemService qfItemService;

    @PostMapping
    public ResponseEntity<QFItemDto> createQFItem(@RequestBody QFItemDto qfi) {
        QFItemDto savedQFItem = qfItemService.createQF(qfi);
        return new ResponseEntity<>(savedQFItem, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<QFItemDto>> getAllQFItems() {
        List<QFItemDto> qfis = qfItemService.getAllQFItems();
        return new ResponseEntity<>(qfis, HttpStatus.OK);
    }

    @GetMapping("{id}")
    public ResponseEntity<QFItemDto> getQFItemById(@PathVariable("id") String qfItemId) {
        QFItemDto qfi = qfItemService.getQFById(qfItemId);
        return new ResponseEntity<>(qfi, HttpStatus.OK);
    }
}
