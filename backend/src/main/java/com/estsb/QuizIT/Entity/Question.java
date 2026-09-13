package com.estsb.QuizIT.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Entity
@Table(name = "Questions")
@EqualsAndHashCode(exclude = {"quiz"})
@AllArgsConstructor
@NoArgsConstructor
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long questionId;

    private String questionText;

    private String questionDescription;

    // Map of answer options, e.g. {"A": "Answer 1", "B": "Answer 2", ...}
    @ElementCollection
    @CollectionTable(name = "question_answers", joinColumns = @JoinColumn(name = "question_id"))
    @MapKeyColumn(name = "answer_option")
    @Column(name = "answer")
    private Map<String, String> answers;

    // Single correct answer key, e.g. "A" or "B"
    private String correctAnswer;

    private String explanation;

    private String tip;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "picture", columnDefinition = "BLOB")
    private byte[] picture;


    @ManyToOne
    @JoinColumn(name = "quiz_id")
    @JsonBackReference(value = "quiz-questions")
    private Quiz quiz;

    private String userAnswer;

    public Question(Long questionId, String questionText, String questionDescription,
                    Map<String, String> answers, String correctAnswer, String explanation,
                    String tip, Quiz quiz, String userAnswer) {
        this.questionId = questionId;
        this.questionText = questionText;
        this.questionDescription = questionDescription;
        this.answers = answers;
        this.correctAnswer = correctAnswer;
        this.explanation = explanation;
        this.tip = tip;
        this.picture = null;  // default null
        this.quiz = quiz;
        this.userAnswer = userAnswer;
    }



}
