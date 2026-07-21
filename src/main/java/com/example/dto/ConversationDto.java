package com.example.dto;

import java.time.LocalDateTime;


public class ConversationDto {

    private Long userId;
    private Long id;
    private Long participantId;
    private String username;
    private String participantName;
    private String avatarUrl;
    private String participantAvatar;
    private String role;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private long unreadCount;

    public ConversationDto() {}


    public Long getUserId()              { return userId; }
    public void setUserId(Long userId)   {
        this.userId = userId;
        this.id = userId;
        this.participantId = userId;
    }

    public Long getId() { return id != null ? id : userId; }
    public void setId(Long id) { this.id = id; }

    public Long getParticipantId() { return participantId != null ? participantId : userId; }
    public void setParticipantId(Long participantId) { this.participantId = participantId; }

    public String getUsername()              { return username; }
    public void setUsername(String username) {
        this.username = username;
        this.participantName = username;
    }

    public String getParticipantName() { return participantName != null ? participantName : username; }
    public void setParticipantName(String participantName) { this.participantName = participantName; }

    public String getAvatarUrl()               { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
        this.participantAvatar = avatarUrl;
    }

    public String getParticipantAvatar() { return participantAvatar != null ? participantAvatar : avatarUrl; }
    public void setParticipantAvatar(String participantAvatar) { this.participantAvatar = participantAvatar; }

    public String getRole()            { return role; }
    public void setRole(String role)   { this.role = role; }

    public String getLastMessage()                 { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public LocalDateTime getLastMessageAt()                    { return lastMessageAt; }
    public void setLastMessageAt(LocalDateTime lastMessageAt)  { this.lastMessageAt = lastMessageAt; }

    public long getUnreadCount()             { return unreadCount; }
    public void setUnreadCount(long unreadCount){ this.unreadCount = unreadCount; }
}
