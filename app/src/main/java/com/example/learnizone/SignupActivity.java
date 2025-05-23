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
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.learnizone.auth.AuthManager;
import com.example.learnizone.viewmodels.AuthViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;

public class SignupActivity extends AppCompatActivity {

    private TextInputEditText fullNameInput;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private RadioGroup userTypeGroup;
    private Button signupButton;
    private TextView loginPrompt;
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

        setContentView(R.layout.activity_signup);

        initViews();
        setupClickListeners();
        setupLoginPrompt();
    }

    private void initViews() {
        fullNameInput = findViewById(R.id.fullname_input);
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        userTypeGroup = findViewById(R.id.user_type_group);
        signupButton = findViewById(R.id.signup_button);
        loginPrompt = findViewById(R.id.login_prompt);
        progressBar = findViewById(R.id.progress_bar);
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
        String userType = getUserType();

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || userType == null) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Le mot de passe doit contenir au moins 6 caractères", Toast.LENGTH_SHORT).show();
            return;
        }

        authViewModel.signUp(email, password, fullName, userType);
    }

    private String getUserType() {
        int selectedId = userTypeGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.radio_student) {
            return "STUDENT";
        } else if (selectedId == R.id.radio_instructor) {
            return "INSTRUCTOR";
        }
        return null;
    }

    private void handleAuthStateChange(FirebaseUser user) {
        if (user != null) {
            Toast.makeText(this, "Inscription réussie. Bienvenue !", Toast.LENGTH_LONG).show();
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
            signupButton.setEnabled(!isLoading);
            fullNameInput.setEnabled(!isLoading);
            emailInput.setEnabled(!isLoading);
            passwordInput.setEnabled(!isLoading);
            userTypeGroup.setEnabled(!isLoading);
        }
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
