package com.lingobingo.Repository;

import com.lingobingo.Entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
    // Fetch lessons created by a specific user
    List<Lesson> findByCreatedById(Long userId);

    // (Optional) fetch only public lessons
    List<Lesson> findByIsPublicTrue();

    // (Optional) fetch all favorite lessons of a user
    List<Lesson> findByCreatedBy_IdAndIsFavoriteTrue(Long userId);
}
