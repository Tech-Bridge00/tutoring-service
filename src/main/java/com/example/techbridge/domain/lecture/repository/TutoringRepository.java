package com.example.techbridge.domain.lecture.repository;

import com.example.techbridge.domain.lecture.entity.Tutoring;
import com.example.techbridge.domain.member.entity.Member;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TutoringRepository extends JpaRepository<Tutoring, Long> {

    @Query("""
        SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END
        FROM Tutoring t
        WHERE t.requester = :requester
        AND t.requestStatus IN :statuses
        AND t.startTime < :endTime
        AND t.endTime > :startTime
        """)
    boolean isAlreadyExistedTutoringByRequester(
        @Param("requester") Member requester,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        @Param("statuses") List<Tutoring.RequestStatus> statuses
    );

    @Query("""
        SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END
        FROM Tutoring t
        WHERE t.receiver = :receiver
        AND t.requestStatus IN :statuses
        AND t.startTime < :endTime
        AND t.endTime > :startTime
        """)
    boolean isAlreadyExistedTutoringByReceiver(
        @Param("receiver") Member receiver,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        @Param("statuses") List<Tutoring.RequestStatus> statuses
    );
}
