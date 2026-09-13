package com.lingobingo.Repository;

import com.lingobingo.Entity.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, Long> {
    Optional<Classroom> findByJoinCode(String joinCode);
    boolean existsByJoinCode(String joinCode);
    List<Classroom> findByTeacherEmail(String email);
}
