package com.example.jobmatch.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jobmatch.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class userNotice extends AppCompatActivity {

    Button logoutBtn, continueBtn, resendEmail, checkVerification;
    TextView verIndicator;
    FirebaseAuth auth;
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_user_notice);

        logoutBtn = findViewById(R.id.logout_btn);
        continueBtn = findViewById(R.id.continue_btn);
        resendEmail = findViewById(R.id.resendEmailBtn);
        checkVerification = findViewById(R.id.checkVerBtn);
        verIndicator = findViewById(R.id.verIndicatorTxt);
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        checkVerification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentUser.reload();
                if (currentUser.isEmailVerified()){
                    continueBtn.setEnabled(true);
                    verIndicator.setText("Email is verified");
                    verIndicator.setTextColor(Color.parseColor("#02fa07"));
                }else {
                    Toast.makeText(userNotice.this, "Email is not Verified", Toast.LENGTH_SHORT).show();
                }
            }
        });

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });

        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), setUpProfile.class);
                startActivity(intent);
                finish();
            }
        });

        resendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(userNotice.this, "Verification Code Sent to "+currentUser.getEmail(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}