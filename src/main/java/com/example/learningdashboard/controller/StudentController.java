package com.example.learningdashboard.controller;

import com.example.learningdashboard.dtos.CategoryItemDto;
import com.example.learningdashboard.dtos.StudentDto;
import com.example.learningdashboard.service.CategoryItemService;
import com.example.learningdashboard.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/students")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @PostMapping
    public ResponseEntity<StudentDto> createStudent(@RequestBody StudentDto student) {
        StudentDto savedStudent = studentService.createStudent(student);
        return new ResponseEntity<>(savedStudent, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<StudentDto>> getAllStudents() {
        List<StudentDto> students = studentService.getAllStudents();
        return new ResponseEntity<>(students, HttpStatus.OK);
    }

    @GetMapping("{id}")
    public ResponseEntity<StudentDto> getStudentById(@PathVariable("id") String studentId) {
        StudentDto student = studentService.getStudentById(studentId);
        return new ResponseEntity<>(student, HttpStatus.OK);
    }

    @GetMapping("/project/{id}")
    public ResponseEntity<List<StudentDto>> getStudentsByProject(@PathVariable("id") String projectId) {
        List<StudentDto> students = studentService.getStudentsByProject(projectId);
        return new ResponseEntity<>(students, HttpStatus.OK);
    }

    @PutMapping("{id}")
    public ResponseEntity<StudentDto> updateStudent(@PathVariable("id") String studentId, @RequestBody StudentDto student) {
        StudentDto updatedStudent = studentService.updateStudent(studentId, student);
        return new ResponseEntity<>(updatedStudent, HttpStatus.OK);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable("id") String studentId) {
        studentService.deleteStudentById(studentId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
