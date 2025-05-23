package com.example.learnizone.models;

import java.util.Date;
import java.util.List;

public class User {
    private String uid;
    private String email;
    private String fullName;
    private String userType; // "STUDENT" ou "INSTRUCTOR"
    private String profileImageUrl;
    private Date createdAt;
    private Date lastLoginAt;
    private List<String> enrolledCourses;
    private List<String> createdCourses; // Pour les instructeurs

    // Constructeur vide requis pour Firestore
    public User() {}

    public User(String uid, String email, String fullName, String userType) {
        this.uid = uid;
        this.email = email;
        this.fullName = fullName;
        this.userType = userType;
        this.createdAt = new Date();
        this.lastLoginAt = new Date();
    }

    // Getters et Setters
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(Date lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public List<String> getEnrolledCourses() {
        return enrolledCourses;
    }

    public void setEnrolledCourses(List<String> enrolledCourses) {
        this.enrolledCourses = enrolledCourses;
    }

    public List<String> getCreatedCourses() {
        return createdCourses;
    }

    public void setCreatedCourses(List<String> createdCourses) {
        this.createdCourses = createdCourses;
    }
} 