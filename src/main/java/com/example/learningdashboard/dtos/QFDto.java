package com.example.learningdashboard.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class QFDto {

    private String id;
    @NotBlank
    private String name;

    @NotBlank
    private String Description;

    @NotBlank
    private String dataSource;

}
