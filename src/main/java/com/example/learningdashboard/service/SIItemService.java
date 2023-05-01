package com.example.learningdashboard.service;

import com.example.learningdashboard.dtos.CategoryItemDto;
import com.example.learningdashboard.dtos.SIItemDto;
import com.example.learningdashboard.repository.SIItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        return siItemRepository.save(siItem, null);
    }

    public List<SIItemDto> getSIItemByProject(String projectId) {
        List<SIItemDto> siItems = siItemRepository.findByProject(projectId);
        return new ArrayList<>(siItems);
    }

    public void deleteSIItemById(String siItemId) {
        siItemRepository.deleteById(siItemId, false);
    }

    public SIItemDto updateSIItemById(String siItemId, SIItemDto siItem) {
        Optional<SIItemDto> optionalSIItem = Optional.ofNullable(siItemRepository.findById(siItemId));
        if (optionalSIItem.isPresent()) {
            siItemRepository.deleteById(siItemId, true);
            return siItemRepository.save(siItem, siItemId);
        } else {
            return null;
        }
    }
}
