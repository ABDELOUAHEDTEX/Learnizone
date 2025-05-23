package com.example.learnizone;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.learnizone.auth.AuthManager;
import com.example.learnizone.viewmodels.AuthViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private Button loginButton;
    private TextView forgotPassword;
    private TextView registerPrompt;
    private ProgressBar progressBar;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialiser le ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Observer l'état de l'utilisateur
        authViewModel.getCurrentUser().observe(this, this::handleAuthStateChange);
        authViewModel.getAuthError().observe(this, this::handleAuthError);
        authViewModel.getIsLoading().observe(this, this::handleLoadingState);

        // Vérifier si l'utilisateur est déjà connecté
        if (authViewModel.getCurrentUser().getValue() != null) {
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
        progressBar = findViewById(R.id.progress_bar);

        // Add Firebase test button
        Button firebaseTestButton = findViewById(R.id.firebase_test_button);
        firebaseTestButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, FirebaseTestActivity.class);
            startActivity(intent);
        });
    }

    private void setupClickListeners() {
        loginButton.setOnClickListener(v -> attemptLogin());

        forgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(this, ForgotPasswordActivity.class);
            startActivity(intent);
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

        authViewModel.signIn(email, password);
    }

    private void handleAuthStateChange(FirebaseUser user) {
        if (user != null) {
            navigateToMain();
        }
    }

    private void handleAuthError(String error) {
        if (error != null) {
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        }
    }

    private void handleLoadingState(Boolean isLoading) {
        if (isLoading != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            loginButton.setEnabled(!isLoading);
            emailInput.setEnabled(!isLoading);
            passwordInput.setEnabled(!isLoading);
        }
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
