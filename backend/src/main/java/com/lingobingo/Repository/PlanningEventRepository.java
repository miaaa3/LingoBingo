package com.lingobingo.Repository;

import com.lingobingo.Entity.PlanningEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanningEventRepository extends JpaRepository<PlanningEvent, Long> {
    List<PlanningEvent> findByClassroomIdInOrderByDueDateAsc(List<Long> classroomIds);
    List<PlanningEvent> findByClassroomIdOrderByDueDateAsc(Long classroomId);
}
