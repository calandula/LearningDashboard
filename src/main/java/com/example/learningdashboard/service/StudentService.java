package com.example.learningdashboard.service;

import com.example.learningdashboard.dtos.MembershipDto;
import com.example.learningdashboard.dtos.StudentDto;
import com.example.learningdashboard.repository.MembershipRepository;
import com.example.learningdashboard.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    public List<StudentDto> getAllStudents() {
        List<StudentDto> studentDtoList = studentRepository.findAll();
        return new ArrayList<>(studentDtoList);
    }

    public StudentDto getStudentById(String studentId) {
        StudentDto student = studentRepository.findById(studentId);
        return student;
    }

    public StudentDto createStudent(StudentDto student) {
        return studentRepository.save(student, null);
    }

    public List<StudentDto> getStudentsByProject(String projectId) {
        List<StudentDto> studentDtoList = studentRepository.findByProject(projectId);
        return new ArrayList<>(studentDtoList);
    }

    public void deleteStudentById(String studentId) {
        studentRepository.deleteById(studentId, false);
    }

    public StudentDto updateStudent(String studentId, StudentDto student) {
        Optional<StudentDto> optionalStudent = Optional.ofNullable(studentRepository.findById(studentId));
        if (optionalStudent.isPresent()) {
            studentRepository.deleteById(studentId, true);
            return studentRepository.save(student, studentId);
        } else {
            return null;
        }
    }

    public MembershipDto createMembership(String studentId, MembershipDto membership) {
        MembershipDto savedMembership = membershipRepository.save(membership, null);
        studentRepository.assignMembership(studentId, savedMembership.getId());
        return savedMembership;
    }
}
