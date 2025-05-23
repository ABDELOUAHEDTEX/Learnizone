package com.example.learnizone.managers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.learnizone.MainActivity;
import com.example.learnizone.R;
import com.example.learnizone.models.UserNotification;
import com.example.learnizone.workers.NotificationWorker;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class LearnIzoneNotificationManager {
    private static final String TAG = "NotificationManager";
    private static LearnIzoneNotificationManager instance;
    
    private final Context context;
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private final NotificationManager notificationManager;
    private final SharedPreferences preferences;
    
    // Collections
    private static final String NOTIFICATIONS_COLLECTION = "notifications";
    private static final String USER_SETTINGS_COLLECTION = "userSettings";
    
    // Notification Channels
    private static final String CHANNEL_COURSE_REMINDERS = "course_reminders";
    private static final String CHANNEL_QUIZ_ALERTS = "quiz_alerts";
    private static final String CHANNEL_ACHIEVEMENTS = "achievements";
    private static final String CHANNEL_GENERAL = "general";
    
    // Preferences
    private static final String PREFS_NAME = "notification_prefs";
    private static final String PREF_COURSE_REMINDERS = "course_reminders_enabled";
    private static final String PREF_QUIZ_ALERTS = "quiz_alerts_enabled";
    private static final String PREF_ACHIEVEMENTS = "achievements_enabled";
    private static final String PREF_GENERAL = "general_enabled";
    
    private LearnIzoneNotificationManager(Context context) {
        this.context = context.getApplicationContext();
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        this.preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        
        createNotificationChannels();
        initializeFirebaseMessaging();
    }
    
    public static synchronized LearnIzoneNotificationManager getInstance(Context context) {
        if (instance == null) {
            instance = new LearnIzoneNotificationManager(context);
        }
        return instance;
    }
    
    /**
     * Crée les canaux de notification
     */
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Canal pour les rappels de cours
            NotificationChannel courseChannel = new NotificationChannel(
                CHANNEL_COURSE_REMINDERS,
                "Rappels de cours",
                NotificationManager.IMPORTANCE_DEFAULT
            );
            courseChannel.setDescription("Notifications pour rappeler de continuer vos cours");
            
            // Canal pour les alertes de quiz
            NotificationChannel quizChannel = new NotificationChannel(
                CHANNEL_QUIZ_ALERTS,
                "Alertes de quiz",
                NotificationManager.IMPORTANCE_HIGH
            );
            quizChannel.setDescription("Notifications pour les quiz à faire ou les délais");
            
            // Canal pour les réussites
            NotificationChannel achievementChannel = new NotificationChannel(
                CHANNEL_ACHIEVEMENTS,
                "Réussites",
                NotificationManager.IMPORTANCE_DEFAULT
            );
            achievementChannel.setDescription("Notifications pour vos réussites et badges");
            
            // Canal général
            NotificationChannel generalChannel = new NotificationChannel(
                CHANNEL_GENERAL,
                "Notifications générales",
                NotificationManager.IMPORTANCE_DEFAULT
            );
            generalChannel.setDescription("Autres notifications de l'application");
            
            notificationManager.createNotificationChannel(courseChannel);
            notificationManager.createNotificationChannel(quizChannel);
            notificationManager.createNotificationChannel(achievementChannel);
            notificationManager.createNotificationChannel(generalChannel);
        }
    }
    
    /**
     * Initialise Firebase Messaging
     */
    private void initializeFirebaseMessaging() {
        FirebaseMessaging.getInstance().getToken()
            .addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                    return;
                }
                
                String token = task.getResult();
                Log.d(TAG, "FCM Registration Token: " + token);
                
                // Sauvegarder le token pour l'utilisateur connecté
                saveTokenForUser(token);
            });
    }
    
    /**
     * Sauvegarde le token FCM pour l'utilisateur
     */
    private void saveTokenForUser(String token) {
        String userId = getCurrentUserId();
        if (userId != null) {
            Map<String, Object> tokenData = new HashMap<>();
            tokenData.put("fcmToken", token);
            tokenData.put("updatedAt", new Date());
            
            db.collection("users").document(userId)
                .update(tokenData)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "FCM token saved successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Error saving FCM token", e));
        }
    }
    
    /**
     * Envoie une notification locale
     */
    public void sendLocalNotification(UserNotification notification) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 
            notification.getId().hashCode(), 
            intent, 
            PendingIntent.FLAG_IMMUTABLE
        );
        
        String channelId = getChannelForType(notification.getType());
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(notification.getTitle())
            .setContentText(notification.getMessage())
            .setPriority(getPriorityForType(notification.getType()))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true);
        
        // Ajouter une image si disponible
        if (notification.getImageUrl() != null) {
            // TODO: Charger l'image avec Glide et l'ajouter à la notification
        }
        
        notificationManager.notify(notification.getId().hashCode(), builder.build());
        
        // Marquer comme envoyée
        markNotificationAsSent(notification.getId());
    }
    
    /**
     * Crée et sauvegarde une notification
     */
    public Task<String> createNotification(UserNotification notification) {
        String notificationId = db.collection(NOTIFICATIONS_COLLECTION).document().getId();
        notification.setId(notificationId);
        notification.setCreatedAt(new Date());
        
        return db.collection(NOTIFICATIONS_COLLECTION)
            .document(notificationId)
            .set(notification.toMap())
            .continueWith(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Notification created: " + notificationId);
                    return notificationId;
                } else {
                    throw new RuntimeException("Failed to create notification");
                }
            });
    }
    
    /**
     * Programme une notification pour plus tard
     */
    public void scheduleNotification(UserNotification notification, long delayMillis) {
        Data inputData = new Data.Builder()
            .putString("notificationId", notification.getId())
            .putString("title", notification.getTitle())
            .putString("message", notification.getMessage())
            .putString("type", notification.getType().name())
            .build();
        
        OneTimeWorkRequest notificationWork = new OneTimeWorkRequest.Builder(NotificationWorker.class)
            .setInputData(inputData)
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .build();
        
        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                "notification_" + notification.getId(),
                ExistingWorkPolicy.REPLACE,
                notificationWork
            );
    }
    
    /**
     * Programme des rappels quotidiens d'apprentissage
     */
    public void scheduleDailyLearningReminders() {
        if (!isNotificationTypeEnabled(UserNotification.NotificationType.COURSE_REMINDER)) {
            return;
        }
        
        Data inputData = new Data.Builder()
            .putString("action", "daily_reminder")
            .build();
        
        PeriodicWorkRequest dailyReminder = new PeriodicWorkRequest.Builder(
            NotificationWorker.class,
            24,
            TimeUnit.HOURS
        )
            .setInputData(inputData)
            .build();
        
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                "daily_learning_reminder",
                ExistingWorkPolicy.REPLACE,
                dailyReminder
            );
    }
    
    /**
     * Récupère les notifications de l'utilisateur
     */
    public Task<List<UserNotification>> getUserNotifications(int limit) {
        String userId = getCurrentUserId();
        if (userId == null) {
            return Tasks.forResult(new ArrayList<>());
        }
        
        return db.collection(NOTIFICATIONS_COLLECTION)
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit)
            .get()
            .continueWith(task -> {
                List<UserNotification> notifications = new ArrayList<>();
                if (task.isSuccessful()) {
                    for (DocumentSnapshot doc : task.getResult()) {
                        notifications.add(UserNotification.fromDocument(doc));
                    }
                }
                return notifications;
            });
    }
    
    /**
     * Marque une notification comme lue
     */
    public Task<Void> markNotificationAsRead(String notificationId) {
        return db.collection(NOTIFICATIONS_COLLECTION)
            .document(notificationId)
            .update("isRead", true, "readAt", new Date());
    }
    
    /**
     * Marque une notification comme envoyée
     */
    private void markNotificationAsSent(String notificationId) {
        db.collection(NOTIFICATIONS_COLLECTION)
            .document(notificationId)
            .update("isSent", true, "sentAt", new Date())
            .addOnFailureListener(e -> Log.e(TAG, "Error marking notification as sent", e));
    }
    
    /**
     * Supprime une notification
     */
    public Task<Void> deleteNotification(String notificationId) {
        return db.collection(NOTIFICATIONS_COLLECTION)
            .document(notificationId)
            .delete();
    }
    
    /**
     * Envoie une notification de réussite
     */
    public void sendAchievementNotification(String title, String message) {
        if (!isNotificationTypeEnabled(UserNotification.NotificationType.ACHIEVEMENT)) {
            return;
        }
        
        UserNotification notification = new UserNotification();
        notification.setUserId(getCurrentUserId());
        notification.setType(UserNotification.NotificationType.ACHIEVEMENT);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setPriority(UserNotification.Priority.NORMAL);
        
        createNotification(notification)
            .addOnSuccessListener(id -> sendLocalNotification(notification))
            .addOnFailureListener(e -> Log.e(TAG, "Error sending achievement notification", e));
    }
    
    /**
     * Envoie une notification de rappel de cours
     */
    public void sendCourseReminderNotification(String courseTitle) {
        if (!isNotificationTypeEnabled(UserNotification.NotificationType.COURSE_REMINDER)) {
            return;
        }
        
        UserNotification notification = new UserNotification();
        notification.setUserId(getCurrentUserId());
        notification.setType(UserNotification.NotificationType.COURSE_REMINDER);
        notification.setTitle("Il est temps d'apprendre !");
        notification.setMessage("Continuez votre progression dans " + courseTitle);
        notification.setPriority(UserNotification.Priority.NORMAL);
        
        createNotification(notification)
            .addOnSuccessListener(id -> sendLocalNotification(notification))
            .addOnFailureListener(e -> Log.e(TAG, "Error sending course reminder", e));
    }
    
    /**
     * Envoie une notification d'alerte de quiz
     */
    public void sendQuizAlertNotification(String quizTitle, String message) {
        if (!isNotificationTypeEnabled(UserNotification.NotificationType.QUIZ_DUE)) {
            return;
        }
        
        UserNotification notification = new UserNotification();
        notification.setUserId(getCurrentUserId());
        notification.setType(UserNotification.NotificationType.QUIZ_DUE);
        notification.setTitle("Quiz à faire : " + quizTitle);
        notification.setMessage(message);
        notification.setPriority(UserNotification.Priority.HIGH);
        
        createNotification(notification)
            .addOnSuccessListener(id -> sendLocalNotification(notification))
            .addOnFailureListener(e -> Log.e(TAG, "Error sending quiz alert", e));
    }
    
    /**
     * Configuration des préférences de notification
     */
    public void setNotificationTypeEnabled(UserNotification.NotificationType type, boolean enabled) {
        String key = getPreferenceKeyForType(type);
        preferences.edit().putBoolean(key, enabled).apply();
        
        // Sauvegarder aussi dans Firestore pour synchronisation
        saveNotificationSettings();
    }
    
    public boolean isNotificationTypeEnabled(UserNotification.NotificationType type) {
        String key = getPreferenceKeyForType(type);
        return preferences.getBoolean(key, true); // Activé par défaut
    }
    
    /**
     * Sauvegarde les paramètres de notification dans Firestore
     */
    private void saveNotificationSettings() {
        String userId = getCurrentUserId();
        if (userId == null) return;
        
        Map<String, Object> settings = new HashMap<>();
        settings.put("courseReminders", preferences.getBoolean(PREF_COURSE_REMINDERS, true));
        settings.put("quizAlerts", preferences.getBoolean(PREF_QUIZ_ALERTS, true));
        settings.put("achievements", preferences.getBoolean(PREF_ACHIEVEMENTS, true));
        settings.put("general", preferences.getBoolean(PREF_GENERAL, true));
        settings.put("updatedAt", new Date());
        
        db.collection(USER_SETTINGS_COLLECTION)
            .document(userId)
            .set(settings)
            .addOnFailureListener(e -> Log.e(TAG, "Error saving notification settings", e));
    }
    
    /**
     * Méthodes utilitaires
     */
    private String getChannelForType(UserNotification.NotificationType type) {
        switch (type) {
            case COURSE_REMINDER:
                return CHANNEL_COURSE_REMINDERS;
            case QUIZ_DUE:
                return CHANNEL_QUIZ_ALERTS;
            case ACHIEVEMENT:
                return CHANNEL_ACHIEVEMENTS;
            default:
                return CHANNEL_GENERAL;
        }
    }
    
    private int getPriorityForType(UserNotification.NotificationType type) {
        switch (type) {
            case QUIZ_DUE:
                return NotificationCompat.PRIORITY_HIGH;
            case ACHIEVEMENT:
            case COURSE_REMINDER:
                return NotificationCompat.PRIORITY_DEFAULT;
            default:
                return NotificationCompat.PRIORITY_LOW;
        }
    }
    
    private String getPreferenceKeyForType(UserNotification.NotificationType type) {
        switch (type) {
            case COURSE_REMINDER:
                return PREF_COURSE_REMINDERS;
            case QUIZ_DUE:
                return PREF_QUIZ_ALERTS;
            case ACHIEVEMENT:
                return PREF_ACHIEVEMENTS;
            default:
                return PREF_GENERAL;
        }
    }
    
    private String getCurrentUserId() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }
} 