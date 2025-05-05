package com.example.techbridge.domain.member.repository;

import com.example.techbridge.domain.member.entity.Member;
import com.example.techbridge.domain.member.entity.Member.Role;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, Long> {

    @EntityGraph(attributePaths = {"student", "tutor"})
    Optional<Member> findByUsername(String username);

    @EntityGraph(attributePaths = {"student", "tutor"})
    Optional<Member> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @EntityGraph(attributePaths = {"student", "tutor"})
    Optional<Member> findWithDetailsById(@Param("id") Long id);

    @EntityGraph(attributePaths = {"student", "tutor"})
    Page<Member> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"student", "tutor"})
    Page<Member> findByRole(Role role, Pageable pageable);

    @Query("select m.role from Member m where m.id = :id")
    Optional<Role> findRoleOnlyById(@Param("id") Long id);
}
