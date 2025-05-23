package com.example.learnizone.workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.learnizone.managers.LearnIzoneNotificationManager;
import com.example.learnizone.models.UserNotification;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class NotificationWorker extends Worker {
    private static final String TAG = "NotificationWorker";
    private final FirebaseFirestore db;

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            String action = getInputData().getString("action");
            if (action != null) {
                switch (action) {
                    case "daily_reminder":
                        return handleDailyReminder();
                    case "streak_reminder":
                        return handleStreakReminder();
                    case "quiz_due":
                        return handleQuizDueReminder();
                    default:
                        return handleScheduledNotification();
                }
            }
            return Result.failure();
        } catch (Exception e) {
            Log.e(TAG, "Error in NotificationWorker", e);
            return Result.failure();
        }
    }

    private Result handleDailyReminder() {
        try {
            // Vérifier les cours incomplets
            checkIncompleteCourses();
            
            // Vérifier les quiz à faire
            checkUpcomingQuizzes();
            
            // Vérifier les séries d'apprentissage
            checkLearningStreaks();
            
            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Error handling daily reminder", e);
            return Result.failure();
        }
    }

    private Result handleStreakReminder() {
        try {
            String userId = getInputData().getString("userId");
            if (userId == null) return Result.failure();

            // Vérifier la série actuelle
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document != null && document.exists()) {
                        Long currentStreak = document.getLong("currentStreak");
                        if (currentStreak != null && currentStreak > 0) {
                            // Créer une notification de rappel de série
                            UserNotification notification = new UserNotification();
                            notification.setUserId(userId);
                            notification.setType(UserNotification.NotificationType.STREAK_REMINDER);
                            notification.setTitle("Ne perdez pas votre série !");
                            notification.setMessage("Vous avez une série de " + currentStreak + " jours. Continuez à apprendre aujourd'hui !");
                            notification.setPriority(UserNotification.Priority.NORMAL);
                            notification.addData("currentStreak", currentStreak);

                            LearnIzoneNotificationManager.getInstance(getApplicationContext())
                                .createNotification(notification)
                                .addOnSuccessListener(id -> 
                                    LearnIzoneNotificationManager.getInstance(getApplicationContext())
                                        .sendLocalNotification(notification)
                                );
                        }
                    }
                });

            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Error handling streak reminder", e);
            return Result.failure();
        }
    }

    private Result handleQuizDueReminder() {
        try {
            String userId = getInputData().getString("userId");
            String quizId = getInputData().getString("quizId");
            if (userId == null || quizId == null) return Result.failure();

            // Récupérer les informations du quiz
            db.collection("quizzes").document(quizId)
                .get()
                .addOnSuccessListener(quizDoc -> {
                    if (quizDoc != null && quizDoc.exists()) {
                        String quizTitle = quizDoc.getString("title");
                        Date dueDate = quizDoc.getDate("dueDate");

                        if (quizTitle != null && dueDate != null) {
                            UserNotification notification = new UserNotification();
                            notification.setUserId(userId);
                            notification.setType(UserNotification.NotificationType.QUIZ_DUE);
                            notification.setTitle("Quiz à faire : " + quizTitle);
                            notification.setMessage("Ce quiz est à faire avant le " + 
                                java.text.DateFormat.getDateInstance().format(dueDate));
                            notification.setPriority(UserNotification.Priority.HIGH);
                            notification.addData("quizId", quizId);
                            notification.addData("dueDate", dueDate);

                            LearnIzoneNotificationManager.getInstance(getApplicationContext())
                                .createNotification(notification)
                                .addOnSuccessListener(id -> 
                                    LearnIzoneNotificationManager.getInstance(getApplicationContext())
                                        .sendLocalNotification(notification)
                                );
                        }
                    }
                });

            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Error handling quiz due reminder", e);
            return Result.failure();
        }
    }

    private Result handleScheduledNotification() {
        try {
            String notificationId = getInputData().getString("notificationId");
            if (notificationId == null) return Result.failure();

            // Récupérer la notification depuis Firestore
            db.collection("notifications").document(notificationId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document != null && document.exists()) {
                        UserNotification notification = UserNotification.fromDocument(document);
                        
                        // Vérifier si la notification n'est pas déjà envoyée
                        if (!notification.isSent() && notification.isPending()) {
                            LearnIzoneNotificationManager.getInstance(getApplicationContext())
                                .sendLocalNotification(notification);
                        }
                    }
                });

            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Error handling scheduled notification", e);
            return Result.failure();
        }
    }

    private void checkIncompleteCourses() {
        String userId = getInputData().getString("userId");
        if (userId == null) return;

        // Vérifier les cours en cours
        db.collection("userCourses")
            .whereEqualTo("userId", userId)
            .whereEqualTo("status", "in_progress")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (!queryDocumentSnapshots.isEmpty()) {
                    // Créer une notification pour chaque cours incomplet
                    for (var doc : queryDocumentSnapshots) {
                        String courseId = doc.getString("courseId");
                        String courseTitle = doc.getString("courseTitle");
                        
                        if (courseId != null && courseTitle != null) {
                            UserNotification notification = new UserNotification();
                            notification.setUserId(userId);
                            notification.setType(UserNotification.NotificationType.COURSE_REMINDER);
                            notification.setTitle("Continuez votre apprentissage");
                            notification.setMessage("Reprenez votre cours : " + courseTitle);
                            notification.setPriority(UserNotification.Priority.NORMAL);
                            notification.addData("courseId", courseId);

                            LearnIzoneNotificationManager.getInstance(getApplicationContext())
                                .createNotification(notification)
                                .addOnSuccessListener(id -> 
                                    LearnIzoneNotificationManager.getInstance(getApplicationContext())
                                        .sendLocalNotification(notification)
                                );
                        }
                    }
                }
            });
    }

    private void checkUpcomingQuizzes() {
        String userId = getInputData().getString("userId");
        if (userId == null) return;

        Date now = new Date();
        Date tomorrow = new Date(now.getTime() + (24 * 60 * 60 * 1000));

        // Vérifier les quiz à venir dans les 24 prochaines heures
        db.collection("quizzes")
            .whereGreaterThanOrEqualTo("dueDate", now)
            .whereLessThanOrEqualTo("dueDate", tomorrow)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (!queryDocumentSnapshots.isEmpty()) {
                    for (var doc : queryDocumentSnapshots) {
                        String quizId = doc.getId();
                        String quizTitle = doc.getString("title");
                        Date dueDate = doc.getDate("dueDate");

                        if (quizId != null && quizTitle != null && dueDate != null) {
                            UserNotification notification = new UserNotification();
                            notification.setUserId(userId);
                            notification.setType(UserNotification.NotificationType.QUIZ_DUE);
                            notification.setTitle("Quiz à faire : " + quizTitle);
                            notification.setMessage("Ce quiz est à faire avant le " + 
                                java.text.DateFormat.getDateInstance().format(dueDate));
                            notification.setPriority(UserNotification.Priority.HIGH);
                            notification.addData("quizId", quizId);
                            notification.addData("dueDate", dueDate);

                            LearnIzoneNotificationManager.getInstance(getApplicationContext())
                                .createNotification(notification)
                                .addOnSuccessListener(id -> 
                                    LearnIzoneNotificationManager.getInstance(getApplicationContext())
                                        .sendLocalNotification(notification)
                                );
                        }
                    }
                }
            });
    }

    private void checkLearningStreaks() {
        String userId = getInputData().getString("userId");
        if (userId == null) return;

        // Vérifier la série d'apprentissage
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener(document -> {
                if (document != null && document.exists()) {
                    Long currentStreak = document.getLong("currentStreak");
                    Date lastActivityDate = document.getDate("lastActivityDate");

                    if (currentStreak != null && currentStreak > 0 && lastActivityDate != null) {
                        // Vérifier si l'utilisateur n'a pas eu d'activité aujourd'hui
                        Date today = new Date();
                        if (today.getDate() != lastActivityDate.getDate()) {
                            UserNotification notification = new UserNotification();
                            notification.setUserId(userId);
                            notification.setType(UserNotification.NotificationType.STREAK_REMINDER);
                            notification.setTitle("Ne perdez pas votre série !");
                            notification.setMessage("Vous avez une série de " + currentStreak + 
                                " jours. Continuez à apprendre aujourd'hui !");
                            notification.setPriority(UserNotification.Priority.NORMAL);
                            notification.addData("currentStreak", currentStreak);

                            LearnIzoneNotificationManager.getInstance(getApplicationContext())
                                .createNotification(notification)
                                .addOnSuccessListener(id -> 
                                    LearnIzoneNotificationManager.getInstance(getApplicationContext())
                                        .sendLocalNotification(notification)
                                );
                        }
                    }
                }
            });
    }
}