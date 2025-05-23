package com.example.learnizone.models;

import android.util.Log;
import android.util.Patterns;

import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class LessonResource implements Serializable {
    private static final String TAG = "LessonResource";
    private static final long MAX_FILE_SIZE = 1024 * 1024 * 1024; // 1GB
    private static final int MAX_TITLE_LENGTH = 100;
    private static final int MAX_DESCRIPTION_LENGTH = 500;
    private static final Pattern SPECIAL_CHARS_PATTERN = Pattern.compile("[<>\"'&]");
    private static final Pattern MIME_TYPE_PATTERN = Pattern.compile("^[a-zA-Z0-9]+/[a-zA-Z0-9\\-\\+\\.]+$");

    private String title;
    private ResourceType type;
    private String url;
    private String description;
    private long fileSize;
    private String mimeType;
    private Map<String, Object> metadata;

    public enum ResourceType {
        PDF("Document PDF", "application/pdf"),
        DOCUMENT("Document", "application/msword"),
        IMAGE("Image", "image/*"),
        VIDEO("Vidéo", "video/*"),
        AUDIO("Audio", "audio/*"),
        LINK("Lien externe", null),
        EXERCISE("Exercice", null);

        private final String displayName;
        private final String mimeType;

        ResourceType(String displayName, String mimeType) {
            this.displayName = displayName;
            this.mimeType = mimeType;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getMimeType() {
            return mimeType;
        }

        public static ResourceType fromString(String value) {
            try {
                return valueOf(value);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Invalid resource type: " + value);
                return null;
            }
        }
    }

    public LessonResource() {
        this.metadata = new HashMap<>();
        this.fileSize = 0;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("title", title);
        map.put("type", type != null ? type.name() : null);
        map.put("url", url);
        map.put("description", description);
        map.put("fileSize", fileSize);
        map.put("mimeType", mimeType);
        map.put("metadata", metadata);
        return map;
    }

    // Getters and Setters
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

    public ResourceType getType() {
        return type;
    }

    public void setType(ResourceType type) {
        this.type = type;
        if (type != null && type.getMimeType() != null) {
            this.mimeType = type.getMimeType();
        }
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        if (url != null && !url.trim().isEmpty()) {
            if (!Patterns.WEB_URL.matcher(url).matches()) {
                throw new IllegalArgumentException("Invalid URL format");
            }
        }
        this.url = url;
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

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        if (fileSize < 0 || fileSize > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size must be between 0 and " + MAX_FILE_SIZE + " bytes");
        }
        this.fileSize = fileSize;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        if (mimeType != null && !mimeType.trim().isEmpty()) {
            if (!MIME_TYPE_PATTERN.matcher(mimeType).matches()) {
                throw new IllegalArgumentException("Invalid MIME type format");
            }
        }
        this.mimeType = mimeType;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata != null ? metadata : new HashMap<>();
    }

    // Utility methods
    public String getFormattedFileSize() {
        if (fileSize <= 0) return "N/A";
        
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(fileSize) / Math.log10(1024));
        return String.format("%.1f %s", fileSize / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    public boolean isDownloadable() {
        return type != ResourceType.LINK;
    }

    public boolean isViewable() {
        if (type == null || mimeType == null) return false;
        
        return type == ResourceType.PDF || 
               type == ResourceType.DOCUMENT || 
               type == ResourceType.IMAGE ||
               mimeType.startsWith("image/") ||
               mimeType.startsWith("application/pdf");
    }

    public boolean isValid() {
        if (title == null || title.trim().isEmpty()) return false;
        if (title.length() > MAX_TITLE_LENGTH) return false;
        if (type == null) return false;
        if (url == null || url.trim().isEmpty()) return false;
        if (!isValidUrl(url)) return false;
        if (description != null && description.length() > MAX_DESCRIPTION_LENGTH) return false;
        if (fileSize < 0 || fileSize > MAX_FILE_SIZE) return false;
        if (mimeType != null && !MIME_TYPE_PATTERN.matcher(mimeType).matches()) return false;
        
        // Validate metadata
        if (metadata == null) return false;
        for (Map.Entry<String, Object> entry : metadata.entrySet()) {
            if (entry.getKey() == null || entry.getKey().trim().isEmpty()) return false;
            if (entry.getValue() == null) return false;
        }
        
        return true;
    }

    private boolean isValidUrl(String urlString) {
        try {
            new URL(urlString);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Invalid URL: " + urlString);
            return false;
        }
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

    public boolean isImage() {
        return type == ResourceType.IMAGE || 
               (mimeType != null && mimeType.startsWith("image/"));
    }

    public boolean isVideo() {
        return type == ResourceType.VIDEO || 
               (mimeType != null && mimeType.startsWith("video/"));
    }

    public boolean isAudio() {
        return type == ResourceType.AUDIO || 
               (mimeType != null && mimeType.startsWith("audio/"));
    }

    public boolean isDocument() {
        return type == ResourceType.PDF || 
               type == ResourceType.DOCUMENT || 
               (mimeType != null && mimeType.startsWith("application/"));
    }

    public boolean isLink() {
        return type == ResourceType.LINK;
    }

    public boolean isExercise() {
        return type == ResourceType.EXERCISE;
    }
} 