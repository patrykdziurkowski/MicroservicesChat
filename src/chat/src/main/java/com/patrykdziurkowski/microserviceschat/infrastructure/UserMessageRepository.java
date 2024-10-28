package com.patrykdziurkowski.microserviceschat.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.patrykdziurkowski.microserviceschat.domain.UserMessage;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
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

    public Optional<List<UserMessage>> getByAmount(UUID chatId, int lastMessagePosition, int messagesToRetrive) {    
        String query = "SELECT m FROM UserMessage m WHERE m.chatRoomId = :chatId";
        return Optional.ofNullable(entityManager
            .createQuery(query, UserMessage.class)
            .setParameter("chatId", chatId)
            .setFirstResult(lastMessagePosition)
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
    
}
