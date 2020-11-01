package com.justice.studentexam;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import es.dmoral.toasty.Toasty;

import static com.justice.studentexam.ApplicationClass.COLLECTION_ALL_QUESTIONS;
import static com.justice.studentexam.ApplicationClass.COLLECTION_QUESTIONS;

public class StudentFirstPageActivity extends AppCompatActivity {
    private static final String TAG = "StudentFirstPageActivit";
    public static final String COLLECTION_STUDENTS = "students";
    public static final int RC_SIGN_IN = 6;
    private TextInputLayout firstNameEdtTxt;
    private TextInputLayout lastNameEdtTxt;
    private TextInputLayout idEdtTxt;
    private TextInputLayout codeEdtTxt;
    public static final String CODE = "code";
    private Button submitBtn;
    private Button logoutBtn;

    private ProgressBar progressBar;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_first_page);
        sharedPreferences = getSharedPreferences(CODE, MODE_PRIVATE);
        initWidgets();
        setOnClickListeners();
        setDefaultValues();

        //      setUplogin();
    }

    private void setDefaultValues() {
        sharedPreferences = getSharedPreferences(CODE, MODE_PRIVATE);
        if (sharedPreferences.getString(CODE, null) != null) {
            Log.d(TAG, "setDefaultValues: default code is available");
            setDefaultCodeToEdtTxt();
        } else {
            Log.d(TAG, "setDefaultValues: default code is not available");
        }
    }

    private void setDefaultCodeToEdtTxt() {
        String code = sharedPreferences.getString(CODE, null);
        ApplicationClass.code = code;
        codeEdtTxt.getEditText().setText(code);
    }

//

    private void showToast(String message) {
        Toasty.error(this, message).show();
    }

    private void setOnClickListeners() {
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSubmitButtonClicked();
            }
        });
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                //onLogoutBtnClicked();
            }
        });

    }


    private void onSubmitButtonClicked() {


        Log.d(TAG, "onClick: enter btn clicked");
        if (editTxtIsEmpty()) {
            Log.d(TAG, "onClick: edit text is empty");
            Toasty.error(StudentFirstPageActivity.this, "please fill the code").show();
            return;
        }
        String code = codeEdtTxt.getEditText().getText().toString().trim();
        ApplicationClass.code = code;
        sharedPreferences.edit().putString(CODE, code).apply();

        Log.d(TAG, "onClick: code saved in shared preference");
        ///////////////
        String firstName = firstNameEdtTxt.getEditText().getText().toString().trim();
        String lastName = lastNameEdtTxt.getEditText().getText().toString().trim();
        String studentId = idEdtTxt.getEditText().getText().toString().trim().replace("/","");

        if (field_is_empty(firstName, lastName, studentId)) {
            Toasty.error(this, "please fill all fields").show();
            Log.d(TAG, "onSubmitButtonClicked: fields are empty");
            return;
        }

        ApplicationClass.student = new Student(firstName, lastName,studentId);
        ApplicationClass.code = codeEdtTxt.getEditText().getText().toString().trim();

        Log.d(TAG, "onSubmitButtonClicked: getting all questions");
        get_all_the_questions_and_start_the_test();


    }

    private boolean editTxtIsEmpty() {
        if (codeEdtTxt.getEditText().getText().toString().trim().isEmpty()) {
            return true;
        }
        return false;
    }


    private void get_all_the_questions_and_start_the_test() {
        Log.d(TAG, "get_all_the_questions_and_start_the_test: getting all questions");
        ApplicationClass.questionList = new ArrayList<>();
        progressBar.setVisibility(View.VISIBLE);
        submitBtn.setVisibility(View.GONE);
        FirebaseFirestore.getInstance().collection(COLLECTION_ALL_QUESTIONS).document(ApplicationClass.code).collection(COLLECTION_QUESTIONS).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "onComplete: success fetching all questions");
                    ApplicationClass.questionList.addAll(task.getResult().toObjects(QuestionModel.class));
                    Log.d(TAG, "onComplete: starting nest activity");
                    startActivity(new Intent(StudentFirstPageActivity.this, TestActivity.class));

                } else {
                    Log.d(TAG, "onComplete: Error"+task.getException());
                    Toast.makeText(StudentFirstPageActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);
                submitBtn.setVisibility(View.VISIBLE);

            }
        });

    }

    private boolean field_is_empty(String firstName, String lastName, String id) {
        if (firstName.isEmpty()) {
            firstNameEdtTxt.setError("Please Fill Field");
            firstNameEdtTxt.requestFocus();
            return true;
        }
        if (lastName.isEmpty()) {
            lastNameEdtTxt.setError("Please Fill Field");
            lastNameEdtTxt.requestFocus();
            return true;
        }
        if (id.isEmpty()) {
            idEdtTxt.setError("Please Fill Field");
            idEdtTxt.requestFocus();
            return true;
        }
        return false;
    }

    private void initWidgets() {
        firstNameEdtTxt = findViewById(R.id.firstNameEdtTxt);
        lastNameEdtTxt = findViewById(R.id.lastNameEdtTxt);
        idEdtTxt = findViewById(R.id.idEdtTxt);
        codeEdtTxt = findViewById(R.id.codeEdtTxt);


        submitBtn = findViewById(R.id.submitBtn);
        logoutBtn = findViewById(R.id.logoutBtn);
        progressBar = findViewById(R.id.progressBar);

    }
}
