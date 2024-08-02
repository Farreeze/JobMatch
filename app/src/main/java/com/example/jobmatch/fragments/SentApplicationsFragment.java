package com.example.jobmatch.fragments;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jobmatch.ApplicantAdapter;
import com.example.jobmatch.ApplicantHandler;
import com.example.jobmatch.R;
import com.example.jobmatch.SentApplicationAdapter;
import com.example.jobmatch.SentApplicationHandler;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SentApplicationsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SentApplicationsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SentApplicationsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SentApplicationsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SentApplicationsFragment newInstance(String param1, String param2) {
        SentApplicationsFragment fragment = new SentApplicationsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    public void onResume() {
        super.onResume();
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.getSupportActionBar().setTitle("Sent Applications");
        }
    }

    FirebaseAuth auth;
    FirebaseFirestore fStore;
    FirebaseUser currentUser;
    String userID;

    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        fStore = FirebaseFirestore.getInstance();
        userID = currentUser.getUid();
    }

    RecyclerView recyclerView;

    TextView textLabel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_sent_applications, container, false);

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);

        //start here

        textLabel = rootView.findViewById(R.id.sent_application_label);

        initFirebase();
        initRecyclerView(rootView);
        fetchAndDisplayApplications();

        return rootView;
    }


    private void fetchAndDisplayApplications(){
        DocumentReference documentReference = fStore.collection("users").document(userID);
        CollectionReference collectionReference = documentReference.collection("sentApplications");

        Query query = collectionReference.orderBy("timestamp", Query.Direction.DESCENDING);

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<SentApplicationHandler> applicationList = new ArrayList<>();
                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots){

                    String documentID = documentSnapshot.getId();

                    String appliedJobTitle = documentSnapshot.getString("jobTitle");
                    String posterName = documentSnapshot.getString("posterName");
                    Double posterRep = documentSnapshot.getDouble("posterRep");
                    String jobDescription = documentSnapshot.getString("jobDescription");
                    String jobRate = documentSnapshot.getString("jobRate");
                    String appliedJobID = documentSnapshot.getString("ApplicationID");
                    String jobPosterUID = documentSnapshot.getString("jobPosterUID");
                    int posterNumRate = Objects.requireNonNull(documentSnapshot.getLong("jobPosterNumRating")).intValue();

                    SentApplicationHandler sentApplicationHandler = new SentApplicationHandler
                            (posterName, posterRep, appliedJobTitle, jobDescription, jobRate, appliedJobID, jobPosterUID);

                    sentApplicationHandler.setDocumentID(documentID);

                    sentApplicationHandler.setNumberOfRating(posterNumRate);

                    applicationList.add(sentApplicationHandler);

                    recyclerView.setAdapter(new SentApplicationAdapter(getContext(), applicationList));

                    textLabel.setVisibility(View.GONE);

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(),
                        "Failed to load your sent applications, try again later",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initRecyclerView(View rootView) {
        recyclerView = rootView.findViewById(R.id.sentApplicationRecyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }


}