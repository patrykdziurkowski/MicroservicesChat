package com.patrykdziurkowski.microserviceschat.domain;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.patrykdziurkowski.microserviceschat.domain.domainevents.ChatDissolvedEvent;
import com.patrykdziurkowski.microserviceschat.domain.domainevents.MessageDeletedEvent;
import com.patrykdziurkowski.microserviceschat.domain.shared.DomainEvent;

public class ChatRoomTests {

    @Test
    public void postMessage_givenValidData_addsMessageToList() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true);
        UUID currentUserId = ownerId;

        chatRoom.postMessage(currentUserId, "Test message");

        assertTrue(chatRoom.getMessages().size() == 1);
    }

    @Test
    public void postMessage_givenInvalidData_doesNotAddMessageToList() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true);
        UUID currentUserId = UUID.randomUUID();

        chatRoom.postMessage(currentUserId, "Test message");

        assertTrue(chatRoom.getMessages().size() == 0);
    }

    @Test
    public void postMessage_givenValidDataDatePostedIsProvided_addsMessageToList() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true);
        UUID currentUserId = ownerId;
        LocalDateTime datePosted = LocalDateTime.now();

        chatRoom.postMessage(currentUserId, "Test message", datePosted);

        assertTrue(chatRoom.getMessages().size() == 1);
    }

    @Test
    public void deleteMessage_givenValidDataOwnerOfChat_deletesMessageFromList() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true);
        UUID currentUserId = UUID.randomUUID();
        chatRoom.inviteMember(currentUserId, ownerId);
        chatRoom.postMessage(currentUserId, "Test message");
        UUID messageId = chatRoom.getMessages().get(0).getId();

        chatRoom.deleteMessage(currentUserId, messageId);

        assertTrue(chatRoom.getMessages().size() == 1);
        DomainEvent event = chatRoom.getDomainEvents().get(0);
        assertTrue(event instanceof MessageDeletedEvent);
    }

    @Test
    public void deleteMessage_givenValidDataOwnerOfMessage_deletesMessageFromList() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true);
        UUID currentUserId = UUID.randomUUID();
        chatRoom.inviteMember(currentUserId, ownerId);
        chatRoom.postMessage(currentUserId, "Test message");
        UUID messageId = chatRoom.getMessages().get(0).getId();

        chatRoom.deleteMessage(currentUserId, messageId);

        assertTrue(chatRoom.getMessages().size() == 1);
        DomainEvent event = chatRoom.getDomainEvents().get(0);
        assertTrue(event instanceof MessageDeletedEvent);
    }

    @Test
    public void deleteMessage_givenInvalidDataNotOwnerMemberTriesToDeleteMessage_doesNotDeleteMessageFromList() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true);
        UUID currentUserId = UUID.randomUUID();
        chatRoom.postMessage(ownerId, "Test message");
        UUID messageId = chatRoom.getMessages().get(0).getId();

        chatRoom.deleteMessage(currentUserId, messageId);

        assertTrue(chatRoom.getMessages().size() == 1);
    }

    @Test
    public void deleteMessage_givenInvalidDataMessageDoesntExist_doesNotDeleteMessageFromList() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true);
        UUID currentUserId = UUID.randomUUID();
        chatRoom.postMessage(ownerId, "Test message");
        UUID messageId = UUID.randomUUID();

        chatRoom.deleteMessage(currentUserId, messageId);

        assertTrue(chatRoom.getMessages().size() == 1);
    }

    @Test
    public void dissolve_givenValidData_dissolvesChatRoom() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true);
        UUID currentUserId = ownerId;

        chatRoom.dissolve(currentUserId);

        DomainEvent event = chatRoom.getDomainEvents().get(0);
        assertTrue(event instanceof ChatDissolvedEvent);
    }

    @Test
    public void dissolve_givenInvalidData_doesNotDissolvesChatRoom() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true);
        UUID currentUserId = UUID.randomUUID();

        chatRoom.dissolve(currentUserId);

        assertTrue(chatRoom.getDomainEvents().isEmpty());
    }

    @Test
    public void invateMember_givenValidData_invatesMember() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true);
        UUID currentUserId = ownerId;
        UUID newMember = UUID.randomUUID();

        chatRoom.inviteMember(newMember, currentUserId);

        assertTrue(chatRoom.getMemberIds().contains(newMember));
        assertTrue(chatRoom.getMemberIds().size() == 2);
    }

    @Test
    public void invateMember_givenInvalidData_doesNotInvateMember() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true);
        UUID currentUserId = UUID.randomUUID();
        UUID newMember = UUID.randomUUID();

        chatRoom.inviteMember(newMember, currentUserId);

        assertTrue(chatRoom.getMemberIds().size() == 1);
    }

    @Test
    public void invitemember_givenIvalidDataMemberAlreadyInChatRoom_doesNotInvateMember() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true);
        UUID currentUserId = ownerId;
        UUID newMember = UUID.randomUUID();
        chatRoom.inviteMember(newMember, ownerId);

        chatRoom.inviteMember(newMember, currentUserId);

        assertTrue(chatRoom.getMemberIds().size() == 2);
    }

    @Test
    public void removeMember_givenValidData_removesMember() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true);
        UUID currentUserId = ownerId;
        UUID memberToRemove = UUID.randomUUID();
        chatRoom.inviteMember(memberToRemove, ownerId);

        chatRoom.removeMember(memberToRemove, currentUserId);

        assertTrue(chatRoom.getMemberIds().size() == 1);
    }

    @Test
    public void removeMember_givenInvalidDataNoOwnerMemberTriesToRemoveMember_doesNotRemoveMember() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true);
        UUID currentUserId = UUID.randomUUID();
        UUID memberToRmove = UUID.randomUUID();
        chatRoom.inviteMember(memberToRmove, ownerId);

        chatRoom.removeMember(memberToRmove, currentUserId);

        assertTrue(chatRoom.getMemberIds().size() == 2);
        assertTrue(chatRoom.getMemberIds().contains(memberToRmove));
    }

    @Test
    public void removeMember_givenInvalidDataOwnerMemberTriesToRemoveOwner_doesNotRemoveMember() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true);
        UUID currentUserId = ownerId;
        UUID memberToRmove = ownerId;

        chatRoom.removeMember(memberToRmove, currentUserId);

        assertTrue(chatRoom.getMemberIds().size() == 1);
        assertTrue(chatRoom.getMemberIds().contains(ownerId));
    }

    @Test
    public void removeMember_givenInvalidDataTargetMemberDoesNotExists_doesNotRemoveMember() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true);
        UUID currentUserId = ownerId;
        UUID memberToRmove = UUID.randomUUID();

        chatRoom.removeMember(memberToRmove, currentUserId);

        assertTrue(chatRoom.getMemberIds().size() == 1);
    }

    @Test
    public void join_givenValidData_joinsChatRoom() {
        UUID ownerId = UUID.randomUUID();
        String passwordHash = "password";
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true, passwordHash);
        UUID currentUserId = UUID.randomUUID();
        String password = "password";

        chatRoom.join(currentUserId, password);

        assertTrue(chatRoom.getMemberIds().size() == 2);
        assertTrue(chatRoom.getMemberIds().contains(currentUserId));
    }

    @Test
    public void join_givenInvalidDataWrongPassword_doesNotJoinChatRoom() {
        UUID ownerId = UUID.randomUUID();
        String passwordHash = "password";
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true, passwordHash);
        UUID currentUserId = UUID.randomUUID();
        String password = "wrongPassword";

        chatRoom.join(currentUserId, password);

        assertTrue(chatRoom.getMemberIds().size() == 1);
        assertTrue(!chatRoom.getMemberIds().contains(currentUserId));
    }

    @Test
    public void join_givenValidDataNoPassword_joinsChatRoom() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true);
        UUID currentUserId = UUID.randomUUID();

        chatRoom.join(currentUserId);

        assertTrue(chatRoom.getMemberIds().size() == 2);
        assertTrue(chatRoom.getMemberIds().contains(currentUserId));
    }

    @Test
    public void join_givenInvalidDataMemberAlreadyInRoom_doesNotJoinChatRoom() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true);
        UUID currentUserId = UUID.randomUUID();
        chatRoom.join(currentUserId);

        chatRoom.join(currentUserId);

        assertTrue(chatRoom.getMemberIds().size() == 2);
        assertTrue(chatRoom.getMemberIds().contains(currentUserId));
    }

    @Test
    public void leave_givenValidData_leavesChatRoom() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true);
        UUID currentUserId = UUID.randomUUID();
        chatRoom.join(currentUserId);

        chatRoom.leave(currentUserId);

        assertTrue(chatRoom.getMemberIds().size() == 1);
        assertTrue(!chatRoom.getMemberIds().contains(currentUserId));
    }

    @Test
    public void leave_givenInvalidDataMemberNotInRoom_doesNotLeaveChatRoom() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true);
        UUID currentUserId = UUID.randomUUID();

        chatRoom.leave(currentUserId);

        assertTrue(chatRoom.getMemberIds().size() == 1);
    }

    @Test
    public void leave_givenValidDataMemberIsOwner_ownerOfRoomChanges() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true);
        UUID currentUserId = ownerId;
        UUID anotherMember = UUID.randomUUID();
        chatRoom.join(anotherMember);

        chatRoom.leave(currentUserId);

        assertTrue(chatRoom.getMemberIds().size() == 1);
        assertTrue(!chatRoom.getMemberIds().contains(ownerId));
        assertTrue(chatRoom.getOwnerId().equals(anotherMember));
    }

    @Test
    public void leave_givenValidDataOwnerIsLastMember_roomIsDeleted() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true);
        UUID currentUserId = ownerId;

        chatRoom.leave(currentUserId);

        assertTrue(chatRoom.getMemberIds().size() == 0);
        DomainEvent event = chatRoom.getDomainEvents().get(0);
        assertTrue(event instanceof ChatDissolvedEvent);
    }

}
