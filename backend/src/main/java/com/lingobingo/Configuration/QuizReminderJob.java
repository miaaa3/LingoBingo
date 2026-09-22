package com.lingobingo.Configuration;

import com.lingobingo.Entity.ClassContent;
import com.lingobingo.Entity.Enrollment;
import com.lingobingo.Entity.Quiz;
import com.lingobingo.Entity.User;
import com.lingobingo.Repository.ClassContentRepository;
import com.lingobingo.Repository.EnrollmentRepository;
import com.lingobingo.Repository.QuizRepository;
import com.lingobingo.Service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Once a minute, find quizzes falling due within the next hour and remind the
 * students they are assigned to - exactly once per quiz. The `dueSoonNotified`
 * flag makes it idempotent, mirroring how QuizDeadlineJob handles the close.
 */
@Component
@RequiredArgsConstructor
public class QuizReminderJob {

    private final QuizRepository quizRepository;
    private final ClassContentRepository classContentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final NotificationService notificationService;

    @Scheduled(fixedRate = 60000)
    public void remindDueSoon() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime inOneHour = now.plusHours(1);
        List<Quiz> soon = quizRepository.findByDueDateBetweenAndDueSoonNotifiedFalse(now, inOneHour);
        for (Quiz quiz : soon) {
            quiz.setDueSoonNotified(true);
            quizRepository.save(quiz);
            remindStudents(quiz);
        }
    }

    /** Notify each student the quiz is assigned to, de-duplicated across classes. */
    private void remindStudents(Quiz quiz) {
        List<ClassContent> assignments = classContentRepository.findByContentTypeAndContentId("QUIZ", quiz.getId());
        if (assignments.isEmpty()) {
            return;
        }
        String message = "Due soon: \"" + quiz.getName() + "\" is due within the hour.";
        String link = "/Quizzes/" + quiz.getId() + "/take";
        Set<Long> notified = new HashSet<>();
        for (ClassContent cc : assignments) {
            if (cc.getClassroom() == null) {
                continue;
            }
            for (Enrollment e : enrollmentRepository.findByClassroomId(cc.getClassroom().getId())) {
                User student = e.getStudent();
                if (student == null || student.getId() == null) {
                    continue;
                }
                if (notified.add(student.getId())) {
                    notificationService.notify(student, "QUIZ_DUE_SOON", message, link);
                }
            }
        }
    }
}
