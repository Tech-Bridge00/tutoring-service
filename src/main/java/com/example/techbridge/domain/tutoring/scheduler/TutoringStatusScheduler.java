package com.example.techbridge.domain.tutoring.scheduler;

import com.example.techbridge.domain.tutoring.entity.Tutoring;
import com.example.techbridge.domain.tutoring.entity.Tutoring.RequestStatus;
import com.example.techbridge.domain.tutoring.repository.TutoringRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class TutoringStatusScheduler {

    private final TutoringRepository tutoringRepository;

    @Scheduled(fixedRate = 60_000) // 1분마다 실행
    @Transactional
    public void updateTutoringStatusByTime() {
        LocalDateTime now = LocalDateTime.now();

        // ACCEPTED 과외가 시작 시간이 된 경우 -> IN_PROGRESS
        List<Tutoring> startList = tutoringRepository.findAcceptedList(now);
        startList.forEach(tutoring -> tutoring.updateStatus(RequestStatus.IN_PROGRESS));

        // IN_PROGRESS 과외가 종료 시간이 된 경우 -> COMPLETED
        List<Tutoring> endList = tutoringRepository.findInProgressList(now);
        endList.forEach(tutoring -> tutoring.updateStatus(RequestStatus.COMPLETED));

        // CREATED 과외가 시작 시간에도 그대로인 경우 -> CANCELED
        List<Tutoring> cancelList = tutoringRepository.findCreatedAndExpiredList(now);
        cancelList.forEach(tutoring -> tutoring.updateStatus(RequestStatus.CANCELED));
    }
}
