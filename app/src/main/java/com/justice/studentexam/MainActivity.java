//package com.justice.studentexam;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//
//import com.google.android.material.textfield.TextInputEditText;
//
//import es.dmoral.toasty.Toasty;
//
//public class MainActivity extends AppCompatActivity {
//    private static final String TAG = "MainActivity";
//    public static final String CODE = "code";
//    private TextInputEditText codeEdtTxt;
//    private Button enterBtn;
//
//    private SharedPreferences sharedPreferences;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        sharedPreferences = getSharedPreferences(CODE, MODE_PRIVATE);
//        initWidgets();
//        setOnClickListener();
//        setDefaultValues();
//    }
//
//    private void setDefaultValues() {
//        sharedPreferences = getSharedPreferences(CODE, MODE_PRIVATE);
//        if (sharedPreferences.getString(CODE, null) != null) {
//            Log.d(TAG, "setDefaultValues: default code is available");
//            setDefaultCodeToEdtTxt();
//        }else {
//            Log.d(TAG, "setDefaultValues: default code is not available");
//        }
//    }
//
//    private void setDefaultCodeToEdtTxt() {
//        String code = sharedPreferences.getString(CODE, null);
//        ApplicationClass.code=code;
//        codeEdtTxt.setText(code);
//    }
//
//    private void setOnClickListener() {
//        enterBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d(TAG, "onClick: enter btn clicked");
//                if (editTxtIsEmpty()) {
//                    Log.d(TAG, "onClick: edit text is empty");
//                    Toasty.error(MainActivity.this, "please fill the Textbox").show();
//                    return;
//                }
//                String code = codeEdtTxt.getEditableText().toString().trim();
//                ApplicationClass.code=code;
//                sharedPreferences.edit().putString(CODE, code).apply();
//                sendMeToTeachersFirstPageActivity();
//                Log.d(TAG, "onClick: code saved in shared preference");
//            }
//        });
//    }
//    private void sendMeToTeachersFirstPageActivity(){
//        Intent intent=new Intent(this,TeacherFirstPageActivity.class);
//        Log.d(TAG, "sendMeToTeachersFirstPageActivity: going to teachers first page activity");
//        startActivity(intent);
//    }
//    private boolean editTxtIsEmpty() {
//        if (codeEdtTxt.getEditableText().toString().trim().isEmpty()) {
//            return true;
//        }
//        return false;
//    }
//
//    private void initWidgets() {
//        codeEdtTxt = findViewById(R.id.codeEdtTxt);
//        enterBtn = findViewById(R.id.enterBtn);
//    }
//
//}