package com.lingobingo.Service;

import com.lingobingo.Entity.Notification;
import com.lingobingo.Entity.User;
import com.lingobingo.Repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repo;

    /** Create one notification for a recipient. No-op if the recipient is null. */
    public void notify(User recipient, String type, String message, String link) {
        if (recipient == null) { return; }
        Notification n = new Notification();
        n.setRecipient(recipient);
        n.setType(type);
        n.setMessage(message);
        n.setLink(link);
        n.setRead(false);
        n.setCreatedAt(LocalDateTime.now());
        repo.save(n);
    }

    public List<Notification> forUser(String email) {
        return repo.findTop30ByRecipient_EmailOrderByCreatedAtDesc(email);
    }

    public long unreadCount(String email) {
        return repo.countByRecipient_EmailAndReadFalse(email);
    }

    public void markRead(Long id, String email) {
        repo.findById(id)
            .filter(n -> n.getRecipient() != null && email.equals(n.getRecipient().getEmail()))
            .ifPresent(n -> { n.setRead(true); repo.save(n); });
    }

    public void markAllRead(String email) {
        List<Notification> list = repo.findTop30ByRecipient_EmailOrderByCreatedAtDesc(email);
        list.forEach(n -> n.setRead(true));
        repo.saveAll(list);
    }
}
