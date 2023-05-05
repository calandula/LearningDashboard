package com.example.learningdashboard.controller;

import com.example.learningdashboard.dtos.QFItemDto;
import com.example.learningdashboard.service.QFItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/qfitems")
public class QFItemController {

    @Autowired
    private QFItemService qfItemService;

    @PostMapping
    public ResponseEntity<QFItemDto> createQFItem(@RequestBody QFItemDto qfi) {
        QFItemDto savedQFItem = qfItemService.createQFItem(qfi);
        return new ResponseEntity<>(savedQFItem, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<QFItemDto>> getAllQFItems() {
        List<QFItemDto> qfis = qfItemService.getAllQFItems();
        return new ResponseEntity<>(qfis, HttpStatus.OK);
    }

    @GetMapping("{id}")
    public ResponseEntity<QFItemDto> getQFItemById(@PathVariable("id") String qfItemId) {
        QFItemDto qfi = qfItemService.getQFItemById(qfItemId);
        return new ResponseEntity<>(qfi, HttpStatus.OK);
    }

    @GetMapping("/sii/{id}")
    public ResponseEntity<List<QFItemDto>> getQFItemBySIItem(@PathVariable("id") String siItemId) {
        List<QFItemDto> qfis = qfItemService.getQFItemBySIItem(siItemId);
        return new ResponseEntity<>(qfis, HttpStatus.OK);
    }

    @GetMapping("/project/{id}")
    public ResponseEntity<List<QFItemDto>> getQFItemByProject(@PathVariable("id") String projectId) {
        List<QFItemDto> qfis = qfItemService.getQFItemByProject(projectId);
        return new ResponseEntity<>(qfis, HttpStatus.OK);
    }

    @PutMapping("{id}")
    public ResponseEntity<QFItemDto> updateQFItem(@PathVariable("id") String qfItemId, @RequestBody QFItemDto qfi) {
        QFItemDto updatedQFItem = qfItemService.updateQFItem(qfItemId, qfi);
        return new ResponseEntity<>(updatedQFItem, HttpStatus.OK);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteQFItem(@PathVariable("id") String qfItemId) {
        qfItemService.deleteQFItem(qfItemId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
