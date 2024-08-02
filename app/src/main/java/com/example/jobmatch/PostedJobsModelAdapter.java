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

import com.example.jobmatch.fragments.MConversationFragment;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import kotlin.collections.UCollectionsKt;

public class PostedJobsModelAdapter extends RecyclerView.Adapter<PostedJobsModelHolder> {

    Context context;
    List<PostedJobsModelHandler> items;

    FirebaseAuth auth;
    FirebaseFirestore fStore;
    FirebaseUser currentUser;

    String currentUserID;

    String name;
    public PostedJobsModelAdapter(Context context, List<PostedJobsModelHandler> items) {
        this.context = context;
        this.items = items;
        this.auth = FirebaseAuth.getInstance();
        this.fStore = FirebaseFirestore.getInstance();
        this.currentUser = auth.getCurrentUser();
        assert currentUser != null;
        currentUserID = currentUser.getUid();
    }

    @NonNull
    @Override
    public PostedJobsModelHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PostedJobsModelHolder(LayoutInflater.from(context).inflate(R.layout.posted_jobs_list_model, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PostedJobsModelHolder holder, int position) {
        holder.jobTitle.setText(items.get(position).getJobTitle().toUpperCase());
        holder.jobDescription.setText(items.get(position).getJobDescription());
        holder.jobRate.setText(items.get(position).getJobRate());
        holder.dateRange.setText(items.get(position).getDateRange());
        holder.jobType.setText(items.get(position).getJobType());
        holder.numberOfWorker.setText(items.get(position).getNumberOfWorker());

        if (items.get(position).getAssignedUserName() != null &&
        items.get(position).getAssignedUserEmail() != null &&
        items.get(position).getAssignedUserContact() != null &&
        items.get(position).getAssignedUserID() != null) {

            holder.assignedUserName.setText(items.get(position).getAssignedUserName());
            holder.assignedUserEmail.setText(items.get(position).getAssignedUserEmail());
            holder.assignedUserContact.setText(items.get(position).getAssignedUserContact());

        } else {
            holder.assignedUserName.setText(R.string.no_assigned_user);
            holder.assignedUserEmail.setText(R.string.no_assigned_user);
            holder.assignedUserContact.setText(R.string.no_assigned_user);
        }

        if (items.get(position).getAssignedUserSched() != null) {
            holder.assignedUserSched.setText(items.get(position).getAssignedUserSched());
        } else {
            holder.assignedUserSched.setText(R.string.no_schedule);
        }

        holder.MrelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.relativeLayout.getVisibility() == View.GONE){
                    holder.relativeLayout.setVisibility(View.VISIBLE);
                } else {
                    holder.relativeLayout.setVisibility(View.GONE);
                }
            }
        });

        holder.chatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //access the chat document
                // open the main chat here

                int clickedPosition = holder.getBindingAdapterPosition();

                String assignedUserID = items.get(clickedPosition).getAssignedUserID();

                if (assignedUserID == null) {
                    Toast.makeText(context, "No assigned user", Toast.LENGTH_SHORT).show();
                    return;
                }

                String docID = generateChatDocumentID(currentUserID, assignedUserID);

                DocumentReference documentReference = fStore.collection("chats").document(docID);

                documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String participant1 = documentSnapshot.getString("participant1");
                        String participant2 = documentSnapshot.getString("participant2");
                        String par2Name = documentSnapshot.getString("par2Name");
                        String par1Name = documentSnapshot.getString("par1Name");

                        String otherUser, otherPartName;

                        if (currentUserID.equals(participant1)) {
                            otherUser = participant2;
                            otherPartName = par2Name;
                        } else {
                            otherUser = participant1;
                            otherPartName = par1Name;
                        }

                        String chatTitle = items.get(clickedPosition).getJobTitle();

                        openMConversationFragment(otherUser, otherPartName, chatTitle);

                    }
                });


            }
        });

        holder.openSchedSetter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int visibility = holder.linearLayout.getVisibility();

                if (visibility == View.GONE) {
                    holder.linearLayout.setVisibility(View.VISIBLE);
                    holder.openSchedSetter.setText(R.string.done);
                } else {
                    holder.linearLayout.setVisibility(View.GONE);
                    holder.openSchedSetter.setText(R.string.set_schedule);
                }
            }
        });

        holder.setSchedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int itemPosition = holder.getBindingAdapterPosition();

                String assignedUserID = items.get(itemPosition).getAssignedUserID();
                String jobID = items.get(itemPosition).getDocumentId();

                if (assignedUserID == null) {
                    Toast.makeText(context, "No assigned user", Toast.LENGTH_SHORT).show();
                    return;
                }

                String spinnerDay = holder.spinner.getSelectedItem().toString();

                Map<String, Object> Day = new HashMap<>();
                Day.put("schedule", spinnerDay);

                DocumentReference documentReference = fStore.collection("users")
                        .document(currentUserID)
                        .collection("postedJobs")
                        .document(items.get(itemPosition).getDocumentId());

                documentReference.update(Day).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        Map<String, Object> assignedUser = new HashMap<>();
                        assignedUser.put("schedule", spinnerDay);

                        DocumentReference documentReference1 = fStore.collection("users")
                                .document(assignedUserID)
                                .collection("assignedJobs")
                                .document(jobID);

                        documentReference1.update(assignedUser).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                holder.assignedUserSched.setText(spinnerDay);
                                Toast.makeText(context, "Schedule has been set", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, "Network Error", Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Network Error", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                holder.progressBar.setVisibility(View.VISIBLE);
                holder.deleteBtn.setEnabled(false);
                holder.finishBtn.setEnabled(false);
                holder.chatBtn.setEnabled(false);
                holder.reminderBtn.setEnabled(false);
                holder.openSchedSetter.setEnabled(false);

                int itemPosition = holder.getBindingAdapterPosition();

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Confirm Delete Job");
                builder.setMessage("Are you sure you want to delete this job?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        DocumentReference documentReference = fStore.collection("users")
                                .document(currentUserID)
                                .collection("postedJobs")
                                .document(items.get(itemPosition).getDocumentId());

                        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {

                                String assignedUserName = documentSnapshot.getString("assignedUser");
                                String assignedUserID = documentSnapshot.getString("assignedUserID");
                                String assignedJob = documentSnapshot.getString("jobId");

                                if (assignedUserName != null && assignedUserID != null && assignedJob != null) {

                                    // add confirmation message here

                                    AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
                                    builder1.setTitle("Confirm Cancellation of Job");
                                    builder1.setMessage("A user is currently assigned to this job, deleting this job" +
                                            " may result to a bad rating.\n\nAre you sure you want to delete this job?");
                                    builder1.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            fStore.collection("users")
                                                    .document(assignedUserID)
                                                    .collection("assignedJobs")
                                                    .document(assignedJob).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {

                                                            Toast.makeText(context, "Job has been cancelled", Toast.LENGTH_SHORT).show();
                                                            addToHistory(currentUserID, assignedUserID, itemPosition, false,
                                                                    holder.progressBar, holder.deleteBtn, holder.finishBtn, holder.chatBtn, holder.reminderBtn,
                                                                    holder.openSchedSetter);

                                                        }
                                                    });
                                        }
                                    });
                                    builder1.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();

                                            holder.progressBar.setVisibility(View.GONE);
                                            holder.deleteBtn.setEnabled(true);
                                            holder.finishBtn.setEnabled(true);
                                            holder.chatBtn.setEnabled(true);
                                            holder.reminderBtn.setEnabled(true);
                                            holder.openSchedSetter.setEnabled(true);

                                        }
                                    });

                                    AlertDialog alertDialog = builder1.create();

                                    alertDialog.setCanceledOnTouchOutside(false);

                                    alertDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                                        @Override
                                        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                                            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                                                // Back button was pressed

                                                dialog.dismiss();
                                                holder.progressBar.setVisibility(View.GONE);
                                                holder.deleteBtn.setEnabled(true);
                                                holder.finishBtn.setEnabled(true);
                                                holder.chatBtn.setEnabled(true);
                                                holder.reminderBtn.setEnabled(true);
                                                holder.openSchedSetter.setEnabled(true);

                                                return true; // Consume the event
                                            }
                                            return false; // Let the dialog handle other key events
                                        }
                                    });

                                    alertDialog.show();

                                } else {

                                    deleteJob(itemPosition, holder.progressBar, holder.deleteBtn, holder.finishBtn, holder.chatBtn, holder.reminderBtn, holder.openSchedSetter);
                                    Toast.makeText(context, "Job has been deleted", Toast.LENGTH_SHORT).show();

                                }

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show();

                                holder.progressBar.setVisibility(View.GONE);
                                holder.deleteBtn.setEnabled(true);
                                holder.finishBtn.setEnabled(true);
                                holder.chatBtn.setEnabled(true);
                                holder.reminderBtn.setEnabled(true);
                                holder.openSchedSetter.setEnabled(true);

                            }
                        });

                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();

                        holder.progressBar.setVisibility(View.GONE);
                        holder.deleteBtn.setEnabled(true);
                        holder.finishBtn.setEnabled(true);
                        holder.chatBtn.setEnabled(true);
                        holder.reminderBtn.setEnabled(true);
                        holder.openSchedSetter.setEnabled(true);

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
                            holder.deleteBtn.setEnabled(true);
                            holder.finishBtn.setEnabled(true);
                            holder.chatBtn.setEnabled(true);
                            holder.reminderBtn.setEnabled(true);
                            holder.openSchedSetter.setEnabled(true);

                            return true; // Consume the event
                        }
                        return false; // Let the dialog handle other key events
                    }
                });

                dialog.show();
            }
        });

        holder.finishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // finish the job, + rep to the currentUser and assignedUser

                holder.progressBar.setVisibility(View.VISIBLE);
                holder.deleteBtn.setEnabled(false);
                holder.finishBtn.setEnabled(false);
                holder.chatBtn.setEnabled(false);
                holder.reminderBtn.setEnabled(false);
                holder.openSchedSetter.setEnabled(false);

                int position = holder.getBindingAdapterPosition();

                String assignedUserID = items.get(position).getAssignedUserID();

                if (assignedUserID == null) {
                    Toast.makeText(context, "No assigned user", Toast.LENGTH_SHORT).show();
                    holder.progressBar.setVisibility(View.GONE);
                    holder.deleteBtn.setEnabled(true);
                    holder.finishBtn.setEnabled(true);
                    holder.chatBtn.setEnabled(true);
                    holder.reminderBtn.setEnabled(true);
                    holder.openSchedSetter.setEnabled(true);
                    return;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Confirm Job Completion");
                builder.setMessage("Are you sure that this job has been completed by the assigned user?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        DocumentReference documentReference = fStore.collection("users")
                                .document(currentUserID)
                                .collection("postedJobs")
                                .document(items.get(position).getDocumentId());

                        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                String assignedUserID = documentSnapshot.getString("assignedUserID");
                                String assignedJobID = documentSnapshot.getString("jobId");

                                if (assignedUserID != null && assignedJobID != null) {
                                    fStore.collection("users")
                                            .document(assignedUserID)
                                            .collection("assignedJobs")
                                            .document(assignedJobID).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    Toast.makeText(context, "Job has been completed", Toast.LENGTH_SHORT).show();
                                                    addToHistory(currentUserID, assignedUserID, position, true,
                                                            holder.progressBar, holder.deleteBtn, holder.finishBtn, holder.chatBtn, holder.reminderBtn,
                                                            holder.openSchedSetter);

                                                }
                                            });
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show();

                                holder.progressBar.setVisibility(View.GONE);
                                holder.deleteBtn.setEnabled(true);
                                holder.finishBtn.setEnabled(true);
                                holder.chatBtn.setEnabled(true);
                                holder.reminderBtn.setEnabled(true);
                                holder.openSchedSetter.setEnabled(true);

                            }
                        });

                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();

                        holder.progressBar.setVisibility(View.GONE);
                        holder.deleteBtn.setEnabled(true);
                        holder.finishBtn.setEnabled(true);
                        holder.chatBtn.setEnabled(true);
                        holder.reminderBtn.setEnabled(true);
                        holder.openSchedSetter.setEnabled(true);

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
                            holder.deleteBtn.setEnabled(true);
                            holder.finishBtn.setEnabled(true);
                            holder.chatBtn.setEnabled(true);
                            holder.reminderBtn.setEnabled(true);
                            holder.openSchedSetter.setEnabled(true);

                            return true; // Consume the event
                        }
                        return false; // Let the dialog handle other key events
                    }
                });

                dialog.show();
            }
        });

        holder.reminderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int itemPosition = holder.getBindingAdapterPosition();
                String assignedUserID = items.get(itemPosition).getAssignedUserID();
                String jobTitle = items.get(itemPosition).getJobTitle();

                if (assignedUserID == null) {
                    Toast.makeText(context, "No assigned user", Toast.LENGTH_SHORT).show();
                    return;
                }

                DocumentReference documentReference = fStore.collection("users")
                        .document(currentUserID);

                documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String fName = documentSnapshot.getString("fName");
                        String lName = documentSnapshot.getString("lName");
                        name = fName +" "+lName;

                        String message = name + " sent you a reminder for JOB: "+ jobTitle.toUpperCase();

                        sendNotification("Reminder", message, assignedUserID);

                        Toast.makeText(context, "Reminder has been sent", Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void deleteJob(int itemPosition, View progressBar, View deleteBtn, View finishBtn, View chatBtn, View reminderBtn, View openSchedSetter) {

        fStore.collection("users").document(Objects.requireNonNull(auth.getCurrentUser()).getUid())
                .collection("postedJobs").document(items.get(itemPosition).getDocumentId()).delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            items.remove(items.get(itemPosition));
                            notifyDataSetChanged();
                            progressBar.setVisibility(View.GONE);
                            deleteBtn.setEnabled(true);
                            finishBtn.setEnabled(true);
                            chatBtn.setEnabled(true);
                            reminderBtn.setEnabled(true);
                            openSchedSetter.setEnabled(true);
                        } else {
                            Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private String generateChatDocumentID(String participant1, String participant2) {
        List<String> participants = new ArrayList<>();
        participants.add(participant1);
        participants.add(participant2);
        Collections.sort(participants);
        return participants.get(0) + "_" + participants.get(1);
    }

    private void openMConversationFragment(String otherUser, String participant2Name, String chatJobTitle) {
        FragmentManager fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // Create an instance of ConversationFragment and set arguments if needed
        MConversationFragment MconversationFragment = new MConversationFragment();
        Bundle args = new Bundle();
        args.putString("otherUser", otherUser);
        args.putString("participant2Name", participant2Name);
        args.putString("chatJobTitle", chatJobTitle);
        MconversationFragment.setArguments(args);

        // Replace the current fragment with the MConversationFragment
        transaction.replace(R.id.fragment_container, MconversationFragment); // Replace with your container ID
        transaction.addToBackStack(null);
        transaction.commit();
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

    public void increaseRep(String currentUserID, String assignedUserID) { //this increases the rep of current user and assigned user

        DocumentReference documentReference = fStore.collection("users").document(currentUserID);
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                Long CurrentUserRep = documentSnapshot.getLong("reputation");

                if (CurrentUserRep != null) {
                    int currentUserRep = CurrentUserRep.intValue();

                    currentUserRep = currentUserRep + 10;

                    Map<String, Object> updateRep = new HashMap<>();
                    updateRep.put("reputation", currentUserRep);

                    documentReference.update(updateRep).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(context, "Reputation increased by 10 points", Toast.LENGTH_SHORT).show();
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

        DocumentReference documentReference1 = fStore.collection("users").document(assignedUserID);
        documentReference1.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Long AssignedUserRep = documentSnapshot.getLong("reputation");

                if (AssignedUserRep != null) {
                    int assignedUserRep = AssignedUserRep.intValue();

                    assignedUserRep = assignedUserRep + 10;

                    Map<String, Object> updateRep = new HashMap<>();
                    updateRep.put("reputation", assignedUserRep);

                    documentReference1.update(updateRep).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }

    public void addToHistory(String currentUserID, String assignedUserID, int clickedPosition, boolean success,
                             View progressBar, View deleteBtn, View finishBtn, View chatBtn, View reminderBtn, View openSchedSetter) { //add the transaction into the firestore

        String jobID = items.get(clickedPosition).getDocumentId();
        // get current date here
        String recruiterName = items.get(clickedPosition).getCurrentUserName();
        String applicantName = items.get(clickedPosition).getAssignedUserName();
        String jobTitle = items.get(clickedPosition).getJobTitle();
        String status;

        if (success) {
            status = "COMPLETED";
        } else {
            status = "CANCELED";
        }

        String currentDate = getCurrentDate();

        Map<String, Object> historyItem = new HashMap<>();
        historyItem.put("recruiterID", currentUserID);
        historyItem.put("applicantID", assignedUserID);
        historyItem.put("canceledBy", currentUserID);
        historyItem.put("jobID", jobID);
        historyItem.put("jobTitle", jobTitle);
        historyItem.put("recruiterName", recruiterName);
        historyItem.put("applicantName", applicantName);
        historyItem.put("status", status);
        historyItem.put("timestamp", FieldValue.serverTimestamp());
        historyItem.put("date", currentDate);

        CollectionReference collectionReference = fStore.collection("users")
                .document(currentUserID)
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
                        deleteJob(clickedPosition, progressBar, deleteBtn, finishBtn, chatBtn, reminderBtn, openSchedSetter);
                        if (success) {
                            increaseRep(currentUserID, assignedUserID);
                            String notificationTitle = "Assigned Job";
                            String message = "JOB: " + jobTitle.toUpperCase() + " has been completed.";
                            sendNotification(notificationTitle, message, assignedUserID);
                        } else {
                            reduceRep(currentUserID);
                            String notificationTitle = "Assigned Job";
                            String message = "JOB: " + jobTitle.toUpperCase() + " has been canceled.";
                            sendNotification(notificationTitle, message, assignedUserID);
                        }
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

    public String getCurrentDate() {

        String currentDate;

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; // Month starts from 0
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        return currentDate = month + "-" + day + "-" + year;

    }

}
