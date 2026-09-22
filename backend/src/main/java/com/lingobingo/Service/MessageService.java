package com.lingobingo.Service;

import com.lingobingo.Dto.MessagingDtos.Contact;
import com.lingobingo.Dto.MessagingDtos.MessageView;
import com.lingobingo.Dto.MessagingDtos.Thread;
import com.lingobingo.Dto.MessagingDtos.ThreadSummary;
import com.lingobingo.Entity.Classroom;
import com.lingobingo.Entity.Enrollment;
import com.lingobingo.Entity.Message;
import com.lingobingo.Entity.User;
import com.lingobingo.Repository.ClassroomRepository;
import com.lingobingo.Repository.EnrollmentRepository;
import com.lingobingo.Repository.MessageRepository;
import com.lingobingo.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ClassroomRepository classroomRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final NotificationService notificationService;

    /** Everyone the user shares a class with (teachers <-> their students, classmates). */
    public List<Contact> contacts(String email) {
        User me = userRepository.findByEmail(email).orElse(null);
        if (me == null) { return List.of(); }
        Map<Long, User> byId = new LinkedHashMap<>();

        for (Classroom c : classroomRepository.findByTeacherEmail(email)) {
            for (Enrollment e : enrollmentRepository.findByClassroomId(c.getId())) {
                if (e.getStudent() != null) { byId.put(e.getStudent().getId(), e.getStudent()); }
            }
        }
        for (Enrollment en : enrollmentRepository.findByStudentEmail(email)) {
            Classroom c = en.getClassroom();
            if (c == null) { continue; }
            if (c.getTeacher() != null) { byId.put(c.getTeacher().getId(), c.getTeacher()); }
            for (Enrollment e : enrollmentRepository.findByClassroomId(c.getId())) {
                if (e.getStudent() != null) { byId.put(e.getStudent().getId(), e.getStudent()); }
            }
        }
        byId.remove(me.getId());

        return byId.values().stream()
                .map(u -> new Contact(u.getId(), displayName(u), u.getEmail(),
                        u.getRole() != null ? u.getRole().name() : null))
                .collect(Collectors.toList());
    }

    /** One entry per person the user has exchanged messages with, newest first. */
    public List<ThreadSummary> threads(String email) {
        List<Message> all = messageRepository.findAllForUser(email); // newest first
        Map<Long, ThreadSummary> summaries = new LinkedHashMap<>();
        for (Message m : all) {
            User other = (m.getSender() != null && email.equals(m.getSender().getEmail()))
                    ? m.getRecipient() : m.getSender();
            if (other == null) { continue; }
            summaries.computeIfAbsent(other.getId(),
                    k -> new ThreadSummary(other.getId(), displayName(other), m.getContent(), m.getCreatedAt(), 0));
            if (!m.isRead() && m.getRecipient() != null && email.equals(m.getRecipient().getEmail())) {
                ThreadSummary cur = summaries.get(other.getId());
                cur.setUnread(cur.getUnread() + 1);
            }
        }
        return new ArrayList<>(summaries.values());
    }

    /** Full conversation with one person; marks their messages to me as read. */
    public Thread thread(String email, Long otherId) {
        User other = userRepository.findById(otherId).orElse(null);
        if (other == null) { return new Thread(otherId, "Unknown", List.of()); }
        List<Message> msgs = messageRepository.findThread(email, other.getEmail());
        boolean changed = false;
        for (Message m : msgs) {
            if (!m.isRead() && m.getRecipient() != null && email.equals(m.getRecipient().getEmail())) {
                m.setRead(true);
                changed = true;
            }
        }
        if (changed) { messageRepository.saveAll(msgs); }
        List<MessageView> views = msgs.stream()
                .map(m -> new MessageView(m.getId(), m.getContent(),
                        m.getSender() != null && email.equals(m.getSender().getEmail()), m.getCreatedAt()))
                .collect(Collectors.toList());
        return new Thread(other.getId(), displayName(other), views);
    }

    public void send(String senderEmail, Long recipientId, String content) {
        if (content == null || content.isBlank()) { return; }
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new NoSuchElementException("Sender not found"));
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new NoSuchElementException("Recipient not found"));
        Message m = new Message();
        m.setSender(sender);
        m.setRecipient(recipient);
        m.setContent(content.trim());
        m.setRead(false);
        m.setCreatedAt(LocalDateTime.now());
        messageRepository.save(m);
        notificationService.notify(recipient, "MESSAGE",
                "New message from " + displayName(sender), "/Messages?with=" + sender.getId());
    }

    public long unreadCount(String email) {
        return messageRepository.countByRecipient_EmailAndReadFalse(email);
    }

    private String displayName(User u) {
        if (u == null) { return "Unknown"; }
        if (u.getName() != null && !u.getName().isBlank()) { return u.getName(); }
        return u.getEmail();
    }
}
