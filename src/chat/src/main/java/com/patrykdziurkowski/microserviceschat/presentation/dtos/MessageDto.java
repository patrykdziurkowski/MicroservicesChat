package com.patrykdziurkowski.microserviceschat.presentation.dtos;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.patrykdziurkowski.microserviceschat.application.models.User;
import com.patrykdziurkowski.microserviceschat.domain.UserMessage;

import jakarta.annotation.Nullable;

public class MessageDto {
    private UUID messageId;
    private Instant datePosted;
    private String text;
    private boolean isMessageOwner;
    private boolean isAnnouncement;
    @Nullable
    private String ownerId;
    @Nullable
    private String ownerUserName;

    private MessageDto() {
    }

    public static MessageDto from(UserMessage message, UUID userId, User chatMember) {
        MessageDto messageDto = new MessageDto();
        messageDto.messageId = message.getId();
        messageDto.text = message.getText();
        messageDto.datePosted = message.getDatePosted();

        messageDto.isAnnouncement = (message.getOwnerId() == null) ? true : false;
        messageDto.isMessageOwner = (userId.equals(message.getOwnerId())) ? true : false;
        messageDto.ownerId = (message.getOwnerId() == null) ? null : message.getOwnerId().toString();
        messageDto.ownerUserName = (message.getOwnerId() == null) ? null : chatMember.getUserName();

        return messageDto;
    }

    public static List<MessageDto> fromList(List<UserMessage> messages, UUID userId, List<User> chatMembers) {
        List<MessageDto> messagesDto = new ArrayList<>();
        for (UserMessage message : messages) {
            User chatMember = getUser(message.getOwnerId(), chatMembers);
            messagesDto.add(from(message, userId, chatMember));
        }
        return messagesDto;
    }

    private static User getUser(UUID messageOwnerId, List<User> chatMembers) {
        User user = new User();
        for (User member : chatMembers) {
            if (member.getUserId().equals(messageOwnerId)) {
                user = member;
            }
        }
        return user;
    }

    public UUID getMessageId() {
        return this.messageId;
    }

    public void setMessageId(UUID messageId) {
        this.messageId = messageId;
    }

    public Instant getDatePosted() {
        return this.datePosted;
    }

    public void setDatePosted(Instant datePosted) {
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

    public String getOwnerUserName() {
        return this.ownerUserName;
    }

    public void setOwnerUserName(String ownerUserName) {
        this.ownerUserName = ownerUserName;
    }

}
