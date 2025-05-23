package com.example.learnizone.managers;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.content.Context;
import android.util.Log;

import com.example.learnizone.models.Lesson;
import com.example.learnizone.models.LessonProgress;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Source;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class LessonManager {
    private static final String TAG = "LessonManager";
    private static LessonManager instance;
    private final FirebaseFirestore db;
    private final CollectionReference lessonsCollection;
    private final CollectionReference progressCollection;
    private final Context context;
    private static final int TIMEOUT_SECONDS = 30;
    private static final long CACHE_SIZE_BYTES = 100 * 1024 * 1024; // 100MB
    private static final long CACHE_EXPIRATION_MS = 24 * 60 * 60 * 1000; // 24 hours

    private LessonManager(Context context) {
        this.context = context.getApplicationContext();
        db = FirebaseFirestore.getInstance();
        db.setFirestoreSettings(new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(CACHE_SIZE_BYTES)
                .build());
        lessonsCollection = db.collection("lessons");
        progressCollection = db.collection("lessonProgress");
    }

    public static synchronized LessonManager getInstance(Context context) {
        if (instance == null) {
            instance = new LessonManager(context);
        }
        return instance;
    }

    public interface OnLessonsLoadedListener {
        void onLessonsLoaded(List<Lesson> lessons);
        void onError(Exception e);
    }

    public interface OnProgressLoadedListener {
        void onProgressLoaded(Map<String, LessonProgress> progressMap);
        void onError(Exception e);
    }

    public interface OnProgressUpdatedListener {
        void onProgressUpdated();
        void onError(Exception e);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }

    private void handleFirestoreError(Exception e, OnLessonsLoadedListener listener) {
        if (e instanceof FirebaseFirestoreException) {
            FirebaseFirestoreException firestoreException = (FirebaseFirestoreException) e;
            switch (firestoreException.getCode()) {
                case PERMISSION_DENIED:
                    listener.onError(new Exception("Permission denied to access lessons"));
                    break;
                case NOT_FOUND:
                    listener.onError(new Exception("Lessons not found"));
                    break;
                case UNAVAILABLE:
                    listener.onError(new Exception("Service temporarily unavailable"));
                    break;
                case DEADLINE_EXCEEDED:
                    listener.onError(new Exception("Request timed out"));
                    break;
                case RESOURCE_EXHAUSTED:
                    listener.onError(new Exception("Resource quota exceeded"));
                    break;
                case FAILED_PRECONDITION:
                    listener.onError(new Exception("Operation cannot be executed in the current state"));
                    break;
                case ABORTED:
                    listener.onError(new Exception("Operation was aborted"));
                    break;
                case OUT_OF_RANGE:
                    listener.onError(new Exception("Operation was attempted past the valid range"));
                    break;
                case UNIMPLEMENTED:
                    listener.onError(new Exception("Operation is not implemented or not supported"));
                    break;
                case INTERNAL:
                    listener.onError(new Exception("Internal error occurred"));
                    break;
                case UNAUTHENTICATED:
                    listener.onError(new Exception("User is not authenticated"));
                    break;
                default:
                    listener.onError(new Exception("Error loading lessons: " + e.getMessage()));
            }
        } else if (e instanceof ExecutionException) {
            listener.onError(new Exception("Network error occurred"));
        } else {
            listener.onError(e);
        }
    }

    private Task<List<Lesson>> getLessonsWithTimeout(String courseId) {
        return Tasks.call(() -> {
            try {
                return Tasks.await(lessonsCollection
                        .whereEqualTo("courseId", courseId)
                        .orderBy("order", Query.Direction.ASCENDING)
                        .get(Source.DEFAULT), TIMEOUT_SECONDS, TimeUnit.SECONDS)
                        .getDocuments()
                        .stream()
                        .map(document -> {
                            try {
                                return Lesson.fromDocument(document);
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing lesson: " + e.getMessage());
                                return null;
                            }
                        })
                        .filter(lesson -> lesson != null && lesson.isValid())
                        .collect(Collectors.toList());
            } catch (ExecutionException e) {
                throw new Exception("Network error occurred");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new Exception("Operation was interrupted");
            }
        });
    }

    public void getLessonsForCourse(String courseId, OnLessonsLoadedListener listener) {
        if (!isNetworkAvailable()) {
            listener.onError(new Exception("No internet connection available"));
            return;
        }

        if (courseId == null || courseId.trim().isEmpty()) {
            listener.onError(new IllegalArgumentException("Course ID cannot be null or empty"));
            return;
        }

        getLessonsWithTimeout(courseId)
                .addOnSuccessListener(lessons -> listener.onLessonsLoaded(lessons))
                .addOnFailureListener(e -> handleFirestoreError(e, listener));
    }

    private Task<Map<String, LessonProgress>> getProgressWithTimeout(String enrollmentId) {
        return Tasks.call(() -> {
            try {
                return Tasks.await(progressCollection
                        .whereEqualTo("enrollmentId", enrollmentId)
                        .get(Source.DEFAULT), TIMEOUT_SECONDS, TimeUnit.SECONDS)
                        .getDocuments()
                        .stream()
                        .collect(Collectors.toMap(
                                document -> document.getString("lessonId"),
                                document -> {
                                    try {
                                        return LessonProgress.fromDocument(document);
                                    } catch (Exception e) {
                                        Log.e(TAG, "Error parsing progress: " + e.getMessage());
                                        return null;
                                    }
                                }
                        ));
            } catch (ExecutionException e) {
                throw new Exception("Network error occurred");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new Exception("Operation was interrupted");
            }
        });
    }

    public void getLessonProgress(String enrollmentId, OnProgressLoadedListener listener) {
        if (!isNetworkAvailable()) {
            listener.onError(new Exception("No internet connection available"));
            return;
        }

        if (enrollmentId == null || enrollmentId.trim().isEmpty()) {
            listener.onError(new IllegalArgumentException("Enrollment ID cannot be null or empty"));
            return;
        }

        getProgressWithTimeout(enrollmentId)
                .addOnSuccessListener(progressMap -> listener.onProgressLoaded(progressMap))
                .addOnFailureListener(e -> {
                    if (e instanceof FirebaseFirestoreException) {
                        listener.onError(new Exception("Error loading progress: " + e.getMessage()));
                    } else {
                        listener.onError(e);
                    }
                });
    }

    public void updateLessonProgress(String enrollmentId, String lessonId, int progress, boolean completed, OnProgressUpdatedListener listener) {
        if (!isNetworkAvailable()) {
            listener.onError(new Exception("No internet connection available"));
            return;
        }

        if (enrollmentId == null || enrollmentId.trim().isEmpty()) {
            listener.onError(new IllegalArgumentException("Enrollment ID cannot be null or empty"));
            return;
        }
        if (lessonId == null || lessonId.trim().isEmpty()) {
            listener.onError(new IllegalArgumentException("Lesson ID cannot be null or empty"));
            return;
        }
        if (progress < 0 || progress > 100) {
            listener.onError(new IllegalArgumentException("Progress must be between 0 and 100"));
            return;
        }

        LessonProgress lessonProgress = new LessonProgress();
        lessonProgress.setEnrollmentId(enrollmentId);
        lessonProgress.setLessonId(lessonId);
        lessonProgress.setProgress(progress);
        lessonProgress.setCompleted(completed);
        lessonProgress.setLastAccessedAt(System.currentTimeMillis());

        progressCollection
                .document(enrollmentId + "_" + lessonId)
                .set(lessonProgress.toMap())
                .addOnSuccessListener(aVoid -> listener.onProgressUpdated())
                .addOnFailureListener(e -> {
                    if (e instanceof FirebaseFirestoreException) {
                        listener.onError(new Exception("Error updating progress: " + e.getMessage()));
                    } else {
                        listener.onError(e);
                    }
                });
    }

    private Task<Lesson> getLessonWithTimeout(String lessonId) {
        return Tasks.call(() -> {
            try {
                DocumentSnapshot document = Tasks.await(lessonsCollection
                        .document(lessonId)
                        .get(Source.DEFAULT), TIMEOUT_SECONDS, TimeUnit.SECONDS);
                
                Lesson lesson = Lesson.fromDocument(document);
                if (lesson == null || !lesson.isValid()) {
                    throw new Exception("Invalid lesson data");
                }
                return lesson;
            } catch (ExecutionException e) {
                throw new Exception("Network error occurred");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new Exception("Operation was interrupted");
            }
        });
    }

    public void getLesson(String lessonId, OnLessonsLoadedListener listener) {
        if (!isNetworkAvailable()) {
            listener.onError(new Exception("No internet connection available"));
            return;
        }

        if (lessonId == null || lessonId.trim().isEmpty()) {
            listener.onError(new IllegalArgumentException("Lesson ID cannot be null or empty"));
            return;
        }

        getLessonWithTimeout(lessonId)
                .addOnSuccessListener(lesson -> {
                    List<Lesson> lessons = new ArrayList<>();
                    lessons.add(lesson);
                    listener.onLessonsLoaded(lessons);
                })
                .addOnFailureListener(e -> handleFirestoreError(e, listener));
    }

    private Task<Lesson> getNextLessonWithTimeout(String courseId, String currentLessonId) {
        return Tasks.call(() -> {
            try {
                List<DocumentSnapshot> documents = Tasks.await(lessonsCollection
                        .whereEqualTo("courseId", courseId)
                        .orderBy("order", Query.Direction.ASCENDING)
                        .get(Source.DEFAULT), TIMEOUT_SECONDS, TimeUnit.SECONDS)
                        .getDocuments();

                boolean foundCurrent = false;
                Lesson nextLesson = null;

                for (DocumentSnapshot document : documents) {
                    try {
                        Lesson lesson = Lesson.fromDocument(document);
                        if (lesson != null && lesson.isValid()) {
                            if (foundCurrent) {
                                nextLesson = lesson;
                                break;
                            }
                            if (lesson.getId().equals(currentLessonId)) {
                                foundCurrent = true;
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing lesson: " + e.getMessage());
                    }
                }

                if (nextLesson == null) {
                    throw new Exception("This is the last lesson in the course");
                }

                return nextLesson;
            } catch (ExecutionException e) {
                throw new Exception("Network error occurred");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new Exception("Operation was interrupted");
            }
        });
    }

    public void getNextLesson(String courseId, String currentLessonId, OnLessonsLoadedListener listener) {
        if (!isNetworkAvailable()) {
            listener.onError(new Exception("No internet connection available"));
            return;
        }

        if (courseId == null || courseId.trim().isEmpty()) {
            listener.onError(new IllegalArgumentException("Course ID cannot be null or empty"));
            return;
        }
        if (currentLessonId == null || currentLessonId.trim().isEmpty()) {
            listener.onError(new IllegalArgumentException("Current Lesson ID cannot be null or empty"));
            return;
        }

        getNextLessonWithTimeout(courseId, currentLessonId)
                .addOnSuccessListener(nextLesson -> {
                    List<Lesson> lessons = new ArrayList<>();
                    lessons.add(nextLesson);
                    listener.onLessonsLoaded(lessons);
                })
                .addOnFailureListener(e -> handleFirestoreError(e, listener));
    }

    public void clearCache() {
        db.clearPersistence()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Cache cleared successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Error clearing cache: " + e.getMessage()));
    }
} 