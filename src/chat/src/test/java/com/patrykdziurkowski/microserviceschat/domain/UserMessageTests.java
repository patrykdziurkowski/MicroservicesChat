package com.patrykdziurkowski.microserviceschat.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

public class UserMessageTests {
    @Test
    void delete_whenOwnerOfMessegeDeletesMessage_messageIsDeleted() {
        UUID msgOwner = UUID.randomUUID();
        UserMessage msg = new UserMessage(UUID.randomUUID(), "test", msgOwner);

        boolean isDeleted = msg.delete(msgOwner, UUID.randomUUID());

        assertTrue(isDeleted);
    }

    @Test
    void delete_whenOwnerOfRoomDeletesMessage_messageIsDeleted() {
        UUID msgOwner = UUID.randomUUID();
        UUID chatRoomOwner = UUID.randomUUID();
        UserMessage msg = new UserMessage(UUID.randomUUID(), "test", msgOwner);

        boolean isDeleted = msg.delete(chatRoomOwner, chatRoomOwner);

        assertTrue(isDeleted);
    }

    @Test
    void delete_whenRandomMemberDeletesMessage_messageIsNotDeleted() {
        UUID msgOwner = UUID.randomUUID();
        UUID chatMemberId = UUID.randomUUID();
        UserMessage msg = new UserMessage(UUID.randomUUID(), "test", msgOwner);

        boolean isDeleted = msg.delete(chatMemberId, UUID.randomUUID());

        assertFalse(isDeleted);
    }

}
