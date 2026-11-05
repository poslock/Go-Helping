package com.gohelping;

import com.google.firebase.Timestamp;

public class ChatRoom {

    private String roomId;
    private String questId;

    private String senderId;
    private String receiverId;

    private String senderName;
    private String receiverName;

    private String lastMessage;
    private Timestamp lastTimestamp;

    public ChatRoom() {}

    public ChatRoom(String roomId,
                    String questId,
                    String senderId,
                    String receiverId,
                    String senderName,
                    String receiverName,
                    String lastMessage,
                    Timestamp lastTimestamp) {

        this.roomId = roomId;
        this.questId = questId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.senderName = senderName;
        this.receiverName = receiverName;
        this.lastMessage = lastMessage;
        this.lastTimestamp = lastTimestamp;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getQuestId() {
        return questId;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public Timestamp getLastTimestamp() {
        return lastTimestamp;
    }
}