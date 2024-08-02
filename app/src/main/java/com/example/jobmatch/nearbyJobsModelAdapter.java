package com.example.jobmatch;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobmatch.fragments.ConversationFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class nearbyJobsModelAdapter extends RecyclerView.Adapter<nearbyJobsModelHolder> {

    private Context context;
    private List<nearbyJobsModelHandler> items;

    FirebaseAuth auth;
    FirebaseFirestore fStore;
    FirebaseUser currentUser;

    String currentUserID;

    public nearbyJobsModelAdapter(Context context, List<nearbyJobsModelHandler> items) {
        this.auth = FirebaseAuth.getInstance();
        this.fStore = FirebaseFirestore.getInstance();
        this.currentUser = auth.getCurrentUser();
        assert currentUser != null;
        this.currentUserID = currentUser.getUid();
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public nearbyJobsModelHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.nearby_job_model, parent, false);
        return new nearbyJobsModelHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull nearbyJobsModelHolder holder, int position) {
        nearbyJobsModelHandler item = items.get(position);

        holder.distance.setText(item.getDistance());
        holder.posterName.setText(item.getPosterName());
        holder.jobTitle.setText(item.getJobTitle().toUpperCase());
        holder.jobDescription.setText(item.getJobDescription());
        holder.jobRate.setText(item.getJobRate());

        double rate = item.getPosterRep();

        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        String formattedDouble = decimalFormat.format(rate);
        double finalRate = Double.parseDouble(formattedDouble);

        holder.posterRep.setText(String.valueOf(finalRate));
        holder.jobCategory.setText(item.getJobCategory());
        holder.dateRange.setText(item.getDateRange());
        holder.jobType.setText(item.getJobType());
        holder.numberOfWorker.setText(item.getNumberOfWorker());

        int numRating = item.getNumberOfRatings();

        String finalNumRating = "("+numRating+")";

        holder.numberOfRating.setText(finalNumRating);

        holder.chatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int clickedPosition = holder.getBindingAdapterPosition();
                if (clickedPosition != RecyclerView.NO_POSITION) {
                    holder.applyBtn.setEnabled(false);
                    holder.chatBtn.setEnabled(false);
                    holder.nearbyJobsModelPb.setVisibility(View.VISIBLE);
                    String chatTitle = items.get(clickedPosition).getJobTitle();
                    String PosterName = items.get(clickedPosition).getPosterName();
                    String jobID = items.get(clickedPosition).getJobID();
                    String posterUserID = items.get(clickedPosition).getPosterUserID();
                    // Open the ConversationFragment
                    openConversationFragment(chatTitle, PosterName, jobID, posterUserID);
                    holder.applyBtn.setEnabled(true);
                    holder.chatBtn.setEnabled(true);
                    holder.nearbyJobsModelPb.setVisibility(View.GONE);
                }
            }
        });

        holder.applyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.applyBtn.setEnabled(false);
                holder.chatBtn.setEnabled(false);
                holder.nearbyJobsModelPb.setVisibility(View.VISIBLE);
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Confirm Application");
                builder.setMessage("The person who posted this job will be able to see the following details about you:\n" +
                        "\n\nName\n\nReputation\n\nGender\n\nAge\n\nAddress\n\nContact\n\nEmail\n");
                builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // event to trigger when confirmed
                        int clickedPosition = holder.getBindingAdapterPosition();


                        String posterID = items.get(clickedPosition).getPosterUserID();

                        String docID = generateChatDocumentID(currentUserID, posterID);

                        DocumentReference chatRef = fStore.collection("chats").document(docID);

                        chatRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();

                                    if (document.exists()) {
                                        sendApplication(clickedPosition, holder.nearbyJobsModelPb, holder.applyBtn, holder.chatBtn);
                                    } else {
                                        Toast.makeText(context, "Chat with recruiter first before applying", Toast.LENGTH_SHORT).show();
                                        holder.applyBtn.setEnabled(true);
                                        holder.chatBtn.setEnabled(true);
                                        holder.nearbyJobsModelPb.setVisibility(View.GONE);
                                    }

                                }
                            }
                        });

                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // close dialog when canceled
                        dialogInterface.dismiss();
                        holder.applyBtn.setEnabled(true);
                        holder.chatBtn.setEnabled(true);
                        holder.nearbyJobsModelPb.setVisibility(View.GONE);
                    }
                });
                AlertDialog dialog = builder.create();

                dialog.setCanceledOnTouchOutside(false);

                dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                            // Back button was pressed

                            dialog.dismiss();
                            holder.nearbyJobsModelPb.setVisibility(View.GONE);
                            holder.applyBtn.setEnabled(true);
                            holder.chatBtn.setEnabled(true);

                            return true; // Consume the event
                        }
                        return false; // Let the dialog handle other key events
                    }
                });

                dialog.show();

            }
        });

    }

    @Override
    public int getItemCount() {
        return items.size();
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

    private void openConversationFragment(String chatTitle, String PosterName, String jobID, String posterUserID) {
        FragmentManager fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // Create an instance of ConversationFragment and set arguments if needed
        ConversationFragment conversationFragment = new ConversationFragment();
        Bundle args = new Bundle();
        args.putString("jobID", jobID);
        args.putString("posterUserID", posterUserID);
        args.putString("chatTitle", chatTitle);
        args.putString("PosterName", PosterName);
        conversationFragment.setArguments(args);

        // Replace the current fragment with the ConversationFragment
        transaction.replace(R.id.fragment_container, conversationFragment); // Replace with your container ID
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private String generateChatDocumentID(String participant1, String participant2) {
        List<String> participants = new ArrayList<>();
        participants.add(participant1);
        participants.add(participant2);
        Collections.sort(participants);
        return participants.get(0) + "_" + participants.get(1);
    }

    public void sendApplication(int clickedPosition, View progressBar, View applyBtn, View chatBtn) {

        DocumentReference documentReference = fStore
                .collection("users")
                .document(currentUserID);

        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String jobID = items.get(clickedPosition).getJobID();
                String posterName = items.get(clickedPosition).getPosterName();
                Double posterRep = items.get(clickedPosition).getPosterRep();
                String jobTitle = items.get(clickedPosition).getJobTitle();
                String jobDesc = items.get(clickedPosition).getJobDescription();
                String jobRate = items.get(clickedPosition).getJobRate();
                int posterNumRating = items.get(clickedPosition).getNumberOfRatings();
                Double applicantRep = documentSnapshot.getDouble("currentAverageRating");
                String applicantGender = documentSnapshot.getString("gender");
                String applicantFirstName = documentSnapshot.getString("fName");
                String applicantLastName = documentSnapshot.getString("lName");
                String applicantBirthDay = documentSnapshot.getString("birthday");
                String applicantAddress = documentSnapshot.getString("address");
                String applicantContact = documentSnapshot.getString("contact");
                String applicantEmail = documentSnapshot.getString("email");
                int applicantNumRating = Objects.requireNonNull(documentSnapshot.getLong("totalNumberRatings")).intValue();

                int applicantAge = calculateAgeFromDateString(applicantBirthDay);
                String applicantName = applicantFirstName + " " + applicantLastName;

                if (clickedPosition != RecyclerView.NO_POSITION) {
                    String posterUserID = items.get(clickedPosition).getPosterUserID();

                    CollectionReference collectionReference = fStore
                            .collection("users")
                            .document(posterUserID)
                            .collection("applicants");

                    Map<String, Object> applicant = new HashMap<>();
                    applicant.put("applicantReputation", applicantRep);
                    applicant.put("applicantUserID", currentUserID);
                    applicant.put("applicantGender", applicantGender);
                    applicant.put("applicantName", applicantName);
                    applicant.put("applicantAge", applicantAge);
                    applicant.put("appliedJobID", jobID);
                    applicant.put("applicantAddress", applicantAddress);
                    applicant.put("applicantContact", applicantContact);
                    applicant.put("applicantEmail", applicantEmail);
                    applicant.put("jobTitle", jobTitle);
                    applicant.put("timestamp", FieldValue.serverTimestamp());
                    applicant.put("applicantNumRating", applicantNumRating);

                    if (currentUserID.equals(posterUserID)) {
                        Toast.makeText(context, "You can't apply to a job you posted", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    DocumentReference documentReference1 = fStore
                            .collection("users")
                            .document(currentUserID)
                            .collection("sentApplications")
                            .document(jobID);

                    documentReference1.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    Toast.makeText(context, "You already applied for this job", Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                    applyBtn.setEnabled(true);
                                    chatBtn.setEnabled(true);
                                } else {

                                    collectionReference.add(applicant).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {

                                            String message = applicantName + " sent an application for JOB: " + jobTitle.toUpperCase();

                                            sendNotification("Applicant", message, posterUserID);

                                            String documentID = documentReference.getId();

                                            CollectionReference collectionReference1 = fStore
                                                    .collection("users")
                                                    .document(currentUserID)
                                                    .collection("sentApplications");

                                            Map<String, Object> jobsApplied = new HashMap<>();
                                            jobsApplied.put("jobRate", jobRate);
                                            jobsApplied.put("ApplicationID", documentID);
                                            jobsApplied.put("jobID", jobID);
                                            jobsApplied.put("jobPosterUID", posterUserID);
                                            jobsApplied.put("posterName", posterName);
                                            jobsApplied.put("posterRep", posterRep);
                                            jobsApplied.put("jobTitle", jobTitle);
                                            jobsApplied.put("jobDescription", jobDesc);
                                            jobsApplied.put("timestamp", FieldValue.serverTimestamp());
                                            jobsApplied.put("jobPosterNumRating", posterNumRating);

                                            collectionReference1.document(jobID).set(jobsApplied).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    Toast.makeText(context, "Application sent to " + posterName, Toast.LENGTH_SHORT).show();
                                                    progressBar.setVisibility(View.GONE);
                                                    applyBtn.setEnabled(true);
                                                    chatBtn.setEnabled(true);
                                                }

                                            });

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(context,
                                                    "Failed to send application. Check network connectivity",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                }
                            } else {
                                Toast.makeText(context,
                                        "Failed to send application. Check network connectivity",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Failed to apply, check network connectivity", Toast.LENGTH_SHORT).show();
            }
        });

    }

    String receiverToken;

    public void sendNotification(String notificationTitle, String message, String receiverID) {

        DocumentReference documentReference = fStore.collection("users").document(receiverID);

        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                receiverToken = documentSnapshot.getString("token");

                if (!notificationTitle.equals("") && !message.equals("") && !receiverID.equals("")) {

                    if (receiverToken == null) {
                        return;
                    }

                    sendNotification.pushNotification(context, receiverToken, notificationTitle, message);

                }

            }
        });

    }

}
