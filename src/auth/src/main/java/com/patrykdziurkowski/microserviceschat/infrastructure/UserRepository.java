package com.patrykdziurkowski.microserviceschat.infrastructure;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.patrykdziurkowski.microserviceschat.domain.User;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository
@Transactional
public class UserRepository {
    @PersistenceContext
    private EntityManager entityManager;

    public List<User> get() {
        return entityManager
                .createQuery("SELECT u FROM User u", User.class)
                .getResultList();
    }

    public Optional<User> getById(UUID id) {
        User user = entityManager.find(User.class, id);
        if (user == null) {
            return Optional.empty();
        }
        return Optional.of(user);
    }

    public Optional<User> getByUserName(String userName) {
        try {
            User user = (User) entityManager
                    .createQuery("SELECT u FROM User u WHERE u.userName = :userName")
                    .setParameter("userName", userName)
                    .getSingleResult();
            return Optional.of(user);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public void save(User user) {
        ArrayList<User> users = new ArrayList<>();
        users.add(user);
        save(users);
    }

    public void save(List<User> users) {
        for (User user : users) {
            if (userExists(user)) {
                entityManager.merge(user);
            } else {
                entityManager.persist(user);
            }
        }
        entityManager.flush();
    }

    private boolean userExists(User user) {
        if (getById(user.getId()).isPresent()) {
            return true;
        }
        return false;
    }
}
