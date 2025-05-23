package com.example.learnizone.models;

import com.google.firebase.firestore.DocumentSnapshot;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class LessonResource implements Serializable {
    private String resourceId;
    private String lessonId;
    private String title;
    private String description;
    private ResourceType type;
    private String url;
    private String fileName;
    private long fileSize; // en bytes
    private boolean isDownloadable;
    private Date uploadedAt;

    public enum ResourceType {
        PDF("pdf", "Document PDF"),
        WORD("word", "Document Word"),
        EXCEL("excel", "Feuille Excel"),
        POWERPOINT("powerpoint", "Présentation"),
        IMAGE("image", "Image"),
        VIDEO("video", "Vidéo"),
        AUDIO("audio", "Audio"),
        LINK("link", "Lien externe"),
        CODE("code", "Code source"),
        OTHER("other", "Autre");

        private final String value;
        private final String displayName;

        ResourceType(String value, String displayName) {
            this.value = value;
            this.displayName = displayName;
        }

        public String getValue() { return value; }
        public String getDisplayName() { return displayName; }

        public static ResourceType fromString(String value) {
            for (ResourceType type : values()) {
                if (type.value.equals(value)) {
                    return type;
                }
            }
            return OTHER;
        }
    }

    public LessonResource() {
        this.uploadedAt = new Date();
        this.isDownloadable = true;
    }

    public static LessonResource fromDocument(DocumentSnapshot document) {
        LessonResource resource = new LessonResource();
        resource.resourceId = document.getId();
        resource.lessonId = document.getString("lessonId");
        resource.title = document.getString("title");
        resource.description = document.getString("description");
        resource.type = ResourceType.fromString(document.getString("type"));
        resource.url = document.getString("url");
        resource.fileName = document.getString("fileName");

        Long fileSize = document.getLong("fileSize");
        resource.fileSize = fileSize != null ? fileSize : 0;

        Boolean isDownloadable = document.getBoolean("isDownloadable");
        resource.isDownloadable = isDownloadable != null ? isDownloadable : true;

        resource.uploadedAt = document.getDate("uploadedAt");

        return resource;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("lessonId", lessonId);
        map.put("title", title);
        map.put("description", description);
        map.put("type", type.getValue());
        map.put("url", url);
        map.put("fileName", fileName);
        map.put("fileSize", fileSize);
        map.put("isDownloadable", isDownloadable);
        map.put("uploadedAt", uploadedAt);
        return map;
    }

    // Getters et Setters
    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }

    public String getLessonId() { return lessonId; }
    public void setLessonId(String lessonId) { this.lessonId = lessonId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ResourceType getType() { return type; }
    public void setType(ResourceType type) { this.type = type; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public boolean isDownloadable() { return isDownloadable; }
    public void setDownloadable(boolean downloadable) { isDownloadable = downloadable; }

    public Date getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(Date uploadedAt) { this.uploadedAt = uploadedAt; }

    // Méthodes utiles
    public String getFormattedFileSize() {
        if (fileSize <= 0) return "N/A";
        
        String[] units = {"B", "KB", "MB", "GB"};
        double size = fileSize;
        int unitIndex = 0;
        
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        
        return String.format("%.1f %s", size, units[unitIndex]);
    }
} 