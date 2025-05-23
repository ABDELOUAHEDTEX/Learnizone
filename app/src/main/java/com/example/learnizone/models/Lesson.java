package com.example.learnizone.models;

import com.google.firebase.firestore.DocumentSnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lesson implements Serializable {
    private String lessonId;
    private String courseId;
    private String sectionId;
    private String title;
    private String description;
    private LessonType type;
    private String content; // Contenu textuel ou HTML
    private String videoUrl;
    private String audioUrl;
    private List<LessonResource> resources;
    private int duration; // Durée en minutes
    private int orderIndex;
    private boolean isPublished;
    private boolean isFree; // Leçon gratuite ou payante
    private Date createdAt;
    private Date updatedAt;
    private Map<String, Object> metadata;

    public enum LessonType {
        VIDEO("video", "Vidéo"),
        AUDIO("audio", "Audio"),
        TEXT("text", "Texte"),
        INTERACTIVE("interactive", "Interactif"),
        QUIZ("quiz", "Quiz"),
        ASSIGNMENT("assignment", "Devoir"),
        LIVE_SESSION("live_session", "Session en direct"),
        DOCUMENT("document", "Document");

        private final String value;
        private final String displayName;

        LessonType(String value, String displayName) {
            this.value = value;
            this.displayName = displayName;
        }

        public String getValue() {
            return value;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static LessonType fromString(String value) {
            for (LessonType type : values()) {
                if (type.value.equals(value)) {
                    return type;
                }
            }
            return TEXT;
        }
    }

    public Lesson() {
        this.resources = new ArrayList<>();
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.isPublished = true;
        this.isFree = false;
        this.metadata = new HashMap<>();
    }

    public static Lesson fromDocument(DocumentSnapshot document) {
        Lesson lesson = new Lesson();
        lesson.lessonId = document.getId();
        lesson.courseId = document.getString("courseId");
        lesson.sectionId = document.getString("sectionId");
        lesson.title = document.getString("title");
        lesson.description = document.getString("description");
        lesson.type = LessonType.fromString(document.getString("type"));
        lesson.content = document.getString("content");
        lesson.videoUrl = document.getString("videoUrl");
        lesson.audioUrl = document.getString("audioUrl");

        Long duration = document.getLong("duration");
        lesson.duration = duration != null ? duration.intValue() : 0;

        Long orderIndex = document.getLong("orderIndex");
        lesson.orderIndex = orderIndex != null ? orderIndex.intValue() : 0;

        Boolean isPublished = document.getBoolean("isPublished");
        lesson.isPublished = isPublished != null ? isPublished : true;

        Boolean isFree = document.getBoolean("isFree");
        lesson.isFree = isFree != null ? isFree : false;

        lesson.createdAt = document.getDate("createdAt");
        lesson.updatedAt = document.getDate("updatedAt");

        Map<String, Object> metadata = (Map<String, Object>) document.get("metadata");
        lesson.metadata = metadata != null ? metadata : new HashMap<>();

        return lesson;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("courseId", courseId);
        map.put("sectionId", sectionId);
        map.put("title", title);
        map.put("description", description);
        map.put("type", type.getValue());
        map.put("content", content);
        map.put("videoUrl", videoUrl);
        map.put("audioUrl", audioUrl);
        map.put("duration", duration);
        map.put("orderIndex", orderIndex);
        map.put("isPublished", isPublished);
        map.put("isFree", isFree);
        map.put("createdAt", createdAt);
        map.put("updatedAt", updatedAt);
        map.put("metadata", metadata);
        return map;
    }

    // Getters et Setters
    public String getLessonId() { return lessonId; }
    public void setLessonId(String lessonId) { this.lessonId = lessonId; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getSectionId() { return sectionId; }
    public void setSectionId(String sectionId) { this.sectionId = sectionId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LessonType getType() { return type; }
    public void setType(LessonType type) { this.type = type; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public String getAudioUrl() { return audioUrl; }
    public void setAudioUrl(String audioUrl) { this.audioUrl = audioUrl; }

    public List<LessonResource> getResources() { return resources; }
    public void setResources(List<LessonResource> resources) { this.resources = resources; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public int getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }

    public boolean isPublished() { return isPublished; }
    public void setPublished(boolean published) { isPublished = published; }

    public boolean isFree() { return isFree; }
    public void setFree(boolean free) { isFree = free; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    // Méthodes utiles
    public String getFormattedDuration() {
        if (duration <= 0) return "N/A";
        
        int hours = duration / 60;
        int minutes = duration % 60;
        
        if (hours > 0) {
            return String.format("%dh %02dmin", hours, minutes);
        } else {
            return String.format("%dmin", minutes);
        }
    }

    public boolean hasVideo() {
        return videoUrl != null && !videoUrl.trim().isEmpty();
    }

    public boolean hasAudio() {
        return audioUrl != null && !audioUrl.trim().isEmpty();
    }
} 