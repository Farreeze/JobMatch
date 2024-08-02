package com.example.jobmatch;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ApplicantAdapter extends RecyclerView.Adapter<ApplicantHolder> {

    FirebaseAuth auth;
    FirebaseFirestore fStore;
    FirebaseUser currentUser;
    String currentUserUID;
    private Context context;
    private List<ApplicantHandler> items;

    public ApplicantAdapter(Context context, List<ApplicantHandler> items) {
        this.context = context;
        this.items = items;
        this.auth = FirebaseAuth.getInstance();
        this.fStore = FirebaseFirestore.getInstance();
        this.currentUser = auth.getCurrentUser();
        assert currentUser != null;
        currentUserUID = currentUser.getUid();
    }


    @NonNull
    @Override
    public ApplicantHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.applicant_model, parent, false);
        return new ApplicantHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ApplicantHolder holder, int position) {

        holder.applicantName.setText(items.get(position).getApplicantName());
        holder.applicantGender.setText(items.get(position).getApplicantGender());

        double rating = items.get(position).getApplicantRep();

        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        String formattedDouble = decimalFormat.format(rating);
        double finalRate = Double.parseDouble(formattedDouble);

        holder.applicantRep.setText(String.valueOf(finalRate));
        holder.applicantAge.setText(items.get(position).getApplicantAge());
        holder.jobTitle.setText(items.get(position).getJobTitle().toUpperCase());
        holder.applicantAddress.setText(items.get(position).getApplicantAddress());
        holder.applicantContact.setText(items.get(position).getApplicantContact());
        holder.applicantEmail.setText(items.get(position).getApplicantEmail());

        int numOfRating = items.get(position).getNumberOfRatings();

        String fNumOfRating = "("+numOfRating+")";

        holder.numberOfRatings.setText(fNumOfRating);

        holder.MrelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.relativeLayout.getVisibility() == View.GONE) {
                    holder.relativeLayout.setVisibility(View.VISIBLE);
                } else {
                    holder.relativeLayout.setVisibility(View.GONE);
                }
            }
        });

        holder.declineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // decline the application

                holder.progressBar.setVisibility(View.VISIBLE);
                holder.declineButton.setEnabled(false);
                holder.acceptButton.setEnabled(false);

                int itemPosition = holder.getBindingAdapterPosition();

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Confirm Reject Application");
                builder.setMessage("Are you sure you want to reject this application?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // delete the sent application of applicant and delete the application in applicant fragment

                        DocumentReference documentReference = fStore.collection("users").document(currentUserUID)
                                .collection("applicants")
                                .document(items.get(itemPosition).getDocumentID());

                        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                String applicantUserID = documentSnapshot.getString("applicantUserID");

                                String message = "Your application for JOB: " + items.get(itemPosition).getJobTitle().toUpperCase() + " has been rejected.";

                                sendNotification("Application", message, applicantUserID);

                                deleteApplicant(itemPosition, holder.progressBar, holder.declineButton, holder.acceptButton);

                            }
                        });

                        Toast.makeText(context, "Application has been deleted", Toast.LENGTH_SHORT).show();

                    }
                });

                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // closes the dialog
                        dialogInterface.dismiss();

                        holder.progressBar.setVisibility(View.GONE);
                        holder.declineButton.setEnabled(true);
                        holder.acceptButton.setEnabled(true);

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
                            holder.declineButton.setEnabled(true);
                            holder.acceptButton.setEnabled(true);

                            return true; // Consume the event
                        }
                        return false; // Let the dialog handle other key events
                    }
                });

                dialog.show();

            }
        });

        holder.acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // assign the user here then delete the application

                holder.progressBar.setVisibility(View.VISIBLE);
                holder.declineButton.setEnabled(false);
                holder.acceptButton.setEnabled(false);

                int itemPosition = holder.getBindingAdapterPosition();

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Confirm Assign User");
                builder.setMessage("Cancellation or deletion of jobs with assigned user will reduce " +
                        "your reputation points.\n\n" +
                        "Are you sure you want to assign this user?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        assignUser(itemPosition, holder.progressBar, holder.declineButton, holder.acceptButton);
                        deleteApplicant(itemPosition, holder.progressBar, holder.declineButton, holder.acceptButton);

                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();

                        holder.progressBar.setVisibility(View.GONE);
                        holder.declineButton.setEnabled(true);
                        holder.acceptButton.setEnabled(true);

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
                            holder.declineButton.setEnabled(true);
                            holder.acceptButton.setEnabled(true);

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

    public void assignUser(int itemPosition, View progressBar, View declineBtn, View acceptBtn) {

        DocumentReference documentReference = fStore.collection("users")
                .document(currentUserUID)
                .collection("applicants")
                .document(items.get(itemPosition).getDocumentID());

        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                // This is the applicant's userID and name
                String applicantUserID = documentSnapshot.getString("applicantUserID");
                String applicantName = documentSnapshot.getString("applicantName");
                String applicantEmail = documentSnapshot.getString("applicantEmail");
                String applicantContact = documentSnapshot.getString("applicantContact");
                String jobID = documentSnapshot.getString("appliedJobID");

                if (jobID != null) {

                    DocumentReference documentReference1 = fStore.collection("users")
                            .document(currentUserUID).collection("postedJobs")
                            .document(jobID);

                    Map<String, Object> user = new HashMap<>();
                    user.put("assignedUser", applicantName);
                    user.put("assignedUserID", applicantUserID);
                    user.put("assignedUserEmail", applicantEmail);
                    user.put("assignedUserContact", applicantContact);

                    documentReference1.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            boolean isAccepted = Boolean.TRUE.equals(documentSnapshot.getBoolean("jobAccepted"));
                            String recruiterName = documentSnapshot.getString("PosterName");
                            String jobTitle = documentSnapshot.getString("jobTitle");
                            String jobRate = documentSnapshot.getString("jobRate");
                            String jobDesc = documentSnapshot.getString("jobDescription");
                            Double recruiterRep = documentSnapshot.getDouble("currentAverageRating");
                            String recruiterID = documentSnapshot.getString("posterUID");
                            int recruiterNumRate = Objects.requireNonNull(documentSnapshot.getLong("numberOfRatings")).intValue();

                            if (!isAccepted && recruiterName != null){

                                documentReference1.set(user, SetOptions.merge())
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {

                                                Map<String, Object> updates = new HashMap<>();
                                                updates.put("jobAccepted", true);

                                                documentReference1.update(updates).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {

                                                        if (applicantUserID != null){

                                                            DocumentReference documentReference2 = fStore.collection("users")
                                                                    .document(applicantUserID)
                                                                    .collection("assignedJobs")
                                                                    .document(jobID);

                                                            Map<String, Object> assignedJob = new HashMap<>();
                                                            assignedJob.put("recruiterName", recruiterName);
                                                            assignedJob.put("jobTitle", jobTitle);
                                                            assignedJob.put("jobRate", jobRate);
                                                            assignedJob.put("jobDesc", jobDesc);
                                                            assignedJob.put("recruiterRep", recruiterRep);
                                                            assignedJob.put("recruiterID", recruiterID);
                                                            assignedJob.put("recruiterNumRate", recruiterNumRate);
                                                            assignedJob.put("jobID", jobID);

                                                            documentReference2.set(assignedJob).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void unused) {

                                                                    assert jobTitle != null;
                                                                    String message = "You have been assigned to JOB: " + jobTitle.toUpperCase();

                                                                    sendNotification("Assigned Job", message, applicantUserID);

                                                                    Toast.makeText(context, applicantName + " has been assigned", Toast.LENGTH_SHORT).show();
                                                                    progressBar.setVisibility(View.GONE);
                                                                    declineBtn.setEnabled(true);
                                                                    acceptBtn.setEnabled(true);
                                                                }
                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Toast.makeText(context, "Something went wrong, check internet connection", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });

                                                        }

                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(context, "Something went wrong, check internet connection", Toast.LENGTH_SHORT).show();
                                                    }
                                                });

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(context, "Something went wrong, check internet connection", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                            } else {
                                Toast.makeText(context, "This job is no longer available", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Something went wrong, check internet connection", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void deleteApplicant(int itemPosition, View progressBar, View declineBtn, View acceptBtn) {

        DocumentReference documentReference = fStore.collection("users")
                .document(currentUserUID)
                .collection("applicants")
                .document(items.get(itemPosition).getDocumentID());

        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                String applicantUID = documentSnapshot.getString("applicantUserID");
                String appliedJobID = documentSnapshot.getString("appliedJobID");

                fStore.collection("users")
                        .document(currentUserUID)
                        .collection("applicants")
                        .document(items.get(itemPosition).getDocumentID())
                        .delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @SuppressLint("NotifyDataSetChanged")
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {

                                    //delete from the user who sent the application

                                    assert appliedJobID != null;
                                    fStore.collection("users")
                                            .document(Objects.requireNonNull(applicantUID))
                                            .collection("sentApplications")
                                            .document(appliedJobID).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    if (task.isSuccessful()) {
                                                        items.remove(items.get(itemPosition));
                                                        progressBar.setVisibility(View.GONE);
                                                        declineBtn.setEnabled(true);
                                                        acceptBtn.setEnabled(true);
                                                        notifyDataSetChanged();
                                                    } else {
                                                        Toast.makeText(context, "Something went wrong, check internet connection", Toast.LENGTH_SHORT).show();
                                                        progressBar.setVisibility(View.GONE);
                                                        declineBtn.setEnabled(true);
                                                        acceptBtn.setEnabled(true);
                                                    }

                                                }
                                            });

                                } else {
                                    Toast.makeText(context, "Something went wrong, check internet connection", Toast.LENGTH_SHORT).show();
                                }

                            }

                        });

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

}
