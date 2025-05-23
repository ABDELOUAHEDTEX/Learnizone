package com.example.learnizone.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.auth.FirebaseUser;
import com.example.learnizone.auth.AuthManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;

public class AuthViewModel extends ViewModel {
    private MutableLiveData<FirebaseUser> currentUser = new MutableLiveData<>();
    private MutableLiveData<String> authError = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private AuthManager authManager;

    public AuthViewModel() {
        // AuthManager sera initialisé dans l'Activity avec le context
    }

    public void init(AuthManager authManager) {
        this.authManager = authManager;
        currentUser.setValue(authManager.getCurrentUser());
    }

    public MutableLiveData<FirebaseUser> getCurrentUser() { return currentUser; }
    public MutableLiveData<String> getAuthError() { return authError; }
    public MutableLiveData<Boolean> getIsLoading() { return isLoading; }

    public void signIn(String email, String password) {
        isLoading.setValue(true);
        authManager.signIn(email, password, task -> {
            isLoading.setValue(false);
            if (task.isSuccessful()) {
                currentUser.setValue(authManager.getCurrentUser());
                authError.setValue(null);
            } else {
                authError.setValue(task.getException() != null ? 
                    task.getException().getMessage() : "Erreur de connexion");
            }
        });
    }

    public void signUp(String email, String password, String fullName, String userType) {
        isLoading.setValue(true);
        authManager.signUp(email, password, fullName, userType, task -> {
            isLoading.setValue(false);
            if (task.isSuccessful()) {
                currentUser.setValue(authManager.getCurrentUser());
                authError.setValue(null);
            } else {
                authError.setValue(task.getException() != null ? 
                    task.getException().getMessage() : "Erreur d'inscription");
            }
        });
    }

    public void signOut() {
        authManager.signOut();
        currentUser.setValue(null);
    }

    public void resetPassword(String email) {
        isLoading.setValue(true);
        authManager.resetPassword(email, task -> {
            isLoading.setValue(false);
            if (!task.isSuccessful()) {
                authError.setValue(task.getException() != null ? 
                    task.getException().getMessage() : "Erreur de réinitialisation du mot de passe");
            }
        });
    }

    public void updateProfile(String fullName, String profileImageUri) {
        isLoading.setValue(true);
        authManager.updateProfile(fullName, profileImageUri, task -> {
            isLoading.setValue(false);
            if (!task.isSuccessful()) {
                authError.setValue(task.getException() != null ? 
                    task.getException().getMessage() : "Erreur de mise à jour du profil");
            }
        });
    }

    public void deleteAccount() {
        isLoading.setValue(true);
        authManager.deleteAccount(task -> {
            isLoading.setValue(false);
            if (task.isSuccessful()) {
                currentUser.setValue(null);
            } else {
                authError.setValue(task.getException() != null ? 
                    task.getException().getMessage() : "Erreur de suppression du compte");
            }
        });
    }
} 