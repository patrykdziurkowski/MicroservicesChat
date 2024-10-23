package com.patrykdziurkowski.microserviceschat.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

public class ChatRoom {
    private UUID id;
    private UUID ownerId;
    private String name;
    private boolean isPublic;
    private boolean isFlaggedForDeletion;
    private ArrayList<UUID> memberIds = new ArrayList<UUID>();
    private ArrayList<Message> messages = new ArrayList<Message>();
    private int totalMessageCount;
    private String passwordHash;
    

    ChatRoom() {}
    public ChatRoom(UUID ownerId, String name, boolean isPublic, String passwordHash) {
        this.id = UUID.randomUUID();
        this.ownerId = ownerId;
        this.name = name;
        this.isPublic = isPublic;
        this.isFlaggedForDeletion = false;
        this.passwordHash = passwordHash;
        memberIds.add(ownerId);
    }
    public ChatRoom(UUID ownerId, String name, boolean isPublic) {
        this.id = UUID.randomUUID();
        this.ownerId = ownerId;
        this.name = name;
        this.isPublic = isPublic;
        this.isFlaggedForDeletion = false;
        this.passwordHash = null;
        memberIds.add(ownerId);
    }
    public boolean dissolve(UUID currentUserId) {
        if (currentUserId != ownerId) {
            return false;
        }
        isFlaggedForDeletion = true;
        return true;
    }
    public boolean invateMember(UUID newMemberId, UUID currentUserId) {
        if (currentUserId != ownerId) {
            return false;
        }
        if (memberIds.contains(newMemberId)) {
            return false;
        }
        memberIds.add(newMemberId);
        return true;
    }
    public boolean removeMember(UUID memberId, UUID currentUserId) {
        if (currentUserId != ownerId) {
            return false;
        }
        if (currentUserId == memberId) {
            return false;
        }
        if (memberIds.remove(memberId)) {
            return true;
        }
        return false;
    }
    public boolean join(UUID currentUserId, String password) {
        if (passwordHash != null) {
            if (password.isEmpty()) {
                return false;
            }
            if (!passwordHash.equals(password)) {
                return false;
            }
        }
        if (memberIds.contains(currentUserId)) {
            return false;
        }
        memberIds.add(currentUserId);
        return true;
    }
    public boolean join(UUID currentUserId) {
        if (passwordHash != null) {
            return false;
        }
        if (memberIds.contains(currentUserId)) {
            return false;
        }
        memberIds.add(currentUserId);
        return true;
    }
    public boolean leave(UUID currentUserId) {
        if (!memberIds.contains(currentUserId)) {
            return false;
        }
        memberIds.remove(memberIds.indexOf(currentUserId));
        if (memberIds.size() == 0) {
            isFlaggedForDeletion = true;
            return true;
        }
        if (currentUserId == ownerId) {
            ownerId = memberIds.get(0);
        }
        return true;
    }
    public boolean postMessage(UUID currentUserId, String text, LocalDateTime datePosted) {
        if (!memberIds.contains(currentUserId)) {
            return false;
        }
        messages.add(new UserMessage(text, currentUserId, datePosted));
        return true;
    }
    public boolean postMessage(UUID currentUserId, String text) {
        if (!memberIds.contains(currentUserId)) {
            return false;
        }
        messages.add(new UserMessage(text, currentUserId));
        return true;
    }
    public boolean deleteMessage(UUID currentUserId, UUID messageId) {
        if (ownerId != currentUserId) {
            return false;
        }
        for (Message message : messages) {
            if (message instanceof UserMessage) {
                UserMessage userMessage = (UserMessage) message;
                if (userMessage.getId().equals(messageId)) {
                    userMessage.setIsDeleted();
                    return true;
                }
            }
        }
        return false;
    }

    public UUID getId() {
        return id;
    }
    public UUID getOwnerId() {
        return ownerId;
    }
    public String getName() {
        return name;
    }
    public boolean getIsPublic() {
        return isPublic;
    }
    public boolean getIsFlaggedForDeletion() {
        return isFlaggedForDeletion;
    }
    public ArrayList<UUID> getMemberIds() {
        return memberIds;
    }
    public ArrayList<Message> getMessages() {
        return messages;
    }
    public int getTotalMessageCount() {
        return totalMessageCount;
    }
    public String getPasswordHash() {
        return passwordHash;
    }
}
