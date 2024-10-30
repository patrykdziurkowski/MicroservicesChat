package com.patrykdziurkowski.microserviceschat.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.patrykdziurkowski.microserviceschat.application.FavoriteChatRepository;
import com.patrykdziurkowski.microserviceschat.domain.FavoriteChatRoom;
import com.patrykdziurkowski.microserviceschat.domain.domainevents.FavoriteUnsetEvent;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository
@Transactional
public class FavoriteChatRepositoryImpl implements FavoriteChatRepository{
    @PersistenceContext
    private EntityManager entityManager;

    public Optional<FavoriteChatRoom> getById(UUID chatId) {
        return Optional.ofNullable(entityManager
            .find(FavoriteChatRoom.class, chatId));
    }

    public List<FavoriteChatRoom> getByUserId(UUID userId) {
        final String query = "SELECT f FROM FavoriteChatRoom f WHERE f.userId = :userId";
        return entityManager
            .createQuery(query, FavoriteChatRoom.class)
            .setParameter("userId", userId)
            .getResultList();
    }

    public void save(FavoriteChatRoom chatRoom) {
        final boolean chatExists = favoriteChatExists(chatRoom.getId());
        if(chatExists) {
            entityManager.merge(chatRoom);
        } else {
            entityManager.persist(chatRoom);
        }
        final boolean chatUnset = chatRoom
            .getDomainEvents()
            .contains(new FavoriteUnsetEvent());
        if(chatUnset) {
            entityManager.remove(chatRoom);
        }
        entityManager.flush();
    }

    private boolean favoriteChatExists(UUID chatId) {
        final String query = "SELECT COUNT(f) FROM FavoriteChatRoom f WHERE f.id = :id";
        return entityManager
            .createQuery(query, Long.class)
            .setParameter("id", chatId)
            .getSingleResult() > 0;
    }
}
