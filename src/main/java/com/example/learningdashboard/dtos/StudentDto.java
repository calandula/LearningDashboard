package com.example.learningdashboard.dtos;

import com.example.learningdashboard.utils.Membership;
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
public class StudentDto {

    private String id;
    @NotBlank
    private String name;
    private ArrayList<Membership<String, String>> memberships;
}
