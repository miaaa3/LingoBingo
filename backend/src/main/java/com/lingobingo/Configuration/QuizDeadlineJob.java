package com.lingobingo.Configuration;

import com.lingobingo.Entity.Quiz;
import com.lingobingo.Repository.QuizRepository;
import com.lingobingo.Service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Once a minute, find quizzes whose deadline has just passed and tell the owning
 * teacher, exactly once, that the quiz has closed. The `closedNotified` flag on
 * the quiz makes this idempotent.
 */
@Component
@RequiredArgsConstructor
public class QuizDeadlineJob {

    private final QuizRepository quizRepository;
    private final NotificationService notificationService;

    @Scheduled(fixedRate = 60000)
    public void notifyClosedQuizzes() {
        List<Quiz> due = quizRepository.findByDueDateBeforeAndClosedNotifiedFalse(LocalDateTime.now());
        for (Quiz q : due) {
            q.setClosedNotified(true);
            quizRepository.save(q);
            if (q.getCreatedBy() != null) {
                notificationService.notify(q.getCreatedBy(), "QUIZ_CLOSED",
                        "Your quiz \"" + q.getName() + "\" has closed.",
                        "/Quizzes/" + q.getId() + "/gradebook");
            }
        }
    }
}
