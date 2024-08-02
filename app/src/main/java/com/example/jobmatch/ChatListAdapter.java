package com.example.jobmatch;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListHolder> {

    private Context context;
    private List<ChatListHandler> items;

    FirebaseFirestore fStore;

    FirebaseAuth auth;

    FirebaseUser currentUser;

    String currentUserID;

    String par1, par2;

    public ChatListAdapter(Context context, List<ChatListHandler> items) {
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
    public ChatListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.chat_list_model, parent, false);
        return new ChatListHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatListHolder holder, int position) {
        ChatListHandler item = items.get(position);

        holder.JobTitle.setText(item.getJobTitle().toUpperCase());
        holder.name.setText(item.getName());
        holder.lastMessage.setText(item.getLastMessage());

        holder.openChat.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                holder.hideChat.setVisibility(View.VISIBLE);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Code to be executed after the delay.
                        holder.hideChat.setVisibility(View.GONE);
                    }
                }, 3000);

                return true;
            }
        });

        holder.openChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String otherUser = item.getOtherUser();
                String participant2Name = item.getName();
                String chatJobTitle = item.getJobTitle();
                openMConversationFragment(otherUser, participant2Name, chatJobTitle);
            }
        });

        holder.hideChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                holder.hideChat.setVisibility(View.GONE);

                int itemPosition = items.indexOf(item);
                holder.hideChat.setEnabled(false);
                // Create and show a confirmation dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Confirm Hide Chat");
                builder.setMessage("Are you sure you want to hide this conversation?" +
                        " \n \nConversations are automatically deleted after 30 days.");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        String CurrentUser = item.getCurrentUser();
                        String otherUser = item.getOtherUser();

                        String chatDocumentId = generateChatDocumentID(CurrentUser, otherUser);

                        DocumentReference documentReference = fStore.collection("chats").document(chatDocumentId);

                        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                par1 = documentSnapshot.getString("participant1");
                                par2 = documentSnapshot.getString("participant2");

                                if (Objects.equals(currentUserID, par1)) {
                                    updateHideField(chatDocumentId, "par1Hide", true, holder.hideChat);
                                } else if (Objects.equals(currentUserID, par2)) {
                                    updateHideField(chatDocumentId, "par2Hide", true, holder.hideChat);
                                }

                                if (itemPosition != -1){
                                    items.remove(itemPosition);
                                    notifyDataSetChanged();
                                    holder.hideChat.setEnabled(true);
                                }

                            }
                        });

                        // Close the dialog
                        dialogInterface.dismiss();
                        holder.hideChat.setEnabled(true);
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Close the dialog
                        dialogInterface.dismiss();
                        holder.hideChat.setEnabled(true);
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
                            holder.hideChat.setEnabled(true);

                            return true; // Consume the event
                        }
                        return false; // Let the dialog handle other key events
                    }
                });

                dialog.show();
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

    private void updateHideField(String chatDocumentId, String fieldToUpdate, boolean newValue, View hideBtn) {
        // Reference to the chat document
        DocumentReference chatDocumentRef = fStore.collection("chats").document(chatDocumentId);

        // Create a map to update the specific field
        Map<String, Object> updates = new HashMap<>();
        updates.put(fieldToUpdate, newValue);

        // Update the document
        chatDocumentRef.update(updates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Document updated successfully
                        hideBtn.setEnabled(true);
                        Log.d("ChatListAdapter", "Document field updated: " + fieldToUpdate);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure
                        hideBtn.setEnabled(true);
                        Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @Override
    public int getItemCount() {
        return items.size();
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

}
