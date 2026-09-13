package com.lingobingo.Service;

import com.lingobingo.Entity.Section;
import com.lingobingo.Repository.SectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SectionService {

    private final SectionRepository sectionRepository;

    // Create or save a section
    public Section createSection(Section section) {
        return sectionRepository.save(section);
    }

    // Get a section by ID
    public Section getSectionById(Long id) {
        return sectionRepository.findById(id).orElse(null);
    }

    // Get all sections of a lesson
    public List<Section> getSectionsByLesson(Long lessonId) {
        return sectionRepository.findByLessonId(lessonId);
    }

    // Update a section
    public Section updateSection(Long id, Section updatedSection) {
        Optional<Section> existingSection = sectionRepository.findById(id);
        if (existingSection.isPresent()) {
            Section section = existingSection.get();
            section.setHeading(updatedSection.getHeading());
            section.setContent(updatedSection.getContent());
            section.setMedia(updatedSection.getMedia());
            return sectionRepository.save(section);
        }
        return null;
    }

    // Delete a section
    public void deleteSection(Long id) {
        sectionRepository.deleteById(id);
    }
}
