package com.lingobingo.Dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/** Small view models for the messaging endpoints (keeps User entities off the wire). */
public class MessagingDtos {

    @Data
    @AllArgsConstructor
    public static class Contact {
        private Long id;
        private String name;
        private String email;
        private String role;
    }

    @Data
    @AllArgsConstructor
    public static class MessageView {
        private Long id;
        private String content;
        private boolean fromMe;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private LocalDateTime createdAt;
    }

    @Data
    @AllArgsConstructor
    public static class ThreadSummary {
        private Long otherId;
        private String otherName;
        private String lastMessage;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private LocalDateTime lastAt;
        private long unread;
    }

    @Data
    @AllArgsConstructor
    public static class Thread {
        private Long otherId;
        private String otherName;
        private List<MessageView> messages;
    }
}
