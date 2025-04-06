package com.example.learnizone;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.example.learnizone.auth.AuthManager;
import com.google.android.material.switchmaterial.SwitchMaterial;

import de.hdodenhof.circleimageview.CircleImageView;
import com.bumptech.glide.Glide;

public class ProfileFragment extends Fragment {

    private CircleImageView profileImage;
    private TextView profileName;
    private TextView profileEmail;
    private TextView coursesCount;
    private TextView hoursCount;
    private TextView streakCount;
    private ConstraintLayout settingsAccount;
    private ConstraintLayout settingsNotifications;
    private ConstraintLayout settingsDarkMode;
    private ConstraintLayout settingsLogout;
    private SwitchMaterial darkModeSwitch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        initViews(view);
        loadUserData();
        setupClickListeners();

        return view;
    }

    private void initViews(View view) {
        profileImage = view.findViewById(R.id.profile_image);
        profileName = view.findViewById(R.id.profile_name);
        profileEmail = view.findViewById(R.id.profile_email);
        coursesCount = view.findViewById(R.id.courses_count);
        hoursCount = view.findViewById(R.id.hours_count);
        streakCount = view.findViewById(R.id.streak_count);
        settingsAccount = view.findViewById(R.id.settings_account);
        settingsNotifications = view.findViewById(R.id.settings_notifications);
        settingsDarkMode = view.findViewById(R.id.settings_dark_mode);
        settingsLogout = view.findViewById(R.id.settings_logout);
        darkModeSwitch = view.findViewById(R.id.settings_dark_mode_switch);
    }

    private void loadUserData() {
        AuthManager authManager = AuthManager.getInstance(requireContext());

        // Vérification de la connexion
        if (!authManager.isLoggedIn()) {
            Toast.makeText(requireContext(), "Utilisateur non connecté", Toast.LENGTH_SHORT).show();
            navigateToLogin();
            return;
        }

        String fullName = authManager.getUserName();
        String email = authManager.getUserEmail();

        // Chargement des données utilisateur
        if (fullName != null && !fullName.isEmpty()) {
            profileName.setText(fullName);
        }

        if (email != null && !email.isEmpty()) {
            profileEmail.setText(email);
        }

        // Données fictives pour la démo
        coursesCount.setText("12");
        hoursCount.setText("45");
        streakCount.setText("7");

        String profilePicUrl = authManager.getProfilePicUrl();

        // Chargement de l'image de profil avec Glide
        if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
            Glide.with(this)
                    .load(profilePicUrl)
                    .placeholder(R.drawable.placeholder_profile) // image par défaut
                    .error(R.drawable.error_profile_pic) // image en cas d'erreur
                    .circleCrop()
                    .into(profileImage);
        } else {
            Glide.with(this)
                    .load(R.drawable.placeholder_profile) // image par défaut
                    .circleCrop()
                    .into(profileImage);
        }
    }

    private void setupClickListeners() {
        settingsAccount.setOnClickListener(v -> {
            // Naviguer vers les paramètres de compte
            navigateToAccountSettings();
        });

        settingsNotifications.setOnClickListener(v -> {
            // Naviguer vers les paramètres de notifications
            navigateToNotificationsSettings();
        });



        settingsLogout.setOnClickListener(v -> {
            // ✅ Déconnexion avec AuthManager
            AuthManager.getInstance(requireContext()).logout();
            Toast.makeText(getContext(), "Déconnexion...", Toast.LENGTH_SHORT).show();
            navigateToLogin();
        });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void navigateToAccountSettings() {
        // Implémenter la navigation vers la page des paramètres de compte
        Toast.makeText(getContext(), "Paramètres du compte", Toast.LENGTH_SHORT).show();
    }

    private void navigateToNotificationsSettings() {
        // Implémenter la navigation vers la page des paramètres de notifications
        Toast.makeText(getContext(), "Paramètres de notifications", Toast.LENGTH_SHORT).show();
    }


}
