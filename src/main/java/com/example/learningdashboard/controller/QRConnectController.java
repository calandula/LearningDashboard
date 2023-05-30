package com.example.learningdashboard.controller;

import com.example.learningdashboard.dtos.QRConnectDto;
import com.example.learningdashboard.service.QRConnectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("api/qrconnect")
public class QRConnectController {

    @Autowired
    QRConnectService qrConnectService;

    @GetMapping("/connect")
    public ResponseEntity<Object> retrieveData(@RequestBody QRConnectDto request) throws IOException {
        try {
            qrConnectService.retrieveData(request);
            return ResponseEntity.ok().body("data retrieved from DataSource successfully!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("data could not be retrieved");
        }
    }
}
