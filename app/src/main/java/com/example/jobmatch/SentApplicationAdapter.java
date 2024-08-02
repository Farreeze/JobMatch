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

import java.text.DecimalFormat;
import java.util.List;

public class SentApplicationAdapter extends RecyclerView.Adapter<SentApplicationHolder> {

    Context context;
    List<SentApplicationHandler> items;

    FirebaseAuth auth;
    FirebaseFirestore fStore;

    FirebaseUser currentUser;

    String currentUserID;

    public SentApplicationAdapter(Context context, List<SentApplicationHandler> items) {
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
    public SentApplicationHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SentApplicationHolder(LayoutInflater.from(context).inflate(R.layout.sent_application_model, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SentApplicationHolder holder, int position) {
        holder.appliedJobTitle.setText(items.get(position).getAppliedJobTitle().toUpperCase());
        holder.posterName.setText(items.get(position).getPosterName());

        double rating = items.get(position).getRecruiterRep();

        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        String formattedDouble = decimalFormat.format(rating);
        double finalRate = Double.parseDouble(formattedDouble);

        holder.recruiterRep.setText(String.valueOf(finalRate));
        holder.jobDescription.setText(items.get(position).getJobDescription());
        holder.jobRate.setText(items.get(position).getJobRate());

        int numRate = items.get(position).getNumberOfRating();

        String fNumRate = "("+numRate+")";

        holder.numberOfRating.setText(fNumRate);

        holder.cancelApplicationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // delete the application to both users

                holder.progressBar.setVisibility(View.VISIBLE);
                holder.cancelApplicationBtn.setEnabled(false);

                int clickedItem = holder.getBindingAdapterPosition();

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Confirm Cancellation");
                builder.setMessage("Are you sure you want to cancel this application?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // cancel the application

                        cancelApplication(clickedItem, holder.progressBar, holder.cancelApplicationBtn);

                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();

                        holder.progressBar.setVisibility(View.GONE);
                        holder.cancelApplicationBtn.setEnabled(true);

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
                            holder.cancelApplicationBtn.setEnabled(true);

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

    public void cancelApplication(int clickedItem, View progressBar, View cancelBtn) {

        DocumentReference documentReference = fStore.collection("users")
                .document(currentUserID)
                .collection("sentApplications")
                .document(items.get(clickedItem).getDocumentID());

        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                String jobPosterUID = documentSnapshot.getString("jobPosterUID");
                String applicationID = documentSnapshot.getString("ApplicationID");

                fStore.collection("users")
                        .document(currentUserID)
                        .collection("sentApplications")
                        .document(items.get(clickedItem).getDocumentID())
                        .delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    assert jobPosterUID != null;
                                    assert applicationID != null;
                                    fStore.collection("users")
                                            .document(jobPosterUID)
                                            .collection("applicants")
                                            .document(applicationID)
                                            .delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @SuppressLint("NotifyDataSetChanged")
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()){
                                                        items.remove(items.get(clickedItem));
                                                        notifyDataSetChanged();
                                                        progressBar.setVisibility(View.GONE);
                                                        cancelBtn.setEnabled(true);
                                                        Toast.makeText(context, "Application cancelled", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Toast.makeText(context, "Failed to cancel application, try again later", Toast.LENGTH_SHORT).show();
                                                    }

                                                }

                                            });

                                } else {
                                    Toast.makeText(context, "Failed to cancel application, try again later", Toast.LENGTH_SHORT).show();
                                }

                            }

                        });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Failed to cancel application, try again later", Toast.LENGTH_SHORT).show();
            }
        });

    }

}
