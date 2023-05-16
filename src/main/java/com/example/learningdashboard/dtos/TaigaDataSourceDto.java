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
public class TaigaDataSourceDto extends DataSourceDto {
    @NotBlank
    private String backlogID;

    public String getType() {
        return "TaigaDataSource";
    }
}
