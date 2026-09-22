package com.lingobingo.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "Quizzes")
@EqualsAndHashCode(exclude = {"questions","createdBy"})
@AllArgsConstructor
@NoArgsConstructor
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "quiz")
    @JsonManagedReference(value = "quiz-questions")
    private List<Question> questions;

    @ManyToOne
    @JoinColumn(name = "created_by")
    @JsonBackReference(value = "user-quiz")
    private User createdBy;

    @OneToMany(mappedBy = "quiz")
    @JsonBackReference(value = "quiz-games")
    private List<Game> games;

    @Column(nullable = false)
    private Boolean isPublic = true;

    // Optional deadline. When set and passed, students can no longer submit and
    // the owning teacher is notified once. Serialized as an ISO string so the
    // Angular date pipe can read it.
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime dueDate;

    // Whether the "quiz closed" teacher notification has already been sent.
    @JsonIgnore
    private boolean closedNotified = false;

    // Whether the "due within the hour" student reminder has already been sent.
    @JsonIgnore
    private boolean dueSoonNotified = false;

    private LocalDateTime createdAt = LocalDateTime.now();

    // Read-only owner identity for the client (createdBy is hidden by
    // @JsonBackReference). No setters, so these are ignored on deserialization.
    @Transient
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String getOwnerEmail() {
        return createdBy != null ? createdBy.getEmail() : null;
    }

    @Transient
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Long getOwnerId() {
        return createdBy != null ? createdBy.getId() : null;
    }

}
