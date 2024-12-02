package com.patrykdziurkowski.microserviceschat.presentation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.patrykdziurkowski.microserviceschat.domain.ChatRoom;

public class ChatRoomDto {
    private UUID id;
    private String name;
    private UUID ownerId;
    private boolean isPublic;
    private boolean isMember;
    private boolean isPasswordProtected;
    private int memberCount;

    private ChatRoomDto() {
    }

    public static ChatRoomDto from(ChatRoom chatRoom, UUID currentUserId) {
        ChatRoomDto chatDto = new ChatRoomDto();
        chatDto.id = chatRoom.getId();
        chatDto.name = chatRoom.getName();
        chatDto.ownerId = chatRoom.getOwnerId();
        chatDto.isPublic = chatRoom.getIsPublic();
        chatDto.isMember = chatRoom.getMemberIds().contains(currentUserId);
        chatDto.isPasswordProtected = chatRoom.getPasswordHash().isPresent();
        chatDto.memberCount = chatRoom.getMemberIds().size();

        return chatDto;
    }

    public static List<ChatRoomDto> fromList(List<ChatRoom> chatRooms, UUID currentUserId) {
        List<ChatRoomDto> chatsDto = new ArrayList<>();
        for (ChatRoom chat : chatRooms) {
            chatsDto.add(from(chat, currentUserId));
        }
        return chatsDto;
    }

    public UUID getId() {
        return this.id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getOwnerId() {
        return this.ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public boolean isIsPublic() {
        return this.isPublic;
    }

    public boolean getIsPublic() {
        return this.isPublic;
    }

    public void setIsPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public boolean isIsMember() {
        return this.isMember;
    }

    public boolean getIsMember() {
        return this.isMember;
    }

    public void setIsMember(boolean isMember) {
        this.isMember = isMember;
    }

    public int getMemberCount() {
        return this.memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public boolean getIsPasswordProtected() {
        return this.isPasswordProtected;
    }

    public void setIsPasswordProtected(boolean isPasswordProtected) {
        this.isPasswordProtected = isPasswordProtected;
    }

}
