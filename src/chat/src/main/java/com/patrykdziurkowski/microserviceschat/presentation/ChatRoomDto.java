package com.patrykdziurkowski.microserviceschat.presentation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.patrykdziurkowski.microserviceschat.domain.ChatRoom;

public class ChatRoomDto {
    private UUID id;
    private String name;
    private boolean isPublic;
    private boolean isMember;
    private boolean isPasswordProtected;
    private List<UUID> memberIds = new ArrayList<>();
    private int memberCount;

    private ChatRoomDto() {
    }

    public static ChatRoomDto from(ChatRoom chatRoom, UUID userId) {
        ChatRoomDto chatDto = new ChatRoomDto();
        chatDto.id = chatRoom.getId();
        chatDto.name = chatRoom.getName();
        chatDto.isPublic = chatRoom.getIsPublic();
        chatDto.memberIds = chatRoom.getMemberIds();
        chatDto.isMember = chatDto.memberIds.contains(userId);
        chatDto.isPasswordProtected = chatRoom.getPasswordHash().isPresent();
        chatDto.memberCount = chatDto.memberIds.size();

        return chatDto;
    }

    public static List<ChatRoomDto> fromList(List<ChatRoom> chatRooms, UUID userId) {
        List<ChatRoomDto> chatsDto = new ArrayList<>();
        for (ChatRoom chat : chatRooms) {
            chatsDto.add(from(chat, userId));
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

    public List<UUID> getMemberIds() {
        return this.memberIds;
    }

    public void setMemberIds(List<UUID> memberIds) {
        this.memberIds = memberIds;
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
