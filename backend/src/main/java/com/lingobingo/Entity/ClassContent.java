package com.lingobingo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "class_content",
       uniqueConstraints = @UniqueConstraint(columnNames = {"classroom_id", "content_type", "content_id"}))
@NoArgsConstructor
@AllArgsConstructor
public class ClassContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "classroom_id")
    private Classroom classroom;

    @Column(name = "content_type")
    private String contentType; // COURSE, QUIZ, FLASHCARD_SET

    @Column(name = "content_id")
    private Long contentId;

    private LocalDateTime assignedAt = LocalDateTime.now();
}
