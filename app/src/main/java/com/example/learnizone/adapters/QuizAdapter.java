package com.example.learnizone.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.learnizone.R;
import com.example.learnizone.managers.QuizManager;
import com.example.learnizone.models.Quiz;
import com.example.learnizone.models.QuizAttempt;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class QuizAdapter extends ListAdapter<Quiz, QuizAdapter.QuizViewHolder> {

    private OnQuizActionListener listener;
    private Context context;
    private QuizManager quizManager;

    public interface OnQuizActionListener {
        void onQuizClick(Quiz quiz);
        void onStartQuiz(Quiz quiz);
        void onViewResults(Quiz quiz);
    }

    public QuizAdapter(OnQuizActionListener listener) {
        super(new QuizDiffCallback());
        this.listener = listener;
        this.quizManager = QuizManager.getInstance();
    }

    @NonNull
    @Override
    public QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_quiz, parent, false);
        return new QuizViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuizViewHolder holder, int position) {
        Quiz quiz = getItem(position);
        holder.bind(quiz);
    }

    private static class QuizDiffCallback extends DiffUtil.ItemCallback<Quiz> {
        @Override
        public boolean areItemsTheSame(@NonNull Quiz oldItem, @NonNull Quiz newItem) {
            return oldItem.getQuizId().equals(newItem.getQuizId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Quiz oldItem, @NonNull Quiz newItem) {
            return oldItem.equals(newItem);
        }
    }

    class QuizViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardView;
        private TextView titleText;
        private TextView descriptionText;
        private TextView timeLimitText;
        private TextView passingScoreText;
        private TextView questionsCountText;
        private TextView lastAttemptText;
        private MaterialButton actionButton;

        public QuizViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            titleText = itemView.findViewById(R.id.quiz_title);
            descriptionText = itemView.findViewById(R.id.quiz_description);
            timeLimitText = itemView.findViewById(R.id.quiz_time_limit);
            passingScoreText = itemView.findViewById(R.id.quiz_passing_score);
            questionsCountText = itemView.findViewById(R.id.quiz_questions_count);
            lastAttemptText = itemView.findViewById(R.id.quiz_last_attempt);
            actionButton = itemView.findViewById(R.id.quiz_action_button);

            cardView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onQuizClick(getItem(position));
                }
            });

            actionButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    Quiz quiz = getItem(position);
                    if (actionButton.getText().toString().equals(context.getString(R.string.view_results))) {
                        listener.onViewResults(quiz);
                    } else {
                        listener.onStartQuiz(quiz);
                    }
                }
            });
        }

        public void bind(Quiz quiz) {
            bindQuizDetails(quiz);
            loadAttemptInfo(quiz);
        }

        private void bindQuizDetails(Quiz quiz) {
            titleText.setText(quiz.getTitle());
            descriptionText.setText(quiz.getDescription());
            
            // Time limit with icon
            if (quiz.getTimeLimit() > 0) {
                timeLimitText.setText(String.format(Locale.getDefault(), 
                    context.getString(R.string.quiz_time_format), quiz.getTimeLimit()));
                timeLimitText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_timer, 0, 0, 0);
                timeLimitText.setVisibility(View.VISIBLE);
            } else {
                timeLimitText.setVisibility(View.GONE);
            }

            // Passing score with icon
            passingScoreText.setText(String.format(Locale.getDefault(),
                context.getString(R.string.quiz_passing_score_format), quiz.getPassingScore()));
            passingScoreText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_score, 0, 0, 0);

            // Questions count with icon
            questionsCountText.setText(String.format(Locale.getDefault(),
                context.getString(R.string.quiz_questions_count_format), quiz.getTotalQuestions()));
            questionsCountText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_question, 0, 0, 0);
        }

        private void loadAttemptInfo(Quiz quiz) {
            quizManager.getUserQuizAttempts(quiz.getQuizId())
                .addOnSuccessListener(attempts -> {
                    if (attempts != null && !attempts.isEmpty()) {
                        QuizAttempt lastAttempt = attempts.get(0);
                        bindAttemptInfo(quiz, lastAttempt);
                    } else {
                        bindAttemptInfo(quiz, null);
                    }
                })
                .addOnFailureListener(e -> {
                    // En cas d'erreur, afficher le quiz comme nouveau
                    bindAttemptInfo(quiz, null);
                });
        }

        private void bindAttemptInfo(Quiz quiz, QuizAttempt lastAttempt) {
            if (lastAttempt != null) {
                // Show last attempt info with icon
                String attemptText = String.format(Locale.getDefault(),
                    context.getString(R.string.quiz_last_attempt_format), lastAttempt.getPercentage());
                lastAttemptText.setText(attemptText);
                lastAttemptText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_score, 0, 0, 0);
                lastAttemptText.setVisibility(View.VISIBLE);

                // Configure action button
                if (lastAttempt.isPassed()) {
                    actionButton.setText(R.string.view_results);
                    actionButton.setIconResource(R.drawable.ic_score);
                    actionButton.setEnabled(true);
                } else if (quiz.getMaxAttempts() == 0 || 
                         lastAttempt.getAttemptNumber() < quiz.getMaxAttempts()) {
                    actionButton.setText(R.string.retry_quiz);
                    actionButton.setIconResource(R.drawable.ic_play);
                    actionButton.setEnabled(true);
                } else {
                    actionButton.setText(R.string.max_attempts_reached);
                    actionButton.setIconResource(R.drawable.ic_score);
                    actionButton.setEnabled(false);
                }
            } else {
                // No previous attempt
                lastAttemptText.setVisibility(View.GONE);
                actionButton.setText(R.string.start_quiz);
                actionButton.setIconResource(R.drawable.ic_play);
                actionButton.setEnabled(true);
            }
        }
    }
} 