package com.patrykdziurkowski.microserviceschat.application.commands;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.patrykdziurkowski.microserviceschat.application.interfaces.UserRepository;
import com.patrykdziurkowski.microserviceschat.domain.User;

@Service
public class ChangeUserNameCommand {
    private UserRepository userRepository;

    public ChangeUserNameCommand(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean execute(UUID currentUserId, String newUserName) {
        User currentUser = userRepository.getById(currentUserId).orElseThrow();

        if (userNameTaken(newUserName)) {
            return false;
        }

        currentUser.setUserName(newUserName);
        userRepository.save(currentUser);
        return true;
    }

    private boolean userNameTaken(String userName) {
        return userRepository.getByUserName(userName).isPresent();
    }
}
