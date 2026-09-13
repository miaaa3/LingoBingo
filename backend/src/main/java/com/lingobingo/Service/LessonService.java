package com.lingobingo.Service;

import com.lingobingo.Entity.Lesson;
import com.lingobingo.Repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;

    /** Create a new lesson */
    public Lesson createLesson(Lesson lesson) {
        // Sections will be saved automatically due to CascadeType.ALL
        return lessonRepository.save(lesson);
    }

    /** Get lesson by ID */
    public Lesson getLessonById(Long id) {
        return lessonRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Lesson not found with ID: " + id));
    }

    /** Get all lessons */
    public List<Lesson> getAllLessons() {
        return lessonRepository.findAll();
    }

    /** Get all lessons created by a user */
    public List<Lesson> getLessonsByUser(Long userId) {
        return lessonRepository.findByCreatedById(userId);
    }

    /** Update a lesson */
    public Lesson updateLesson(Long id, Lesson updatedLesson) {
        Lesson existingLesson = getLessonById(id);

        existingLesson.setTitle(updatedLesson.getTitle());
        existingLesson.setIsFavorite(updatedLesson.getIsFavorite());
        existingLesson.setIsPublic(updatedLesson.getIsPublic());

        // Optional: update sections if provided
        if (updatedLesson.getSections() != null) {
            existingLesson.getSections().clear();
            existingLesson.getSections().addAll(updatedLesson.getSections());
            // Make sure each section points back to this lesson
            existingLesson.getSections().forEach(section -> section.setLesson(existingLesson));
        }

        return lessonRepository.save(existingLesson);
    }

    /** Delete a lesson */
    public void deleteLesson(Long id) {
        if (!lessonRepository.existsById(id)) {
            throw new EntityNotFoundException("Lesson not found with ID: " + id);
        }
        lessonRepository.deleteById(id);
    }

    /** Toggle favorite status */
    public Lesson toggleFavorite(Long id) {
        Lesson lesson = getLessonById(id);
        lesson.setIsFavorite(!lesson.getIsFavorite());
        return lessonRepository.save(lesson);
    }

    /** Toggle visibility status */
    public Lesson toggleVisibility(Long id) {
        return lessonRepository.findById(id).map(lesson -> {
            lesson.setIsPublic(!lesson.getIsPublic());
            return lessonRepository.save(lesson);
        }).orElse(null);
    }
}
