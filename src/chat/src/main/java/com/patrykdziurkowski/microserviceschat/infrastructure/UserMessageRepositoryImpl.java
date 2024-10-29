package com.patrykdziurkowski.microserviceschat.infrastructure;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.patrykdziurkowski.microserviceschat.application.UserMessageRepository;
import com.patrykdziurkowski.microserviceschat.domain.UserMessage;
import com.patrykdziurkowski.microserviceschat.domain.domainevents.MessageDeletedEvent;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository
@Transactional
public class UserMessageRepositoryImpl implements UserMessageRepository{
    @PersistenceContext
    EntityManager entityManager;

    public List<UserMessage> get() {    
        final String query = "SELECT m FROM UserMessage m";
        return entityManager
            .createQuery(query, UserMessage.class)
            .getResultList();
    }    

    // Last Message provided by lastMessageId is skipped
    public Optional<List<UserMessage>> getByAmount(UUID chatId, Optional<UUID> lastMessageId, int messagesToRetrive) {
        LocalDateTime dateOfMessage = LocalDateTime.now().withYear(1);
        if(lastMessageId.isPresent()) {
            Optional<UserMessage> lastMessage = getById(lastMessageId.get());
            final boolean messageDoesntExistsInChat = lastMessage.isEmpty() 
                || lastMessage.get().getChatRoomId().equals(chatId) == false;
            if(messageDoesntExistsInChat) {
                return Optional.empty();
            }
            dateOfMessage = messageDatePostedById(lastMessageId.get()).plusNanos(1000); // Rounding issues
        }
        final String query = "SELECT m FROM UserMessage m WHERE " + 
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

    public List<UserMessage> getByOwnerId(UUID messageOwnerId) {
        final String query = "SELECT m FROM UserMessage m WHERE m.ownerId = :ownerId";
        return entityManager
            .createQuery(query, UserMessage.class)
            .setParameter("ownerId", messageOwnerId)
            .getResultList();
    }

    public void save(UserMessage message) {
        final boolean messageExists = messageExists(message.getId());
        if(messageExists) {
            entityManager.merge(message);
        } else {
            entityManager.persist(message);
        }
        final boolean messageDeleted = message
            .getDomainEvents()
            .contains(new MessageDeletedEvent());
        if(messageDeleted) {
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

    private LocalDateTime messageDatePostedById(UUID messageId) {
        return entityManager
            .find(UserMessage.class, messageId)
            .getDatePosted();
    }
    
}
