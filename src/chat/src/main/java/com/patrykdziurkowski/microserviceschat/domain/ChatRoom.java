package com.patrykdziurkowski.microserviceschat.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.patrykdziurkowski.microserviceschat.domain.domainevents.ChatDissolvedEvent;
import com.patrykdziurkowski.microserviceschat.domain.domainevents.MessageDeletedEvent;
import com.patrykdziurkowski.microserviceschat.domain.shared.AggreggateRoot;

import jakarta.annotation.Nullable;
import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;


@Entity
@Table(name = "chatroom")
public class ChatRoom extends AggreggateRoot {

    @Id
    private UUID id;

    private UUID ownerId;
    private String name;
    private boolean isPublic;

    // @ElementCollection
    // @CollectionTable(name = "memberIds", joinColumns = @JoinColumn(name = "chatRoomId"))
    // @Column(name = "id")
    private List<UUID> memberIds = new ArrayList<UUID>();

    // @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "chatRoom")
    private List<UUID> messageIds = new ArrayList<UUID>();

    private int totalMessageCount;

    @Nullable
    @Basic(optional = true)
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
        this.messageIds = new ArrayList<UUID>();
        this.totalMessageCount = 0;
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
        if (memberIds.isEmpty()) {
            raiseDomainEvent(new ChatDissolvedEvent(id));
            return true;
        }
        if (currentUserId == ownerId) {
            ownerId = memberIds.get(0);
        }
        return true;
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

    public List<UUID> getMemberIds() {
        return memberIds;
    }

    public List<UUID> getMessageIds() {
        return messageIds;
    }

    public int getTotalMessageCount() {
        return totalMessageCount;
    }

    public Optional<String> getPasswordHash() {
        return Optional.ofNullable(passwordHash);
    }
}
