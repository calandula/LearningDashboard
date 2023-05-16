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
public class GithubDataSourceDto extends DataSourceDto {
    @NotBlank
    private String repository;
    @NotBlank
    private String owner;

    public String getType() {
        return "GithubDataSource";
    }
}
