package com.lingobingo.Repository;

import com.lingobingo.Entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // Full conversation between two people, oldest first.
    @Query("select m from Message m where (m.sender.email = :a and m.recipient.email = :b) " +
           "or (m.sender.email = :b and m.recipient.email = :a) order by m.createdAt asc")
    List<Message> findThread(@Param("a") String a, @Param("b") String b);

    // Everything the user is part of, newest first (used to build conversation list).
    @Query("select m from Message m where m.sender.email = :email or m.recipient.email = :email " +
           "order by m.createdAt desc")
    List<Message> findAllForUser(@Param("email") String email);

    long countByRecipient_EmailAndReadFalse(String email);
}
