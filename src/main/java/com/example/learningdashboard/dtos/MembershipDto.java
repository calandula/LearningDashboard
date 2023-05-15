package com.example.learningdashboard.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MembershipDto {

    private String id;
    @NotBlank
    private String username;

    @NotEmpty
    private String basedDataSource;
}
