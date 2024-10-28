package com.patrykdziurkowski.microserviceschat.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.patrykdziurkowski.microserviceschat.domain.ChatRoom;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository
@Transactional
public class ChatRepository {
    @PersistenceContext
    private EntityManager entityManager;

    public List<ChatRoom> get() {
        return entityManager
            .createQuery("SELECT c FROM ChatRoom c", ChatRoom.class)
            .getResultList();
    }

    public Optional<ChatRoom> getById(UUID id) {
        return Optional.ofNullable(entityManager.find(ChatRoom.class, id));
    }

    public Optional<List<ChatRoom>> getByMemberId(UUID memberId) {
        String query = "SELECT c FROM ChatRoom c JOIN c.memberIds m WHERE m = :memberId OR c.isPublic ";
        return Optional.ofNullable(entityManager
            .createQuery(query, ChatRoom.class)
            .setParameter("memberId", memberId)
            .getResultList());
    }

    public List<UUID> getMembers(UUID chatId) {
        String query = "SELECT m FROM ChatRoom c JOIN c.memberIds m WHERE c.id = :chatId";
        return entityManager
            .createQuery(query, UUID.class)
            .setParameter("chatId", chatId)
            .getResultList();
    }

    public void save(ChatRoom chatRoom) {
        boolean chatExists = chatExists(chatRoom.getId());
        if(chatExists) {
            entityManager.merge(chatRoom);
        } else {
            entityManager.persist(chatRoom);
        }
        entityManager.flush();
    }

    private boolean chatExists(UUID id) {
        String query = "SELECT COUNT(c) FROM ChatRoom c WHERE c.id = :id";
        return entityManager
            .createQuery(query, Long.class)
            .setParameter("id", id)
            .getSingleResult() > 0;
    }
}
