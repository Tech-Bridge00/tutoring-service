package com.example.techbridge.domain.member.repository;

import com.example.techbridge.domain.member.entity.Student;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByMemberId(Long memberId);
}
