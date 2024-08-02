package com.example.jobmatch;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryHolder> {

    Context context;
    List<HistoryHandler> items;

    FirebaseAuth auth;
    FirebaseFirestore fStore;
    FirebaseUser currentUser;

    String currentUserID;

    public HistoryAdapter(Context context, List<HistoryHandler> items) {
        this.context = context;
        this.items = items;
        this.auth = FirebaseAuth.getInstance();
        this.fStore = FirebaseFirestore.getInstance();
        this.currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            this.currentUserID = currentUser.getUid();
        }

    }

    @NonNull
    @Override
    public HistoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HistoryHolder(LayoutInflater.from(context).inflate(R.layout.history_model, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryHolder holder, int position) {
        //create logic to set text
        holder.date.setText(items.get(position).getDate());
        holder.recruiterName.setText(items.get(position).getRecruiterName());
        holder.applicantName.setText(items.get(position).getApplicantName());
        holder.jobTitle.setText(items.get(position).getJobTitle().toUpperCase());
        holder.status.setText(items.get(position).getStatus());

        holder.openRateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.ratingLayout.getVisibility() == View.GONE){
                    holder.ratingLayout.setVisibility(View.VISIBLE);
                    holder.openRateBtn.setText(R.string.close);

                    int clickedItem = holder.getBindingAdapterPosition();

                    DocumentReference documentReference = fStore.collection("users")
                            .document(currentUserID)
                            .collection("history")
                            .document(items.get(clickedItem).getDocumentID());

                    documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            Double rating = documentSnapshot.getDouble(currentUserID+" rate");

                            if (rating != null){
                                DecimalFormat decimalFormat = new DecimalFormat("#.##");
                                String formattedDouble = decimalFormat.format(rating);
                                double finalRate = Double.parseDouble(formattedDouble);
                                holder.ratingBar.setRating((float) finalRate);
                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });

                } else {
                    holder.ratingLayout.setVisibility(View.GONE);
                    holder.openRateBtn.setText(R.string.rate);
                }
            }
        });

        holder.giveRateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int clickedItem = holder.getBindingAdapterPosition();

                double rating = holder.ratingBar.getRating();

                if (rating == 0) {
                    Toast.makeText(context, "Select at least 1 star", Toast.LENGTH_SHORT).show();
                    return;
                }

                //give the rating to the other user here...
                DocumentReference documentReference = fStore.collection("users")
                        .document(currentUserID)
                        .collection("history").document(items.get(clickedItem).getDocumentID());

                documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        boolean rated = Boolean.TRUE.equals(documentSnapshot.getBoolean(currentUserID));

                        String recruiterID = documentSnapshot.getString("recruiterID");
                        String applicantID = documentSnapshot.getString("applicantID");
                        String otherUser, currentRater, currentUserName;

                        if (Objects.equals(currentUserID, recruiterID)) {
                            otherUser = applicantID;
                            currentUserName = documentSnapshot.getString("recruiterName");
                            currentRater = recruiterID;
                        } else {
                            otherUser = recruiterID;
                            currentUserName = documentSnapshot.getString("applicantName");
                            currentRater = applicantID;
                        }

                        if (rated) {
                            Toast.makeText(context, "You already rated this user", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (otherUser != null) {
                            DocumentReference documentReference1 = fStore.collection("users")
                                    .document(otherUser);
                            documentReference1.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    double currentAverageRating = Double.parseDouble(String.valueOf(documentSnapshot.getDouble("currentAverageRating")));
                                    int numberTotalRating = Integer.parseInt(String.valueOf(documentSnapshot.getLong("totalNumberRatings")));

                                    double updatedRating = updateAverage(currentAverageRating, numberTotalRating, rating);

                                    numberTotalRating += 1;

                                    Map<String, Object> updates = new HashMap<>();
                                    updates.put("currentAverageRating", updatedRating);
                                    updates.put("totalNumberRatings", numberTotalRating);

                                    documentReference1.update(updates).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {

                                            Map<String, Object> insertBooleanRated = new HashMap<>();
                                            insertBooleanRated.put(currentRater, true);
                                            insertBooleanRated.put(currentRater+" rate", rating);
                                            documentReference.update(insertBooleanRated).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    sendNotification("User Feedback",
                                                            currentUserName+" gave you a "+rating+" star rating.",
                                                                    otherUser);
                                                    Toast.makeText(context, "Rating Submitted", Toast.LENGTH_SHORT).show();
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
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(context, "Network Error", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Network Error", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        /*
        String canceledBy = items.get(position).getCanceledBy();
        if (Objects.equals(canceledBy, currentUserID) && Objects.equals(items.get(position).getStatus(), "CANCELED")){
            holder.minusRep.setVisibility(View.VISIBLE);
            holder.plusRep.setVisibility(View.GONE);
        } else if (!Objects.equals(items.get(position).getStatus(), "CANCELED")){
            holder.minusRep.setVisibility(View.GONE);
            holder.plusRep.setVisibility(View.VISIBLE);
        } else {
            holder.minusRep.setVisibility(View.GONE);
            holder.plusRep.setVisibility(View.GONE);
        }
        */

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static double updateAverage(double currentAverage, int totalRatings, double newRating) {
        if (totalRatings == 0) {
            return newRating;
        }
        return ((currentAverage * totalRatings) + newRating) / (totalRatings + 1);
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
