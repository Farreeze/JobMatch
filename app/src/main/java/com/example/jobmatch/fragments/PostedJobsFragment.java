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

import com.example.jobmatch.PostedJobsModelAdapter;
import com.example.jobmatch.PostedJobsModelHandler;
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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PostedJobsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PostedJobsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    //firebase
    FirebaseAuth auth;
    FirebaseUser currentUser;
    FirebaseFirestore fStore;
    String userID;
    RecyclerView recyclerView;
    TextView noJobsLabel;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public PostedJobsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PostedJobsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PostedJobsFragment newInstance(String param1, String param2) {
        PostedJobsFragment fragment = new PostedJobsFragment();
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
            activity.getSupportActionBar().setTitle("Posted Jobs");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_posted_jobs, container, false);

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);

        noJobsLabel = rootView.findViewById(R.id.noPostedJobLabel);

        initFirebase();
        initRecyclerView(rootView);
        fetchAndDisplayPostedJobs();

        return rootView;
    }

    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        fStore = FirebaseFirestore.getInstance();
        userID = currentUser.getUid();
    }

    private void initRecyclerView(View rootView) {
        recyclerView = rootView.findViewById(R.id.list_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }






    private void fetchAndDisplayPostedJobs() {
        DocumentReference documentReference = fStore.collection("users").document(userID);

        CollectionReference collectionReference = documentReference.collection("postedJobs");

        Query query = collectionReference.orderBy("timestamp", Query.Direction.DESCENDING);

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                List<PostedJobsModelHandler> jobList = new ArrayList<>();

                for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots){

                    String documentId = documentSnapshot.getId();



                    String jobTitle = documentSnapshot.getString("jobTitle");
                    String jobDesc = documentSnapshot.getString("jobDescription");
                    String jobRate = documentSnapshot.getString("jobRate");
                    String assignedUserName = documentSnapshot.getString("assignedUser");
                    String assignedUserEmail = documentSnapshot.getString("assignedUserEmail");
                    String assignedUserContact = documentSnapshot.getString("assignedUserContact");
                    String assignedUserID = documentSnapshot.getString("assignedUserID");
                    String currentUserName = documentSnapshot.getString("PosterName");
                    String currentUserID = documentSnapshot.getString("posterUID");
                    String schedDay = documentSnapshot.getString("schedule");
                    String startDate = documentSnapshot.getString("date1");
                    String endDate = documentSnapshot.getString("date2");
                    String jobType = documentSnapshot.getString("jobType");
                    String numOfWorker = documentSnapshot.getString("numberOfWorker");

                    String dateRange = startDate + " - " + endDate;

                    PostedJobsModelHandler post = new PostedJobsModelHandler(jobTitle, jobDesc, jobRate);

                    post.setDocumentId(documentId);

                    post.setAssignedUserSched(schedDay);

                    post.setDateRange(dateRange);

                    post.setJobType(jobType);

                    post.setNumberOfWorker(numOfWorker);

                    if (assignedUserName != null &&
                            assignedUserContact != null &&
                            assignedUserEmail != null) {

                        post.setAssignedUserName(assignedUserName);
                        post.setAssignedUserEmail(assignedUserEmail);
                        post.setAssignedUserContact(assignedUserContact);
                        post.setAssignedUserID(assignedUserID);
                        post.setCurrentUserName(currentUserName);
                        post.setCurrentUserID(currentUserID);

                    }

                    jobList.add(post);

                    recyclerView.setAdapter(new PostedJobsModelAdapter(getContext(), jobList));

                    noJobsLabel.setVisibility(View.GONE);
                }

            }
        });


    }




}