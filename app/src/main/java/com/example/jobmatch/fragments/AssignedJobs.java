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

import com.example.jobmatch.AssignedJobAdapter;
import com.example.jobmatch.AssignedJobHandler;
import com.example.jobmatch.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AssignedJobs#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AssignedJobs extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AssignedJobs() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AssignedJobs.
     */
    // TODO: Rename and change types and number of parameters
    public static AssignedJobs newInstance(String param1, String param2) {
        AssignedJobs fragment = new AssignedJobs();
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


    //Variables

    FirebaseAuth auth;
    FirebaseFirestore fStore;
    FirebaseUser currentUser;

    String currentUserID;

    RecyclerView recyclerView;

    TextView label;

    public void initFirebase() {
        auth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        currentUser = auth.getCurrentUser();
        assert currentUser != null;
        currentUserID = currentUser.getUid();
    }

    public void initRecyclerview(View rootView) {
        recyclerView = rootView.findViewById(R.id.assignJobs_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }


    @Override
    public void onResume() {
        super.onResume();
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.getSupportActionBar().setTitle("Assigned Jobs");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_assigned_jobs, container, false);

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);

        //start here

        label = rootView.findViewById(R.id.AJ_label);

        initFirebase();
        initRecyclerview(rootView);
        fetchAndDisplayAssignedJobs();

        return rootView;
    }

    public void fetchAndDisplayAssignedJobs() {

        DocumentReference documentReference = fStore.collection("users").document(currentUserID);
        CollectionReference collectionReference = documentReference.collection("assignedJobs");

        collectionReference.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                List<AssignedJobHandler> assignedJobs = new ArrayList<>();

                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {

                    String documentID = documentSnapshot.getId();

                    String recruiterName = documentSnapshot.getString("recruiterName");
                    Double recruiterRep = documentSnapshot.getDouble("recruiterRep");
                    String jobTitle = documentSnapshot.getString("jobTitle");
                    String jobRate = documentSnapshot.getString("jobRate");
                    String jobDesc = documentSnapshot.getString("jobDesc");
                    String recruiterID = documentSnapshot.getString("recruiterID");
                    String schedule = documentSnapshot.getString("schedule");
                    int numberOfRating = Objects.requireNonNull(documentSnapshot.getLong("recruiterNumRate")).intValue();

                    AssignedJobHandler assignedJobHandler = new AssignedJobHandler
                            (recruiterName, recruiterRep, jobTitle, jobRate, jobDesc);

                    assignedJobHandler.setPosterID(recruiterID);

                    assignedJobHandler.setSchedule(schedule);

                    assignedJobHandler.setNumberOfRating(numberOfRating);

                    assignedJobHandler.setJobID(documentID);

                    assignedJobs.add(assignedJobHandler);

                    recyclerView.setAdapter(new AssignedJobAdapter(getContext(), assignedJobs));

                    label.setVisibility(View.GONE);

                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }

}