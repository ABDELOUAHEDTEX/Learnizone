package com.example.learnizone.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.learnizone.R;
import com.example.learnizone.adapters.LessonAdapter;
import com.example.learnizone.managers.CourseManager;
import com.example.learnizone.managers.LessonManager;
import com.example.learnizone.models.Course;
import com.example.learnizone.models.Lesson;
import com.example.learnizone.models.LessonProgress;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LessonListActivity extends AppCompatActivity implements LessonAdapter.OnLessonClickListener {

    public static final String EXTRA_COURSE_ID = "course_id";
    public static final String EXTRA_ENROLLMENT_ID = "enrollment_id";

    private String courseId;
    private String enrollmentId;
    private Course course;
    private List<Lesson> lessons = new ArrayList<>();
    private Map<String, LessonProgress> progressMap = new HashMap<>();

    private RecyclerView lessonsRecyclerView;
    private LessonAdapter lessonAdapter;
    private LinearProgressIndicator courseProgressIndicator;
    private TextView progressText;
    private View fabDownloadAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_list);

        // Get extras
        courseId = getIntent().getStringExtra(EXTRA_COURSE_ID);
        enrollmentId = getIntent().getStringExtra(EXTRA_ENROLLMENT_ID);

        if (courseId == null || enrollmentId == null) {
            finish();
            return;
        }

        // Initialize views
        initializeViews();
        setupRecyclerView();
        loadCourse();
        loadLessons();
        loadProgress();
    }

    private void initializeViews() {
        lessonsRecyclerView = findViewById(R.id.lessonsRecyclerView);
        courseProgressIndicator = findViewById(R.id.progressIndicator);
        progressText = findViewById(R.id.progressText);
        fabDownloadAll = findViewById(R.id.fabDownloadAll);

        // Setup toolbar
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Setup FAB
        fabDownloadAll.setOnClickListener(v -> downloadAllLessons());
    }

    private void setupRecyclerView() {
        lessonAdapter = new LessonAdapter(this);
        lessonsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        lessonsRecyclerView.setAdapter(lessonAdapter);
    }

    private void loadCourse() {
        CourseManager.getInstance().getCourse(courseId, new CourseManager.OnCourseLoadedListener() {
            @Override
            public void onCourseLoaded(Course course) {
                LessonListActivity.this.course = course;
                getSupportActionBar().setTitle(course.getTitle());
            }

            @Override
            public void onError(Exception e) {
                // Handle error
            }
        });
    }

    private void loadLessons() {
        LessonManager.getInstance().getLessonsForCourse(courseId, new LessonManager.OnLessonsLoadedListener() {
            @Override
            public void onLessonsLoaded(List<Lesson> lessons) {
                LessonListActivity.this.lessons = lessons;
                lessonAdapter.setLessons(lessons);
                updateCourseProgress();
            }

            @Override
            public void onError(Exception e) {
                // Handle error
            }
        });
    }

    private void loadProgress() {
        LessonManager.getInstance().getLessonProgress(enrollmentId, new LessonManager.OnProgressLoadedListener() {
            @Override
            public void onProgressLoaded(Map<String, LessonProgress> progressMap) {
                LessonListActivity.this.progressMap = progressMap;
                lessonAdapter.setProgressMap(progressMap);
                updateCourseProgress();
            }

            @Override
            public void onError(Exception e) {
                // Handle error
            }
        });
    }

    private void updateCourseProgress() {
        if (lessons.isEmpty() || progressMap.isEmpty()) {
            return;
        }

        int totalProgress = 0;
        int completedLessons = 0;

        for (Lesson lesson : lessons) {
            LessonProgress progress = progressMap.get(lesson.getId());
            if (progress != null) {
                totalProgress += progress.getProgress();
                if (progress.isCompleted()) {
                    completedLessons++;
                }
            }
        }

        int averageProgress = totalProgress / lessons.size();
        courseProgressIndicator.setProgress(averageProgress);
        progressText.setText(getString(R.string.lesson_progress_format, averageProgress));
    }

    private void downloadAllLessons() {
        // Implement download all lessons functionality
    }

    @Override
    public void onLessonClick(Lesson lesson) {
        LessonProgress progress = progressMap.get(lesson.getId());
        if (progress != null) {
            Intent intent = new Intent(this, LessonDetailActivity.class);
            intent.putExtra(LessonDetailActivity.EXTRA_LESSON_ID, lesson.getId());
            intent.putExtra(LessonDetailActivity.EXTRA_ENROLLMENT_ID, enrollmentId);
            startActivity(intent);
        }
    }

    @Override
    public void onDownloadClick(Lesson lesson) {
        // Implement download lesson functionality
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 