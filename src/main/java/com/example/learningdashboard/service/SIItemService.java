package com.example.learningdashboard.service;

import com.example.learningdashboard.dtos.SIItemDto;
import com.example.learningdashboard.repository.SIItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SIItemService {

    @Autowired
    private SIItemRepository siItemRepository;

    public List<SIItemDto> getAllSIItems() {
        List<SIItemDto> siItems = siItemRepository.findAll();
        return new ArrayList<>(siItems);
    }

    public SIItemDto getSIItemById(String siItemId) {
        SIItemDto siItem = siItemRepository.findById(siItemId);
        return siItem;
    }

    public SIItemDto createSIItem(SIItemDto siItem) {
        return siItemRepository.save(siItem);
    }

    public SIItemDto getSIItemByProject(String projectId) {
        return null;
    }

    public void deleteSIItemById(String siItemId) {
    }

    public SIItemDto updateSIItemById(String siItemId, SIItemDto siItem) {
        return siItem;
    }
}
