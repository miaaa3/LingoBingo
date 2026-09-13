package com.lingobingo.Service;

import com.lingobingo.Dto.BadgeDto;
import com.lingobingo.Dto.DashboardDto;
import com.lingobingo.Dto.LeaderboardEntryDto;
import com.lingobingo.Dto.PlanningEventDto;
import com.lingobingo.Entity.Classroom;
import com.lingobingo.Entity.Enrollment;
import com.lingobingo.Entity.QuizResult;
import com.lingobingo.Entity.User;
import com.lingobingo.Repository.EnrollmentRepository;
import com.lingobingo.Repository.PlanningEventRepository;
import com.lingobingo.Repository.QuizResultRepository;
import com.lingobingo.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsService {

    private static final int XP_PER_LEVEL = 500;

    private final QuizResultRepository quizResultRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final PlanningEventRepository planningEventRepository;

    @Transactional(readOnly = true)
    public DashboardDto forStudent(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        List<QuizResult> myResults = quizResultRepository.findByStudentEmail(email);
        int points = myResults.stream().mapToInt(QuizResult::getScore).sum();
        int quizzesDone = myResults.size();
        int level = points / XP_PER_LEVEL + 1;
        int xpIntoLevel = points % XP_PER_LEVEL;
        int streak = computeStreak(myResults);

        String className = null;
        int rank = 0;
        List<LeaderboardEntryDto> leaderboard = new ArrayList<>();

        List<Enrollment> myEnrollments = enrollmentRepository.findByStudentEmail(email);
        if (!myEnrollments.isEmpty()) {
            Classroom cls = myEnrollments.get(0).getClassroom();
            className = cls.getName();
            List<LeaderboardEntryDto> entries = new ArrayList<>();
            for (Enrollment en : enrollmentRepository.findByClassroomId(cls.getId())) {
                User s = en.getStudent();
                int p = quizResultRepository.findByStudentEmail(s.getEmail()).stream()
                        .mapToInt(QuizResult::getScore).sum();
                entries.add(new LeaderboardEntryDto(0, s.getName(), p, s.getEmail().equals(email)));
            }
            entries.sort((a, b) -> Integer.compare(b.getPoints(), a.getPoints()));
            for (int i = 0; i < entries.size(); i++) {
                entries.get(i).setRank(i + 1);
                if (entries.get(i).isMe()) rank = i + 1;
            }
            leaderboard = entries.stream().limit(5).collect(Collectors.toList());
        }

        boolean perfect = myResults.stream().anyMatch(r -> r.getMaxScore() > 0 && r.getScore() == r.getMaxScore());
        List<BadgeDto> badges = List.of(
                new BadgeDto("first_quiz", "First quiz", quizzesDone >= 1),
                new BadgeDto("streak_7", "7-day streak", streak >= 7),
                new BadgeDto("perfect", "Perfect score", perfect),
                new BadgeDto("top3", "Top 3", rank >= 1 && rank <= 3)
        );

        List<PlanningEventDto> upcoming = new ArrayList<>();
        List<Long> classIds = myEnrollments.stream()
                .map(e -> e.getClassroom().getId()).collect(Collectors.toList());
        if (!classIds.isEmpty()) {
            upcoming = planningEventRepository.findByClassroomIdInOrderByDueDateAsc(classIds).stream()
                    .limit(4)
                    .map(e -> new PlanningEventDto(e.getId(), e.getTitle(), e.getDescription(), e.getType(),
                            e.getDueDate(),
                            e.getClassroom() != null ? e.getClassroom().getId() : null,
                            e.getClassroom() != null ? e.getClassroom().getName() : null))
                    .collect(Collectors.toList());
        }

        return new DashboardDto(user.getName(), points, level, xpIntoLevel, XP_PER_LEVEL,
                streak, quizzesDone, rank, className, badges, leaderboard, upcoming);
    }

    private int computeStreak(List<QuizResult> results) {
        Set<LocalDate> days = results.stream()
                .filter(r -> r.getSubmittedAt() != null)
                .map(r -> r.getSubmittedAt().toLocalDate())
                .collect(Collectors.toSet());
        if (days.isEmpty()) return 0;
        LocalDate today = LocalDate.now();
        LocalDate cursor;
        if (days.contains(today)) cursor = today;
        else if (days.contains(today.minusDays(1))) cursor = today.minusDays(1);
        else return 0;
        int streak = 0;
        while (days.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }
}
