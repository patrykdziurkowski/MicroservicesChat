package com.patrykdziurkowski.microserviceschat.presentation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.patrykdziurkowski.microserviceschat.domain.ChatRoom;
import com.patrykdziurkowski.microserviceschat.domain.UserMessage;

import jakarta.annotation.Nullable;

public class MessageDto {
    UUID messageId;
    LocalDateTime datePosted;
    String text;
    boolean isMessageOwner;
    boolean isAnnouncement;
    @Nullable
    String ownerId;

    private MessageDto() {}

    public static MessageDto from(UserMessage message, UUID userId) {
        MessageDto messageDto = new MessageDto();
        messageDto.messageId = message.getId();
        messageDto.text = message.getText();
        messageDto.datePosted = message.getDatePosted();
        messageDto.isAnnouncement = false;
        messageDto.isMessageOwner = false;

        if(message.getOwnerId() == null) {
            messageDto.isAnnouncement = true;
        } else {
            messageDto.ownerId = message.getId().toString();
        }
        if(message.getOwnerId().equals(userId)) {
            messageDto.isMessageOwner = true;
        }

        return messageDto;
    }

    public static List<MessageDto> fromList(List<UserMessage> messages, UUID userId) {
        List<MessageDto> messagesDto = new ArrayList<>();
        for (UserMessage message : messages) {
            messagesDto.add(from(message, userId));
        }
        return messagesDto;
    }

    public UUID getMessageId() {
        return this.messageId;
    }

    public void setMessageId(UUID messageId) {
        this.messageId = messageId;
    }

    public LocalDateTime getDatePosted() {
        return this.datePosted;
    }

    public void setDatePosted(LocalDateTime datePosted) {
        this.datePosted = datePosted;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isIsMessageOwner() {
        return this.isMessageOwner;
    }

    public boolean getIsMessageOwner() {
        return this.isMessageOwner;
    }

    public void setIsMessageOwner(boolean isMessageOwner) {
        this.isMessageOwner = isMessageOwner;
    }

    public boolean isIsAnnouncement() {
        return this.isAnnouncement;
    }

    public boolean getIsAnnouncement() {
        return this.isAnnouncement;
    }

    public void setIsAnnouncement(boolean isAnnouncement) {
        this.isAnnouncement = isAnnouncement;
    }

    public String getOwnerId() {
        return this.ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }
}
