package com.example.learnizone.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.learnizone.R;
import com.example.learnizone.models.Lesson;
import com.example.learnizone.models.LessonProgress;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LessonAdapter extends RecyclerView.Adapter<LessonAdapter.LessonViewHolder> {

    private List<Lesson> lessons = new ArrayList<>();
    private Map<String, LessonProgress> progressMap;
    private OnLessonClickListener listener;

    public interface OnLessonClickListener {
        void onLessonClick(Lesson lesson);
        void onDownloadClick(Lesson lesson);
    }

    public LessonAdapter(OnLessonClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public LessonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lesson, parent, false);
        return new LessonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LessonViewHolder holder, int position) {
        Lesson lesson = lessons.get(position);
        LessonProgress progress = progressMap != null ? progressMap.get(lesson.getId()) : null;

        holder.bind(lesson, progress);
    }

    @Override
    public int getItemCount() {
        return lessons.size();
    }

    public void setLessons(List<Lesson> lessons) {
        this.lessons = lessons;
        notifyDataSetChanged();
    }

    public void setProgressMap(Map<String, LessonProgress> progressMap) {
        this.progressMap = progressMap;
        notifyDataSetChanged();
    }

    class LessonViewHolder extends RecyclerView.ViewHolder {
        private final ImageView lessonTypeIcon;
        private final TextView lessonTitle;
        private final TextView lessonDuration;
        private final ImageView lessonStatus;
        private final LinearProgressIndicator lessonProgress;

        LessonViewHolder(@NonNull View itemView) {
            super(itemView);
            lessonTypeIcon = itemView.findViewById(R.id.lessonTypeIcon);
            lessonTitle = itemView.findViewById(R.id.lessonTitle);
            lessonDuration = itemView.findViewById(R.id.lessonDuration);
            lessonStatus = itemView.findViewById(R.id.lessonStatus);
            lessonProgress = itemView.findViewById(R.id.lessonProgress);
        }

        void bind(Lesson lesson, LessonProgress progress) {
            lessonTitle.setText(lesson.getTitle());
            lessonDuration.setText(itemView.getContext().getString(
                    R.string.lesson_duration_format, lesson.getDurationMinutes()));

            // Set lesson type icon
            switch (lesson.getType()) {
                case VIDEO:
                    lessonTypeIcon.setImageResource(R.drawable.ic_lesson_video);
                    break;
                case TEXT:
                    lessonTypeIcon.setImageResource(R.drawable.ic_lesson_text);
                    break;
                case AUDIO:
                    lessonTypeIcon.setImageResource(R.drawable.ic_lesson_audio);
                    break;
                case QUIZ:
                    lessonTypeIcon.setImageResource(R.drawable.ic_quiz);
                    break;
            }

            // Set lesson status and progress
            if (progress != null) {
                lessonProgress.setProgress(progress.getProgress());
                lessonProgress.setVisibility(View.VISIBLE);

                if (progress.isCompleted()) {
                    lessonStatus.setImageResource(R.drawable.ic_check_circle);
                    lessonStatus.setContentDescription(
                            itemView.getContext().getString(R.string.lesson_completed));
                } else if (progress.getProgress() > 0) {
                    lessonStatus.setImageResource(R.drawable.ic_pause);
                    lessonStatus.setContentDescription(
                            itemView.getContext().getString(R.string.lesson_in_progress));
                } else {
                    lessonStatus.setImageResource(R.drawable.ic_play_arrow);
                    lessonStatus.setContentDescription(
                            itemView.getContext().getString(R.string.lesson_not_started));
                }
            } else {
                lessonProgress.setVisibility(View.GONE);
                lessonStatus.setImageResource(R.drawable.ic_lock);
                lessonStatus.setContentDescription(
                        itemView.getContext().getString(R.string.lesson_locked));
            }

            // Set click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onLessonClick(lesson);
                }
            });

            lessonStatus.setOnClickListener(v -> {
                if (listener != null) {
                    if (progress == null) {
                        listener.onDownloadClick(lesson);
                    } else {
                        listener.onLessonClick(lesson);
                    }
                }
            });
        }
    }
} 