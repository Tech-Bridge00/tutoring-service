package com.example.techbridge.domain.member.repository;

import com.example.techbridge.domain.member.entity.Tutor;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TutorRepository extends JpaRepository<Tutor, Long> {

    Optional<Tutor> findByMember_Id(Long memberId);
}
