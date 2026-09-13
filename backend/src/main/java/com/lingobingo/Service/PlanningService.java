package com.lingobingo.Service;

import com.lingobingo.Entity.Classroom;
import com.lingobingo.Entity.PlanningEvent;
import com.lingobingo.Entity.User;
import com.lingobingo.Repository.ClassroomRepository;
import com.lingobingo.Repository.EnrollmentRepository;
import com.lingobingo.Repository.PlanningEventRepository;
import com.lingobingo.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlanningService {

    private final PlanningEventRepository eventRepository;
    private final ClassroomRepository classroomRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;

    public PlanningEvent create(String teacherEmail, Long classId, String title,
                                String description, String type, LocalDateTime dueDate) {
        User teacher = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        Classroom classroom = classroomRepository.findById(classId)
                .orElseThrow(() -> new NoSuchElementException("Class not found"));
        if (classroom.getTeacher() == null || !classroom.getTeacher().getEmail().equals(teacherEmail)) {
            throw new SecurityException("Not your class");
        }
        PlanningEvent event = new PlanningEvent();
        event.setTitle(title);
        event.setDescription(description);
        event.setType(type);
        event.setDueDate(dueDate);
        event.setClassroom(classroom);
        event.setCreatedBy(teacher);
        return eventRepository.save(event);
    }

    public List<PlanningEvent> forStudent(String email) {
        List<Long> classIds = enrollmentRepository.findByStudentEmail(email).stream()
                .map(e -> e.getClassroom().getId())
                .collect(Collectors.toList());
        if (classIds.isEmpty()) return List.of();
        return eventRepository.findByClassroomIdInOrderByDueDateAsc(classIds);
    }

    public List<PlanningEvent> forTeacher(String email) {
        List<Long> classIds = classroomRepository.findByTeacherEmail(email).stream()
                .map(Classroom::getId)
                .collect(Collectors.toList());
        if (classIds.isEmpty()) return List.of();
        return eventRepository.findByClassroomIdInOrderByDueDateAsc(classIds);
    }

    public boolean delete(Long id, String email) {
        PlanningEvent event = eventRepository.findById(id).orElse(null);
        if (event == null) return false;
        if (event.getCreatedBy() == null || !event.getCreatedBy().getEmail().equals(email)) {
            throw new SecurityException("Not your event");
        }
        eventRepository.deleteById(id);
        return true;
    }
}
