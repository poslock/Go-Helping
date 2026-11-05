package com.gohelping;

import com.google.firebase.firestore.DocumentId;

public class Quest {
    @DocumentId
    private String questId;
    private boolean completed;
    private String chatId;
    private String title;
    private String description;
    private String location;
    private int reward;
    private String status;
    private String creatorId;
    private String helperId;
    private int reportCount; // ⬅️ BARU

    public Quest() {}

    public Quest(String title, String location, int reward, String status, String creatorId) {
        this.title = title;
        this.location = location;
        this.reward = reward;
        this.status = status;
        this.creatorId = creatorId;
        this.reportCount = 0;
    }

    // Getter & Setter
    public String getQuestId() { return questId; }
    public void setQuestId(String questId) { this.questId = questId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public int getReward() { return reward; }
    public void setReward(int reward) { this.reward = reward; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatorId() { return creatorId; }
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }

    public String getHelperId() { return helperId; }
    public void setHelperId(String helperId) { this.helperId = helperId; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public String getChatId() { return chatId; }
    public void setChatId(String chatId) { this.chatId = chatId; }

    public int getReportCount() { return reportCount; } // ⬅️ BARU
    public void setReportCount(int reportCount) { this.reportCount = reportCount; } // ⬅️ BARU
}