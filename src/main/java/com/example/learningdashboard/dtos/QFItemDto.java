package com.example.learningdashboard.dtos;

import com.example.learningdashboard.utils.SerializablePair;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class QFItemDto {

    private String id;

    @NotBlank
    private String sourceQF;

    private float value;

    private float threshold;

    @NotBlank
    private String category;

    @NotEmpty
    private ArrayList<SerializablePair<String, Float>> metricWeights;
}
