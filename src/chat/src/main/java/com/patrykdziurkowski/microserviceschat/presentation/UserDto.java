package com.patrykdziurkowski.microserviceschat.presentation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.patrykdziurkowski.microserviceschat.application.User;

public class UserDto {
    private UUID userId;
    private String userName;

    private UserDto(UUID userId, String userName) {
        this.userId = userId;
        this.userName = userName;
    }

    public static List<UserDto> fromList(List<User> users) {
        List<UserDto> userDtos = new ArrayList<>();
        for (User user : users) {
            userDtos.add(from(user));
        }
        return userDtos;
    }

    public static UserDto from(User user) {
        return new UserDto(user.getUserId(), user.getUserName());
    }

    public UUID getUserId() {
        return this.userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

}
