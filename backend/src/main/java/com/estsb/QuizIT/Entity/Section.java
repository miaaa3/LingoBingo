package com.estsb.QuizIT.Entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "lesson_sections")
@AllArgsConstructor
@NoArgsConstructor
public class Section {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String heading; // Section title

    @Column(columnDefinition = "TEXT")
    private String content; // Section content

    @Lob
    @Column(name = "media", columnDefinition = "LONGBLOB")
    private byte[] media; // Binary content stored directly in DB

    private String mediaFilename;
    private String mediaType; // image/png, application/pdf...


    @ManyToOne
    @JoinColumn(name = "lesson_id")
    @JsonManagedReference(value = "lesson-sections")
    private Lesson lesson;
}
