package com.example.learnizone;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.learnizone.managers.QuizManager;
import com.example.learnizone.models.QuestionAnswer;
import com.example.learnizone.models.Quiz;
import com.example.learnizone.models.QuizAttempt;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class QuizActivity extends AppCompatActivity {

    private TextView timerText;
    private QuizManager quizManager;
    private Quiz quiz;
    private QuizAttempt currentAttempt;
    private List<QuestionAnswer> userAnswers;
    private CountDownTimer countDownTimer;
    private boolean isQuizCompleted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        timerText = findViewById(R.id.timer_text);
        quizManager = QuizManager.getInstance();
        userAnswers = new ArrayList<QuestionAnswer>();

        String quizId = getIntent().getStringExtra("quizId");
        if (quizId != null) {
            loadQuiz(quizId);
        } else {
            Toast.makeText(this, R.string.quiz_error_not_found, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadQuiz(String quizId) {
        showLoading(true);
        
        quizManager.getQuizWithQuestions(quizId)
            .addOnSuccessListener(loadedQuiz -> {
                this.quiz = loadedQuiz;
                userAnswers.clear();
                for (int i = 0; i < quiz.getQuestions().size(); i++) {
                    userAnswers.add(new QuestionAnswer(quiz.getQuestions().get(i).getQuestionId(), currentAttempt != null ? currentAttempt.getAttemptId() : ""));
                }
                setupQuizUI();
                startQuizAttempt();
                showLoading(false);
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                Toast.makeText(this, R.string.quiz_error_loading, Toast.LENGTH_SHORT).show();
                finish();
            });
    }

    private void startQuizAttempt() {
        if (quiz == null || quiz.getQuizId().isEmpty()) {
             Toast.makeText(this, R.string.quiz_error_start, Toast.LENGTH_SHORT).show();
             finish();
             return;
        }

        quizManager.startQuizAttempt(quiz.getQuizId())
            .addOnSuccessListener(attempt -> {
                this.currentAttempt = attempt;

                if (userAnswers != null && currentAttempt != null) {
                    for (QuestionAnswer answer : userAnswers) {
                        if (answer.getAttemptId().isEmpty()) {
                            answer.setAttemptId(currentAttempt.getAttemptId());
                        }
                    }
                }

                if (quiz.getTimeLimit() > 0) {
                    startTimer(quiz.getTimeLimit() * 60 * 1000);
                    timerText.setVisibility(View.VISIBLE);
                } else {
                    timerText.setVisibility(View.GONE);
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, R.string.quiz_error_start, Toast.LENGTH_SHORT).show();
                finish();
            });
    }

    private void showSubmitConfirmation() {
        int answeredQuestions = 0;
        if (userAnswers != null) {
            for (QuestionAnswer answer : userAnswers) {
                if (answer.getUserAnswer() != null && !answer.getUserAnswer().trim().isEmpty()) {
                    answeredQuestions++;
                }
            }
        }
        
        String message;
        if (quiz != null && answeredQuestions < quiz.getQuestions().size()) {
            int unanswered = quiz.getQuestions().size() - answeredQuestions;
            message = getString(R.string.quiz_submit_unanswered, unanswered);
        } else {
            message = getString(R.string.quiz_submit_message);
        }
        
        new AlertDialog.Builder(this)
            .setTitle(R.string.quiz_submit_title)
            .setMessage(message)
            .setPositiveButton(R.string.quiz_submit_confirm, (dialog, which) -> submitQuiz())
            .setNegativeButton(R.string.quiz_submit_cancel, null)
            .show();
    }

    private void submitQuiz() {
        if (isQuizCompleted) return;
        
        isQuizCompleted = true;
        showLoading(true);
        
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        
        if (currentAttempt != null && userAnswers != null) {
            quizManager.submitQuizAttempt(currentAttempt.getAttemptId(), userAnswers)
                .addOnSuccessListener(completedAttempt -> {
                    showLoading(false);
                    showQuizResults(completedAttempt);
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, R.string.quiz_error_submit, Toast.LENGTH_SHORT).show();
                    isQuizCompleted = false;
                });
        } else {
             showLoading(false);
             Toast.makeText(this, R.string.quiz_error_submit, Toast.LENGTH_SHORT).show();
             isQuizCompleted = false;
        }
    }

    private void autoSubmitQuiz() {
        if (isQuizCompleted) return;
        
        new AlertDialog.Builder(this)
            .setTitle(R.string.quiz_timeout_title)
            .setMessage(R.string.quiz_timeout_message)
            .setPositiveButton(android.R.string.ok, (dialog, which) -> submitQuiz())
            .setCancelable(false)
            .show();
    }

    private void showQuizResults(QuizAttempt attempt) {
        if (attempt == null) {
            Toast.makeText(this, R.string.quiz_error_loading, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String title = attempt.isPassed() ? 
            getString(R.string.quiz_results_title_success) : 
            getString(R.string.quiz_results_title_failed);
        
        String resultMessage = attempt.isPassed() ? 
            getString(R.string.quiz_results_success) : 
            getString(R.string.quiz_results_failed);
        
        String message = getString(R.string.quiz_results_message,
            attempt.getPercentage(),
            attempt.getDurationInMinutes(),
            resultMessage);
        
        new AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.quiz_results_view, (dialog, which) -> {
                finish();
            })
            .setNegativeButton(R.string.quiz_results_close, (dialog, which) -> finish())
            .setCancelable(false)
            .show();
    }

    private void showLoading(boolean show) {
        View loadingOverlay = findViewById(R.id.loading_overlay);
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void showExitConfirmation() {
        if (isQuizCompleted) {
            finish();
            return;
        }
        
        new AlertDialog.Builder(this)
            .setTitle(R.string.quiz_exit_title)
            .setMessage(R.string.quiz_exit_message)
            .setPositiveButton(R.string.quiz_exit_confirm, (dialog, which) -> finish())
            .setNegativeButton(R.string.quiz_exit_cancel, null)
            .show();
    }

    private void setupQuizUI() {
        if (quiz != null) {
            // Example: Initialize ViewPager, TabLayout, Buttons
            // viewPager = findViewById(R.id.view_pager);
            // tabLayout = findViewById(R.id.tab_layout);
            // previousButton = findViewById(R.id.button_previous);
            // nextButton = findViewById(R.id.button_next);
            // submitButton = findViewById(R.id.button_submit);

            // Example: Set up ViewPager with an adapter (you need to create QuizPagerAdapter)
            // QuizPagerAdapter pagerAdapter = new QuizPagerAdapter(this, quiz.getQuestions(), userAnswers);
            // viewPager.setAdapter(pagerAdapter);
            // new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> tab.setText("Question " + (position + 1))).attach();

            // Example: Set button listeners
            // previousButton.setOnClickListener(v -> { if (viewPager.getCurrentItem() > 0) viewPager.setCurrentItem(viewPager.getCurrentItem() - 1); });
            // nextButton.setOnClickListener(v -> { if (viewPager.getCurrentItem() < quiz.getQuestions().size() - 1) viewPager.setCurrentItem(viewPager.getCurrentItem() + 1); });
            // submitButton.setOnClickListener(v -> showSubmitConfirmation());

            // Update button states based on current question
            // viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            //     @Override
            //     public void onPageSelected(int position) {
            //         // Update button visibility/enabled state here
            //     }
            // });
        }
    }

    private void startTimer(long millisInFuture) {
        countDownTimer = new CountDownTimer(millisInFuture, 1000) {
            public void onTick(long millisUntilFinished) {
                long minutes = (millisUntilFinished / 1000) / 60;
                long seconds = (millisUntilFinished / 1000) % 60;
                timerText.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
            }

            public void onFinish() {
                timerText.setText("00:00");
                autoSubmitQuiz();
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
} 