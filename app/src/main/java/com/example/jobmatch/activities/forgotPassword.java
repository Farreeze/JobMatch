package com.example.jobmatch.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.jobmatch.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class forgotPassword extends AppCompatActivity {

    ImageView BackBtn;
    TextInputEditText emailInput;
    Button SubmitBtn;
    FirebaseAuth mAuth;
    String email;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_forgot_password);

        BackBtn = findViewById(R.id.backBtn_FP);
        emailInput = findViewById(R.id.email_input);
        SubmitBtn = findViewById(R.id.submitBtn);
        progressBar = findViewById(R.id.ProgressBar);
        mAuth = FirebaseAuth.getInstance();

        BackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });

        SubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = String.valueOf(emailInput.getText());
                if (TextUtils.isEmpty(email)){
                    Toast.makeText(forgotPassword.this, "Email cannot be empty", Toast.LENGTH_SHORT).show();
                } else {
                    SubmitBtn.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                    sendEmail();
                }
            }
        });

    }

    private void sendEmail() {
        mAuth.sendPasswordResetEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                progressBar.setVisibility(View.GONE);
                SubmitBtn.setVisibility(View.VISIBLE);
                Toast.makeText(forgotPassword.this, "Email is sent to "+ email, Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBar.setVisibility(View.GONE);
                SubmitBtn.setVisibility(View.VISIBLE);
                Toast.makeText(forgotPassword.this, "No match found, please try again", Toast.LENGTH_SHORT).show();
            }
        });
    }

}