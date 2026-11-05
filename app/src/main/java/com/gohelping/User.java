package com.gohelping;

public class User {
    private String userId;
    private String name;
    private String email;
    private String bio;
    private boolean isPrivate;
    private boolean isBlocked;
    private long blockedUntil;
    private String blockedReason;
    private String role;
    private long points;
    private long questsComplete;
    private long totalGdEarned;
    private String rating;
    private long level;

    public User() {}

    // Getter & Setter
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public boolean isPrivate() { return isPrivate; }
    public void setPrivate(boolean aPrivate) { isPrivate = aPrivate; }

    public boolean isBlocked() { return isBlocked; }
    public void setBlocked(boolean blocked) { isBlocked = blocked; }

    public long getBlockedUntil() { return blockedUntil; }
    public void setBlockedUntil(long blockedUntil) { this.blockedUntil = blockedUntil; }

    public String getBlockedReason() { return blockedReason; }
    public void setBlockedReason(String blockedReason) { this.blockedReason = blockedReason; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public long getPoints() { return points; }
    public void setPoints(long points) { this.points = points; }

    public long getQuestsComplete() { return questsComplete; }
    public void setQuestsComplete(long questsComplete) { this.questsComplete = questsComplete; }

    public long getTotalGdEarned() { return totalGdEarned; }
    public void setTotalGdEarned(long totalGdEarned) { this.totalGdEarned = totalGdEarned; }

    public String getRating() { return rating; }
    public void setRating(String rating) { this.rating = rating; }

    public long getLevel() { return level; }
    public void setLevel(long level) { this.level = level; }
}