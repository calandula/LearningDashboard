package com.example.learningdashboard.user;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PetsController {
    @GetMapping("/test")
    public String Brad() {
        return "brad";
    }
}
