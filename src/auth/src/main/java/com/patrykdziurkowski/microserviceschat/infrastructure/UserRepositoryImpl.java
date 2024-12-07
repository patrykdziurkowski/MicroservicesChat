package com.patrykdziurkowski.microserviceschat.infrastructure;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.patrykdziurkowski.microserviceschat.application.UserRepository;
import com.patrykdziurkowski.microserviceschat.domain.User;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository
@Transactional
public class UserRepositoryImpl implements UserRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<User> getByNumber(int number, int offset, String contains) {
        final String query = "SELECT u FROM User u WHERE u.userName LIKE :contains ORDER BY u.userName";
        return entityManager
                .createQuery(query, User.class)
                .setParameter("contains", "%" + contains + "%")
                .setFirstResult(offset)
                .setMaxResults(number)
                .getResultList();
    }

    @Override
    public List<User> getByNumber(int number, int offset) {
        final String query = "SELECT u FROM User u ORDER BY u.userName";
        return entityManager
                .createQuery(query, User.class)
                .setFirstResult(offset)
                .setMaxResults(number)
                .getResultList();
    }

    @Override
    public Optional<User> getById(UUID id) {
        User user = entityManager.find(User.class, id);
        return Optional.ofNullable(user);
    }

    @Override
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

    @Override
    public void save(User user) {
        ArrayList<User> users = new ArrayList<>();
        users.add(user);
        save(users);
    }

    @Override
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
        return getById(user.getId()).isPresent();
    }
}
