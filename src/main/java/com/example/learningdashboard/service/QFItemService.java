package com.example.learningdashboard.service;

import com.example.learningdashboard.dtos.QFItemDto;
import com.example.learningdashboard.repository.QFItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class QFItemService {

    @Autowired
    private QFItemRepository qfItemRepository;

    public List<QFItemDto> getAllQFItems() {
        List<QFItemDto> qfItem = qfItemRepository.findAll();
        return new ArrayList<>(qfItem);
    }

    public QFItemDto getQFItemById(String qfItemId) {
        QFItemDto qfItem = qfItemRepository.findById(qfItemId);
        return qfItem;
    }

    public QFItemDto createQFItem(QFItemDto qfItem) {
        return qfItemRepository.save(qfItem);
    }

    public List<QFItemDto> getQFItemBySIItem(String siItemId) {
        return null;
    }

    public List<QFItemDto> getQFItemByProject(String projectId) {
        return null;
    }

    public QFItemDto updateQFItem(String qfItemId, QFItemDto qfi) {
        return qfi;
    }

    public void deleteQFItem(String qfItemId) {
    }
}
