package com.example.jobmatch.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobmatch.R;
import com.example.jobmatch.nearbyJobsModelAdapter;
import com.example.jobmatch.nearbyJobsModelHandler;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class HomeFragment extends Fragment {

    double dist;

    TextView jobsIndicator, RadiusKm, negBtn, posBtn;

    ProgressBar progressBar;

    RecyclerView recyclerView;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 123;

    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private LocationCallback locationCallback;
    private ActivityResultLauncher<String> locationPermissionLauncher;
    private Context fragmentContext; // Store the context when the fragment is attached
    SearchView searchView;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        fragmentContext = context; // Store the context
    }

    @Override
    public void onDetach() {
        super.onDetach();
        fragmentContext = null; // Clear the stored context
    }

    double gRadius = 5000;

    boolean locationUpdatesStarted = false;

    double latitude, longitude;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);


        progressBar = rootView.findViewById(R.id.homeProgessbar);

        negBtn = rootView.findViewById(R.id.negativeBtn);
        posBtn = rootView.findViewById(R.id.positiveBtn);
        RadiusKm = rootView.findViewById(R.id.radiusKM);
        searchView = rootView.findViewById(R.id.searchView);

        searchView.clearFocus();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterJobs(latitude, longitude, newText);
                return true;
            }
        });

        negBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchView.setQuery("", false);
                searchView.clearFocus();
                if (gRadius == 2000) {
                    Toast.makeText(getContext(), "minimum scanning radius is 2 km", Toast.LENGTH_SHORT).show();
                } else {
                    gRadius = gRadius - 1000;
                    int radiusKM = Integer.parseInt((String) RadiusKm.getText());
                    radiusKM = radiusKM - 1;
                    RadiusKm.setText(String.valueOf(radiusKM));

                    recyclerView.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                    jobsIndicator.setVisibility(View.GONE);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            startLocationUpdates();

                            recyclerView.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);

                        }
                    }, 3000);

                }
            }
        });

        posBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchView.setQuery("", false);
                searchView.clearFocus();
                if (gRadius == 15000) {
                    Toast.makeText(getContext(), "maximum scanning radius is 15 km", Toast.LENGTH_SHORT).show();
                } else {
                    gRadius = gRadius + 1000;
                    int radiusKM = Integer.parseInt((String) RadiusKm.getText());
                    radiusKM = radiusKM + 1;
                    RadiusKm.setText(String.valueOf(radiusKM));

                    recyclerView.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                    jobsIndicator.setVisibility(View.GONE);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            startLocationUpdates();

                            recyclerView.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                        }
                    }, 3000);

                }
            }
        });


        jobsIndicator = rootView.findViewById(R.id.NoJobsLabel);

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        recyclerView = rootView.findViewById(R.id.nearbyJobRv);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                Location currentLocation = locationResult.getLastLocation();
                assert currentLocation != null;
                detectLocationsWithinRadius(currentLocation.getLatitude(), currentLocation.getLongitude());
                latitude = currentLocation.getLatitude();
                longitude = currentLocation.getLongitude();
                fusedLocationClient.removeLocationUpdates(locationCallback);
                locationUpdatesStarted = false;
            }
        };

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
        firestore = FirebaseFirestore.getInstance();

        // Initialize the locationPermissionLauncher
        locationPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                startLocationUpdates();
            } else {
                // Handle permission not granted
                Toast.makeText(getContext(), "Location permission denied, please allow", Toast.LENGTH_SHORT).show();
            }
        });

        // Check and request location permission
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request location permission using the locationPermissionLauncher
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            startLocationUpdates();
        }

        return rootView;
    }

    private void startLocationUpdates() {

        if (fragmentContext == null) {
            return; // Fragment is not attached, do not proceed
        }

        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 0).build();

        // Use the class-level locationCallback here
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                locationUpdatesStarted = true;
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        } else {
            // Handle permission not granted
            Toast.makeText(getContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopLocationUpdates() {
        // Stop location updates by passing the class-level locationCallback
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void detectLocationsWithinRadius(double latitude, double longitude) {
        double radius = gRadius; // 5 km in meters

        CollectionReference usersCollection = firestore.collection("users");
        List<nearbyJobsModelHandler> nearbyJobList = new ArrayList<>(); // Create a list to hold all jobs

        if (fragmentContext != null) {
            AtomicInteger queriesCompleted = new AtomicInteger(0);
            AtomicInteger jobsFoundCounter = new AtomicInteger(0);

            usersCollection.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    int totalQueries = task.getResult().size();

                    for (QueryDocumentSnapshot userDocument : task.getResult()) {
                        String userId = userDocument.getId();

                        CollectionReference postedJobsCollection = usersCollection
                                .document(userId)
                                .collection("postedJobs");

                        postedJobsCollection.get().addOnCompleteListener(jobsTask -> {
                            if (jobsTask.isSuccessful()) {
                                queriesCompleted.incrementAndGet();

                                for (QueryDocumentSnapshot jobDocument : jobsTask.getResult()) {
                                    String latitudeStr = jobDocument.getString("latitude");
                                    String longitudeStr = jobDocument.getString("longitude");
                                    Boolean isAccepted = jobDocument.getBoolean("jobAccepted");

                                    if (latitudeStr != null && longitudeStr != null && isAccepted != null) {
                                        double jobLatitude = Double.parseDouble(latitudeStr);
                                        double jobLongitude = Double.parseDouble(longitudeStr);

                                        double distance = calculateDistance(latitude, longitude, jobLatitude, jobLongitude);
                                        if (distance <= radius && !isAccepted) {
                                            distance = distance / 1000;
                                            dist = Math.round(distance * 100.0) / 100.0;
                                            jobsFoundCounter.incrementAndGet();

                                            // Add the job to the list for display later
                                            String posterName = jobDocument.getString("PosterName");
                                            String JobTitle = jobDocument.getString("jobTitle");
                                            String JobRate = jobDocument.getString("jobRate");
                                            String JobDescription = jobDocument.getString("jobDescription");
                                            String JobDistance = String.valueOf(dist);
                                            String fJobDistance = "Distance: " + JobDistance + " KM";
                                            String jobID = jobDocument.getString("jobId");
                                            String posterUserID = jobDocument.getString("posterUID");
                                            Double posterRep = jobDocument.getDouble("currentAverageRating");
                                            String jobCategory = jobDocument.getString("category");
                                            String date1 = jobDocument.getString("date1");
                                            String date2 = jobDocument.getString("date2");
                                            String jobType = jobDocument.getString("jobType");
                                            String numOfWorker = jobDocument.getString("numberOfWorker");
                                            Long numberOfRatings = jobDocument.getLong("numberOfRatings");
                                            String dateRange = date1 + " - " + date2;

                                            nearbyJobsModelHandler nearbyJobPost = new nearbyJobsModelHandler(JobTitle, JobDescription,
                                                    JobRate, fJobDistance, posterName, jobID, posterUserID, posterRep);
                                            nearbyJobPost.setJobCategory(jobCategory);
                                            nearbyJobPost.setDocumentId(userId);
                                            nearbyJobPost.setDateRange(dateRange);
                                            nearbyJobPost.setJobType(jobType);
                                            nearbyJobPost.setNumberOfWorker(numOfWorker);

                                            if (numberOfRatings != null){
                                                int intNumberOfRatings = numberOfRatings.intValue();
                                                nearbyJobPost.setNumberOfRatings(intNumberOfRatings);
                                            }

                                            nearbyJobList.add(nearbyJobPost);
                                        }
                                    }
                                }

                                if (queriesCompleted.get() == totalQueries) {
                                    recyclerView.setAdapter(new nearbyJobsModelAdapter(getContext(), nearbyJobList));
                                    if (jobsFoundCounter.get() > 0) {
                                        // Jobs found within the radius, hide the indicator
                                        jobsIndicator.setVisibility(View.GONE);
                                    } else {
                                        // No jobs found within the radius, show the indicator
                                        jobsIndicator.setVisibility(View.VISIBLE);
                                    }
                                }
                            } else {
                                queriesCompleted.incrementAndGet();
                                // Error occurred while querying postedJobs collection
                                if (queriesCompleted.get() == totalQueries) {
                                    Toast.makeText(getContext(), "Your actions are too fast", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                } else {
                    // Error occurred while querying users collection
                    Toast.makeText(getContext(), "Your actions are too fast", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.getSupportActionBar().setTitle("Nearby Jobs");
        }

        if (gRadius == 2000) {
            RadiusKm.setText("2");
        } else if (gRadius == 3000) {
            RadiusKm.setText("3");
        } else if (gRadius == 4000) {
            RadiusKm.setText("4");
        } else if (gRadius == 5000) {
            RadiusKm.setText("5");
        } else if (gRadius == 6000) {
            RadiusKm.setText("6");
        } else if (gRadius == 7000) {
            RadiusKm.setText("7");
        } else if (gRadius == 8000) {
            RadiusKm.setText("8");
        } else if (gRadius == 9000) {
            RadiusKm.setText("9");
        } else if (gRadius == 10000) {
            RadiusKm.setText("10");
        } else if (gRadius == 11000) {
            RadiusKm.setText("11");
        } else if (gRadius == 12000) {
            RadiusKm.setText("12");
        } else if (gRadius == 13000) {
            RadiusKm.setText("13");
        } else if (gRadius == 14000) {
            RadiusKm.setText("14");
        } else if (gRadius == 15000) {
            RadiusKm.setText("15");
        }

        searchView.setQuery("", false);
        searchView.clearFocus();

    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final double earthRadius = 6371.0; // Radius of the Earth in kilometers

        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        double deltaLat = lat2Rad - lat1Rad;
        double deltaLon = lon2Rad - lon1Rad;

        double a = Math.pow(Math.sin(deltaLat / 2), 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) * Math.pow(Math.sin(deltaLon / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double distance = earthRadius * c * 1000; // Convert to meters

        return distance;
    }

    // ... (other methods)

    private void filterJobs(double latitude, double longitude, String text) {
        double radius = gRadius; // 5 km in meters

        CollectionReference usersCollection = firestore.collection("users");
        List<nearbyJobsModelHandler> nearbyJobList = new ArrayList<>(); // Create a list to hold all jobs

        if (fragmentContext != null) {
            AtomicInteger queriesCompleted = new AtomicInteger(0);
            AtomicInteger jobsFoundCounter = new AtomicInteger(0);

            usersCollection.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    int totalQueries = task.getResult().size();

                    for (QueryDocumentSnapshot userDocument : task.getResult()) {
                        String userId = userDocument.getId();

                        CollectionReference postedJobsCollection = usersCollection
                                .document(userId)
                                .collection("postedJobs");

                        postedJobsCollection.get().addOnCompleteListener(jobsTask -> {
                            if (jobsTask.isSuccessful()) {
                                queriesCompleted.incrementAndGet();

                                for (QueryDocumentSnapshot jobDocument : jobsTask.getResult()) {
                                    String latitudeStr = jobDocument.getString("latitude");
                                    String longitudeStr = jobDocument.getString("longitude");
                                    Boolean isAccepted = jobDocument.getBoolean("jobAccepted");
                                    String jobTitle = jobDocument.getString("jobTitle");
                                    String jobCategory0 = jobDocument.getString("category");

                                    if (latitudeStr != null && longitudeStr != null && isAccepted != null
                                            && jobTitle != null) {
                                        double jobLatitude = Double.parseDouble(latitudeStr);
                                        double jobLongitude = Double.parseDouble(longitudeStr);

                                        double distance = calculateDistance(latitude, longitude, jobLatitude, jobLongitude);
                                        if (distance <= radius && !isAccepted) {

                                            if (jobCategory0 != null && jobCategory0.toLowerCase().contains(text.toLowerCase())
                                                    || jobTitle.toLowerCase().contains(text.toLowerCase())) {

                                                distance = distance / 1000;
                                                dist = Math.round(distance * 100.0) / 100.0;
                                                jobsFoundCounter.incrementAndGet();

                                                // Add the job to the list for display later
                                                String posterName = jobDocument.getString("PosterName");
                                                String JobTitle = jobDocument.getString("jobTitle");
                                                String JobRate = jobDocument.getString("jobRate");
                                                String JobDescription = jobDocument.getString("jobDescription");
                                                String JobDistance = String.valueOf(dist);
                                                String fJobDistance = "Distance: " + JobDistance + " KM";
                                                String jobID = jobDocument.getString("jobId");
                                                String posterUserID = jobDocument.getString("posterUID");
                                                Double posterRep = jobDocument.getDouble("currentAverageRating");
                                                String jobCategory = jobDocument.getString("category");
                                                String date1 = jobDocument.getString("date1");
                                                String date2 = jobDocument.getString("date2");
                                                String jobType = jobDocument.getString("jobType");
                                                String numOfWorker = jobDocument.getString("numberOfWorker");
                                                Long numberOfRatings = jobDocument.getLong("numberOfRatings");
                                                String dateRange = date1 + " - " + date2;

                                                nearbyJobsModelHandler nearbyJobPost = new nearbyJobsModelHandler(JobTitle, JobDescription,
                                                        JobRate, fJobDistance, posterName, jobID, posterUserID, posterRep);
                                                nearbyJobPost.setJobCategory(jobCategory);
                                                nearbyJobPost.setDocumentId(userId);
                                                nearbyJobPost.setDateRange(dateRange);
                                                nearbyJobPost.setJobType(jobType);
                                                nearbyJobPost.setNumberOfWorker(numOfWorker);

                                                if (numberOfRatings != null){
                                                    int intNumberOfRatings = numberOfRatings.intValue();
                                                    nearbyJobPost.setNumberOfRatings(intNumberOfRatings);
                                                }

                                                nearbyJobList.add(nearbyJobPost);

                                            }

                                        }

                                    }
                                }

                                if (queriesCompleted.get() == totalQueries) {
                                    recyclerView.setAdapter(new nearbyJobsModelAdapter(getContext(), nearbyJobList));
                                    if (jobsFoundCounter.get() > 0) {
                                        // Jobs found within the radius, hide the indicator
                                        jobsIndicator.setVisibility(View.GONE);
                                    } else {
                                        // No jobs found within the radius, show the indicator
                                        jobsIndicator.setVisibility(View.VISIBLE);
                                    }
                                }
                            } else {
                                queriesCompleted.incrementAndGet();
                                // Error occurred while querying postedJobs collection
                                if (queriesCompleted.get() == totalQueries) {
                                    Toast.makeText(getContext(), "Your actions are too fast", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                } else {
                    // Error occurred while querying users collection
                    Toast.makeText(getContext(), "Your actions are too fast", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

}
