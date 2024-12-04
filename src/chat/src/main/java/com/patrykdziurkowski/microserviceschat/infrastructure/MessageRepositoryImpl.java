package com.patrykdziurkowski.microserviceschat.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.patrykdziurkowski.microserviceschat.application.MessageRepository;
import com.patrykdziurkowski.microserviceschat.domain.UserMessage;
import com.patrykdziurkowski.microserviceschat.domain.domainevents.MessageDeletedEvent;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository
@Transactional
public class MessageRepositoryImpl implements MessageRepository {
    @PersistenceContext
    EntityManager entityManager;

    // Last Message provided by lastMessageId is skipped
    public List<UserMessage> getByAmount(UUID chatId, int lastMessagePosition, int messagesToRetrieve) {
        final String query = "SELECT m FROM UserMessage m WHERE " +
                "m.chatRoomId = :chatId ORDER BY m.datePosted";
        return entityManager
                .createQuery(query, UserMessage.class)
                .setParameter("chatId", chatId)
                .setFirstResult(lastMessagePosition)
                .setMaxResults(messagesToRetrieve)
                .getResultList();
    }

    public Optional<UserMessage> getById(UUID messageId) {
        return Optional.ofNullable(entityManager
                .find(UserMessage.class, messageId));
    }

    public void save(UserMessage message) {
        final boolean messageExists = messageExists(message.getId());
        if (messageExists) {
            entityManager.merge(message);
        } else {
            entityManager.persist(message);
        }
        final boolean messageDeleted = message
                .getDomainEvents()
                .contains(new MessageDeletedEvent());
        if (messageDeleted) {
            entityManager.remove(message);
        }
        entityManager.flush();
    }

    private boolean messageExists(UUID messageId) {
        final String query = "SELECT COUNT(m) FROM UserMessage m WHERE m.id = :id";
        return entityManager
                .createQuery(query, Long.class)
                .setParameter("id", messageId)
                .getSingleResult() > 0;
    }

}
