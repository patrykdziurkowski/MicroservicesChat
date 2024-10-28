package com.patrykdziurkowski.microserviceschat.infrastructure;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.patrykdziurkowski.microserviceschat.domain.UserMessage;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository
@Transactional
public class UserMessageRepository {
    @PersistenceContext
    EntityManager entityManager;

    public Optional<List<UserMessage>> get() {    
        String query = "SELECT m FROM UserMessage m";
        return Optional.ofNullable(entityManager
            .createQuery(query, UserMessage.class)
            .getResultList());
    }    

    // Last Message provided by lastMessageId is skipped
    public Optional<List<UserMessage>> getByAmount(UUID chatId, Optional<UUID> lastMessageId, int messagesToRetrive) {
        LocalDateTime dateOfMessage = LocalDateTime.now().withYear(1);
        if(lastMessageId.isPresent()) {
            Optional<UserMessage> lastMessage = getById(lastMessageId.get());
            boolean messageDoesntExistsInChat = lastMessage.isEmpty() 
                || lastMessage.get().getChatRoomId().equals(chatId) == false;
            if(messageDoesntExistsInChat) {
                return Optional.empty();
            }
            dateOfMessage = messageDatePostedById(lastMessageId.get()).plusNanos(1000);
        }
        String query = "SELECT m FROM UserMessage m WHERE " + 
                "m.chatRoomId = :chatId AND m.datePosted > :datePosted ORDER BY m.datePosted";
        return Optional.ofNullable(entityManager
            .createQuery(query, UserMessage.class)
            .setParameter("chatId", chatId)
            .setParameter("datePosted", dateOfMessage)
            .setMaxResults(messagesToRetrive)
            .getResultList());
    }

    public Optional<UserMessage> getById(UUID messageId) {
        return Optional.ofNullable(entityManager
            .find(UserMessage.class, messageId));
    }

    public Optional<List<UserMessage>> getByOwnerId(UUID messageOwnerId) {
        String query = "SELECT m FROM UserMessage m WHERE m.ownerId = :ownerId";
        return Optional.ofNullable(entityManager
            .createQuery(query, UserMessage.class)
            .setParameter("ownerId", messageOwnerId)
            .getResultList());
    }

    public void save(UserMessage message) {
        boolean messageExists = messageExists(message.getId());
        if(messageExists) {
            entityManager.merge(message);
        } else {
            entityManager.persist(message);
        }
        entityManager.flush();
    }

    private boolean messageExists(UUID messageId) {
        String query = "SELECT COUNT(m) FROM UserMessage m WHERE m.id = :id";
        return entityManager
            .createQuery(query, Long.class)
            .setParameter("id", messageId)
            .getSingleResult() > 0;
    }

    private LocalDateTime messageDatePostedById(UUID messageId) {
        return entityManager
            .find(UserMessage.class, messageId)
            .getDatePosted();
    }
    
}
