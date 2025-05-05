package com.example.techbridge.domain.tutoring.service;

import com.example.techbridge.domain.member.entity.Member;
import com.example.techbridge.domain.member.exception.MemberNotFoundException;
import com.example.techbridge.domain.member.exception.UnauthorizedException;
import com.example.techbridge.domain.member.repository.MemberRepository;
import com.example.techbridge.domain.tutoring.dto.TutoringRequest;
import com.example.techbridge.domain.tutoring.entity.Tutoring;
import com.example.techbridge.domain.tutoring.entity.Tutoring.RequestStatus;
import com.example.techbridge.domain.tutoring.exception.AlreadyProcessedTutoringRequestException;
import com.example.techbridge.domain.tutoring.exception.InvalidTutoringRequestException;
import com.example.techbridge.domain.tutoring.exception.InvalidTutoringStatusException;
import com.example.techbridge.domain.tutoring.exception.InvalidTutoringTimeException;
import com.example.techbridge.domain.tutoring.exception.TutoringAlreadyExistsException;
import com.example.techbridge.domain.tutoring.exception.TutoringNotFoundException;
import com.example.techbridge.domain.tutoring.repository.TutoringRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TutoringCommandService {

    private final EntityManager em;
    private final MemberRepository memberRepository;
    private final TutoringRepository tutoringRepository;

    // 과외 신청
    public void requestTutoring(TutoringRequest request, Long loginMemberId) {
        // 로그인한 사용자의 아이디가 과외 신청인 아이디와 동일한지 검증
        validateRequester(loginMemberId, request.getRequesterId());

        // 수신자 아이디 존재 여부 확인
        if (!memberRepository.existsById(request.getReceiverId())) {
            throw new MemberNotFoundException();
        }

        // 프록시 객체로 Member 조회해 쿼리 지연 처리
        Member requester = em.getReference(Member.class, request.getRequesterId());
        Member receiver = em.getReference(Member.class, request.getReceiverId());

        // 자기 자신한테 신청 불가능
        if (requester.getId().equals(receiver.getId())) {
            throw new InvalidTutoringRequestException();
        }

        // 시간 유효성 검증
        if (request.getStartTime().isAfter(request.getEndTime())
            || request.getStartTime().isBefore(LocalDateTime.now())
            || request.getEndTime().isBefore(LocalDateTime.now())) {
            throw new InvalidTutoringTimeException();
        }

        // 과외 신청자가 해당 시간대에 ACCEPTED 또는 IN_PROGRESS 상태의 과외가 이미 있는 경우 불가능
        if (tutoringRepository.isAlreadyExistedTutoringByRequester(
            request.getRequesterId(), request.getStartTime(), request.getEndTime(),
            RequestStatus.activeStatues()
        )) {
            throw new TutoringAlreadyExistsException();
        }

        // 과외 수신자가 해당 시간대에 ACCEPTED 또는 IN_PROGRESS 상태의 과외가 이미 있는 경우 불가능
        if (tutoringRepository.isAlreadyExistedTutoringByReceiver(request.getReceiverId(),
            request.getStartTime(),
            request.getEndTime(), RequestStatus.activeStatues())) {
            throw new TutoringAlreadyExistsException();
        }

        Tutoring tutoring = Tutoring.of(request, requester, receiver, RequestStatus.CREATED);
        tutoringRepository.save(tutoring);
    }

    // 과외 수락
    public void acceptTutoring(Long tutoringId, Long loginMemberId) {
        Tutoring tutoring = tutoringRepository.findById(tutoringId)
            .orElseThrow(TutoringNotFoundException::new);

        // 로그인한 사용자가 과외 요청 수신자인지 검증
        validateReceiver(loginMemberId, tutoring.getReceiver().getId());

        // CREATED 상태인 과외만 수락 가능
        if (!tutoring.getRequestStatus().canAcceptOrReject()) {
            throw new AlreadyProcessedTutoringRequestException();
        }

        tutoring.updateStatus(RequestStatus.ACCEPTED);
    }

    // 과외 거절
    public void rejectTutoring(Long tutoringId, Long loginMemberId) {
        Tutoring tutoring = tutoringRepository.findById(tutoringId)
            .orElseThrow(TutoringNotFoundException::new);

        // 로그인한 사용자가 과외 요청 수신자인지 검증
        validateReceiver(loginMemberId, tutoring.getReceiver().getId());

        // CREATED 상태인 과외만 거절 가능
        if (!tutoring.getRequestStatus().canAcceptOrReject()) {
            throw new AlreadyProcessedTutoringRequestException();
        }

        tutoring.updateStatus(RequestStatus.REJECTED);
    }

    // 과외 취소
    public void cancelTutoring(Long tutoringId, Long loginMemberId) {
        Tutoring tutoring = tutoringRepository.findById(tutoringId)
            .orElseThrow(TutoringNotFoundException::new);

        // 로그인한 사용자가 신청자 또는 수신자인지 검증
        validateSameMember(loginMemberId, tutoring.getRequester().getId(),
            tutoring.getReceiver().getId());

        // CREATED, ACCEPTED 경우에만 취소 가능
        if (!tutoring.getRequestStatus().canBeCanceled()) {
            throw new InvalidTutoringStatusException();
        }

        tutoring.updateStatus(RequestStatus.CANCELED);
    }

    private void validateSameMember(Long loginMemberId, Long requesterId, Long receiverId) {
        if (!loginMemberId.equals(requesterId) && !loginMemberId.equals(receiverId)) {
            throw new UnauthorizedException();
        }
    }

    private void validateRequester(Long loginMemberId, Long requesterId) {
        if (!loginMemberId.equals(requesterId)) {
            throw new UnauthorizedException();
        }
    }

    private void validateReceiver(Long loginMemberId, Long receiverId) {
        if (!loginMemberId.equals(receiverId)) {
            throw new UnauthorizedException();
        }
    }
}
