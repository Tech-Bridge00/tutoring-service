package com.example.techbridge.domain.tutoring.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.techbridge.domain.member.entity.Member;
import com.example.techbridge.domain.member.repository.MemberRepository;
import com.example.techbridge.domain.member.repository.StudentRepository;
import com.example.techbridge.domain.member.repository.TutorRepository;
import com.example.techbridge.domain.member.support.AbstractMemberTestSupport;
import com.example.techbridge.domain.tutoring.entity.Tutoring;
import com.example.techbridge.domain.tutoring.entity.Tutoring.RequestStatus;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class TutoringRepositoryTest extends AbstractMemberTestSupport {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    TutorRepository tutorRepository;

    @Autowired
    TutoringRepository tutoringRepository;

    @Autowired
    EntityManager em;

    private Member student;
    private Member tutor;

    @BeforeEach
    void init() {
        // 학생
        var studentWrapper = initStudent();
        student = memberRepository.save(studentWrapper.toMemberEntity());
        studentRepository.save(studentWrapper.toStudentEntity(student));

        // 튜터
        var tutorWrapper = initTutor();
        tutor = memberRepository.save(tutorWrapper.toMemberEntity());
        tutorRepository.save(tutorWrapper.toTutorEntity(tutor));
    }

    @Test
    void exists_returns_true_when_tutoring_already_exists() {
        // given
        LocalDateTime testTime = LocalDateTime.now();
        tutoringRepository.save(Tutoring.builder()
            .requester(student)
            .receiver(tutor)
            .requestStatus(RequestStatus.ACCEPTED)
            .startTime(testTime.plusHours(1))
            .endTime(testTime.plusHours(2))
            .build());

        em.flush();
        em.clear();

        // when
        boolean exists = tutoringRepository.isAlreadyExistedTutoringByRequester(
            student.getId(),
            testTime,
            testTime.plusHours(3),
            RequestStatus.activeStatues()
        );

        // then
        assertThat(exists).isTrue();
    }

    @Test
    void findPageIdListByRequester_returns_idList_in_desc_order() {
        // given : 정렬 검증용
        LocalDateTime testTime = LocalDateTime.now();
        Tutoring first = tutoringRepository.save(Tutoring.builder()
            .requester(student).receiver(tutor).requestStatus(RequestStatus.ACCEPTED)
            .startTime(testTime.plusHours(1)).endTime(testTime.plusHours(2)).build());
        Tutoring second = tutoringRepository.save(Tutoring.builder()
            .requester(student).receiver(tutor).requestStatus(RequestStatus.ACCEPTED)
            .startTime(testTime.plusHours(3)).endTime(testTime.plusHours(4)).build());

        em.flush();
        em.clear();

        // when
        Page<Long> page = tutoringRepository.findPageIdListByRequester(
            student.getId(),
            RequestStatus.ACCEPTED,
            PageRequest.of(0, 10)
        );

        // then
        assertThat(page.getContent())
            .containsExactly(second.getId(), first.getId());
        assertThat(page.getTotalElements()).isEqualTo(2);
    }

    @Test
    void fetchReceiverTutor_fetches_tutor() {
        // given
        Tutoring tutoring = tutoringRepository.save(Tutoring.builder()
            .requester(student).receiver(tutor).requestStatus(RequestStatus.ACCEPTED)
            .startTime(LocalDateTime.now().plusHours(1))
            .endTime(LocalDateTime.now().plusHours(2))
            .build());

        em.flush();
        em.clear();

        // when
        List<Tutoring> list =
            tutoringRepository.fetchReceiverTutor(List.of(tutoring.getId()));

        // then
        assertThat(list).hasSize(1);
        assertThat(list.get(0).getReceiver().getTutor()).isNotNull();
    }
}
