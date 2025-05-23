private void loadQuiz(String quizId) {
    showLoading(true);
    
    quizManager.getQuizWithQuestions(quizId)
        .addOnSuccessListener(loadedQuiz -> {
            this.quiz = loadedQuiz;
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
    quizManager.startQuizAttempt(quiz.getQuizId())
        .addOnSuccessListener(attempt -> {
            this.currentAttempt = attempt;
            
            // Démarrer le minuteur si nécessaire
            if (quiz.getTimeLimit() > 0) {
                startTimer(quiz.getTimeLimit() * 60 * 1000); // Convertir en millisecondes
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
    // Vérifier si toutes les questions ont été répondues
    int answeredQuestions = 0;
    for (QuestionAnswer answer : userAnswers) {
        if (answer.getUserAnswer() != null && !answer.getUserAnswer().trim().isEmpty()) {
            answeredQuestions++;
        }
    }
    
    String message;
    if (answeredQuestions < quiz.getQuestions().size()) {
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
    
    // Arrêter le minuteur
    if (countDownTimer != null) {
        countDownTimer.cancel();
    }
    
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
            // TODO: Naviguer vers l'écran de résultats détaillés
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