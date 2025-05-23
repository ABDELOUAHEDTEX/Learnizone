package com.example.learnizone.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import androidx.annotation.NonNull;
import com.example.learnizone.models.User;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

    private final FirebaseAuth mAuth;
    private final FirebaseFirestore db;
    private final FirebaseStorage storage;
    private static final String USERS_COLLECTION = "users";

    private Context appContext;

    // Constructeur privé pour empêcher l'instanciation directe
    private AuthManager(Context context) {
        this.appContext = context.getApplicationContext();
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    // Méthode pour obtenir l'instance unique
    public static synchronized AuthManager getInstance(Context context) {
        if (instance == null) {
            instance = new AuthManager(context);
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

    public void signUp(String email, String password, String fullName, String userType,
                      OnCompleteListener<AuthResult> listener) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = task.getResult().getUser();
                        if (firebaseUser != null) {
                            User user = new User(firebaseUser.getUid(), email, fullName, userType);
                            saveUserToFirestore(user, firestoreTask -> listener.onComplete(task));
                        }
                    } else {
                        listener.onComplete(task);
                    }
                });
    }

    private void saveUserToFirestore(User user, OnCompleteListener<Void> listener) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("uid", user.getUid());
        userMap.put("email", user.getEmail());
        userMap.put("fullName", user.getFullName());
        userMap.put("userType", user.getUserType());
        userMap.put("createdAt", user.getCreatedAt());
        userMap.put("lastLoginAt", user.getLastLoginAt());

        db.collection(USERS_COLLECTION).document(user.getUid())
                .set(userMap)
                .addOnCompleteListener(listener);
    }

    public void signIn(String email, String password, OnCompleteListener<AuthResult> listener) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        updateLastLogin();
                    }
                    listener.onComplete(task);
                });
    }

    private void updateLastLogin() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            db.collection(USERS_COLLECTION).document(currentUser.getUid())
                    .update("lastLoginAt", new Date());
        }
    }

    public void signOut() {
        mAuth.signOut();
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public void updateProfile(String fullName, String profileImageUri,
                            OnCompleteListener<Void> listener) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            UserProfileChangeRequest.Builder profileUpdates = new UserProfileChangeRequest.Builder();
            if (fullName != null) {
                profileUpdates.setDisplayName(fullName);
            }
            if (profileImageUri != null) {
                profileUpdates.setPhotoUri(Uri.parse(profileImageUri));
            }

            user.updateProfile(profileUpdates.build())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            updateUserInFirestore(fullName, profileImageUri, listener);
                        } else {
                            listener.onComplete(task);
                        }
                    });
        }
    }

    private void updateUserInFirestore(String fullName, String profileImageUrl,
                                     OnCompleteListener<Void> listener) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            Map<String, Object> updates = new HashMap<>();
            if (fullName != null) {
                updates.put("fullName", fullName);
            }
            if (profileImageUrl != null) {
                updates.put("profileImageUrl", profileImageUrl);
            }

            db.collection(USERS_COLLECTION).document(user.getUid())
                    .update(updates)
                    .addOnCompleteListener(listener);
        }
    }

    public void uploadProfileImage(Uri imageUri, OnCompleteListener<Uri> listener) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            StorageReference storageRef = storage.getReference()
                    .child("profile_images")
                    .child(user.getUid());

            storageRef.putFile(imageUri)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            storageRef.getDownloadUrl()
                                    .addOnCompleteListener(listener);
                        } else {
                            // Create a failed task with the error
                            listener.onComplete(Tasks.forException(task.getException()));
                        }
                    });
        } else {
            // Create a failed task if user is null
            listener.onComplete(Tasks.forException(new Exception("User not logged in")));
        }
    }

    public void deleteAccount(OnCompleteListener<Void> listener) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db.collection(USERS_COLLECTION).document(user.getUid())
                    .delete()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            user.delete().addOnCompleteListener(listener);
                        } else {
                            listener.onComplete(task);
                        }
                    });
        }
    }

    public void resetPassword(String email, OnCompleteListener<Void> listener) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(listener);
    }

    public void testUserDocument() {
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(document -> {
                    // Success!
                })
                .addOnFailureListener(e -> {
                    // Log error
                });
    }
}