package com.example.learningdashboard.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class IterationDto {
    private String id;
    @NotBlank
    private String name;
    private String subject;
    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate from;
    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate to;
    private ArrayList<String> associatedProjects;
}
