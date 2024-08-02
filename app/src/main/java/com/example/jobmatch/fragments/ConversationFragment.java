package com.example.jobmatch.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

public class ConversationFragment extends Fragment {

    FirebaseAuth auth;
    FirebaseUser currentUser;
    FirebaseFirestore fStore;
    String userID, senderName;

    RecyclerView recyclerView;
    ImageView backBtn, sendBtn;
    TextView conversationTitle, conversationTitle1;
    TextInputEditText chatMessageEdt;
    String jobID, posterUserID, posterName, chatTitle;
    ProgressBar progressBar;

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

    private void initRecyclerView(View rootView) {
        recyclerView = rootView.findViewById(R.id.chatRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_conversation, container, false);


        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);


        initFirebase();
        initRecyclerView(rootView);

        backBtn = rootView.findViewById(R.id.backBtn_convo);
        sendBtn = rootView.findViewById(R.id.send_btn);
        conversationTitle = rootView.findViewById(R.id.convo_title);
        conversationTitle1 = rootView.findViewById(R.id.convo_title1);
        chatMessageEdt = rootView.findViewById(R.id.message_input);
        progressBar = rootView.findViewById(R.id.convoProgressbar);

        Bundle args = getArguments();
        if (args != null) {
            chatTitle = args.getString("chatTitle");
            posterName = args.getString("PosterName");
            posterUserID = args.getString("posterUserID");
            jobID = args.getString("jobID");



            if (chatTitle != null && posterName != null) {
                conversationTitle1.setText(posterName);
                conversationTitle.setText(chatTitle.toUpperCase());
            }
        }

        backBtn.setOnClickListener(view -> {
            chatMessageEdt.onEditorAction(EditorInfo.IME_ACTION_DONE);
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        sendBtn.setOnClickListener(view -> {

            if (userID.equals(posterUserID)){
                Toast.makeText(getContext(), "You can't message yourself", Toast.LENGTH_SHORT).show();
                return;
            }

            String messageText = String.valueOf(chatMessageEdt.getText());

            if (TextUtils.isEmpty(messageText)){
                return;
            }

            sendBtn.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);

            if (!messageText.isEmpty()) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                CollectionReference chatsCollection = db.collection("chats");

                String chatDocumentID = generateChatDocumentID(posterUserID, userID);
                DocumentReference chatDocumentRef = chatsCollection.document(chatDocumentID);

                chatDocumentRef.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            sendMessage(chatDocumentRef, messageText, chatTitle);
                            sendNotification(senderName, messageText, posterUserID);
                        } else {
                            createNewChatDocument(chatDocumentRef, messageText);
                            sendNotification(senderName, messageText, posterUserID);
                        }
                    } else {
                        Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        sendBtn.setVisibility(View.VISIBLE);
                    }
                });
            }
        });

        return rootView;
    }

    String receiverToken;

    public void sendNotification(String senderName, String message, String posterUserID) {

        DocumentReference documentReference = fStore.collection("users").document(posterUserID);

        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                receiverToken = documentSnapshot.getString("token");

                if (!senderName.equals("")&&!message.equals("")&&!posterUserID.equals("")){

                    if (receiverToken == null){
                        return;
                    }

                    sendNotification.pushNotification(getContext(), receiverToken, senderName, message);

                }

            }
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
                                chatMessageEdt.setText("");
                                progressBar.setVisibility(View.GONE);
                                sendBtn.setVisibility(View.VISIBLE);
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
        sendBtn.setVisibility(View.VISIBLE);
    }

    private void createNewChatDocument(DocumentReference chatDocumentRef, String initialMessage) {

        boolean par1Hide = false;
        boolean par2Hide = false;


        Map<String, Object> participants = new HashMap<>();

        participants.put("jobID", jobID);
        participants.put("participant1", posterUserID);
        participants.put("participant2", userID);
        participants.put("par1Name", posterName);
        participants.put("par2Name", senderName);
        participants.put("chatTitle", chatTitle);
        participants.put("par1Hide", par1Hide);
        participants.put("par2Hide", par2Hide);


        chatDocumentRef.set(participants)
                .addOnSuccessListener(aVoid -> {
                    sendMessage(chatDocumentRef, initialMessage, chatTitle);
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                });
    }

    private String generateChatDocumentID(String participant1, String participant2) {
        List<String> participants = new ArrayList<>();
        participants.add(participant1);
        participants.add(participant2);
        Collections.sort(participants);
        return participants.get(0) + "_" + participants.get(1);
    }

    private void fetchAndDisplayMessages(ChatMessageAdapter adapter) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String chatDocumentID = generateChatDocumentID(posterUserID, userID);
        CollectionReference messagesCollection = db.collection("chats").document(chatDocumentID).collection("messages");

        messagesCollection.orderBy("timestamp")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        // Handle error
                        return;
                    }

                    List<ChatMessageHandler> messagesList = new ArrayList<>();
                    assert queryDocumentSnapshots != null;
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String senderName = documentSnapshot.getString("senderName");
                        String messageText = documentSnapshot.getString("message");
                        String senderUID = documentSnapshot.getString("sentBy");
                        Timestamp timestamp = documentSnapshot.getTimestamp("timestamp");

                        ChatMessageHandler message = new ChatMessageHandler(senderName, messageText);
                        message.setSenderUID(senderUID);
                        message.setTimestamp(timestamp);
                        messagesList.add(message);
                    }

                    adapter.updateData(messagesList);

                    // Auto-scroll to the latest message
                    recyclerView.scrollToPosition(messagesList.size() - 1);
                });
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initRecyclerView(view);
        ChatMessageAdapter adapter = new ChatMessageAdapter(requireContext(), new ArrayList<>(), userID);
        recyclerView.setAdapter(adapter);

        fetchAndDisplayMessages(adapter);

    }
}
