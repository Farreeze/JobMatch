package com.example.jobmatch.fragments;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.jobmatch.ApplicantAdapter;
import com.example.jobmatch.ApplicantHandler;
import com.example.jobmatch.PostedJobsModelAdapter;
import com.example.jobmatch.R;
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
 * Use the {@link ApplicantFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ApplicantFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ApplicantFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ApplicantFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ApplicantFragment newInstance(String param1, String param2) {
        ApplicantFragment fragment = new ApplicantFragment();
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

    @Override
    public void onResume() {
        super.onResume();
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.getSupportActionBar().setTitle("Applicants");
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

    TextView textView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_applicant, container, false);

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);

        textView = rootView.findViewById(R.id.applicant_indicator);

        initFirebase();
        initRecyclerView(rootView);
        fetchAndDisplayApplicants();


        return  rootView;
    }

    private void fetchAndDisplayApplicants() {
        DocumentReference documentReference = fStore.collection("users").document(userID);
        CollectionReference collectionReference = documentReference.collection("applicants");

        Query query = collectionReference.orderBy("timestamp", Query.Direction.DESCENDING);

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                List<ApplicantHandler> applicantList = new ArrayList<>();

                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots){

                    String documentID = documentSnapshot.getId();

                    String applicantName = documentSnapshot.getString("applicantName");
                    String applicantGender = documentSnapshot.getString("applicantGender");
                    Double applicantReputation = documentSnapshot.getDouble("applicantReputation");
                    String applicantAge = String.valueOf(documentSnapshot.getLong("applicantAge"));
                    String applicantAddress = documentSnapshot.getString("applicantAddress");
                    String applicantContact = documentSnapshot.getString("applicantContact");
                    String applicantEmail = documentSnapshot.getString("applicantEmail");
                    String jobTitle = documentSnapshot.getString("jobTitle");
                    int applicantNumRating = Objects.requireNonNull(documentSnapshot.getLong("applicantNumRating")).intValue();

                    ApplicantHandler applicantHandler = new ApplicantHandler
                            (applicantName, jobTitle, applicantAge,
                                    applicantAddress, applicantContact, applicantEmail,
                                    applicantGender, applicantReputation);

                    applicantHandler.setDocumentID(documentID);

                    applicantHandler.setNumberOfRatings(applicantNumRating);

                    applicantList.add(applicantHandler);

                    recyclerView.setAdapter(new ApplicantAdapter(getContext(), applicantList));

                    textView.setVisibility(View.GONE);

                }

            }
        });
    }

    private void initRecyclerView(View rootView) {
        recyclerView = rootView.findViewById(R.id.ApplicantRecyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

}