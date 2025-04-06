package com.example.learnizone;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.learnizone.auth.AuthManager;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private Button loginButton;
    private TextView forgotPassword;
    private TextView registerPrompt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ Rediriger si déjà connecté
        if (AuthManager.getInstance(this).isLoggedIn()) {
            navigateToMain();
            return;
        }

        setContentView(R.layout.activity_login);

        initViews();
        setupClickListeners();
        setupRegisterPrompt();
    }

    private void initViews() {
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        loginButton = findViewById(R.id.login_button);
        forgotPassword = findViewById(R.id.forgot_password);
        registerPrompt = findViewById(R.id.register_prompt);
    }

    private void setupClickListeners() {
        loginButton.setOnClickListener(v -> attemptLogin());

        forgotPassword.setOnClickListener(v -> {
            Toast.makeText(LoginActivity.this, "Fonction de récupération de mot de passe", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupRegisterPrompt() {
        String text = getString(R.string.no_account) + " " + getString(R.string.signup_link);
        SpannableString spannableString = new SpannableString(text);

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                navigateToSignup();
            }
        };

        int startIndex = text.indexOf(getString(R.string.signup_link));
        spannableString.setSpan(clickableSpan, startIndex, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        registerPrompt.setText(spannableString);
        registerPrompt.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void attemptLogin() {
        String email = emailInput.getText() != null ? emailInput.getText().toString() : "";
        String password = passwordInput.getText() != null ? passwordInput.getText().toString() : "";

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Simuler la connexion (à remplacer plus tard par authentification réelle)
        AuthManager.getInstance(this).login(
                "1", // id fictif
                "Utilisateur Test",
                email,
                "https://example.com/profile.jpg" // image fictive
        );

        navigateToMain();
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish(); // évite retour à la page login
    }

    private void navigateToSignup() {
        Intent intent = new Intent(this, SignupActivity.class);
        startActivity(intent);
    }
}
