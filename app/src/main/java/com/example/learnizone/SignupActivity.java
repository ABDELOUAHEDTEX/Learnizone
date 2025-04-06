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

public class SignupActivity extends AppCompatActivity {

    private TextInputEditText fullNameInput;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private Button signupButton;
    private TextView loginPrompt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ Si déjà connecté, passer direct à MainActivity
        if (AuthManager.getInstance(this).isLoggedIn()) {
            navigateToMain();
            return;
        }

        setContentView(R.layout.activity_signup);

        initViews();
        setupClickListeners();
        setupLoginPrompt();
    }

    private void initViews() {
        fullNameInput = findViewById(R.id.fullname_input);
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        signupButton = findViewById(R.id.signup_button);
        loginPrompt = findViewById(R.id.login_prompt);
    }

    private void setupClickListeners() {
        signupButton.setOnClickListener(v -> attemptSignup());
    }

    private void setupLoginPrompt() {
        String text = getString(R.string.have_account) + " " + getString(R.string.login_link);
        SpannableString spannableString = new SpannableString(text);

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                finish(); // Retourner à l'écran de connexion
            }
        };

        int startIndex = text.indexOf(getString(R.string.login_link));
        spannableString.setSpan(clickableSpan, startIndex, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        loginPrompt.setText(spannableString);
        loginPrompt.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void attemptSignup() {
        String fullName = fullNameInput.getText() != null ? fullNameInput.getText().toString() : "";
        String email = emailInput.getText() != null ? emailInput.getText().toString() : "";
        String password = passwordInput.getText() != null ? passwordInput.getText().toString() : "";

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Enregistrer l'utilisateur simulé avec AuthManager
        AuthManager.getInstance(this).login(
                "2", // ID fictif différent de celui du login
                fullName,
                email,
                "https://example.com/default-profile.jpg" // Image par défaut
        );

        Toast.makeText(this, "Inscription réussie. Bienvenue " + fullName + " !", Toast.LENGTH_LONG).show();

        navigateToMain();
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
