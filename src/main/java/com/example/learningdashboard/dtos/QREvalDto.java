package com.example.learningdashboard.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class QREvalDto {
    @NotBlank
    private String dsId;
    @NotBlank
    private String metricId;
    @NotBlank
    private String method;
    private String target = "general";

}
