package com.patrykdziurkowski.microserviceschat.presentation.dtos;

import java.util.List;
import java.util.UUID;

import com.patrykdziurkowski.microserviceschat.application.models.User;
import com.patrykdziurkowski.microserviceschat.domain.ChatRoom;

public class ChatRoomDetailsDto {
    private UUID id;
    private String name;
    private UUID ownerId;
    private boolean isPublic;
    private boolean isMember;
    private boolean isPasswordProtected;
    private int memberCount;
    private List<UserDto> members;

    private ChatRoomDetailsDto() {
    }

    public static ChatRoomDetailsDto from(ChatRoom chatRoom, List<User> members, UUID currentUserId) {
        ChatRoomDetailsDto chatDto = new ChatRoomDetailsDto();
        chatDto.id = chatRoom.getId();
        chatDto.name = chatRoom.getName();
        chatDto.ownerId = chatRoom.getOwnerId();
        chatDto.isPublic = chatRoom.getIsPublic();
        chatDto.isMember = chatRoom.getMemberIds().contains(currentUserId);
        chatDto.isPasswordProtected = chatRoom.getPasswordHash().isPresent();
        chatDto.memberCount = chatRoom.getMemberIds().size();
        chatDto.members = UserDto.fromList(members);
        return chatDto;
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

    public List<UserDto> getMembers() {
        return this.members;
    }

    public void setMembers(List<UserDto> members) {
        this.members = members;
    }

}
