package com.lingobingo.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "notifications")
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Who receives it. Not serialized — the client only ever fetches its own.
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "recipient_id")
    private User recipient;

    @Column(length = 500)
    private String message;

    private String type;   // NEW_QUIZ, NEW_FLASHCARD_SET, NEW_COURSE, QUIZ_CLOSED
    private String link;   // frontend route to open when clicked

    @Column(name = "is_read")
    private boolean read = false;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt = LocalDateTime.now();
}
