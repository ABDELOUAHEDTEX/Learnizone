package com.example.learnizone.auth;

import android.content.Context;
import android.content.SharedPreferences;

public class AuthManager {
    private static final String PREF_NAME = "learnizone_auth";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_PROFILE_PIC = "user_profile_pic";

    // Instance unique (Singleton)
    private static AuthManager instance;

    // SharedPreferences pour stocker les données d'authentification
    private SharedPreferences sharedPreferences;

    // Constructeur privé pour empêcher l'instanciation directe
    private AuthManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // Méthode pour obtenir l'instance unique
    public static synchronized AuthManager getInstance(Context context) {
        if (instance == null) {
            instance = new AuthManager(context.getApplicationContext());
        }
        return instance;
    }

    // Méthode pour connecter un utilisateur
    public void login(String userId, String userName, String userEmail, String profilePicUrl) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_NAME, userName);
        editor.putString(KEY_USER_EMAIL, userEmail);
        editor.putString(KEY_USER_PROFILE_PIC, profilePicUrl);
        editor.apply();
    }

    // Méthode pour déconnecter un utilisateur
    public void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    // Méthode pour vérifier si un utilisateur est connecté
    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // Méthode pour obtenir l'ID de l'utilisateur
    public String getUserId() {
        return sharedPreferences.getString(KEY_USER_ID, null);
    }

    // Méthode pour obtenir le nom de l'utilisateur
    public String getUserName() {
        return sharedPreferences.getString(KEY_USER_NAME, null);
    }

    // Méthode pour obtenir l'email de l'utilisateur
    public String getUserEmail() {
        return sharedPreferences.getString(KEY_USER_EMAIL, null);
    }

    // Méthode pour obtenir l'URL de la photo de profil
    public String getProfilePicUrl() {
        return sharedPreferences.getString(KEY_USER_PROFILE_PIC, null);
    }

    // Méthode pour mettre à jour les informations utilisateur
    public void updateUserInfo(String userName, String userEmail, String profilePicUrl) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_NAME, userName);
        editor.putString(KEY_USER_EMAIL, userEmail);
        editor.putString(KEY_USER_PROFILE_PIC, profilePicUrl);
        editor.apply();
    }
}