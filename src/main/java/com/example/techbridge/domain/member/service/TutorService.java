package com.example.techbridge.domain.member.service;

import com.example.techbridge.domain.member.dto.TutorInfoRequest;
import com.example.techbridge.domain.member.dto.TutorUpdateRequest;
import com.example.techbridge.domain.member.entity.Member;
import com.example.techbridge.domain.member.entity.Tutor;
import com.example.techbridge.domain.member.exception.MemberNotFoundException;
import com.example.techbridge.domain.member.repository.TutorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TutorService {

    private final TutorRepository tutorRepository;

    public Tutor saveTutorInfo(Member member, TutorInfoRequest request) {
        Tutor tutor = Tutor.builder()
            .member(member)
            .introduction(request.getIntroduction())
            .jobTitle(request.getJobTitle())
            .portfolioUrl(request.getPortfolioUrl())
            .totalExperience(request.getTotalExperience())
            .currentlyEmployed(request.getCurrentlyEmployed())
            .build();

        member.setTutor(tutor);

        return tutorRepository.save(tutor);
    }

    @Transactional
    public void updateTutorInfo(Member member, TutorUpdateRequest request) {
        Tutor tutor = tutorRepository.findByMemberId(member.getId())
            .orElseThrow(MemberNotFoundException::new);

        tutor.updateProfile(request);
    }
}
