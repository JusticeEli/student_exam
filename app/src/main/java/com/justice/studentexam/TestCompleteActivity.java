package com.justice.studentexam;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import static com.justice.studentexam.ApplicationClass.COLLECTION_ALL_RESULTS;
import static com.justice.studentexam.ApplicationClass.COLLECTION_RESULTS;

public class TestCompleteActivity extends AppCompatActivity {
    private static final String TAG = "TestCompleteActivity";
    private FirebaseFirestore firebaseFirestore=FirebaseFirestore.getInstance();

    private ProgressBar progressBar;
    private TextView resultCorrect;
    private TextView resultWrong;
    private TextView resultMissed;

    private TextView resultPercent;
    private TextView nameTxtView;
    private TextView studentIdTxtView;


    private ProgressBar resultProgress;

    private Button resultHomeBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_complete);
        initWidgets();
        setOnClickListeners();
        getResults();
    }

    private void getResults() { //Get Results
        Log.d(TAG, "getResults: started loading results...");

        firebaseFirestore
                .collection(COLLECTION_ALL_RESULTS)
                .document(ApplicationClass.code)
                .collection(COLLECTION_RESULTS)
                .document(ApplicationClass.student.getStudentId()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult().exists()) {
                    progressBar.setVisibility(View.GONE);
                    Log.d(TAG, "onComplete: finished loading results data");
                    Student student = task.getResult().toObject(Student.class);
                    Results results = student.getResults();
                    Long correct = results.getCorrect();
                    Long wrong = results.getWrong();
                    Long missed = results.getUnanswered();

                    resultCorrect.setText(correct.toString());
                    resultWrong.setText(wrong.toString());
                    resultMissed.setText(missed.toString());

                    //Calculate Progress
                    Long total = correct + wrong + missed;
                    Long percent = (correct * 100) / total;

                    resultPercent.setText(percent + "%");
                    resultProgress.setProgress(percent.intValue());


                    ///setting name and student id
                    nameTxtView.setText(ApplicationClass.student.getFirstName() + " " + ApplicationClass.student.getLastName());
                    studentIdTxtView.setText(ApplicationClass.student.getStudentId());
                }
            }
        });
    }

    private void setOnClickListeners() {
        resultHomeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(TestCompleteActivity.this, StudentFirstPageActivity.class));
            }
        });

    }

    private void initWidgets() {  //Initialize UI Elements
        progressBar = findViewById(R.id.progressBar);
        resultCorrect = findViewById(R.id.results_correct_text);
        resultWrong = findViewById(R.id.results_wrong_text);
        resultMissed = findViewById(R.id.results_missed_text);

        nameTxtView = findViewById(R.id.nameTxtView);
        studentIdTxtView = findViewById(R.id.studentIdTxtView);

        resultHomeBtn = findViewById(R.id.results_home_btn);
        resultPercent = findViewById(R.id.results_percent);
        resultProgress = findViewById(R.id.results_progress);

    }


}



