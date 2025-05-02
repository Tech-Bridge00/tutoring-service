package com.example.techbridge.domain.member.service;

import com.example.techbridge.domain.member.dto.StudentInfoRequest;
import com.example.techbridge.domain.member.dto.StudentUpdateRequest;
import com.example.techbridge.domain.member.entity.Member;
import com.example.techbridge.domain.member.entity.Student;
import com.example.techbridge.domain.member.exception.MemberNotFoundException;
import com.example.techbridge.domain.member.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentService {

    private final StudentRepository studentRepository;

    public Student saveStudentInfo(Member member, StudentInfoRequest request) {
        Student student = Student.builder()
            .member(member)
            .interestedField(request.getInterestedField())
            .status(request.getStatus())
            .build();

        member.setStudent(student);

        return studentRepository.save(student);
    }

    @Transactional
    public void updateStudentInfo(Member member, StudentUpdateRequest request) {
        Student student = studentRepository.findByMemberId(member.getId())
            .orElseThrow(MemberNotFoundException::new);

        student.updateProfile(request);
    }
}
