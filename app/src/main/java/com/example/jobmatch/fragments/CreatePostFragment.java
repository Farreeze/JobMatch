package com.example.jobmatch.fragments;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.jobmatch.R;
import com.example.jobmatch.activities.setUpProfile;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CreatePostFragment extends Fragment {

    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private FirebaseFirestore fStore;

    private View rootView;
    private TextInputEditText jobTitle, jobDescription, jobRate, noOfWorker, date1, date2;
    private Button createPost;
    private String latitudeStr;
    private String longitudeStr;

    private FusedLocationProviderClient fusedLocationClient;
    ProgressBar progressBar;

    RadioGroup radioGroup, radioGroup1;
    RadioButton radioButton, radioButton1;
    TextView selectJobType, selectDateRange, selectJobCategory;
    LinearLayout jobTypeLayout, dateRangeLayout, jobCategoryLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_create_post, container, false);

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        fStore = FirebaseFirestore.getInstance();

        jobTitle = rootView.findViewById(R.id.jobTitle_edTxt);
        jobDescription = rootView.findViewById(R.id.jobDescription_edTxt);
        jobRate = rootView.findViewById(R.id.jobRate_edTxt);
        createPost = rootView.findViewById(R.id.createPost_btn);
        progressBar = rootView.findViewById(R.id.createPostProgressBar);
        radioGroup = rootView.findViewById(R.id.jobCateg);
        radioGroup1 = rootView.findViewById(R.id.jobTypeRadioGroup);
        selectJobType = rootView.findViewById(R.id.selectJobType_click);
        selectDateRange = rootView.findViewById(R.id.selectDateRange_click);
        selectJobCategory = rootView.findViewById(R.id.selectJobCateg_click);
        jobTypeLayout = rootView.findViewById(R.id.jobType_dropdown);
        dateRangeLayout = rootView.findViewById(R.id.dateRange_dropdown);
        jobCategoryLayout = rootView.findViewById(R.id.jobCateg_dropdown);
        noOfWorker = rootView.findViewById(R.id.NoOfWorker_edTxt);
        date1 = rootView.findViewById(R.id.date1);
        date2 = rootView.findViewById(R.id.date2);

        selectJobType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (jobTypeLayout.getVisibility() == View.GONE){
                    jobTypeLayout.setVisibility(View.VISIBLE);
                } else {
                    jobTypeLayout.setVisibility(View.GONE);
                }
            }
        });

        selectDateRange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dateRangeLayout.getVisibility() == View.GONE){
                    dateRangeLayout.setVisibility(View.VISIBLE);
                } else {
                    dateRangeLayout.setVisibility(View.GONE);
                }
            }
        });

        selectJobCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (jobCategoryLayout.getVisibility() == View.GONE){
                    jobCategoryLayout.setVisibility(View.VISIBLE);
                } else {
                    jobCategoryLayout.setVisibility(View.GONE);
                }
            }
        });

        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);

        date1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        month = month+1;
                        String date = day+"/"+month+"/"+year;
                        date1.setText(date);
                    }
                },year,month,day);
                datePickerDialog.show();
            }
        });

        date2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        month = month+1;
                        String date = day+"/"+month+"/"+year;
                        date2.setText(date);
                    }
                },year,month,day);
                datePickerDialog.show();
            }
        });

        createPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(jobTitle.getText()) || TextUtils.isEmpty(jobRate.getText()) || TextUtils.isEmpty(jobDescription.getText())
                || radioGroup.getCheckedRadioButtonId() == -1 || radioGroup1.getCheckedRadioButtonId() == -1 || TextUtils.isEmpty(date1.getText())
                || TextUtils.isEmpty(date2.getText()) || TextUtils.isEmpty(noOfWorker.getText())) {
                    Toast.makeText(getActivity(), "All fields must be filled", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);
                createPost.setVisibility(View.GONE);

                // Request location permission using the launcher
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        });

        return rootView;
    }

    private void getLocationAndInsertData() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Permission granted, proceed with getting location
            fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        latitudeStr = String.valueOf(latitude);
                        longitudeStr = String.valueOf(longitude);

                        // Insert data into FireStore
                        insertDataIntoFireStore();

                        //create Geofence


                    } else {
                        Toast.makeText(getActivity(), "Please turn on location service", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        createPost.setVisibility(View.VISIBLE);
                    }
                }
            });
        } else {
            // Location permission not granted, request it using the launcher
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }    // Request location permission using ActivityResultContracts.RequestPermission
    ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    // Permission granted, proceed with getting location and inserting data
                    getLocationAndInsertData();
                } else {
                    // Permission denied
                    Toast.makeText(requireContext(), "Location permission denied. Cannot get location.", Toast.LENGTH_SHORT).show();
                }
            }
    );

    String JobPoster;

    private void insertDataIntoFireStore() {

        String jobTitleStr = Objects.requireNonNull(jobTitle.getText()).toString().trim();
        String jobDescriptionStr = Objects.requireNonNull(jobDescription.getText()).toString().trim();
        String jobRateStr = Objects.requireNonNull(jobRate.getText()).toString().trim();
        String numberOfWorker = Objects.requireNonNull(noOfWorker.getText()).toString().trim();
        String startDate = Objects.requireNonNull(date1.getText()).toString();
        String endDate = Objects.requireNonNull(date2.getText()).toString();

        // Get user ID
        String userID = currentUser.getUid();

        DocumentReference documentReference = fStore.collection("users").document(userID);

        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                // Retrieve the "fName" field from the document snapshot
                String firstName = documentSnapshot.getString("fName");
                String lastName = documentSnapshot.getString("lName");
                Double posterRep = documentSnapshot.getDouble("currentAverageRating");
                Double numberOfRating = documentSnapshot.getDouble("totalNumberRatings");
                JobPoster = firstName + " " + lastName;

                // Create a reference to the "postedJobs" subcollection for the user
                CollectionReference collectionReference = documentReference.collection("postedJobs");

                boolean isJobAccepted = false;

                int radioID = radioGroup.getCheckedRadioButtonId();
                int radioID1 = radioGroup1.getCheckedRadioButtonId();

                radioButton = rootView.findViewById(radioID);
                radioButton1 = rootView.findViewById(radioID1);

                String category = String.valueOf(radioButton.getText());
                String jobType = String.valueOf(radioButton1.getText());

                // Create a map to store the job data
                Map<String, Object> post = new HashMap<>();
                post.put("PosterName", JobPoster);
                post.put("currentAverageRating", posterRep);
                post.put("jobAccepted", isJobAccepted);
                post.put("jobTitle", jobTitleStr);
                post.put("jobDescription", jobDescriptionStr);
                post.put("jobRate", "Php " + jobRateStr);
                post.put("latitude", latitudeStr);
                post.put("longitude", longitudeStr);
                post.put("posterUID", currentUser.getUid());
                post.put("category", category);
                post.put("jobType", jobType);
                post.put("numberOfWorker", numberOfWorker);
                post.put("date1", startDate);
                post.put("date2", endDate);
                post.put("numberOfRatings", numberOfRating);
                post.put("timestamp", FieldValue.serverTimestamp());

                // Add the job data to the "postedJobs" subcollection
                collectionReference.add(post).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                // Get the ID of the newly added document
                                String newDocumentId = documentReference.getId();

                                // Update the "jobId" field of the newly added document
                                documentReference.update("jobId", newDocumentId)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                // Display a success message and clear the input fields
                                                Toast.makeText(getActivity(), "Job posted successfully", Toast.LENGTH_SHORT).show();
                                                progressBar.setVisibility(View.GONE);
                                                createPost.setVisibility(View.VISIBLE);
                                                clearFields();
                                                radioButton.setChecked(false);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Display an error message and clear the input fields
                                                Toast.makeText(getActivity(), "Failed to post job", Toast.LENGTH_SHORT).show();
                                                progressBar.setVisibility(View.GONE);
                                                createPost.setVisibility(View.VISIBLE);
                                                clearFields();
                                            }
                                        });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Display an error message and clear the input fields
                                Toast.makeText(getActivity(), "Failed to post job", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                                createPost.setVisibility(View.VISIBLE);
                                clearFields();
                            }
                        });
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.getSupportActionBar().setTitle("Create Post");
        }
    }

    private void clearFields() {
        jobTitle.setText("");
        jobDescription.setText("");
        jobRate.setText("");
        noOfWorker.setText("");
        date1.setText("");
        date2.setText("");
        radioGroup.clearCheck();
        radioGroup1.clearCheck();
        jobRate.onEditorAction(EditorInfo.IME_ACTION_DONE);
    }


}
