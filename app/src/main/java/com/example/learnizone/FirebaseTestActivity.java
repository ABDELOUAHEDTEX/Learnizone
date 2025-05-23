package com.example.learnizone;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseTestActivity extends AppCompatActivity {
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase_test);
        
        statusText = findViewById(R.id.status_text);
        testFirebaseConnection();
    }

    private void testFirebaseConnection() {
        StringBuilder status = new StringBuilder();
        
        // Test Firebase Auth
        FirebaseAuth auth = FirebaseAuth.getInstance();
        status.append("Firebase Auth: ").append(auth != null ? "✅ OK" : "❌ ERROR").append("\n");
        
        // Test Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        status.append("Firestore: ").append(db != null ? "✅ OK" : "❌ ERROR").append("\n");
        
        statusText.setText(status.toString());
    }
} 