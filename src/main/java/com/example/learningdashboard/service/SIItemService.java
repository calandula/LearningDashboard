package com.example.learningdashboard.service;

import com.example.learningdashboard.dtos.SIItemDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SIItemService {
    public SIItemDto createSIItem(SIItemDto siItem) {
        return siItem;
    }

    public List<SIItemDto> getAllSIItems() {
        return null;
    }

    public SIItemDto getSIItemById(String siItemId) {
        return null;
    }
}
