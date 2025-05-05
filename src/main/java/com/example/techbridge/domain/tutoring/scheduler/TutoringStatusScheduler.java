package com.example.techbridge.domain.tutoring.scheduler;

import com.example.techbridge.domain.tutoring.repository.TutoringRepository;
import java.time.LocalDateTime;
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
        int started = tutoringRepository.bulkStart(now);

        // IN_PROGRESS 과외가 종료 시간이 된 경우 -> COMPLETED
        int completed = tutoringRepository.bulkComplete(now);

        // CREATED 과외가 시작 시간에도 그대로인 경우 -> CANCELED
        int canceled = tutoringRepository.bulkCancel(now);

        log.debug("status updated: started={}, completed={}, canceled={}", started, completed,
            canceled);
    }
}
