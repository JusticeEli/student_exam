package com.justice.studentexam;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import es.dmoral.toasty.Toasty;

import static com.justice.studentexam.ApplicationClass.COLLECTION_ALL_QUESTIONS;
import static com.justice.studentexam.ApplicationClass.COLLECTION_ALL_RESULTS;
import static com.justice.studentexam.ApplicationClass.COLLECTION_QUESTIONS;
import static com.justice.studentexam.ApplicationClass.COLLECTION_RESULTS;

public class TestActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "TestActivity";
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

    private String currentUserId;

    private String quizName;
    private String quizId;

    //UI Elements
    private TextView quizTitle;
    private Button optionOneBtn;
    private Button optionTwoBtn;
    private Button optionThreeBtn;
    private Button nextBtn;
    private ImageButton closeBtn;
    private TextView questionFeedback;
    private TextView questionText;
    private TextView questionTime;
    private ProgressBar questionProgress;
    private ProgressBar progressBarLoading;

    private TextView questionNumber;

    private List<QuestionModel> questionsToAnswer = new ArrayList<>();
    private int totalQuestionsToAnswer;
    private CountDownTimer countDownTimer;

    private boolean canAnswer = false;
    private int currentQuestion = 0;

    private int correctAnswers = 0;
    private int wrongAnswers = 0;
    private int notAnswered = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        initWidgets();
        setOnClickListeners();
        queryFirestoreData();

    }

    private void queryFirestoreData() {
        //Query Firestore Data
        firebaseFirestore = FirebaseFirestore.getInstance();


        firebaseFirestore.collection(COLLECTION_ALL_QUESTIONS).document(ApplicationClass.code).collection(COLLECTION_QUESTIONS).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    questionsToAnswer = task.getResult().toObjects(QuestionModel.class);
                    totalQuestionsToAnswer = questionsToAnswer.size();
                    pickQuestions();
                    loadUI();
                } else {
                    quizTitle.setText("Error : " + task.getException().getMessage());
                }
            }
        });

    }

    private void loadUI() {
        //Quiz Data Loaded, Load the UI
        quizTitle.setText(quizName);
        questionText.setText("Load First Question");

        //Enable Options
        enableOptions();
        if (questionsToAnswer.isEmpty()) {
            Log.d(TAG, "loadUI: the are no questions");
            Toasty.error(this, "No questions Available").show();
            return;
        }
        //Load First Question
        loadQuestion(1);
    }

    private void loadQuestion(int questNum) {


        //Set Question Number
        questionNumber.setText(questNum + "");

        //Load Question Text
        questionText.setText(questionsToAnswer.get(questNum - 1).getQuestion());

        //Load Options
        optionOneBtn.setText(questionsToAnswer.get(questNum - 1).getOption_a());
        optionTwoBtn.setText(questionsToAnswer.get(questNum - 1).getOption_b());
        optionThreeBtn.setText(questionsToAnswer.get(questNum - 1).getOption_c());

        //Question Loaded, Set Can Answer
        canAnswer = true;
        currentQuestion = questNum;

        //Start Question Timer
        startTimer(questNum);
    }

    private void startTimer(int questionNumber) {

        //Set Timer Text
        final Long timeToAnswer = questionsToAnswer.get(questionNumber - 1).getTimer();
        questionTime.setText(timeToAnswer.toString());

        //Show Timer ProgressBar
        questionProgress.setVisibility(View.VISIBLE);
        if (countDownTimer != null) {
            countDownTimer.cancel();

        }
        //Start CountDown
        countDownTimer = new CountDownTimer(timeToAnswer * 1000, 10) {
            @Override
            public void onTick(long millisUntilFinished) {
                //Update Time
                questionTime.setText(millisUntilFinished / 1000 + "");

                //Progress in percent
                Long percent = millisUntilFinished / (timeToAnswer * 10);
                questionProgress.setProgress(percent.intValue());
                Log.d(TAG, "onTick: " + millisUntilFinished / 1000);
            }

            @Override
            public void onFinish() {
                //Time Up, Cannot Answer Question Anymore
                canAnswer = false;

                questionFeedback.setText("Time Up! No answer was submitted.");
                questionFeedback.setTextColor(ContextCompat.getColor(TestActivity.this, R.color.colorPrimary));
                notAnswered++;
                showNextBtn();
            }
        };

        countDownTimer.start();
    }

    private void enableOptions() {
        //Show All Option Buttons
        optionOneBtn.setVisibility(View.VISIBLE);
        optionTwoBtn.setVisibility(View.VISIBLE);
        optionThreeBtn.setVisibility(View.VISIBLE);

        //Enable Option Buttons
        optionOneBtn.setEnabled(true);
        optionTwoBtn.setEnabled(true);
        optionThreeBtn.setEnabled(true);

        //Hide Feedback and next Button
        questionFeedback.setVisibility(View.INVISIBLE);
        nextBtn.setVisibility(View.INVISIBLE);
        nextBtn.setEnabled(false);
    }

    private void pickQuestions() {

        Collections.shuffle(questionsToAnswer);
    }

    private int getRandomInt(int min, int max) {
        return ((int) (Math.random() * (max - min))) + min;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.quiz_close_btn:
                closeBtnClicked();
                break;

            case R.id.quiz_option_one:
                verifyAnswer(optionOneBtn);
                break;
            case R.id.quiz_option_two:
                verifyAnswer(optionTwoBtn);
                break;
            case R.id.quiz_option_three:
                verifyAnswer(optionThreeBtn);
                break;
            case R.id.quiz_next_btn:
                if (currentQuestion == totalQuestionsToAnswer) {
                    //Load Results
                    submitResults();
                } else {
                    currentQuestion++;
                    loadQuestion(currentQuestion);
                    resetOptions();
                }
                break;
        }
    }

    private void closeBtnClicked() {
        startActivity(new Intent(this, StudentFirstPageActivity.class));
        finish();
    }

    private void submitResults() {

        Results results = new Results();
        results.setCorrect((long) correctAnswers);
        results.setWrong((long) wrongAnswers);
        results.setUnanswered((long) notAnswered);

        ApplicationClass.student.setResults(results);
        progressBarLoading.setVisibility(View.VISIBLE);
        nextBtn.setVisibility(View.GONE);
        Log.d(TAG, "submitResults: submitting  results");
        firebaseFirestore.collection(COLLECTION_ALL_RESULTS)
                .document(ApplicationClass.code)
                .collection(COLLECTION_RESULTS)
                .document(ApplicationClass.student.getStudentId()).set(ApplicationClass.student).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "onComplete: submittion success");
                    //Go To Results Page
                    startActivity(new Intent(TestActivity.this, TestCompleteActivity.class));
                } else {
                    //Show Error
                    Log.d(TAG, "onComplete: Error: "+task.getException());
                    quizTitle.setText(task.getException().getMessage());
                    Toasty.error(TestActivity.this, task.getException().getMessage()).show();
                }
                progressBarLoading.setVisibility(View.GONE);
                nextBtn.setVisibility(View.VISIBLE);

            }
        });


    }

    private void resetOptions() {
        optionOneBtn.setBackground(getResources().getDrawable(R.drawable.outline_light_btn_bg, null));
        optionTwoBtn.setBackground(getResources().getDrawable(R.drawable.outline_light_btn_bg, null));
        optionThreeBtn.setBackground(getResources().getDrawable(R.drawable.outline_light_btn_bg, null));

        optionOneBtn.setTextColor(ContextCompat.getColor(this, R.color.colorLightText));
        optionTwoBtn.setTextColor(ContextCompat.getColor(this, R.color.colorLightText));
        optionThreeBtn.setTextColor(ContextCompat.getColor(this, R.color.colorLightText));

        questionFeedback.setVisibility(View.INVISIBLE);
        nextBtn.setVisibility(View.INVISIBLE);
        nextBtn.setEnabled(false);
    }

    private void verifyAnswer(Button selectedAnswerBtn) {
        //Check Answer
        if (canAnswer) {
            //Set Answer Btn Text Color to Black
            selectedAnswerBtn.setTextColor(ContextCompat.getColor(this, R.color.colorDark));

            if (questionsToAnswer.get(currentQuestion - 1).getAnswer().equals(selectedAnswerBtn.getText())) {
                //Correct Answer
                correctAnswers++;
                selectedAnswerBtn.setBackground(getResources().getDrawable(R.drawable.correct_answer_btn_bg, null));

                //Set Feedback Text
                questionFeedback.setText("Correct Answer");
                questionFeedback.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
            } else {
                //Wrong Answer
                wrongAnswers++;
                selectedAnswerBtn.setBackground(getResources().getDrawable(R.drawable.wrong_answer_btn_bg, null));

                //Set Feedback Text
                questionFeedback.setText("Wrong Answer \n \n Correct Answer : " + questionsToAnswer.get(currentQuestion - 1).getAnswer());
                questionFeedback.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
            }
            //Set Can answer to false
            canAnswer = false;

            //Stop The Timer
            countDownTimer.cancel();

            //Show Next Button
            showNextBtn();
        }
    }

    private void showNextBtn() {
        if (currentQuestion == totalQuestionsToAnswer) {
            nextBtn.setText("Submit Results");
        }
        questionFeedback.setVisibility(View.VISIBLE);
        nextBtn.setVisibility(View.VISIBLE);
        nextBtn.setEnabled(true);
    }


    private void setOnClickListeners() {
        //Set Button Click Listeners
        closeBtn.setOnClickListener(this);
        optionOneBtn.setOnClickListener(this);
        optionTwoBtn.setOnClickListener(this);
        optionThreeBtn.setOnClickListener(this);
        nextBtn.setOnClickListener(this);
    }


    private void initWidgets() {
        //UI Initialize
        closeBtn = findViewById(R.id.quiz_close_btn);
        quizTitle = findViewById(R.id.quiz_title);
        optionOneBtn = findViewById(R.id.quiz_option_one);
        optionTwoBtn = findViewById(R.id.quiz_option_two);
        optionThreeBtn = findViewById(R.id.quiz_option_three);
        nextBtn = findViewById(R.id.quiz_next_btn);
        questionFeedback = findViewById(R.id.quiz_question_feedback);
        questionText = findViewById(R.id.quiz_question);
        questionTime = findViewById(R.id.quiz_question_time);
        questionProgress = findViewById(R.id.quiz_question_progress);
        progressBarLoading = findViewById(R.id.progressBarLoading);
        questionNumber = findViewById(R.id.quiz_question_number);


        //Query Firestore Data
        Log.d(TAG, "initWidgets: loading questions");
        firebaseFirestore.collection(COLLECTION_ALL_QUESTIONS).document(ApplicationClass.code).collection(COLLECTION_QUESTIONS).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    questionsToAnswer = task.getResult().toObjects(QuestionModel.class);
                    Log.d(TAG, "onComplete: number of questions"+questionsToAnswer.size());
                    pickQuestions();
                    loadUI();
                } else {
                    quizTitle.setText("Error : " + task.getException().getMessage());
                }
            }
        });

        //Set Button Click Listeners
        optionOneBtn.setOnClickListener(this);
        optionTwoBtn.setOnClickListener(this);
        optionThreeBtn.setOnClickListener(this);

        nextBtn.setOnClickListener(this);
    }
}
