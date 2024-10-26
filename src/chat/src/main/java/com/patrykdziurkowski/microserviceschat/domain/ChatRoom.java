package com.patrykdziurkowski.microserviceschat.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import com.patrykdziurkowski.microserviceschat.domain.domainevents.ChatDissolvedEvent;
import com.patrykdziurkowski.microserviceschat.domain.domainevents.MessageDeletedEvent;
import com.patrykdziurkowski.microserviceschat.domain.shared.AggreggateRoot;

import jakarta.annotation.Nullable;

public class ChatRoom extends AggreggateRoot {
    private UUID id;
    private UUID ownerId;
    private String name;
    private boolean isPublic;
    private ArrayList<UUID> memberIds = new ArrayList<UUID>();
    private ArrayList<Message> messages = new ArrayList<Message>();
    private int totalMessageCount;
    @Nullable
    private String passwordHash;

    ChatRoom() {
    }

    public ChatRoom(UUID ownerId, String name, boolean isPublic) {
        this(ownerId, name, isPublic, null);
    }

    public ChatRoom(UUID ownerId, String name, boolean isPublic, String passwordHash) {
        this.id = UUID.randomUUID();
        this.ownerId = ownerId;
        this.name = name;
        this.isPublic = isPublic;
        this.passwordHash = passwordHash;
        this.memberIds = new ArrayList<UUID>();
        this.messages = new ArrayList<Message>();
        memberIds.add(ownerId);
    }

    public boolean dissolve(UUID currentUserId) {
        if (currentUserId != ownerId) {
            return false;
        }
        raiseDomainEvent(new ChatDissolvedEvent(id));
        return true;
    }

    public boolean inviteMember(UUID newMemberId, UUID currentUserId) {
        if (isPublic == false && currentUserId != ownerId) {
            return false;
        }
        if (getPasswordHash().isPresent() && currentUserId != ownerId) {
            return false;
        }
        if (memberIds.contains(newMemberId)) {
            return false;
        }
        if (memberIds.contains(currentUserId) == false) {
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

    public boolean join(UUID currentUserId) {
        return join(currentUserId, null);
    }

    public boolean join(UUID currentUserId, String givenPasswordHash) {
        if (getPasswordHash().isPresent() && givenPasswordHash.isEmpty()) {
            return false;
        }
        if (getPasswordHash().isPresent()
                && getPasswordHash().get().equals(givenPasswordHash) == false) {
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
            raiseDomainEvent(new ChatDissolvedEvent(id));
            return true;
        }
        if (currentUserId == ownerId) {
            ownerId = memberIds.get(0);
        }
        return true;
    }

    public boolean postMessage(UUID currentUserId, String text) {
        return postMessage(currentUserId, text, LocalDateTime.now());
    }

    public boolean postMessage(UUID currentUserId, String text, LocalDateTime datePosted) {
        if (memberIds.contains(currentUserId) == false) {
            return false;
        }
        messages.add(new UserMessage(text, currentUserId, datePosted));
        return true;
    }

    public boolean deleteMessage(UUID currentUserId, UUID messageId) {
        for (Message message : messages) {
            if (message instanceof UserMessage == false) {
                continue;
            }
            UserMessage userMessage = (UserMessage) message;
            if (userMessage.getId().equals(messageId) == false) {
                continue;
            }
            boolean hasDeletePermissions = currentUserId == ownerId || currentUserId.equals(userMessage.getOwnerId());
            if (hasDeletePermissions == false) {
                return false;
            }
            raiseDomainEvent(new MessageDeletedEvent(messageId));
            return true;
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

    public ArrayList<UUID> getMemberIds() {
        return memberIds;
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public int getTotalMessageCount() {
        return totalMessageCount;
    }

    public Optional<String> getPasswordHash() {
        return Optional.ofNullable(passwordHash);
    }
}
