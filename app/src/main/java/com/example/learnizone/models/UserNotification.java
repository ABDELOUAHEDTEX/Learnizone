package com.example.learnizone.models;

import com.google.firebase.firestore.DocumentSnapshot;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UserNotification implements Serializable {
    private String id;
    private String userId;
    private NotificationType type;
    private String title;
    private String message;
    private String imageUrl;
    private Priority priority;
    private Date createdAt;
    private Date scheduledAt;
    private Date sentAt;
    private Date readAt;
    private boolean isRead;
    private boolean isSent;
    private Map<String, Object> data; // Données additionnelles (courseId, quizId, etc.)
    
    public UserNotification() {
        this.createdAt = new Date();
        this.isRead = false;
        this.isSent = false;
        this.priority = Priority.NORMAL;
        this.data = new HashMap<>();
    }
    
    // Énumérations
    public enum NotificationType {
        COURSE_REMINDER("course_reminder", "Rappel de cours"),
        QUIZ_DUE("quiz_due", "Quiz à faire"),
        QUIZ_RESULT("quiz_result", "Résultat de quiz"),
        ACHIEVEMENT("achievement", "Réussite"),
        NEW_CONTENT("new_content", "Nouveau contenu"),
        MESSAGE_FROM_INSTRUCTOR("message_instructor", "Message d'instructeur"),
        COURSE_COMPLETED("course_completed", "Cours terminé"),
        STREAK_REMINDER("streak_reminder", "Rappel de série"),
        GENERAL("general", "Général");
        
        private final String value;
        private final String displayName;
        
        NotificationType(String value, String displayName) {
            this.value = value;
            this.displayName = displayName;
        }
        
        public String getValue() {
            return value;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public static NotificationType fromString(String value) {
            for (NotificationType type : values()) {
                if (type.value.equals(value)) {
                    return type;
                }
            }
            return GENERAL;
        }
    }
    
    public enum Priority {
        LOW("low", 1),
        NORMAL("normal", 2),
        HIGH("high", 3),
        URGENT("urgent", 4);
        
        private final String value;
        private final int level;
        
        Priority(String value, int level) {
            this.value = value;
            this.level = level;
        }
        
        public String getValue() {
            return value;
        }
        
        public int getLevel() {
            return level;
        }
        
        public static Priority fromString(String value) {
            for (Priority priority : values()) {
                if (priority.value.equals(value)) {
                    return priority;
                }
            }
            return NORMAL;
        }
    }
    
    // Méthode pour créer depuis Firestore
    public static UserNotification fromDocument(DocumentSnapshot document) {
        UserNotification notification = new UserNotification();
        notification.id = document.getId();
        notification.userId = document.getString("userId");
        notification.type = NotificationType.fromString(document.getString("type"));
        notification.title = document.getString("title");
        notification.message = document.getString("message");
        notification.imageUrl = document.getString("imageUrl");
        notification.priority = Priority.fromString(document.getString("priority"));
        notification.createdAt = document.getDate("createdAt");
        notification.scheduledAt = document.getDate("scheduledAt");
        notification.sentAt = document.getDate("sentAt");
        notification.readAt = document.getDate("readAt");
        
        Boolean isRead = document.getBoolean("isRead");
        notification.isRead = isRead != null ? isRead : false;
        
        Boolean isSent = document.getBoolean("isSent");
        notification.isSent = isSent != null ? isSent : false;
        
        Map<String, Object> data = (Map<String, Object>) document.get("data");
        notification.data = data != null ? data : new HashMap<>();
        
        return notification;
    }
    
    // Convertir en Map pour Firestore
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("type", type.getValue());
        map.put("title", title);
        map.put("message", message);
        map.put("imageUrl", imageUrl);
        map.put("priority", priority.getValue());
        map.put("createdAt", createdAt);
        map.put("scheduledAt", scheduledAt);
        map.put("sentAt", sentAt);
        map.put("readAt", readAt);
        map.put("isRead", isRead);
        map.put("isSent", isSent);
        map.put("data", data);
        return map;
    }
    
    // Getters et Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public NotificationType getType() {
        return type;
    }
    
    public void setType(NotificationType type) {
        this.type = type;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public Priority getPriority() {
        return priority;
    }
    
    public void setPriority(Priority priority) {
        this.priority = priority;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    public Date getScheduledAt() {
        return scheduledAt;
    }
    
    public void setScheduledAt(Date scheduledAt) {
        this.scheduledAt = scheduledAt;
    }
    
    public Date getSentAt() {
        return sentAt;
    }
    
    public void setSentAt(Date sentAt) {
        this.sentAt = sentAt;
    }
    
    public Date getReadAt() {
        return readAt;
    }
    
    public void setReadAt(Date readAt) {
        this.readAt = readAt;
    }
    
    public boolean isRead() {
        return isRead;
    }
    
    public void setRead(boolean read) {
        isRead = read;
        if (read && readAt == null) {
            readAt = new Date();
        }
    }
    
    public boolean isSent() {
        return isSent;
    }
    
    public void setSent(boolean sent) {
        isSent = sent;
        if (sent && sentAt == null) {
            sentAt = new Date();
        }
    }
    
    public Map<String, Object> getData() {
        return data;
    }
    
    public void setData(Map<String, Object> data) {
        this.data = data;
    }
    
    // Méthodes utilitaires pour les données
    public void addData(String key, Object value) {
        if (data == null) {
            data = new HashMap<>();
        }
        data.put(key, value);
    }
    
    public Object getData(String key) {
        return data != null ? data.get(key) : null;
    }
    
    public String getDataAsString(String key) {
        Object value = getData(key);
        return value != null ? value.toString() : null;
    }
    
    // Méthodes de convenance
    public boolean isScheduled() {
        return scheduledAt != null && scheduledAt.after(new Date());
    }
    
    public boolean isPending() {
        return !isSent && (scheduledAt == null || scheduledAt.before(new Date()));
    }
    
    public boolean isExpired() {
        // Une notification expire après 30 jours si elle n'est pas lue
        if (isRead) return false;
        
        Date expirationDate = new Date(createdAt.getTime() + (30L * 24 * 60 * 60 * 1000));
        return new Date().after(expirationDate);
    }
    
    public String getFormattedTimeAgo() {
        if (createdAt == null) return "";
        
        long diffMillis = new Date().getTime() - createdAt.getTime();
        long diffMinutes = diffMillis / (60 * 1000);
        long diffHours = diffMillis / (60 * 60 * 1000);
        long diffDays = diffMillis / (24 * 60 * 60 * 1000);
        
        if (diffMinutes < 1) {
            return "À l'instant";
        } else if (diffMinutes < 60) {
            return diffMinutes + " min";
        } else if (diffHours < 24) {
            return diffHours + "h";
        } else if (diffDays < 7) {
            return diffDays + "j";
        } else {
            return java.text.DateFormat.getDateInstance().format(createdAt);
        }
    }
    
    @Override
    public String toString() {
        return "UserNotification{" +
                "id='" + id + '\'' +
                ", type=" + type +
                ", title='" + title + '\'' +
                ", isRead=" + isRead +
                ", createdAt=" + createdAt +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        UserNotification that = (UserNotification) obj;
        return id != null ? id.equals(that.id) : that.id == null;
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
} 