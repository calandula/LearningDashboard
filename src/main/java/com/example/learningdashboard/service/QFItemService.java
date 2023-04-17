package com.example.learningdashboard.service;

import com.example.learningdashboard.dtos.QFItemDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QFItemService {
    public QFItemDto createQF(QFItemDto qfi) {
        return qfi;
    }

    public List<QFItemDto> getAllQFItems() {
        return null;
    }

    public QFItemDto getQFById(String qfItemId) {
        return null;
    }
}
