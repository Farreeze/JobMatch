package com.example.jobmatch;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssignedJobAdapter extends RecyclerView.Adapter<AssignedJobHolder> {

    private Context context;
    private List<AssignedJobHandler> items;

    //firebase
    FirebaseAuth auth;
    FirebaseFirestore fStore;
    FirebaseUser currentUser;

    String currentUserID;

    String currentUserName, posterName;

    public AssignedJobAdapter(Context context, List<AssignedJobHandler> items) {
        this.context = context;
        this.items = items;

        this.auth = FirebaseAuth.getInstance();
        this.fStore = FirebaseFirestore.getInstance();
        this.currentUser = auth.getCurrentUser();
        assert currentUser != null;
        this.currentUserID = currentUser.getUid();

    }

    @NonNull
    @Override
    public AssignedJobHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.assigned_job_model, parent, false);
        return new AssignedJobHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AssignedJobHolder holder, int position) {

        holder.recruiterName.setText(items.get(position).getPosterName());

        double rating = items.get(position).getRecruiterRep();

        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        String formattedDouble = decimalFormat.format(rating);
        double finalRate = Double.parseDouble(formattedDouble);

        holder.recruiterRep.setText(String.valueOf(finalRate));
        holder.jobTitle.setText(items.get(position).getJobTitle().toUpperCase());
        holder.jobRate.setText(items.get(position).getJobRate());
        holder.jobDescription.setText(items.get(position).getJobDescription());

        int numOfRate = items.get(position).getNumberOfRating();

        String fNumOfRate = "("+numOfRate+")";

        holder.numberOfRating.setText(fNumOfRate);

        if (items.get(position).getSchedule() != null) {
            holder.schedule.setText(items.get(position).getSchedule());
        } else {
            holder.schedule.setText(R.string.no_schedule);
        }

        holder.cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                holder.progressBar.setVisibility(View.VISIBLE);
                holder.chatBtn.setEnabled(false);
                holder.cancelBtn.setEnabled(false);

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Confirm Cancellation");
                builder.setMessage("Cancelling this job may result to a bad rating. " +
                        "Are you sure you want to cancel this job?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        int clickedPosition = holder.getBindingAdapterPosition();

                        String posterUserID = items.get(clickedPosition).getPosterID();
                        String jobID = items.get(clickedPosition).getJobID();

                        DocumentReference documentReference = fStore.collection("users").document(currentUserID);

                        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                String fName = documentSnapshot.getString("fName");
                                String lName = documentSnapshot.getString("lName");

                                currentUserName = fName +" "+lName;

                                DocumentReference documentReference1 = fStore.collection("users").document(posterUserID);

                                documentReference1.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        String fName = documentSnapshot.getString("fName");
                                        String lName = documentSnapshot.getString("lName");

                                        posterName = fName +" "+ lName;

                                        fStore.collection("users")
                                                .document(posterUserID)
                                                .collection("postedJobs")
                                                .document(jobID).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @SuppressLint("NotifyDataSetChanged")
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        addToHistory(posterUserID, currentUserID, clickedPosition, false);
                                                        items.remove(items.get(clickedPosition));
                                                        notifyDataSetChanged();
                                                        holder.progressBar.setVisibility(View.GONE);
                                                        holder.chatBtn.setEnabled(true);
                                                        holder.cancelBtn.setEnabled(true);
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(builder.getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                                                        holder.progressBar.setVisibility(View.GONE);
                                                        holder.chatBtn.setEnabled(true);
                                                        holder.cancelBtn.setEnabled(true);
                                                    }
                                                });

                                    }
                                });

                            }
                        });

                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        holder.progressBar.setVisibility(View.GONE);
                        holder.chatBtn.setEnabled(true);
                        holder.cancelBtn.setEnabled(true);
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
                            holder.progressBar.setVisibility(View.GONE);
                            holder.chatBtn.setEnabled(true);
                            holder.cancelBtn.setEnabled(true);


                            return true; // Consume the event
                        }
                        return false; // Let the dialog handle other key events
                    }
                });

                dialog.show();

            }
        });

        holder.chatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //open the conversation here

                int clickedPosition = holder.getBindingAdapterPosition();

                if (clickedPosition != RecyclerView.NO_POSITION) {
                    String chatTitle = items.get(clickedPosition).getJobTitle();
                    String recruiterName = items.get(clickedPosition).getPosterName();
                    String jobID = items.get(clickedPosition).getJobID();
                    String recruiterID = items.get(clickedPosition).getPosterID();
                    openConversationFragment(chatTitle, recruiterName, jobID, recruiterID);
                }


            }
        });

    }

    public void deleteAssignedJob(String jobID, String posterID) {
        fStore.collection("users")
                .document(currentUserID)
                .collection("assignedJobs")
                .document(jobID)
                .delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            reduceRep(currentUserID);
                            sendNotification("Assigned Jobs",currentUserName+" has canceled the job", posterID);
                        }
                    }
                });
    }

    public String getCurrentDate() {

        String currentDate;

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; // Month starts from 0
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        return currentDate = month + "-" + day + "-" + year;

    }

    public void addToHistory(String recruiterID, String assignedUserID, int clickedPosition, boolean success) {

        String jobID = items.get(clickedPosition).getJobID();
        String jobTitle = items.get(clickedPosition).getJobTitle();
        String status;

        if (success) {
            status = "COMPLETED";
        } else {
            status = "CANCELED";
        }

        String currentDate = getCurrentDate();

        Map<String, Object> historyItem = new HashMap<>();
        historyItem.put("recruiterID", recruiterID);
        historyItem.put("applicantID", assignedUserID);
        historyItem.put("canceledBy", assignedUserID);
        historyItem.put("jobID", jobID);
        historyItem.put("jobTitle", jobTitle);
        historyItem.put("recruiterName", posterName);
        historyItem.put("applicantName", currentUserName);
        historyItem.put("status", status);
        historyItem.put("timestamp", FieldValue.serverTimestamp());
        historyItem.put("date", currentDate);

        CollectionReference collectionReference = fStore.collection("users")
                .document(recruiterID)
                .collection("history");

        collectionReference.add(historyItem).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {

                CollectionReference collectionReference1 = fStore.collection("users")
                        .document(assignedUserID)
                        .collection("history");

                collectionReference1.add(historyItem).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        // add complete here
                        deleteAssignedJob(jobID, recruiterID);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

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

                if (!notificationTitle.equals("")&&!message.equals("")&&!receiverID.equals("")){

                    if (receiverToken == null){
                        return;
                    }

                    sendNotification.pushNotification(context, receiverToken, notificationTitle, message);

                }

            }
        });

    }

    public void reduceRep(String currentUserID) { // this reduces the rep of the current user

        DocumentReference documentReference = fStore.collection("users").document(currentUserID);
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                Long CurrentUserRep = documentSnapshot.getLong("reputation");

                if (CurrentUserRep != null) {
                    int currentUserRep = CurrentUserRep.intValue();

                    currentUserRep = currentUserRep - 10;

                    Map<String, Object> updateRep = new HashMap<>();
                    updateRep.put("reputation", currentUserRep);

                    documentReference.update(updateRep).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(context, "Reputation reduced by 10 points", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show();
                        }
                    });

                }

            }
        });

    }

    @Override
    public int getItemCount() {
        return items.size();
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



}
