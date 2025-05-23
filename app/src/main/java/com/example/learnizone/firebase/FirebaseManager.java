package com.example.learnizone.firebase;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.android.gms.tasks.Tasks;



import java.util.HashMap;
import java.util.Map;

public class FirebaseManager {
    private static final String TAG = "FirebaseManager";
    private static FirebaseManager instance;
    private final FirebaseFirestore firestore;
    private final FirebaseStorage storage;

    // Collection names
    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_COURSES = "courses";
    public static final String COLLECTION_ENROLLMENTS = "enrollments";
    public static final String COLLECTION_REVIEWS = "reviews";
    public static final String COLLECTION_CATEGORIES = "categories";

    // Listener registrations
    private ListenerRegistration usersListener;
    private ListenerRegistration coursesListener;
    private ListenerRegistration categoriesListener;

    private FirebaseManager() {
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    public static synchronized FirebaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }

    public static void initialize(Context context) {
        FirebaseApp.initializeApp(context);
    }

    public FirebaseFirestore getFirestore() {
        return firestore;
    }

    public FirebaseStorage getStorage() {
        return storage;
    }

    // Collection References
    public CollectionReference getUsersCollection() {
        return firestore.collection(COLLECTION_USERS);
    }

    public CollectionReference getCoursesCollection() {
        return firestore.collection(COLLECTION_COURSES);
    }

    public CollectionReference getEnrollmentsCollection() {
        return firestore.collection(COLLECTION_ENROLLMENTS);
    }

    public CollectionReference getReviewsCollection() {
        return firestore.collection(COLLECTION_REVIEWS);
    }

    public CollectionReference getCategoriesCollection() {
        return firestore.collection(COLLECTION_CATEGORIES);
    }

    // Create initial collections with sample data
    public void createInitialCollections() {
        createCategories();
        createSampleCourses();
    }

    private void createCategories() {
        Map<String, Object> categories = new HashMap<>();
        categories.put("Development", new HashMap<String, Object>() {{
            put("name", "Development");
            put("description", "Programming and software development courses");
            put("courseCount", 0);
        }});
        categories.put("Business", new HashMap<String, Object>() {{
            put("name", "Business");
            put("description", "Business and management courses");
            put("courseCount", 0);
        }});
        categories.put("Design", new HashMap<String, Object>() {{
            put("name", "Design");
            put("description", "Design and creative courses");
            put("courseCount", 0);
        }});

        for (Map.Entry<String, Object> category : categories.entrySet()) {
            getCategoriesCollection()
                .document(category.getKey())
                .set(category.getValue())
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Category created: " + category.getKey()))
                .addOnFailureListener(e -> Log.e(TAG, "Error creating category: " + category.getKey(), e));
        }
    }

    private void createSampleCourses() {
        // This will be called after categories are created
        getCategoriesCollection().get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                String categoryId = queryDocumentSnapshots.getDocuments().get(0).getId();
                
                Map<String, Object> course = new HashMap<>();
                course.put("title", "Introduction to Android Development");
                course.put("description", "Learn Android development from scratch");
                course.put("category", categoryId);
                course.put("price", 49.99);
                course.put("level", "BEGINNER");
                course.put("rating", 4.5);
                course.put("totalRatings", 0);
                course.put("enrolledStudents", 0);
                course.put("isPublished", true);

                getCoursesCollection()
                    .add(course)
                    .addOnSuccessListener(documentReference -> {
                        Log.d(TAG, "Sample course created with ID: " + documentReference.getId());
                        // Update category course count
                        getCategoriesCollection().document(categoryId)
                            .update("courseCount", 1);
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Error creating sample course", e));
            }
        });
    }

    // Helper method to create a new document in any collection
    public Task<DocumentReference> createDocument(String collectionName, Map<String, Object> data) {
        return firestore.collection(collectionName).add(data);
    }

    // Helper method to update a document
    public Task<Void> updateDocument(String collectionName, String documentId, Map<String, Object> data) {
        return firestore.collection(collectionName).document(documentId).update(data);
    }

    // Helper method to delete a document
    public Task<Void> deleteDocument(String collectionName, String documentId) {
        return firestore.collection(collectionName).document(documentId).delete();
    }

    // Helper method to get a document
    public Task<DocumentReference> getDocument(String collectionName, String documentId) {
        return Tasks.forResult(firestore.collection(collectionName).document(documentId));
    }

    // Real-time listeners for collections
    public ListenerRegistration observeUsers(OnCollectionChangedListener listener) {
        if (usersListener != null) {
            usersListener.remove();
        }
        usersListener = getUsersCollection()
            .addSnapshotListener((value, error) -> {
                if (error != null) {
                    Log.e(TAG, "Error listening to users collection", error);
                    return;
                }
                if (value != null) {
                    listener.onCollectionChanged(value);
                }
            });
        return usersListener;
    }

    public ListenerRegistration observeCourses(OnCollectionChangedListener listener) {
        if (coursesListener != null) {
            coursesListener.remove();
        }
        coursesListener = getCoursesCollection()
            .addSnapshotListener((value, error) -> {
                if (error != null) {
                    Log.e(TAG, "Error listening to courses collection", error);
                    return;
                }
                if (value != null) {
                    listener.onCollectionChanged(value);
                }
            });
        return coursesListener;
    }

    public ListenerRegistration observeCategories(OnCollectionChangedListener listener) {
        if (categoriesListener != null) {
            categoriesListener.remove();
        }
        categoriesListener = getCategoriesCollection()
            .addSnapshotListener((value, error) -> {
                if (error != null) {
                    Log.e(TAG, "Error listening to categories collection", error);
                    return;
                }
                if (value != null) {
                    listener.onCollectionChanged(value);
                }
            });
        return categoriesListener;
    }

    // Real-time listener for a specific document
    public ListenerRegistration observeDocument(String collectionName, String documentId, OnDocumentChangedListener listener) {
        return firestore.collection(collectionName)
            .document(documentId)
            .addSnapshotListener((snapshot, error) -> {
                if (error != null) {
                    Log.e(TAG, "Error listening to document", error);
                    return;
                }
                if (snapshot != null) {
                    listener.onDocumentChanged(snapshot);
                }
            });
    }

    // Real-time listener for user's enrolled courses
    public ListenerRegistration observeUserEnrollments(String userId, OnCollectionChangedListener listener) {
        return getEnrollmentsCollection()
            .whereEqualTo("userId", userId)
            .addSnapshotListener((value, error) -> {
                if (error != null) {
                    Log.e(TAG, "Error listening to user enrollments", error);
                    return;
                }
                if (value != null) {
                    listener.onCollectionChanged(value);
                }
            });
    }

    // Real-time listener for course reviews
    public ListenerRegistration observeCourseReviews(String courseId, OnCollectionChangedListener listener) {
        return getReviewsCollection()
            .whereEqualTo("courseId", courseId)
            .addSnapshotListener((value, error) -> {
                if (error != null) {
                    Log.e(TAG, "Error listening to course reviews", error);
                    return;
                }
                if (value != null) {
                    listener.onCollectionChanged(value);
                }
            });
    }

    // Remove all listeners
    public void removeAllListeners() {
        if (usersListener != null) usersListener.remove();
        if (coursesListener != null) coursesListener.remove();
        if (categoriesListener != null) categoriesListener.remove();
    }

    // Interface for collection changes
    public interface OnCollectionChangedListener {
        void onCollectionChanged(QuerySnapshot snapshot);
    }

    // Interface for document changes
    public interface OnDocumentChangedListener {
        void onDocumentChanged(DocumentSnapshot snapshot);
    }
} 