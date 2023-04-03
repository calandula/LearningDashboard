package com.example.learningdashboard.user;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    @GetMapping("/brad")
    public String Brad() {
        return "brad";
    }
}
