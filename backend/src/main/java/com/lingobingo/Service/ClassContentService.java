package com.lingobingo.Service;

import com.lingobingo.Dto.ClassContentDto;
import com.lingobingo.Entity.ClassContent;
import com.lingobingo.Entity.Classroom;
import com.lingobingo.Entity.Enrollment;
import com.lingobingo.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassContentService {

    private final ClassContentRepository classContentRepository;
    private final ClassroomRepository classroomRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final QuizRepository quizRepository;
    private final LessonRepository lessonRepository;
    private final FlashcardSetRepository flashcardSetRepository;
    private final NotificationService notificationService;

    public ClassContent assign(String teacherEmail, Long classId, String contentType, Long contentId) {
        Classroom classroom = classroomRepository.findById(classId)
                .orElseThrow(() -> new NoSuchElementException("Class not found"));
        if (classroom.getTeacher() == null || !classroom.getTeacher().getEmail().equals(teacherEmail)) {
            throw new SecurityException("Not your class");
        }
        if (classContentRepository.existsByClassroomIdAndContentTypeAndContentId(classId, contentType, contentId)) {
            return null; // already assigned
        }
        ClassContent cc = new ClassContent();
        cc.setClassroom(classroom);
        cc.setContentType(contentType);
        cc.setContentId(contentId);
        ClassContent saved = classContentRepository.save(cc);
        notifyStudents(classroom, contentType, contentId);
        return saved;
    }

    /** Tell every enrolled student that new content just landed in their class. */
    private void notifyStudents(Classroom classroom, String contentType, Long contentId) {
        String title = resolveTitle(contentType, contentId);
        String kind = "QUIZ".equals(contentType) ? "quiz"
                : "FLASHCARD_SET".equals(contentType) ? "flashcard set"
                : "COURSE".equals(contentType) ? "course" : "item";
        String message = "New " + kind + " in " + classroom.getName() + ": " + title;
        String link = linkFor(contentType, contentId);
        for (Enrollment e : enrollmentRepository.findByClassroomId(classroom.getId())) {
            notificationService.notify(e.getStudent(), "NEW_" + contentType, message, link);
        }
    }

    private String linkFor(String type, Long id) {
        if (type == null || id == null) { return "/Home"; }
        switch (type) {
            case "QUIZ":          return "/Quizzes/" + id + "/take";
            case "FLASHCARD_SET": return "/Display-set-of-flashcard/" + id;
            case "COURSE":        return "/Courses/" + id;
            default:              return "/Home";
        }
    }

    public boolean unassign(Long id, String teacherEmail) {
        ClassContent cc = classContentRepository.findById(id).orElse(null);
        if (cc == null) return false;
        Classroom classroom = cc.getClassroom();
        if (classroom == null || classroom.getTeacher() == null
                || !classroom.getTeacher().getEmail().equals(teacherEmail)) {
            throw new SecurityException("Not your class");
        }
        classContentRepository.deleteById(id);
        return true;
    }

    public List<ClassContentDto> forClass(Long classId) {
        return classContentRepository.findByClassroomId(classId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<ClassContentDto> forStudent(String email) {
        List<Long> classIds = enrollmentRepository.findByStudentEmail(email).stream()
                .map(e -> e.getClassroom().getId())
                .collect(Collectors.toList());
        if (classIds.isEmpty()) return List.of();
        return classContentRepository.findByClassroomIdIn(classIds).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private ClassContentDto toDto(ClassContent cc) {
        String title = resolveTitle(cc.getContentType(), cc.getContentId());
        Long classId = (cc.getClassroom() != null) ? cc.getClassroom().getId() : null;
        String className = (cc.getClassroom() != null) ? cc.getClassroom().getName() : null;
        return new ClassContentDto(cc.getId(), cc.getContentType(), cc.getContentId(),
                title, classId, className, cc.getAssignedAt());
    }

    private String resolveTitle(String type, Long id) {
        if (type == null || id == null) return "Unknown";
        switch (type) {
            case "QUIZ":
                return quizRepository.findById(id).map(q -> q.getName()).orElse("Deleted quiz");
            case "COURSE":
                return lessonRepository.findById(id).map(l -> l.getTitle()).orElse("Deleted course");
            case "FLASHCARD_SET":
                return flashcardSetRepository.findById(id).map(f -> f.getTitle()).orElse("Deleted set");
            default:
                return "Unknown";
        }
    }
}
