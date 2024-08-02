package com.example.jobmatch.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobmatch.ChatMessageAdapter;
import com.example.jobmatch.ChatMessageHandler;
import com.example.jobmatch.R;
import com.example.jobmatch.sendNotification;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;

public class MConversationFragment extends Fragment {

    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private FirebaseFirestore fStore;
    private String userID, senderName;

    private RecyclerView recyclerView;
    private ImageView MbackBtn, MsendBtn;
    private TextView MconversationTitle, MconversationTitle1;
    private TextInputEditText MchatMessageEdt;
    private ProgressBar progressBar;

    private String otherUser, participant2Name, chatJobTitle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_m_conversation, container, false);

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                MchatMessageEdt.onEditorAction(EditorInfo.IME_ACTION_DONE);
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);

        initFirebase();
        initRecyclerView(rootView);

        MbackBtn = rootView.findViewById(R.id.MbackBtn_convo);
        MsendBtn = rootView.findViewById(R.id.Msend_btn);
        MconversationTitle = rootView.findViewById(R.id.Mconvo_title);
        MconversationTitle1 = rootView.findViewById(R.id.Mconvo_title1);
        MchatMessageEdt = rootView.findViewById(R.id.Mmessage_input);
        progressBar = rootView.findViewById(R.id.MconvoProgressbar);

        Bundle args = getArguments();
        if (args != null) {
            chatJobTitle = args.getString("chatJobTitle");
            otherUser = args.getString("otherUser");
            participant2Name = args.getString("participant2Name");


            if (chatJobTitle != null && participant2Name != null) {
                MconversationTitle1.setText(participant2Name);
                MconversationTitle.setText(chatJobTitle.toUpperCase());
            }
        }

        ChatMessageAdapter adapter = new ChatMessageAdapter(getContext(), new ArrayList<>(), userID);
        recyclerView.setAdapter(adapter);

        fetchAndDisplayMessages(adapter);

        MbackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MchatMessageEdt.onEditorAction(EditorInfo.IME_ACTION_DONE);
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });

        MsendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String messageText = String.valueOf(MchatMessageEdt.getText());

                if (TextUtils.isEmpty(messageText)) {
                    return;
                }

                MsendBtn.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);

                if (!messageText.isEmpty()) {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    CollectionReference chatsCollection = db.collection("chats");

                    String chatDocumentID = generateChatDocumentID(otherUser, userID);
                    DocumentReference chatDocumentRef = chatsCollection.document(chatDocumentID);

                    // Call the sendMessage method here
                    sendMessage(chatDocumentRef, messageText, chatJobTitle);

                    sendNotification(senderName, messageText, otherUser);

                }
            }
        });

        return rootView;
    }

    String receiverToken;

    public void sendNotification(String senderName, String message, String receiverUID) {

        DocumentReference documentReference = fStore.collection("users").document(receiverUID);

        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                receiverToken = documentSnapshot.getString("token");

                if (!senderName.equals("")&&!message.equals("")&&!receiverUID.equals("")){

                    if (receiverToken == null){
                        return;
                    }

                    sendNotification.pushNotification(getContext(), receiverToken, senderName, message);

                }

            }
        });

    }

    private void fetchAndDisplayMessages(ChatMessageAdapter adapter) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Query the "chats" collection
        db.collection("chats")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        // Handle error
                        return;
                    }

                    List<ChatMessageHandler> allMessages = new ArrayList<>();
                    assert queryDocumentSnapshots != null;
                    for (DocumentSnapshot chatDocument : queryDocumentSnapshots.getDocuments()) {
                        String participant1 = chatDocument.getString("participant1");
                        String participant2 = chatDocument.getString("participant2");

                        if (participant1 != null && participant2 != null) {
                            String chatDocumentID = generateChatDocumentID(participant1, participant2);
                            if (chatDocumentID.equals(generateChatDocumentID(userID, otherUser))) {
                                List<ChatMessageHandler> chatMessages = new ArrayList<>();

                                CollectionReference messagesCollection = chatDocument.getReference().collection("messages");

                                messagesCollection.orderBy("timestamp")
                                        .addSnapshotListener((messageQueryDocumentSnapshots, messageError) -> {
                                            if (messageError != null) {
                                                // Handle error
                                                return;
                                            }

                                            chatMessages.clear(); // Clear the list before adding messages

                                            assert messageQueryDocumentSnapshots != null;
                                            for (DocumentSnapshot messageDocument : messageQueryDocumentSnapshots.getDocuments()) {
                                                String senderName = messageDocument.getString("senderName");
                                                String messageText = messageDocument.getString("message");
                                                String senderUID = messageDocument.getString("sentBy");
                                                Timestamp timestamp = messageDocument.getTimestamp("timestamp");

                                                ChatMessageHandler message = new ChatMessageHandler(senderName, messageText);
                                                message.setSenderUID(senderUID);
                                                message.setTimestamp(timestamp);
                                                chatMessages.add(message);
                                            }

                                            // Update the adapter with messages from the matching chat documents
                                            adapter.updateData(chatMessages);

                                            // Auto-scroll to the latest message
                                            recyclerView.scrollToPosition(chatMessages.size() - 1);
                                        });

                                // Add chatMessages list to allMessages list
                                allMessages.addAll(chatMessages);
                            }
                        }
                    }
                    // Update the adapter with messages from all chats
                    adapter.updateData(allMessages);

                    // Auto-scroll to the latest message of all chats
                    recyclerView.scrollToPosition(allMessages.size() - 1);
                });
    }

    private void sendMessage(DocumentReference chatDocumentRef, String messageText, String chatTitle) {
        CollectionReference messagesCollection = chatDocumentRef.collection("messages");

        Map<String, Object> messageData = new HashMap<>();
        messageData.put("senderName", senderName);
        messageData.put("sentBy", userID);
        messageData.put("message", messageText);
        messageData.put("timestamp", FieldValue.serverTimestamp());

        Map<String, Object> updateFields = new HashMap<>();
        updateFields.put("chatTitle", chatTitle);
        updateFields.put("par1Hide", false); // Update par1Hide to false
        updateFields.put("par2Hide", false); // Update par2Hide to false

        chatDocumentRef.update(updateFields)
                .addOnSuccessListener(aVoid -> {
                    // Add the message data to the messages subcollection
                    messagesCollection.add(messageData)
                            .addOnSuccessListener(documentReference -> {
                                // Message added successfully
                                MchatMessageEdt.setText("");
                                progressBar.setVisibility(View.GONE);
                                MsendBtn.setVisibility(View.VISIBLE);
                            })
                            .addOnFailureListener(e -> {
                                // Handle the failure
                                handleSendMessageFailure("Failed to send message, check internet connection");
                            });
                })
                .addOnFailureListener(e -> {
                    // Handle the failure to update chatTitle
                    handleSendMessageFailure("Failed to update chat title");
                });
    }

    private void handleSendMessageFailure(String errorMessage) {
        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
        progressBar.setVisibility(View.GONE);
        MsendBtn.setVisibility(View.VISIBLE);
    }

    private String generateChatDocumentID(String participant1, String participant2) {
        if (participant1 == null || participant2 == null) {
            return ""; // Return a default value or handle the case where participants are null
        }

        List<String> participants = new ArrayList<>();
        participants.add(participant1);
        participants.add(participant2);
        Collections.sort(participants);
        return participants.get(0) + "_" + participants.get(1);
    }

    private void initRecyclerView(View rootView) {
        recyclerView = rootView.findViewById(R.id.MchatRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        fStore = FirebaseFirestore.getInstance();
        userID = currentUser.getUid();

        DocumentReference documentReference = fStore.collection("users").document(userID);

        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String firstName = documentSnapshot.getString("fName");
                String lastName = documentSnapshot.getString("lName");
                senderName = firstName + " " + lastName;
            }
        });
    }

}
