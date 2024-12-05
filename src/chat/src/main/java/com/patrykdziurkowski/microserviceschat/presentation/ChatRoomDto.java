package com.patrykdziurkowski.microserviceschat.presentation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.patrykdziurkowski.microserviceschat.domain.ChatRoom;
import com.patrykdziurkowski.microserviceschat.domain.FavoriteChatRoom;

import io.jsonwebtoken.lang.Collections;

public class ChatRoomDto {
    private UUID id;
    private String name;
    private UUID ownerId;
    private boolean isPublic;
    private boolean isMember;
    private boolean isPasswordProtected;
    private boolean isFavorite;
    private int memberCount;

    private ChatRoomDto() {
    }

    public static ChatRoomDto from(ChatRoom chatRoom, UUID currentUserId) {
        return from(chatRoom, false, currentUserId);
    }

    public static ChatRoomDto from(ChatRoom chatRoom, boolean isFavorite, UUID currentUserId) {
        ChatRoomDto chatDto = new ChatRoomDto();
        chatDto.id = chatRoom.getId();
        chatDto.name = chatRoom.getName();
        chatDto.ownerId = chatRoom.getOwnerId();
        chatDto.isPublic = chatRoom.getIsPublic();
        chatDto.isMember = chatRoom.getMemberIds().contains(currentUserId);
        chatDto.isPasswordProtected = chatRoom.getPasswordHash().isPresent();
        chatDto.memberCount = chatRoom.getMemberIds().size();
        chatDto.isFavorite = isFavorite;

        return chatDto;
    }

    public static List<ChatRoomDto> fromList(
            List<ChatRoom> chatRooms,
            List<FavoriteChatRoom> favorites,
            UUID currentUserId) {
        List<ChatRoomDto> chatsDto = new ArrayList<>();
        for (ChatRoom chat : chatRooms) {
            boolean isFavorite = favorites.stream().anyMatch(f -> f.getChatRoomId().equals(chat.getId()));
            chatsDto.add(from(chat, isFavorite, currentUserId));
        }
        return chatsDto;
    }

    public static List<ChatRoomDto> fromList(List<ChatRoom> chatRooms, UUID currentUserId) {
        return fromList(chatRooms, Collections.emptyList(), currentUserId);
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

    public boolean isIsPasswordProtected() {
        return this.isPasswordProtected;
    }

    public boolean getIsFavorite() {
        return this.isFavorite;
    }

    public void setIsFavorite(boolean isFavorite) {
        this.isFavorite = isFavorite;
    }

}
