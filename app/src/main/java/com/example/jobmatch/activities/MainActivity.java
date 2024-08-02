package com.example.jobmatch.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.jobmatch.R;
import com.example.jobmatch.fragments.ApplicantFragment;
import com.example.jobmatch.fragments.AssignedJobs;
import com.example.jobmatch.fragments.ChatFragment;
import com.example.jobmatch.fragments.CreatePostFragment;
import com.example.jobmatch.fragments.HistoryFragment;
import com.example.jobmatch.fragments.HomeFragment;
import com.example.jobmatch.fragments.PostedJobsFragment;
import com.example.jobmatch.fragments.SentApplicationsFragment;
import com.example.jobmatch.fragments.SettingsFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;

    View header;
    TextView userName, userEmail, userRep, numRating;
    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseFirestore fStore;
    String userID;

    String deviceToken;

    public void updateToken() {

        DocumentReference documentReference = fStore.collection("users").document(userID);

        Map<String, Object> token = new HashMap<>();
        token.put("token", deviceToken);

        documentReference.set(token, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Failed to get notification token", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    public void setUpNotificationToken(){

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {

                if (task.isSuccessful()){

                    deviceToken = task.getResult();
                    updateToken();

                }else {

                    Toast.makeText(MainActivity.this, "Failed to get notification token", Toast.LENGTH_SHORT).show();

                }

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(MainActivity.this, "Enable notifications in settings", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(user == null){
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        } else {
            setProfile();
            setUpNotificationToken();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        //Firebase Ins
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        fStore = FirebaseFirestore.getInstance();
        userID = user.getUid();
        //Menu Drawer Ins
        Toolbar toolbar = findViewById(R.id.toolbar);
        NavigationView navigationView = findViewById(R.id.nav_view);
        drawerLayout = findViewById(R.id.drawer_layout);
        //Passing Username and Email
        header = navigationView.getHeaderView(0);
        userName = header.findViewById(R.id.user_name);
        userEmail = header.findViewById(R.id.user_email);
        userRep = header.findViewById(R.id.user_rep);
        numRating = header.findViewById(R.id.user_numRate);

        setSupportActionBar(toolbar);

        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }


    }

    private void setProfile() {
        DocumentReference documentReference = fStore.collection("users").document(userID);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value != null){
                    userName.setText(value.getString("fName")+" "+(value.getString("lName")));
                    userEmail.setText(value.getString("email"));
                    Double rating = value.getDouble("currentAverageRating");
                    int numberOfRating = Objects.requireNonNull(value.getLong("totalNumberRatings")).intValue();

                    String fNumberOfRating = "("+numberOfRating+")";

                    DecimalFormat decimalFormat = new DecimalFormat("#.##");
                    String formattedDouble = decimalFormat.format(rating);
                    double finalRate = Double.parseDouble(formattedDouble);

                    userRep.setText(String.valueOf(finalRate));
                    numRating.setText(fNumberOfRating);
                }
            }
        });

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_home){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
        } else if (item.getItemId() == R.id.nav_jobs) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AssignedJobs()).commit();
        } else if (item.getItemId() == R.id.nav_chat) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ChatFragment()).commit();
        } else if (item.getItemId() == R.id.nav_createPost){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new CreatePostFragment()).commit();
        } else if (item.getItemId() == R.id.nav_PostedJobs) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new PostedJobsFragment()).commit();
        } else if (item.getItemId() == R.id.nav_applicants) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ApplicantFragment()).commit();
        } else if (item.getItemId() == R.id.nav_sent_applications) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SentApplicationsFragment()).commit();
        } else if (item.getItemId() == R.id.nav_history) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HistoryFragment()).commit();
        } else if (item.getItemId() == R.id.nav_settings) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SettingsFragment()).commit();
        } else if (item.getItemId() == R.id.nav_logout) {

            DocumentReference documentReference = fStore.collection("users")
                            .document(userID);

            Map<String, Object> removeToken = new HashMap<>();
            removeToken.put("token", "noToken");

            documentReference.update(removeToken).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
                }
            });

            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {

        if (drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

    }

}