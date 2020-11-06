package com.justice.studentexam;


import android.app.Application;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.gson.Gson;

import java.util.List;

public class ApplicationClass extends Application {
    public static final String COLLECTION_QUESTIONS = "questions";
    public static final String COLLECTION_ALL_QUESTIONS = "all questions";
    public static final String COLLECTION_RESULTS="results";
    public static final String COLLECTION_ALL_RESULTS="all results";


    public static DocumentSnapshot documentSnapshot;
    public static String code;
    public static Student student;
    public static int marks;
    public static List<QuestionModel> questionList;

}
