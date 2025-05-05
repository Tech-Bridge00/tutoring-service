package com.example.techbridge.domain.tutoring.service;

import com.example.techbridge.domain.member.entity.Member.Role;
import com.example.techbridge.domain.member.exception.MemberNotFoundException;
import com.example.techbridge.domain.member.repository.MemberRepository;
import com.example.techbridge.domain.tutoring.dto.ReceiveTutoringSimpleResponse;
import com.example.techbridge.domain.tutoring.dto.RequestTutoringSimpleResponse;
import com.example.techbridge.domain.tutoring.entity.Tutoring;
import com.example.techbridge.domain.tutoring.entity.Tutoring.RequestStatus;
import com.example.techbridge.domain.tutoring.repository.TutoringRepository;
import jakarta.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TutoringQueryService {

    private final TutoringRepository tutoringRepository;
    private final MemberRepository memberRepository;

    public Page<RequestTutoringSimpleResponse> getSentTutoringList(
        Long loginId, @Nullable RequestStatus status, Pageable pageable) {
        Role role = getMemberRole(loginId);

        return toPage(
            tutoringRepository.findPageIdListByRequester(loginId, status, pageable),
            (role == Role.TUTOR)
                ? tutoringRepository::fetchReceiverStudent
                : tutoringRepository::fetchReceiverTutor,
            RequestTutoringSimpleResponse::from,
            pageable);
    }

    public Page<ReceiveTutoringSimpleResponse> getReceivedTutoringList(
        Long loginId, @Nullable RequestStatus status, Pageable pageable) {
        Role role = getMemberRole(loginId);

        return toPage(
            tutoringRepository.findPageIdListByReceiver(loginId, status, pageable),
            (role == Role.TUTOR)
                ? tutoringRepository::fetchRequesterStudent
                : tutoringRepository::fetchRequesterTutor,
            ReceiveTutoringSimpleResponse::from,
            pageable);
    }

    // 역할 조회
    private Role getMemberRole(Long id) {
        return memberRepository.findRoleOnlyById(id)
            .orElseThrow(MemberNotFoundException::new);
    }

    // Tutoring 리스트 정렬 및 Page 객체로 변환
    private <R> Page<R> toPage(
        Page<Long> idPage,
        Function<Collection<Long>, List<Tutoring>> fetchFunction,
        Function<Tutoring, R> mapper,
        Pageable pageable) {

        List<Long> idList = idPage.getContent();
        if (idList.isEmpty()) {
            return Page.empty(pageable);
        }

        Map<Long, Tutoring> map = fetchFunction.apply(idList).stream()
            .collect(Collectors.toMap(Tutoring::getId, Function.identity()));

        List<R> content = idList.stream()
            .map(map::get)
            .filter(Objects::nonNull)
            .map(mapper)
            .toList();

        return new PageImpl<>(content, pageable, idPage.getTotalElements());
    }
}
