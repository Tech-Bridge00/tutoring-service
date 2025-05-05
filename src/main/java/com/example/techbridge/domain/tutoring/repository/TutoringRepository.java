package com.example.techbridge.domain.tutoring.repository;

import com.example.techbridge.domain.member.entity.Member;
import com.example.techbridge.domain.tutoring.entity.Tutoring;
import com.example.techbridge.domain.tutoring.entity.Tutoring.RequestStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    // 로그인한 사용자가 과외 신청한 목록을 ID로 조회 및 시간순으로 정렬
    @Query("""
        SELECT t.id FROM Tutoring t
        WHERE t.requester.id = :requesterId
        AND (:status IS NULL OR t.requestStatus = :status)
        ORDER BY t.startTime DESC
        """)
    Page<Long> findTutoringPagesByRequesterIdAndStatus(@Param("requesterId") Long requesterId,
        @Param("status") RequestStatus status,
        Pageable pageable);

    // 로그인한 사용자가 과외 신청을 받은 목록을 ID로 조회 및 시간순으로 정렬
    @Query("""
        SELECT t.id FROM Tutoring t
        WHERE t.receiver.id = :receiverId
        AND (:status IS NULL OR t.requestStatus = :status)
        ORDER BY t.startTime DESC
        """)
    Page<Long> findTutoringPagesByReceiverIdAndStatus(@Param("receiverId") Long receiverId,
        @Param("status") RequestStatus status,
        Pageable pageable);

    // Tutor -> Student 과외 신청 목록
    @Query("""
        SELECT t FROM Tutoring t
        JOIN FETCH t.receiver r
        JOIN FETCH r.student
        WHERE t.id IN :ids
        """)
    List<Tutoring> findWithReceiverStudentByIds(@Param("ids") List<Long> ids);

    // Tutor -> Student 과외 신청 받은 목록
    @Query("""
        SELECT t FROM Tutoring t
        JOIN FETCH t.requester r
        JOIN FETCH r.student
        WHERE t.id IN :ids
        """)
    List<Tutoring> findWithRequesterStudentByIds(@Param("ids") List<Long> ids);

    // Student -> Tutor 과외 신청 목록
    @Query("""
        SELECT t FROM Tutoring t
        JOIN FETCH t.receiver r
        JOIN FETCH r.tutor
        WHERE t.id IN :ids
        """)
    List<Tutoring> findWithReceiverTutorByIds(@Param("ids") List<Long> ids);

    // student -> tutor 과외 신청 받은 목록
    @Query("""
        SELECT t FROM Tutoring t
        JOIN FETCH t.requester r
        JOIN FETCH r.tutor
        WHERE t.id IN :ids
        """)
    List<Tutoring> findWithRequesterTutorByIds(@Param("ids") List<Long> ids);
}
