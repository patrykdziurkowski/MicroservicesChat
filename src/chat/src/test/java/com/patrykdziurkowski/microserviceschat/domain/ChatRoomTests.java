package com.patrykdziurkowski.microserviceschat.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.Test;

public class ChatRoomTests {

    @Test
    public void postMessage_givenValidData_addsMessageToList() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true);
        UUID currentUserId = ownerId;
        String text = "Test message";

        chatRoom.postMessage(currentUserId, text);

        assert(chatRoom.getMessages().size() == 1);
    }

    @Test
    public void postMessage_givenInvalidData_doesNotAddMessageToList() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true);
        UUID currentUserId = UUID.randomUUID();
        String text = "Test message";

        chatRoom.postMessage(currentUserId, text);

        assert(chatRoom.getMessages().size() == 0);
    }

    @Test
    public void postMessage_givenValidDataDatePostedIsProvided_addsMessageToList() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true);
        UUID currentUserId = ownerId;
        String text = "Test message";
        LocalDateTime datePosted = LocalDateTime.now();

        chatRoom.postMessage(currentUserId, text, datePosted);

        assert(chatRoom.getMessages().size() == 1);
    }


    @Test
    public void deleteMessage_givenValidData_deletesMessageFromList() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true);
        UUID currentUserId = ownerId;
        String text = "Test message";
        chatRoom.postMessage(ownerId, text);
        UUID messegeId = chatRoom.getMessages().get(0).getId();

        chatRoom.deleteMessage(currentUserId, messegeId);

        UserMessage userMessage = (UserMessage) chatRoom.getMessages().get(0);
        assert(chatRoom.getMessages().size() == 1);
        assert(userMessage.getIsDeleted() == true);
    }

    @Test
    public void deleteMessage_givenInvalidDataNotOwnerMemberTriesToDeleteMessage_doesNotDeleteMessageFromList() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true);
        UUID currentUserId = UUID.randomUUID();
        String text = "Test message";
        chatRoom.postMessage(ownerId, text);
        UUID messegeId = chatRoom.getMessages().get(0).getId();

        chatRoom.deleteMessage(currentUserId, messegeId);

        assert(chatRoom.getMessages().size() == 1);
    }

    @Test
    public void deleteMessage_givenInvalidDataMessageDoesntExist_doesNotDeleteMessageFromList() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true);
        UUID currentUserId = UUID.randomUUID();
        String text = "Test message";
        chatRoom.postMessage(ownerId, text);
        UUID messegeId = UUID.randomUUID();

        chatRoom.deleteMessage(currentUserId, messegeId);

        assert(chatRoom.getMessages().size() == 1);
    }

    @Test
    public void dissolve_givenValidData_dissolvesChatRoom() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true);
        UUID currentUserId = ownerId;

        chatRoom.dissolve(currentUserId);

        assert(chatRoom.getIsFlaggedForDeletion() == true);
    }

    @Test
    public void dissolve_givenInvalidData_doesNotDissolvesChatRoom() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true);
        UUID currentUserId = UUID.randomUUID();

        chatRoom.dissolve(currentUserId);

        assert(chatRoom.getIsFlaggedForDeletion() == false);
    }

    @Test
    public void invateMember_givenValidData_invatesMember() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true);
        UUID currentUserId = ownerId;
        UUID newMember = UUID.randomUUID();

        chatRoom.invateMember(newMember, currentUserId);

        assert(chatRoom.getMemberIds().contains(newMember));
        assert(chatRoom.getMemberIds().size() == 2);
    }

    @Test
    public void invateMember_givenInvalidData_doesNotInvateMember() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true);
        UUID currentUserId = UUID.randomUUID();
        UUID newMember = UUID.randomUUID();

        chatRoom.invateMember(newMember, currentUserId);

        assert(chatRoom.getMemberIds().size() == 1);
    }

    @Test
    public void invitemember_givenIvalidDataMemberAlreadyInChatRoom_doesNotInvateMember() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true);
        UUID currentUserId = ownerId;
        UUID newMember = UUID.randomUUID();
        chatRoom.invateMember(newMember, ownerId);

        chatRoom.invateMember(newMember, currentUserId);

        assert(chatRoom.getMemberIds().size() == 2);
    }

    @Test
    public void removeMember_givenValidData_removesMember() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true);
        UUID currentUserId = ownerId;
        UUID memberToRemove = UUID.randomUUID();
        chatRoom.invateMember(memberToRemove, ownerId);

        chatRoom.removeMember(memberToRemove, currentUserId);

        assert(chatRoom.getMemberIds().size() == 1);
    }
    
    @Test
    public void removeMember_givenInvalidDataNoOwnerMemberTriesToRemoveMember_doesNotRemoveMember() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true);
        UUID currentUserId = UUID.randomUUID();
        UUID memberToRmove = UUID.randomUUID();
        chatRoom.invateMember(memberToRmove, ownerId);

        chatRoom.removeMember(memberToRmove, currentUserId);

        assert(chatRoom.getMemberIds().size() == 2);
        assert(chatRoom.getMemberIds().contains(memberToRmove));
    }

    @Test
    public void removeMember_givenInvalidDataOwnerMemberTriesToRemoveOwner_doesNotRemoveMember() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true);
        UUID currentUserId = ownerId;
        UUID memberToRmove = ownerId;

        chatRoom.removeMember(memberToRmove, currentUserId);

        assert(chatRoom.getMemberIds().size() == 1);
        assert(chatRoom.getMemberIds().contains(ownerId));
    }

    @Test
    public void removeMember_givenInvalidDataTargetMemberDoesNotExists_doesNotRemoveMember() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true);
        UUID currentUserId = ownerId;
        UUID memberToRmove = UUID.randomUUID();

        chatRoom.removeMember(memberToRmove, currentUserId);

        assert(chatRoom.getMemberIds().size() == 1);
    }

    @Test
    public void join_givenValidData_joinsChatRoom() {
        UUID ownerId = UUID.randomUUID();
        String passwordHash = "password";
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true, passwordHash);
        UUID currentUserId = UUID.randomUUID();
        String password = "password";

        chatRoom.join(currentUserId, password);

        assert(chatRoom.getMemberIds().size() == 2);
        assert(chatRoom.getMemberIds().contains(currentUserId));
    }

    @Test
    public void join_givenInvalidDataWrongPassword_doesNotJoinChatRoom() {
        UUID ownerId = UUID.randomUUID();
        String passwordHash = "password";
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true, passwordHash);
        UUID currentUserId = UUID.randomUUID();
        String password = "wrongPassword";

        chatRoom.join(currentUserId, password);

        assert(chatRoom.getMemberIds().size() == 1);
        assert(!chatRoom.getMemberIds().contains(currentUserId));
    }

    @Test
    public void join_givenValidDataNoPassword_joinsChatRoom() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true);
        UUID currentUserId = UUID.randomUUID();

        chatRoom.join(currentUserId);

        assert(chatRoom.getMemberIds().size() == 2);
        assert(chatRoom.getMemberIds().contains(currentUserId));
    }

    @Test
    public void join_givenInvalidDataMemberAlreadyInRoom_doesNotJoinChatRoom() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true);
        UUID currentUserId = UUID.randomUUID();
        chatRoom.join(currentUserId);

        chatRoom.join(currentUserId);

        assert(chatRoom.getMemberIds().size() == 2);
        assert(chatRoom.getMemberIds().contains(currentUserId));
    }
    
    @Test
    public void leave_givenValidData_leavesChatRoom() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true);
        UUID currentUserId = UUID.randomUUID();
        chatRoom.join(currentUserId);

        chatRoom.leave(currentUserId);

        assert(chatRoom.getMemberIds().size() == 1);
        assert(!chatRoom.getMemberIds().contains(currentUserId));
    }

    @Test
    public void leave_givenInvalidDataMemberNotInRoom_doesNotLeaveChatRoom() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true);
        UUID currentUserId = UUID.randomUUID();

        chatRoom.leave(currentUserId);

        assert(chatRoom.getMemberIds().size() == 1);
    }

    @Test
    public void leave_givenValidDataMemberIsOwner_ownerOfRoomChanges() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true);
        UUID currentUserId = ownerId;
        UUID anotherMember = UUID.randomUUID();
        chatRoom.join(anotherMember);
        

        chatRoom.leave(currentUserId);

        assert(chatRoom.getMemberIds().size() == 1);
        assert(!chatRoom.getMemberIds().contains(ownerId));
        assert(chatRoom.getOwnerId().equals(anotherMember));
    }

    @Test
    public void leave_givenValidDataOwnerIsLastMember_roomIsDeleted() {
        UUID ownerId = UUID.randomUUID();
        ChatRoom chatRoom = new ChatRoom(ownerId, "Test Room", true);
        UUID currentUserId = ownerId;

        chatRoom.leave(currentUserId);

        assert(chatRoom.getMemberIds().size() == 0);
        assert(chatRoom.getIsFlaggedForDeletion());
    }




}
