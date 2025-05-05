package com.example.techbridge.domain.tutoring.repository;

import com.example.techbridge.domain.tutoring.entity.Tutoring;
import com.example.techbridge.domain.tutoring.entity.Tutoring.RequestStatus;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TutoringRepository extends JpaRepository<Tutoring, Long> {

    @Query("""
        SELECT EXISTS (
            SELECT 1
            FROM Tutoring t
            WHERE t.requester.id = :requesterId
            AND t.requestStatus IN (:statuses)
            AND t.startTime < :endTime
            AND t.endTime   > :startTime
        )
        """)
    boolean isAlreadyExistedTutoringByRequester(
        @Param("requesterId") Long requesterId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        @Param("statuses") List<Tutoring.RequestStatus> statuses
    );

    @Query("""
        SELECT EXISTS (
            SELECT 1
            FROM Tutoring t
            WHERE t.receiver.id = :receiverId
            AND t.requestStatus IN (:statuses)
            AND t.startTime < :endTime
            AND t.endTime   > :startTime
        )
        """)
    boolean isAlreadyExistedTutoringByReceiver(
        @Param("receiverId") Long receiverId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        @Param("statuses") List<Tutoring.RequestStatus> statuses
    );

    // 로그인한 사용자가 과외 신청한 목록을 ID로 조회 및 시간순으로 정렬
    @Query(value = """
        SELECT t.id
        FROM Tutoring t
        WHERE t.requester.id = :requesterId
            AND (:status IS NULL OR t.requestStatus = :status)
        ORDER BY t.startTime DESC
        """,
        countQuery = """
            SELECT COUNT(t)
            FROM Tutoring t
            WHERE t.requester.id = :requesterId
                AND (:status IS NULL OR t.requestStatus = :status)
            """)
    Page<Long> findPageIdListByRequester(@Param("requesterId") Long requesterId,
        @Param("status") RequestStatus status,
        Pageable pageable);

    // 로그인한 사용자가 과외 신청을 받은 목록을 ID로 조회 및 시간순으로 정렬
    @Query(value = """
        SELECT t.id
        FROM Tutoring t
        WHERE t.receiver.id = :receiverId
            AND (:status IS NULL OR t.requestStatus = :status)
        ORDER BY t.startTime DESC
        """,
        countQuery = """
            SELECT COUNT(t)
            FROM Tutoring t
            WHERE t.receiver.id = :receiverId
                AND (:status IS NULL OR t.requestStatus = :status)
            """)
    Page<Long> findPageIdListByReceiver(@Param("receiverId") Long receiverId,
        @Param("status") RequestStatus status,
        Pageable pageable);

    // Tutor -> Student 과외 신청 목록
    @Query("""
        SELECT DISTINCT t
        FROM Tutoring t
        JOIN FETCH t.receiver r
        JOIN FETCH r.student
        LEFT JOIN FETCH r.tutor
        WHERE t.id IN (:ids)
        """)
    List<Tutoring> fetchReceiverStudent(@Param("ids") Collection<Long> ids);

    // Tutor -> Student 과외 신청 받은 목록
    @Query("""
        SELECT DISTINCT t
        FROM Tutoring t
        JOIN FETCH t.requester r
        JOIN FETCH r.student
        LEFT JOIN FETCH r.tutor
        WHERE t.id IN (:ids)
        """)
    List<Tutoring> fetchRequesterStudent(@Param("ids") Collection<Long> ids);

    // Student -> Tutor 과외 신청 목록
    @Query("""
        SELECT DISTINCT t
        FROM Tutoring t
        JOIN FETCH t.receiver r
        JOIN FETCH r.tutor
        LEFT JOIN FETCH r.student
        WHERE t.id IN (:ids)
        """)
    List<Tutoring> fetchReceiverTutor(@Param("ids") Collection<Long> ids);

    // student -> tutor 과외 신청 받은 목록
    @Query("""
        SELECT DISTINCT t
        FROM Tutoring t
        JOIN FETCH t.requester r
        JOIN FETCH r.tutor
        LEFT JOIN FETCH r.student
        WHERE t.id IN (:ids)
        """)
    List<Tutoring> fetchRequesterTutor(@Param("ids") Collection<Long> ids);

    @Query("""
        SELECT t FROM Tutoring t
        WHERE t.requestStatus = 'ACCEPTED'
        AND t.startTime <= :now
        """)
    List<Tutoring> findAcceptedList(@Param("now") LocalDateTime now);

    @Query("""
        SELECT t FROM Tutoring t
        WHERE t.requestStatus = 'IN_PROGRESS'
        AND t.endTime <= :now
        """)
    List<Tutoring> findInProgressList(@Param("now") LocalDateTime now);

    @Query("""
        SELECT t FROM Tutoring t
        WHERE t.requestStatus = 'CREATED'
        AND t.startTime <= :now
        """)
    List<Tutoring> findCreatedAndExpiredList(@Param("now") LocalDateTime now);
}
