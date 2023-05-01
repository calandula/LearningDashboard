package com.example.learningdashboard.service;

import com.example.learningdashboard.dtos.CategoryItemDto;
import com.example.learningdashboard.dtos.QFItemDto;
import com.example.learningdashboard.repository.QFItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        return qfItemRepository.save(qfItem, null);
    }

    public List<QFItemDto> getQFItemBySIItem(String siItemId) {
        List<QFItemDto> qfItem = qfItemRepository.findBySIItem(siItemId);
        return new ArrayList<>(qfItem);
    }

    public List<QFItemDto> getQFItemByProject(String projectId) {
        List<QFItemDto> qfItem = qfItemRepository.findByProject(projectId);
        return new ArrayList<>(qfItem);
    }

    public QFItemDto updateQFItem(String qfItemId, QFItemDto qfi) {
        Optional<QFItemDto> optionalQFItem = Optional.ofNullable(qfItemRepository.findById(qfItemId));
        if (optionalQFItem.isPresent()) {
            qfItemRepository.deleteById(qfItemId, true);
            return qfItemRepository.save(qfi, qfItemId);
        } else {
            return null;
        }
    }

    public void deleteQFItem(String qfItemId) {
        qfItemRepository.deleteById(qfItemId, false);
    }
}
