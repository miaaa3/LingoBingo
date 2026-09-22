package com.lingobingo.Repository;

import com.lingobingo.Entity.ClassContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassContentRepository extends JpaRepository<ClassContent, Long> {
    List<ClassContent> findByClassroomId(Long classroomId);
    List<ClassContent> findByClassroomIdIn(List<Long> classroomIds);
    boolean existsByClassroomIdAndContentTypeAndContentId(Long classroomId, String contentType, Long contentId);

    // Every class this piece of content is assigned to (used by the due-soon reminder).
    List<ClassContent> findByContentTypeAndContentId(String contentType, Long contentId);
}
