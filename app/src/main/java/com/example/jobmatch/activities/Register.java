package com.example.jobmatch.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jobmatch.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Register extends AppCompatActivity {

    TextInputEditText emailEdtTxt, passwordEdtTxt, confirmEmail, confirmPassword;
    Button regBtn;
    TextView goToLoginTxt, goToTermsAndConditions;
    FirebaseAuth mAuth;
    FirebaseFirestore fStore;
    FirebaseUser currentUser;
    ProgressBar progressBar;
    String collection = "users";
    String userID;
    CheckBox termsCB;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            checkUserProfile();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_register);

        emailEdtTxt = findViewById(R.id.email_input);
        passwordEdtTxt = findViewById(R.id.password_input);
        confirmEmail = findViewById(R.id.confirmEmail_input);
        confirmPassword = findViewById(R.id.confirmPassword_input);
        regBtn = findViewById(R.id.register_btn);
        mAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressBar);
        goToLoginTxt = findViewById(R.id.loginNow_txt);
        fStore = FirebaseFirestore.getInstance();
        goToTermsAndConditions = findViewById(R.id.termsAndConditionsLink);
        termsCB = findViewById(R.id.termsCB);

        termsCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                regBtn.setEnabled(isChecked);

            }
        });

        goToTermsAndConditions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), termsAndConditions.class);
                startActivity(intent);
                finish();
            }
        });

        goToLoginTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });

        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                regBtn.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                String email, password, cEmail, cPassword;

                email = String.valueOf(emailEdtTxt.getText());
                password = String.valueOf(passwordEdtTxt.getText());
                cEmail = String.valueOf(confirmEmail.getText());
                cPassword = String.valueOf(confirmPassword.getText());

                if (TextUtils.isEmpty(email)){
                    progressBar.setVisibility(View.GONE);
                    regBtn.setVisibility(View.VISIBLE);
                    Toast.makeText(Register.this, "Email cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)){
                    progressBar.setVisibility(View.GONE);
                    regBtn.setVisibility(View.VISIBLE);
                    Toast.makeText(Register.this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!String.valueOf(cEmail).equals(String.valueOf(email))){
                    progressBar.setVisibility(View.GONE);
                    regBtn.setVisibility(View.VISIBLE);
                    Toast.makeText(Register.this, "Email does not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!String.valueOf(cPassword).equals(String.valueOf(password))){
                    progressBar.setVisibility(View.GONE);
                    regBtn.setVisibility(View.VISIBLE);
                    Toast.makeText(Register.this, "Password does not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    currentUser = mAuth.getCurrentUser();
                                    assert currentUser != null;
                                    currentUser.sendEmailVerification();
                                    Intent intent = new Intent(getApplicationContext(), userNotice.class);
                                    startActivity(intent);
                                    finish();
                                    Toast.makeText(Register.this, "Account Created. Check Email",
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    regBtn.setVisibility(View.VISIBLE);
                                    Toast.makeText(Register.this, "Failed to create account. Check inputs",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }

    void checkUserProfile(){ // checks if user has set up profile or not
        currentUser = mAuth.getCurrentUser();
        assert currentUser != null;
        userID = currentUser.getUid();
        DocumentReference documentReference = fStore.collection(collection).document(userID);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        progressBar.setVisibility(View.GONE);
                        startActivity(intent);
                        finish();
                    } else {
                        Intent intent = new Intent(getApplicationContext(), userNotice.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }
        });
    }

}