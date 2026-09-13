package com.estsb.QuizIT.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@Entity
@Table(name = "players")
@EqualsAndHashCode(exclude = "games")
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    private String avatarUrl;  // This will store the URL of the avatar image

    private Long score = 0L;;
    private Boolean finished;


    @ManyToMany(mappedBy = "players", fetch = FetchType.LAZY)
    @JsonIgnore// This is the relationship with the Game entity
    private List<Game> games;
}
