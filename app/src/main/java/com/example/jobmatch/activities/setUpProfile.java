package com.example.jobmatch.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.jobmatch.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class setUpProfile extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseUser currentUser;
    FirebaseFirestore fStore;
    StorageReference storageReference;
    Button finishBtn, uploadIDBtn;
    TextInputEditText fNameInput, lNameInput, mNameInput, contactInput, addressInput, birthdayInput;
    String userID, storageLoc;
    ImageView idImg, backBtn;
    Uri imageUri;
    ProgressBar progressBar;

    DatePickerDialog.OnDateSetListener setListener;

    RadioGroup radioGroup;

    RadioButton radioButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_set_up_profile);

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        fStore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        uploadIDBtn = findViewById(R.id.uploadID_btn);
        finishBtn = findViewById(R.id.finish_btn);

        idImg = findViewById(R.id.id_img);
        backBtn = findViewById(R.id.backBtn_SP);

        fNameInput = findViewById(R.id.fName_input);
        lNameInput = findViewById(R.id.lName_input);
        mNameInput = findViewById(R.id.mName_input);
        contactInput = findViewById(R.id.phoneNumber_input);
        addressInput = findViewById(R.id.address_input);
        birthdayInput = findViewById(R.id.Birthday_input);
        radioGroup = findViewById(R.id.gender_radioGroup);

        progressBar = findViewById(R.id.setUpProfile_progressbar);

        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);

        birthdayInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        setUpProfile.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        month = month+1;
                        String date = day+"/"+month+"/"+year;
                        birthdayInput.setText(date);
                    }
                },year,month,day);
                datePickerDialog.show();
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), userNotice.class);
                startActivity(intent);
                finish();
            }
        });

        finishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                progressBar.setVisibility(View.VISIBLE);
                finishBtn.setVisibility(View.GONE);

                String fName, lName, mName, contact, address, birthday, gender;
                int currentAverage = 0, totalNumRatings = 0;
                userID = currentUser.getUid();
                fName = String.valueOf(fNameInput.getText());
                lName = String.valueOf(lNameInput.getText());
                mName = String.valueOf(mNameInput.getText());
                birthday = String.valueOf(birthdayInput.getText());
                contact = String.valueOf(contactInput.getText());
                address = String.valueOf(addressInput.getText());

                if (TextUtils.isEmpty(fName)) {
                    Toast.makeText(setUpProfile.this, "First name cannot be empty", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    finishBtn.setVisibility(View.VISIBLE);
                    return;
                }
                if (TextUtils.isEmpty(lName)) {
                    Toast.makeText(setUpProfile.this, "Last name cannot be empty", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    finishBtn.setVisibility(View.VISIBLE);
                    return;
                }
                if (TextUtils.isEmpty(mName)) {
                    Toast.makeText(setUpProfile.this, "Middle name cannot be empty", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    finishBtn.setVisibility(View.VISIBLE);
                    return;
                }
                if (TextUtils.isEmpty(birthday)) {
                    Toast.makeText(setUpProfile.this, "Birthday cannot be empty", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    finishBtn.setVisibility(View.VISIBLE);
                    return;
                }
                if (TextUtils.isEmpty(address)) {
                    Toast.makeText(setUpProfile.this, "Address cannot be empty", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    finishBtn.setVisibility(View.VISIBLE);
                    return;
                }
                if (TextUtils.isEmpty(contact)) {
                    Toast.makeText(setUpProfile.this, "Contact cannot be empty", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    finishBtn.setVisibility(View.VISIBLE);
                    return;
                }
                if (radioGroup.getCheckedRadioButtonId() == -1){
                    Toast.makeText(setUpProfile.this, "No gender selected", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    finishBtn.setVisibility(View.VISIBLE);
                    return;
                }

                if (!idImg.isShown()) {
                    Toast.makeText(setUpProfile.this, "Upload ID first", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    finishBtn.setVisibility(View.VISIBLE);
                    return;
                }

                int age = calculateAgeFromDateString(birthday);

                if (age < 18) {
                    Toast.makeText(setUpProfile.this, "Age must be 18 above", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    finishBtn.setVisibility(View.VISIBLE);
                    return;
                }

                int radioID = radioGroup.getCheckedRadioButtonId();

                radioButton = findViewById(radioID);

                gender = String.valueOf(radioButton.getText());

                String cfName = fName.substring(0, 1).toUpperCase() + fName.substring(1);
                String clName = lName.substring(0, 1).toUpperCase() + lName.substring(1);
                String cmName = mName.substring(0, 1).toUpperCase() + mName.substring(1);

                DocumentReference documentReference = fStore.collection("users").document(userID);

                Map<String, Object> user = new HashMap<>();
                user.put("currentAverageRating", currentAverage);
                user.put("totalNumberRatings", totalNumRatings);
                user.put("fName", cfName.trim());
                user.put("lName", clName.trim());
                user.put("mName", cmName.trim());
                user.put("gender", gender);
                user.put("birthday", birthday);
                user.put("contact", contact.trim());
                user.put("address", address.trim());
                user.put("email", currentUser.getEmail());
                uploadIdToFirebase(imageUri);
                user.put("ID Location", storageLoc);

                documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("TAG", "onSuccess: user profile is created for " + userID);
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        finish();
                        Toast.makeText(setUpProfile.this, "Profile set up finished", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(setUpProfile.this, "Network Error", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        finishBtn.setVisibility(View.VISIBLE);
                    }
                });

            }

        });

        uploadIDBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void onClick(View view) {
                //open gallery
                Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(openGalleryIntent, 1000);
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000) {
            if (resultCode == Activity.RESULT_OK) {
                assert data != null;
                imageUri = data.getData();
                idImg.setImageURI(imageUri);
                idImg.setVisibility(View.VISIBLE);

            }
        }
    }

    private void uploadIdToFirebase(Uri imageUri) {
        StorageReference fileRef = storageReference.child("userIDs/" + currentUser.getUid() + "/ID.jpg");
        storageLoc = ("userIDs/" + currentUser.getUid() + "/ID.jpg");
        fileRef.putFile(imageUri);
    }

    public static int calculateAgeFromDateString(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy", Locale.US); // Use the desired locale

        try {
            Date birthDate = sdf.parse(dateString);
            Date currentDate = new Date();

            assert birthDate != null;
            long ageInMillis = currentDate.getTime() - birthDate.getTime();
            long ageInYears = ageInMillis / (1000L * 60 * 60 * 24 * 365);

            return (int) ageInYears;
        } catch (ParseException e) {
            // Handle parsing exception
            e.printStackTrace();
            return -1; // Return an error value if parsing fails
        }
    }

}