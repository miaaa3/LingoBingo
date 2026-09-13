package com.lingobingo.Controller;

import com.lingobingo.Dto.LessonDTO;
import com.lingobingo.Dto.LessonResponseDTO;
import com.lingobingo.Dto.SectionDTO;
import com.lingobingo.Dto.SectionResponseDTO;
import com.lingobingo.Entity.Lesson;
import com.lingobingo.Entity.Section;
import com.lingobingo.Entity.User;
import com.lingobingo.Repository.UserRepository;
import com.lingobingo.Repository.SectionRepository;
import com.lingobingo.Service.LessonService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/lessons")
public class LessonController {

    private final LessonService lessonService;
    private final UserRepository userRepository;
    private final SectionRepository sectionRepository;


    @PostMapping("/add")
    public ResponseEntity<LessonResponseDTO> createLesson(
            Principal principal,
            @ModelAttribute LessonDTO lessonFormDTO) throws IOException {

        // Create a new Lesson entity
        Lesson lesson = new Lesson();
        lesson.setTitle(lessonFormDTO.getTitle());
        // Default to true if null
        lesson.setIsPublic(lessonFormDTO.getIsPublic() != null ? lessonFormDTO.getIsPublic() : true);
        lesson.setIsFavorite(lessonFormDTO.getIsFavorite() != null ? lessonFormDTO.getIsFavorite() : true);

        // Prepare list for section responses (to send back without media byte[])
        List<SectionResponseDTO> sectionResponses = new ArrayList<>();
        if (lessonFormDTO.getSections() != null) {
            List<Section> sections = new ArrayList<>();
            for (SectionDTO secDto : lessonFormDTO.getSections()) {
                // Create new Section entity
                Section section = new Section();
                section.setHeading(secDto.getHeading());
                section.setContent(secDto.getContent());

                // Handle media if provided
                if (secDto.getMedia() != null && !secDto.getMedia().isEmpty()) {
                    section.setMedia(secDto.getMedia().getBytes()); // Save file as byte[]
                    section.setMediaFilename(secDto.getMedia().getOriginalFilename()); // Save original file name
                    section.setMediaType(secDto.getMedia().getContentType()); // Save MIME type
                }

                section.setLesson(lesson); // Link section to lesson
                sections.add(section);

                // Create response DTO without media byte[] (only metadata)
                sectionResponses.add(new SectionResponseDTO(
                        section.getHeading(),
                        section.getContent(),
                        section.getMediaFilename(),
                        section.getMediaType()
                ));
            }
            lesson.setSections(sections);
        }

        // Set current user as lesson creator if logged in
        if (principal != null) {
            userRepository.findByEmail(principal.getName())
                    .ifPresent(lesson::setCreatedBy);
        }

        // Save lesson via service
        Lesson createdLesson = lessonService.createLesson(lesson);

        // Prepare response DTO with sections (without raw media)
        LessonResponseDTO responseDTO = new LessonResponseDTO(
                createdLesson.getId(),
                createdLesson.getTitle(),
                createdLesson.getIsPublic(),
                createdLesson.getIsFavorite(),
                sectionResponses
        );

        // Return HTTP 201 (Created) with response DTO
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }


    /** Get a lesson by ID */
    @GetMapping("/{id}")
    public ResponseEntity<Lesson> getLessonById(@PathVariable Long id) {
        Lesson lesson = lessonService.getLessonById(id);
        return new ResponseEntity<>(lesson, HttpStatus.OK);
    }

    /** Get all lessons */
    @GetMapping("/all")
    public ResponseEntity<List<Lesson>> getAllLessons() {
        return new ResponseEntity<>(lessonService.getAllLessons(), HttpStatus.OK);
    }

    /** Get all lessons created by a specific user */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Lesson>> getLessonsByUser(@PathVariable Long userId) {
        return new ResponseEntity<>(lessonService.getLessonsByUser(userId), HttpStatus.OK);
    }

    /** Update an existing lesson */
    @PutMapping("/{id}/update")
    public ResponseEntity<Lesson> updateLesson(@PathVariable Long id, @RequestBody Lesson updatedLesson) {
        Lesson lesson = lessonService.updateLesson(id, updatedLesson);
        return new ResponseEntity<>(lesson, HttpStatus.OK);
    }

    /** Delete a lesson */
    @DeleteMapping("/{id}/delete")
    public ResponseEntity<String> deleteLesson(@PathVariable Long id) {
        lessonService.deleteLesson(id);
        return new ResponseEntity<>("Lesson deleted successfully", HttpStatus.OK);
    }

    /** Toggle favorite status */
    @PutMapping("/{id}/favorite/toggle")
    public ResponseEntity<Lesson> toggleFavorite(@PathVariable Long id) {
        Lesson lesson = lessonService.toggleFavorite(id);
        return new ResponseEntity<>(lesson, HttpStatus.OK);
    }

    /** Toggle visibility status (public/private) */
    @PutMapping("/{id}/visibility/toggle")
    public ResponseEntity<Lesson> toggleVisibility(@PathVariable Long id) {
        Lesson lesson = lessonService.toggleVisibility(id);
        return (lesson != null) ?
                new ResponseEntity<>(lesson, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /** Get all public lessons (for students to browse) */
    @GetMapping("/public")
    public ResponseEntity<List<Lesson>> getPublicLessons() {
        return new ResponseEntity<>(lessonService.getPublicLessons(), HttpStatus.OK);
    }

    /** Serve a section's image (public so <img> tags can load it without a token) */
    @GetMapping("/sections/{sectionId}/image")
    public ResponseEntity<byte[]> getSectionImage(@PathVariable Long sectionId) {
        Section section = sectionRepository.findById(sectionId).orElse(null);
        if (section == null || section.getMedia() == null) {
            return ResponseEntity.notFound().build();
        }
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        try {
            if (section.getMediaType() != null) {
                mediaType = MediaType.parseMediaType(section.getMediaType());
            }
        } catch (Exception ignored) { }
        return ResponseEntity.ok().contentType(mediaType).body(section.getMedia());
    }
}
