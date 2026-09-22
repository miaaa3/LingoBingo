package com.lingobingo.Repository;

import com.lingobingo.Entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // Latest first, capped so the bell stays light.
    List<Notification> findTop30ByRecipient_EmailOrderByCreatedAtDesc(String email);
    long countByRecipient_EmailAndReadFalse(String email);
}
