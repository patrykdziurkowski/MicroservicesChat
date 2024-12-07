package com.patrykdziurkowski.microserviceschat.presentation.models;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ChatModel {
    @NotBlank
    @Size(min = 3, max = 30)
    @Pattern(regexp = "^[\\w ]+$") // alphanumeric characters and spaces only
    private String chatName;
    @NotNull
    private boolean isPublic;
    @Nullable
    @Size(min = 8)
    @Pattern(regexp = "^(?=.*\\d)(?=.*\\W).*$") // needs at least one digit and one non-alphanumeric character
    private String chatPassword;

    public ChatModel(String chatName, Boolean isPublic, String chatPassword) {
        this.chatName = chatName;
        this.isPublic = isPublic;
        this.chatPassword = chatPassword;
    }

    public String getChatName() {
        return this.chatName;
    }

    public void setChatName(String chatName) {
        this.chatName = chatName;
    }

    public boolean getIsPublic() {
        return this.isPublic;
    }

    public void setIsPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public String getChatPassword() {
        return this.chatPassword;
    }

    public void setChatPassword(String chatPassword) {
        this.chatPassword = chatPassword;
    }
}
