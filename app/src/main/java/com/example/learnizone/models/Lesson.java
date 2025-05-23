package com.example.learnizone.models;

import com.google.firebase.firestore.DocumentSnapshot;
import android.util.Patterns;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Lesson implements Serializable {
    private static final Pattern SPECIAL_CHARS_PATTERN = Pattern.compile("[<>\"'&]");
    private static final int MAX_TITLE_LENGTH = 100;
    private static final int MAX_DESCRIPTION_LENGTH = 1000;
    private static final int MAX_RESOURCES = 20;

    private String id;
    private String courseId;
    private String title;
    private String description;
    private LessonType type;
    private int order;
    private int durationMinutes;
    private String contentUrl;
    private List<LessonResource> resources;
    private boolean isLocked;
    private String prerequisiteLessonId;
    private Date createdAt;
    private Date updatedAt;
    private Map<String, Object> metadata;

    public enum LessonType {
        VIDEO("Vidéo"),
        TEXT("Texte"),
        AUDIO("Audio"),
        QUIZ("Quiz");

        private final String displayName;

        LessonType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static LessonType fromString(String value) {
            try {
                return valueOf(value);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }

    public Lesson() {
        this.resources = new ArrayList<>();
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.metadata = new HashMap<>();
        this.order = 0;
        this.durationMinutes = 0;
        this.isLocked = false;
    }

    public static Lesson fromDocument(DocumentSnapshot document) {
        if (document == null || !document.exists()) {
            return null;
        }

        try {
            Lesson lesson = new Lesson();
            lesson.id = document.getId();
            lesson.courseId = document.getString("courseId");
            lesson.title = document.getString("title");
            lesson.description = document.getString("description");
            
            String typeStr = document.getString("type");
            lesson.type = typeStr != null ? LessonType.fromString(typeStr) : null;
            
            Long order = document.getLong("order");
            lesson.order = order != null ? order.intValue() : 0;
            
            Long duration = document.getLong("durationMinutes");
            lesson.durationMinutes = duration != null ? duration.intValue() : 0;
            
            lesson.contentUrl = document.getString("contentUrl");
            lesson.isLocked = document.getBoolean("isLocked") != null ? document.getBoolean("isLocked") : false;
            lesson.prerequisiteLessonId = document.getString("prerequisiteLessonId");

            List<Map<String, Object>> resourcesData = (List<Map<String, Object>>) document.get("resources");
            if (resourcesData != null) {
                for (Map<String, Object> resourceData : resourcesData) {
                    try {
                        LessonResource resource = new LessonResource();
                        resource.setTitle((String) resourceData.get("title"));
                        String resourceType = (String) resourceData.get("type");
                        if (resourceType != null) {
                            resource.setType(LessonResource.ResourceType.valueOf(resourceType));
                        }
                        resource.setUrl((String) resourceData.get("url"));
                        if (resource.isValid()) {
                            lesson.resources.add(resource);
                        }
                    } catch (Exception e) {
                        // Log error but continue processing other resources
                        e.printStackTrace();
                    }
                }
            }

            Date createdAt = document.getDate("createdAt");
            lesson.createdAt = createdAt != null ? createdAt : new Date();
            
            Date updatedAt = document.getDate("updatedAt");
            lesson.updatedAt = updatedAt != null ? updatedAt : lesson.createdAt;

            Map<String, Object> metadata = (Map<String, Object>) document.get("metadata");
            lesson.metadata = metadata != null ? metadata : new HashMap<>();

            return lesson;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("courseId", courseId);
        map.put("title", title);
        map.put("description", description);
        map.put("type", type != null ? type.name() : null);
        map.put("order", order);
        map.put("durationMinutes", durationMinutes);
        map.put("contentUrl", contentUrl);
        map.put("isLocked", isLocked);
        map.put("prerequisiteLessonId", prerequisiteLessonId);
        map.put("createdAt", createdAt);
        map.put("updatedAt", updatedAt);
        map.put("metadata", metadata);

        List<Map<String, Object>> resourcesData = new ArrayList<>();
        for (LessonResource resource : resources) {
            if (resource.isValid()) {
                resourcesData.add(resource.toMap());
            }
        }
        map.put("resources", resourcesData);

        return map;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title != null) {
            title = title.trim();
            if (title.length() > MAX_TITLE_LENGTH) {
                title = title.substring(0, MAX_TITLE_LENGTH);
            }
            if (SPECIAL_CHARS_PATTERN.matcher(title).find()) {
                throw new IllegalArgumentException("Title contains invalid special characters");
            }
        }
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (description != null) {
            description = description.trim();
            if (description.length() > MAX_DESCRIPTION_LENGTH) {
                description = description.substring(0, MAX_DESCRIPTION_LENGTH);
            }
            if (SPECIAL_CHARS_PATTERN.matcher(description).find()) {
                throw new IllegalArgumentException("Description contains invalid special characters");
            }
        }
        this.description = description;
    }

    public LessonType getType() {
        return type;
    }

    public void setType(LessonType type) {
        this.type = type;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        if (order < 0) {
            throw new IllegalArgumentException("Order cannot be negative");
        }
        this.order = order;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        if (durationMinutes < 0) {
            throw new IllegalArgumentException("Duration cannot be negative");
        }
        this.durationMinutes = durationMinutes;
    }

    public String getContentUrl() {
        return contentUrl;
    }

    public void setContentUrl(String contentUrl) {
        if (contentUrl != null && !contentUrl.trim().isEmpty()) {
            if (!Patterns.WEB_URL.matcher(contentUrl).matches()) {
                throw new IllegalArgumentException("Invalid content URL format");
            }
        }
        this.contentUrl = contentUrl;
    }

    public List<LessonResource> getResources() {
        return resources;
    }

    public void setResources(List<LessonResource> resources) {
        if (resources != null && resources.size() > MAX_RESOURCES) {
            throw new IllegalArgumentException("Maximum number of resources exceeded");
        }
        this.resources = resources;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public String getPrerequisiteLessonId() {
        return prerequisiteLessonId;
    }

    public void setPrerequisiteLessonId(String prerequisiteLessonId) {
        this.prerequisiteLessonId = prerequisiteLessonId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        if (createdAt == null) {
            throw new IllegalArgumentException("Created date cannot be null");
        }
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        if (updatedAt == null) {
            throw new IllegalArgumentException("Updated date cannot be null");
        }
        if (createdAt != null && updatedAt.before(createdAt)) {
            throw new IllegalArgumentException("Updated date cannot be before created date");
        }
        this.updatedAt = updatedAt;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata != null ? metadata : new HashMap<>();
    }

    // Utility methods
    public String getFormattedDuration() {
        if (durationMinutes <= 0) return "N/A";
        
        int hours = durationMinutes / 60;
        int minutes = durationMinutes % 60;
        
        if (hours > 0) {
            return String.format("%dh %02dmin", hours, minutes);
        } else {
            return String.format("%dmin", minutes);
        }
    }

    public boolean hasVideo() {
        return type == LessonType.VIDEO && contentUrl != null && !contentUrl.trim().isEmpty();
    }

    public boolean hasAudio() {
        return type == LessonType.AUDIO && contentUrl != null && !contentUrl.trim().isEmpty();
    }

    public boolean isValid() {
        if (id == null || id.trim().isEmpty()) return false;
        if (courseId == null || courseId.trim().isEmpty()) return false;
        if (title == null || title.trim().isEmpty()) return false;
        if (title.length() > MAX_TITLE_LENGTH) return false;
        if (description != null && description.length() > MAX_DESCRIPTION_LENGTH) return false;
        if (type == null) return false;
        if (order < 0) return false;
        if (durationMinutes < 0) return false;
        if (contentUrl != null && !contentUrl.trim().isEmpty() && !Patterns.WEB_URL.matcher(contentUrl).matches()) return false;
        if (createdAt == null) return false;
        if (updatedAt == null) return false;
        if (updatedAt.before(createdAt)) return false;
        if (resources.size() > MAX_RESOURCES) return false;
        
        // Validate resources
        for (LessonResource resource : resources) {
            if (!resource.isValid()) return false;
        }
        
        return true;
    }

    public void addResource(LessonResource resource) {
        if (resource != null && resource.isValid()) {
            if (resources.size() >= MAX_RESOURCES) {
                throw new IllegalStateException("Maximum number of resources reached");
            }
            resources.add(resource);
        }
    }

    public void removeResource(LessonResource resource) {
        if (resource != null) {
            resources.remove(resource);
        }
    }

    public void clearResources() {
        resources.clear();
    }

    public void addMetadata(String key, Object value) {
        if (key != null && !key.trim().isEmpty() && value != null) {
            metadata.put(key, value);
        }
    }

    public void removeMetadata(String key) {
        if (key != null) {
            metadata.remove(key);
        }
    }

    public void clearMetadata() {
        metadata.clear();
    }
} 