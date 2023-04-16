package com.example.learningdashboard.service;

import com.example.learningdashboard.dtos.QFDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QFService {
    public QFDto createQF(QFDto qf) {
        return qf;
    }

    public List<QFDto> getAllQFs() {
        return null;
    }

    public QFDto getQFById(String qfId) {
        return null;
    }
}
