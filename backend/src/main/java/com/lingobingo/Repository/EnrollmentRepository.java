package com.lingobingo.Repository;

import com.lingobingo.Entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByStudentEmail(String email);
    List<Enrollment> findByClassroomId(Long classroomId);
    boolean existsByClassroomIdAndStudentEmail(Long classroomId, String email);
    long countByClassroomId(Long classroomId);
}
