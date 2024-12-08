package com.patrykdziurkowski.microserviceschat.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.patrykdziurkowski.microserviceschat.domain.domainevents.ChatDissolvedEvent;
import com.patrykdziurkowski.microserviceschat.domain.shared.DomainEvent;

class ChatRoomTests {

    @Test
    void dissolve_givenValidData_dissolvesChatRoom() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true);
        UUID currentUserId = ownerId;

        chatRoom.dissolve(currentUserId);

        DomainEvent event = chatRoom.getDomainEvents().get(0);
        assertTrue(event instanceof ChatDissolvedEvent);
    }

    @Test
    void dissolve_givenInvalidData_doesNotDissolvesChatRoom() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true);
        UUID currentUserId = UUID.randomUUID();

        chatRoom.dissolve(currentUserId);

        assertTrue(chatRoom.getDomainEvents().isEmpty());
    }

    @Test
    void inviteMember_givenValidData_invitesMember() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true);
        UUID currentUserId = ownerId;
        UUID newMember = UUID.randomUUID();

        chatRoom.inviteMember(newMember, "username", currentUserId);

        assertTrue(chatRoom.getMemberIds().contains(newMember));
        assertEquals(2, chatRoom.getMemberIds().size());
    }

    @Test
    void inviteMember_givenInvalidData_doesNotInvateMember() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true);
        UUID currentUserId = UUID.randomUUID();
        UUID newMember = UUID.randomUUID();

        chatRoom.inviteMember(newMember, "username", currentUserId);

        assertEquals(1, chatRoom.getMemberIds().size());
    }

    @Test
    void invitemember_givenInvalidDataMemberAlreadyInChatRoom_doesNotInvateMember() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true);
        UUID currentUserId = ownerId;
        UUID newMember = UUID.randomUUID();
        chatRoom.inviteMember(newMember, "username", ownerId);

        chatRoom.inviteMember(newMember, "username", currentUserId);

        assertEquals(2, chatRoom.getMemberIds().size());
    }

    @Test
    void removeMember_givenValidData_removesMember() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true);
        UUID currentUserId = ownerId;
        UUID memberToRemove = UUID.randomUUID();
        chatRoom.inviteMember(memberToRemove, "username", ownerId);

        chatRoom.removeMember(memberToRemove, "username", currentUserId);

        assertEquals(1, chatRoom.getMemberIds().size());
    }

    @Test
    void removeMember_givenInvalidDataNoOwnerMemberTriesToRemoveMember_doesNotRemoveMember() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true);
        UUID currentUserId = UUID.randomUUID();
        UUID memberToRmove = UUID.randomUUID();
        chatRoom.inviteMember(memberToRmove, "username", ownerId);

        chatRoom.removeMember(memberToRmove, "username", currentUserId);

        assertEquals(2, chatRoom.getMemberIds().size());
        assertTrue(chatRoom.getMemberIds().contains(memberToRmove));
    }

    @Test
    void removeMember_givenInvalidDataOwnerMemberTriesToRemoveOwner_doesNotRemoveMember() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true);
        UUID currentUserId = ownerId;
        UUID memberToRmove = ownerId;

        chatRoom.removeMember(memberToRmove, "username", currentUserId);

        assertEquals(1, chatRoom.getMemberIds().size());
        assertTrue(chatRoom.getMemberIds().contains(ownerId));
    }

    @Test
    void removeMember_givenInvalidDataTargetMemberDoesNotExists_doesNotRemoveMember() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true);
        UUID currentUserId = ownerId;
        UUID memberToRmove = UUID.randomUUID();

        chatRoom.removeMember(memberToRmove, "username", currentUserId);

        assertEquals(1, chatRoom.getMemberIds().size());
    }

    @Test
    void join_givenValidData_joinsChatRoom() {
        UUID ownerId = UUID.randomUUID();
        String passwordHash = "password";
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true, passwordHash);
        UUID currentUserId = UUID.randomUUID();

        chatRoom.join(currentUserId, "username");

        assertEquals(2, chatRoom.getMemberIds().size());
        assertTrue(chatRoom.getMemberIds().contains(currentUserId));
    }

    @Test
    void join_givenInvalidDataMemberAlreadyInRoom_doesNotJoinChatRoom() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true);
        UUID currentUserId = UUID.randomUUID();
        chatRoom.join(currentUserId, "username");

        chatRoom.join(currentUserId, "username");

        assertEquals(2, chatRoom.getMemberIds().size());
        assertTrue(chatRoom.getMemberIds().contains(currentUserId));
    }

    @Test
    void leave_givenValidData_leavesChatRoom() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true);
        UUID currentUserId = UUID.randomUUID();
        chatRoom.join(currentUserId, "username");

        chatRoom.leave(currentUserId, "username");

        assertEquals(1, chatRoom.getMemberIds().size());
        assertTrue(!chatRoom.getMemberIds().contains(currentUserId));
    }

    @Test
    void leave_givenInvalidDataMemberNotInRoom_doesNotLeaveChatRoom() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true);
        UUID currentUserId = UUID.randomUUID();

        chatRoom.leave(currentUserId, "username");

        assertEquals(1, chatRoom.getMemberIds().size());
    }

    @Test
    void leave_givenValidDataMemberIsOwner_ownerOfRoomChanges() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true);
        UUID currentUserId = ownerId;
        UUID anotherMember = UUID.randomUUID();
        chatRoom.join(anotherMember, "username");

        chatRoom.leave(currentUserId, "username");

        assertEquals(1, chatRoom.getMemberIds().size());
        assertTrue(!chatRoom.getMemberIds().contains(ownerId));
        assertEquals(chatRoom.getOwnerId(), anotherMember);
    }

    @Test
    void leave_givenValidDataOwnerIsLastMember_roomIsDeleted() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true);
        UUID currentUserId = ownerId;

        chatRoom.leave(currentUserId, "username");

        assertTrue(chatRoom.getMemberIds().isEmpty());
        DomainEvent event = chatRoom.getDomainEvents().get(0);
        assertTrue(event instanceof ChatDissolvedEvent);
    }

}
