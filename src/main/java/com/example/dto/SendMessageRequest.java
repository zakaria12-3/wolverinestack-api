package com.example.dto;

public class SendMessageRequest {

    private Long receiverId;
    private Long conversationId;
    private String content;

    public Long getReceiverId()              { return receiverId; }
    public void setReceiverId(Long receiverId){ this.receiverId = receiverId; }

    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }

    public String getContent()             { return content; }
    public void setContent(String content) { this.content = content; }
}
