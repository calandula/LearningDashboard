package com.example.learningdashboard.service;

import com.example.learningdashboard.dtos.MembershipDto;
import com.example.learningdashboard.dtos.StudentDto;
import com.example.learningdashboard.repository.MembershipRepository;
import com.example.learningdashboard.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MembershipService {

    @Autowired
    private MembershipRepository membershipRepository;

    public List<MembershipDto> getAllMemberships() {
        List<MembershipDto> membershipDtoList = membershipRepository.findAll();
        return new ArrayList<>(membershipDtoList);
    }

    public MembershipDto getMembershipById(String membershipId) {
        MembershipDto membership = membershipRepository.findById(membershipId);
        return membership;
    }

    public MembershipDto createMembership(MembershipDto membership) {
        return membershipRepository.save(membership, null);
    }

    public List<MembershipDto> getMembershipsByStudentInProject(String datasourceId, String studentId) {
        List<MembershipDto> membershipDtoList = membershipRepository.getMembershipsByStudentInProject(datasourceId, studentId);
        return new ArrayList<>(membershipDtoList);
    }

    public void deleteMembershipById(String membershipId) {
        membershipRepository.deleteById(membershipId, false);
    }

    public MembershipDto updateMembership(String membershipId, MembershipDto membership) {
        Optional<MembershipDto> optionalMembership = Optional.ofNullable(membershipRepository.findById(membershipId));
        if (optionalMembership.isPresent()) {
            membershipRepository.deleteById(membershipId, true);
            return membershipRepository.save(membership, membershipId);
        } else {
            return null;
        }
    }
}
