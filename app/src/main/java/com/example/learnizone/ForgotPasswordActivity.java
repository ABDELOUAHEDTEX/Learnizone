package com.example.learnizone;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.learnizone.auth.AuthManager;
import com.google.android.material.textfield.TextInputEditText;

public class ForgotPasswordActivity extends AppCompatActivity {
    private TextInputEditText emailInput;
    private Button resetButton;
    private TextView backToLogin;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        
        authManager = AuthManager.getInstance(this);
        initViews();
        setupClickListeners();
    }

    private void initViews() {
        emailInput = findViewById(R.id.email_input);
        resetButton = findViewById(R.id.reset_button);
        backToLogin = findViewById(R.id.back_to_login);
    }

    private void setupClickListeners() {
        resetButton.setOnClickListener(v -> resetPassword());
        backToLogin.setOnClickListener(v -> finish());
    }

    private void resetPassword() {
        String email = emailInput.getText() != null ? emailInput.getText().toString() : "";
        if (email.isEmpty()) {
            Toast.makeText(this, "Veuillez entrer votre email", Toast.LENGTH_SHORT).show();
            return;
        }

        authManager.resetPassword(email, task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Email de réinitialisation envoyé", Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(this, "Erreur: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
} 