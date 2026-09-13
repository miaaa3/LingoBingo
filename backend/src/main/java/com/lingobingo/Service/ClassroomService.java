package com.lingobingo.Service;

import com.lingobingo.Entity.Classroom;
import com.lingobingo.Entity.Enrollment;
import com.lingobingo.Entity.User;
import com.lingobingo.Repository.ClassroomRepository;
import com.lingobingo.Repository.EnrollmentRepository;
import com.lingobingo.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassroomService {

    private static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // no ambiguous chars
    private static final int CODE_LENGTH = 6;
    private final SecureRandom random = new SecureRandom();

    private final ClassroomRepository classroomRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;

    public Classroom createClass(String teacherEmail, String name, String description) {
        User teacher = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new NoSuchElementException("Teacher not found"));
        Classroom classroom = new Classroom();
        classroom.setName(name);
        classroom.setDescription(description);
        classroom.setTeacher(teacher);
        classroom.setJoinCode(generateUniqueCode());
        return classroomRepository.save(classroom);
    }

    // Returns the enrollment, or null if the student is already enrolled.
    public Enrollment joinClass(String studentEmail, String joinCode) {
        Classroom classroom = classroomRepository.findByJoinCode(joinCode)
                .orElseThrow(() -> new NoSuchElementException("No class with that code"));
        if (enrollmentRepository.existsByClassroomIdAndStudentEmail(classroom.getId(), studentEmail)) {
            return null;
        }
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new NoSuchElementException("Student not found"));
        Enrollment enrollment = new Enrollment();
        enrollment.setClassroom(classroom);
        enrollment.setStudent(student);
        return enrollmentRepository.save(enrollment);
    }

    public List<Classroom> getClassesForTeacher(String email) {
        return classroomRepository.findByTeacherEmail(email);
    }

    public List<Classroom> getClassesForStudent(String email) {
        return enrollmentRepository.findByStudentEmail(email).stream()
                .map(Enrollment::getClassroom)
                .collect(Collectors.toList());
    }

    public List<User> getMembers(Long classId) {
        return enrollmentRepository.findByClassroomId(classId).stream()
                .map(Enrollment::getStudent)
                .collect(Collectors.toList());
    }

    public long memberCount(Long classId) {
        return enrollmentRepository.countByClassroomId(classId);
    }

    public Optional<Classroom> getById(Long id) {
        return classroomRepository.findById(id);
    }

    private String generateUniqueCode() {
        String code;
        do {
            StringBuilder sb = new StringBuilder(CODE_LENGTH);
            for (int i = 0; i < CODE_LENGTH; i++) {
                sb.append(CODE_ALPHABET.charAt(random.nextInt(CODE_ALPHABET.length())));
            }
            code = sb.toString();
        } while (classroomRepository.existsByJoinCode(code));
        return code;
    }
}
