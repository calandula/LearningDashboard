package com.example.learningdashboard.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DataRetrievalDto {

    @NotBlank
    private String dataSourceName;

    @NotBlank
    private String objectName;

    private Map<String, String> apiConfig;

    // getters and setters
}
