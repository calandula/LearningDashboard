package com.example.learningdashboard.service;

import com.example.learningdashboard.dtos.QFDto;
import com.example.learningdashboard.repository.QFRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class QFService {

    @Autowired
    private QFRepository qfRepository;

    public List<QFDto> getAllQFs() {
        List<QFDto> qfs = qfRepository.findAll();
        return new ArrayList<>(qfs);
    }

    public QFDto getQFById(String qfId) {
        return qfRepository.findById(qfId);
    }

    public QFDto createQF(QFDto qf) {
        return qfRepository.save(qf);
    }

    public void deleteQFById(String qfId) {
    }

    public QFDto updateQFById(String qfId, QFDto qf) {
        return qf;
    }
}
