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

import com.example.jobmatch.HistoryAdapter;
import com.example.jobmatch.HistoryHandler;
import com.example.jobmatch.R;
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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HistoryFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HistoryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HistoryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HistoryFragment newInstance(String param1, String param2) {
        HistoryFragment fragment = new HistoryFragment();
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
            activity.getSupportActionBar().setTitle("History");
        }
    }

    FirebaseAuth auth;
    FirebaseFirestore fStore;
    FirebaseUser currentUser;

    String currentUserID;

    RecyclerView recyclerView;

    TextView historyLabel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_history, container, false);

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);

        //start here
        historyLabel = rootView.findViewById(R.id.historyLabel);

        initFirebase();
        initRecyclerview(rootView);
        fetchAndDisplayHistory();


        return rootView;
    }

    public void fetchAndDisplayHistory() {
        // Create a reference to the "history" collection for the current user
        DocumentReference documentReference = fStore.collection("users").document(currentUserID);
        CollectionReference collectionReference = documentReference.collection("history");

        // Query the "history" collection and order it by the "timestamp" field in ascending order
        Query query = collectionReference.orderBy("timestamp", Query.Direction.DESCENDING);

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                List<HistoryHandler> historyItems = new ArrayList<>();

                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    String documentID = documentSnapshot.getId();
                    String date = documentSnapshot.getString("date");
                    String jobTitle = documentSnapshot.getString("jobTitle");
                    String recruiterName = documentSnapshot.getString("recruiterName");
                    String applicantName = documentSnapshot.getString("applicantName");
                    String status = documentSnapshot.getString("status");
                    String canceledBy = documentSnapshot.getString("canceledBy");

                    HistoryHandler historyHandler = new HistoryHandler(date, recruiterName,
                            applicantName, jobTitle, status);

                    historyHandler.setDocumentID(documentID);

                    historyHandler.setCanceledBy(canceledBy);

                    historyItems.add(historyHandler);
                }

                recyclerView.setAdapter(new HistoryAdapter(getContext(), historyItems));

                if (historyItems.isEmpty()) {
                    historyLabel.setVisibility(View.VISIBLE);
                } else {
                    historyLabel.setVisibility(View.GONE);
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Handle failures
            }
        });
    }


    public void initFirebase() {
        auth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            currentUserID = currentUser.getUid();
        }

    }

    public void initRecyclerview(View rootView) {
        recyclerView = rootView.findViewById(R.id.history_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }


}