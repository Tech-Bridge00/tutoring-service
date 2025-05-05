package com.example.techbridge.domain.tutoring.service;

import com.example.techbridge.domain.member.entity.Member;
import com.example.techbridge.domain.member.entity.Member.Role;
import com.example.techbridge.domain.member.exception.MemberNotFoundException;
import com.example.techbridge.domain.member.repository.MemberRepository;
import com.example.techbridge.domain.tutoring.dto.ReceiveTutoringSimpleResponse;
import com.example.techbridge.domain.tutoring.dto.RequestTutoringSimpleResponse;
import com.example.techbridge.domain.tutoring.dto.TutoringListType;
import com.example.techbridge.domain.tutoring.dto.TutoringSimpleResponse;
import com.example.techbridge.domain.tutoring.entity.Tutoring;
import com.example.techbridge.domain.tutoring.exception.InvalidTutoringRequestTypeException;
import com.example.techbridge.domain.tutoring.repository.TutoringRepository;
import java.util.List;
import java.util.Map;
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

    public Page<? extends TutoringSimpleResponse> getTutoringList(
        Long loginMemberId,
        TutoringListType type,
        Pageable pageable
    ) {
        // 로그인한 회원 Role 조회
        Member member = memberRepository.findById(loginMemberId)
            .orElseThrow(MemberNotFoundException::new);
        Role memberRole = member.getRole();

        switch (type) {
            case SENT -> {
                // 신청한 과외 목록 ID 페이징
                Page<Long> idPage = tutoringRepository.findTutoringPagesByRequesterId(loginMemberId,
                    pageable);
                List<Long> idList = idPage.getContent();

                // 로그인한 회원 role에 따라 수신자 정보 조회
                List<Tutoring> tutoringList = (memberRole == Role.TUTOR)
                    ? tutoringRepository.findWithReceiverStudentByIds(idList)
                    : tutoringRepository.findWithReceiverTutorByIds(idList);

                // dto 변환 후 페이지로 반환
                return toPage(idList, tutoringList, RequestTutoringSimpleResponse::from, pageable,
                    idPage.getTotalElements());
            }
            case RECEIVED -> {
                // 신청 받은 과외 목록 ID 페이징
                Page<Long> idPage = tutoringRepository.findTutoringPagesByReceiverId(loginMemberId,
                    pageable);
                List<Long> idList = idPage.getContent();

                // 로그인한 회원 role에 따라 수신자 정보 조회
                List<Tutoring> tutoringList = (memberRole == Role.TUTOR)
                    ? tutoringRepository.findWithRequesterStudentByIds(idList)
                    : tutoringRepository.findWithRequesterTutorByIds(idList);

                // dto 변환 후 페이지로 반환
                return toPage(idList, tutoringList, ReceiveTutoringSimpleResponse::from, pageable,
                    idPage.getTotalElements());
            }

            // sent, received 타입 검증 에러 반환
            default -> throw new InvalidTutoringRequestTypeException();
        }
    }

    // Tutoring 리스트 정렬 및 Page 객체로 변환
    private <T extends TutoringSimpleResponse> Page<T> toPage(
        List<Long> idList,
        List<Tutoring> tutoringList,
        Function<Tutoring, T> mapper,
        Pageable pageable,
        long total
    ) {
        // id -> tutoring 매핑
        Map<Long, Tutoring> tutoringMap = tutoringList.stream()
            .collect(Collectors.toMap(Tutoring::getId, Function.identity()));

        // id 기준으로 dto 매핑
        List<T> content = idList.stream()
            .map(tutoringMap::get)
            .map(mapper)
            .toList();

        return new PageImpl<>(content, pageable, total);
    }
}
