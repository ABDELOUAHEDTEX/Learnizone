package com.example.learnizone.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.learnizone.R;
import com.example.learnizone.models.Question;
import com.example.learnizone.models.QuestionAnswer;

import java.util.ArrayList;
import java.util.List;

public class QuestionFragment extends Fragment {
    
    private static final String ARG_QUESTION = "question";
    private static final String ARG_ANSWER = "answer";
    private static final String ARG_QUESTION_NUMBER = "question_number";
    
    private Question question;
    private QuestionAnswer answer;
    private int questionNumber;
    
    private TextView questionNumberText;
    private TextView questionText;
    private ImageView questionImage;
    private LinearLayout answerContainer;
    private RadioGroup radioGroup;
    private EditText shortAnswerEdit;
    private LinearLayout checkboxContainer;
    
    public static QuestionFragment newInstance(Question question, QuestionAnswer answer, int questionNumber) {
        QuestionFragment fragment = new QuestionFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_QUESTION, question);
        args.putSerializable(ARG_ANSWER, answer);
        args.putInt(ARG_QUESTION_NUMBER, questionNumber);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            question = (Question) getArguments().getSerializable(ARG_QUESTION);
            answer = (QuestionAnswer) getArguments().getSerializable(ARG_ANSWER);
            questionNumber = getArguments().getInt(ARG_QUESTION_NUMBER);
        }
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_question, container, false);
        
        initViews(view);
        setupQuestion();
        
        return view;
    }
    
    private void initViews(View view) {
        questionNumberText = view.findViewById(R.id.question_number);
        questionText = view.findViewById(R.id.question_text);
        questionImage = view.findViewById(R.id.question_image);
        answerContainer = view.findViewById(R.id.answer_container);
        radioGroup = view.findViewById(R.id.radio_group);
        shortAnswerEdit = view.findViewById(R.id.short_answer_edit);
        checkboxContainer = view.findViewById(R.id.checkbox_container);
    }
    
    private void setupQuestion() {
        if (question == null) return;
        
        // Afficher le numéro de la question
        questionNumberText.setText(String.format("Question %d", questionNumber));
        
        // Afficher le texte de la question
        questionText.setText(question.getQuestionText());
        
        // Afficher l'image si elle existe
        if (question.getImageUrl() != null && !question.getImageUrl().isEmpty()) {
            questionImage.setVisibility(View.VISIBLE);
            Glide.with(this)
                .load(question.getImageUrl())
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(questionImage);
        } else {
            questionImage.setVisibility(View.GONE);
        }
        
        // Configurer les réponses selon le type de question
        setupAnswerView();
    }
    
    private void setupAnswerView() {
        // Cacher tous les containers d'abord
        radioGroup.setVisibility(View.GONE);
        shortAnswerEdit.setVisibility(View.GONE);
        checkboxContainer.setVisibility(View.GONE);
        
        switch (question.getType()) {
            case MULTIPLE_CHOICE:
                setupMultipleChoice();
                break;
            case TRUE_FALSE:
                setupTrueFalse();
                break;
            case FILL_IN_BLANK:
            case SHORT_ANSWER:
                setupShortAnswer();
                break;
            case MATCHING:
                setupMatching();
                break;
            case ESSAY:
                setupEssay();
                break;
            default:
                setupMultipleChoice();
                break;
        }
    }
    
    private void setupMultipleChoice() {
        radioGroup.setVisibility(View.VISIBLE);
        radioGroup.removeAllViews();
        
        for (int i = 0; i < question.getOptions().size(); i++) {
            String option = question.getOptions().get(i);
            
            RadioButton radioButton = new RadioButton(getContext());
            radioButton.setText(option);
            radioButton.setId(i);
            radioButton.setPadding(16, 16, 16, 16);
            radioButton.setTextSize(16);
            
            // Restaurer la réponse précédente si elle existe
            if (answer.getUserAnswer() != null && answer.getUserAnswer().equals(option)) {
                radioButton.setChecked(true);
            }
            
            radioGroup.addView(radioButton);
        }
        
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId != -1) {
                RadioButton selectedRadio = group.findViewById(checkedId);
                answer.setUserAnswer(selectedRadio.getText().toString());
            }
        });
    }
    
    private void setupTrueFalse() {
        radioGroup.setVisibility(View.VISIBLE);
        radioGroup.removeAllViews();
        
        String[] options = {"Vrai", "Faux"};
        
        for (int i = 0; i < options.length; i++) {
            RadioButton radioButton = new RadioButton(getContext());
            radioButton.setText(options[i]);
            radioButton.setId(i);
            radioButton.setPadding(16, 16, 16, 16);
            radioButton.setTextSize(16);
            
            // Restaurer la réponse précédente
            if (answer.getUserAnswer() != null && answer.getUserAnswer().equals(options[i])) {
                radioButton.setChecked(true);
            }
            
            radioGroup.addView(radioButton);
        }
        
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId != -1) {
                RadioButton selectedRadio = group.findViewById(checkedId);
                answer.setUserAnswer(selectedRadio.getText().toString());
            }
        });
    }
    
    private void setupShortAnswer() {
        shortAnswerEdit.setVisibility(View.VISIBLE);
        
        // Restaurer la réponse précédente
        if (answer.getUserAnswer() != null) {
            shortAnswerEdit.setText(answer.getUserAnswer());
        }
        
        shortAnswerEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                answer.setUserAnswer(s.toString());
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    private void setupMatching() {
        checkboxContainer.setVisibility(View.VISIBLE);
        checkboxContainer.removeAllViews();
        
        // Pour les questions à choix multiples, permettre plusieurs sélections
        for (int i = 0; i < question.getOptions().size(); i++) {
            String option = question.getOptions().get(i);
            
            CheckBox checkBox = new CheckBox(getContext());
            checkBox.setText(option);
            checkBox.setPadding(16, 16, 16, 16);
            checkBox.setTextSize(16);
            
            // Restaurer les réponses précédentes
            if (answer.getUserAnswers() != null && answer.getUserAnswers().contains(option)) {
                checkBox.setChecked(true);
            }
            
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (answer.getUserAnswers() == null) {
                    answer.setUserAnswers(new ArrayList<>());
                }
                
                if (isChecked) {
                    if (!answer.getUserAnswers().contains(option)) {
                        answer.getUserAnswers().add(option);
                    }
                } else {
                    answer.getUserAnswers().remove(option);
                }
            });
            
            checkboxContainer.addView(checkBox);
        }
    }
    
    private void setupEssay() {
        shortAnswerEdit.setVisibility(View.VISIBLE);
        shortAnswerEdit.setMinLines(5);
        shortAnswerEdit.setMaxLines(10);
        shortAnswerEdit.setHint("Rédigez votre réponse ici...");
        
        // Restaurer la réponse précédente
        if (answer.getUserAnswer() != null) {
            shortAnswerEdit.setText(answer.getUserAnswer());
        }
        
        shortAnswerEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                answer.setUserAnswer(s.toString());
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    /**
     * Vérifie si la question a été répondue
     */
    public boolean isAnswered() {
        if (answer == null) return false;
        
        switch (question.getType()) {
            case MULTIPLE_CHOICE:
            case TRUE_FALSE:
            case FILL_IN_BLANK:
            case SHORT_ANSWER:
            case ESSAY:
                return answer.getUserAnswer() != null && !answer.getUserAnswer().trim().isEmpty();
            case MATCHING:
                return answer.getUserAnswers() != null && !answer.getUserAnswers().isEmpty();
            default:
                return false;
        }
    }
    
    /**
     * Récupère la réponse actuelle
     */
    public QuestionAnswer getCurrentAnswer() {
        return answer;
    }
    
    /**
     * Force la sauvegarde de la réponse actuelle
     */
    public void saveCurrentAnswer() {
        switch (question.getType()) {
            case MULTIPLE_CHOICE:
            case TRUE_FALSE:
                int checkedId = radioGroup.getCheckedRadioButtonId();
                if (checkedId != -1) {
                    RadioButton selectedRadio = radioGroup.findViewById(checkedId);
                    answer.setUserAnswer(selectedRadio.getText().toString());
                }
                break;
            case FILL_IN_BLANK:
            case SHORT_ANSWER:
            case ESSAY:
                answer.setUserAnswer(shortAnswerEdit.getText().toString());
                break;
            case MATCHING:
                List<String> selectedAnswers = new ArrayList<>();
                for (int i = 0; i < checkboxContainer.getChildCount(); i++) {
                    View child = checkboxContainer.getChildAt(i);
                    if (child instanceof CheckBox) {
                        CheckBox checkBox = (CheckBox) child;
                        if (checkBox.isChecked()) {
                            selectedAnswers.add(checkBox.getText().toString());
                        }
                    }
                }
                answer.setUserAnswers(selectedAnswers);
                break;
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();
        // Sauvegarder la réponse quand le fragment n'est plus visible
        saveCurrentAnswer();
    }
} 