package com.example.learnizone;

import android.app.Application;
import android.util.Log;
import com.google.firebase.FirebaseApp;

import com.example.learnizone.firebase.FirebaseManager;

public class LearnIzoneApp extends Application {
    private static final String TAG = "LearnIzoneApp";

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        
        // Initialize collections (only for first run or when needed)
        initializeCollections();
    }

    private void initializeCollections() {
        FirebaseManager.getInstance().createInitialCollections();
    }
} 