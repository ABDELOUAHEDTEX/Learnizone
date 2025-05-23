package com.example.learnizone.models;

import com.google.firebase.firestore.DocumentSnapshot;
import android.util.Log;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class LessonProgress implements Serializable {
    private static final String TAG = "LessonProgress";
    private static final int MAX_PROGRESS = 100;
    private static final int MIN_PROGRESS = 0;
    private static final long MAX_TIMESTAMP = System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000); // 1 year in future
    private static final long MIN_TIMESTAMP = 0L;

    private String id;
    private String enrollmentId;
    private String lessonId;
    private int progress;
    private boolean completed;
    private long lastAccessedAt;
    private Date completedAt;
    private Map<String, Object> metadata;

    public LessonProgress() {
        this.metadata = new HashMap<>();
        this.progress = 0;
        this.completed = false;
        this.lastAccessedAt = System.currentTimeMillis();
    }

    public static LessonProgress fromDocument(DocumentSnapshot document) {
        if (document == null || !document.exists()) {
            return null;
        }

        try {
            LessonProgress progress = new LessonProgress();
            progress.id = document.getId();
            progress.enrollmentId = document.getString("enrollmentId");
            progress.lessonId = document.getString("lessonId");
            
            Long progressValue = document.getLong("progress");
            progress.progress = progressValue != null ? progressValue.intValue() : 0;
            
            progress.completed = document.getBoolean("completed") != null ? document.getBoolean("completed") : false;
            
            Long lastAccessed = document.getLong("lastAccessedAt");
            progress.lastAccessedAt = lastAccessed != null ? lastAccessed : System.currentTimeMillis();
            
            Date completedAt = document.getDate("completedAt");
            progress.completedAt = completedAt;
            
            Map<String, Object> metadata = (Map<String, Object>) document.get("metadata");
            progress.metadata = metadata != null ? metadata : new HashMap<>();

            return progress;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing progress document: " + e.getMessage());
            return null;
        }
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("enrollmentId", enrollmentId);
        map.put("lessonId", lessonId);
        map.put("progress", progress);
        map.put("completed", completed);
        map.put("lastAccessedAt", lastAccessedAt);
        map.put("completedAt", completedAt);
        map.put("metadata", metadata);
        return map;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(String enrollmentId) {
        if (enrollmentId != null && enrollmentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Enrollment ID cannot be empty");
        }
        this.enrollmentId = enrollmentId;
    }

    public String getLessonId() {
        return lessonId;
    }

    public void setLessonId(String lessonId) {
        if (lessonId != null && lessonId.trim().isEmpty()) {
            throw new IllegalArgumentException("Lesson ID cannot be empty");
        }
        this.lessonId = lessonId;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        if (progress < MIN_PROGRESS || progress > MAX_PROGRESS) {
            throw new IllegalArgumentException("Progress must be between " + MIN_PROGRESS + " and " + MAX_PROGRESS);
        }
        this.progress = progress;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
        if (completed && completedAt == null) {
            this.completedAt = new Date();
        } else if (!completed) {
            this.completedAt = null;
        }
    }

    public long getLastAccessedAt() {
        return lastAccessedAt;
    }

    public void setLastAccessedAt(long lastAccessedAt) {
        if (lastAccessedAt < MIN_TIMESTAMP || lastAccessedAt > MAX_TIMESTAMP) {
            throw new IllegalArgumentException("Invalid timestamp value");
        }
        this.lastAccessedAt = lastAccessedAt;
    }

    public Date getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Date completedAt) {
        if (completedAt != null) {
            long timestamp = completedAt.getTime();
            if (timestamp < MIN_TIMESTAMP || timestamp > MAX_TIMESTAMP) {
                throw new IllegalArgumentException("Invalid completion date");
            }
        }
        this.completedAt = completedAt;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata != null ? metadata : new HashMap<>();
    }

    public boolean isValid() {
        if (enrollmentId == null || enrollmentId.trim().isEmpty()) return false;
        if (lessonId == null || lessonId.trim().isEmpty()) return false;
        if (progress < MIN_PROGRESS || progress > MAX_PROGRESS) return false;
        if (lastAccessedAt < MIN_TIMESTAMP || lastAccessedAt > MAX_TIMESTAMP) return false;
        if (completed && completedAt == null) return false;
        if (completedAt != null) {
            long timestamp = completedAt.getTime();
            if (timestamp < MIN_TIMESTAMP || timestamp > MAX_TIMESTAMP) return false;
        }
        
        // Validate metadata
        if (metadata == null) return false;
        for (Map.Entry<String, Object> entry : metadata.entrySet()) {
            if (entry.getKey() == null || entry.getKey().trim().isEmpty()) return false;
            if (entry.getValue() == null) return false;
        }
        
        return true;
    }

    public void updateProgress(int newProgress) {
        setProgress(newProgress);
        setLastAccessedAt(System.currentTimeMillis());
    }

    public void markAsCompleted() {
        setCompleted(true);
        setProgress(MAX_PROGRESS);
        setLastAccessedAt(System.currentTimeMillis());
    }

    public void reset() {
        setCompleted(false);
        setProgress(MIN_PROGRESS);
        setCompletedAt(null);
        setLastAccessedAt(System.currentTimeMillis());
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

    public boolean hasMetadata(String key) {
        return key != null && metadata.containsKey(key);
    }

    public Object getMetadataValue(String key) {
        return key != null ? metadata.get(key) : null;
    }
} 