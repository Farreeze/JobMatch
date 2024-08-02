package com.example.jobmatch.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobmatch.ChatListAdapter;
import com.example.jobmatch.ChatListHandler;
import com.example.jobmatch.ChatListHolder;
import com.example.jobmatch.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ChatFragment extends Fragment {

    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private FirebaseFirestore fStore;

    private String userID;

    private RecyclerView recyclerView;
    private ChatListAdapter chatAdapter;
    private List<ChatListHandler> chatList;

    private boolean isParticipant1 = false;

    TextView noChat;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chat, container, false);


        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);


        noChat = rootView.findViewById(R.id.noChatLabel);

        initFirebase();
        initRecyclerView(rootView);
        fetchAndDisplayChatParticipants();

        return rootView;
    }

    String otherUser;

    @SuppressLint("NotifyDataSetChanged")
    private void fetchAndDisplayChatParticipants() {
        Query participant1Query = fStore.collection("chats")
                .whereEqualTo("participant1", userID);

        Query participant2Query = fStore.collection("chats")
                .whereEqualTo("participant2", userID);

        Task<QuerySnapshot> query1 = participant1Query.get();
        Task<QuerySnapshot> query2 = participant2Query.get();

        Task<List<QuerySnapshot>> allTasks = Tasks.whenAllSuccess(query1, query2);

        allTasks.addOnSuccessListener(querySnapshots -> {
            chatList.clear();
            for (QuerySnapshot querySnapshot : querySnapshots) {
                for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
                    String participant1 = documentSnapshot.getString("participant1");
                    String participant2 = documentSnapshot.getString("participant2");
                    String par1Name = documentSnapshot.getString("par1Name");
                    String par2Name = documentSnapshot.getString("par2Name");
                    String chatTitle = documentSnapshot.getString("chatTitle");
                    boolean par1Hide = Boolean.TRUE.equals(documentSnapshot.getBoolean("par1Hide"));
                    boolean par2Hide = Boolean.TRUE.equals(documentSnapshot.getBoolean("par2Hide"));

                    String otherUser;
                    String otherParticipantName;
                    boolean shouldDisplayChat = true;

                    if (userID.equals(participant1)) {
                        // Current user is participant1, check par1Hide
                        isParticipant1 = true;
                        otherUser = participant2;
                        otherParticipantName = par2Name;
                        if (par1Hide) {
                            shouldDisplayChat = false;
                        }
                    } else {
                        // Current user is participant2, check par2Hide
                        isParticipant1 = false;
                        otherUser = participant1;
                        otherParticipantName = par1Name;
                        if (par2Hide) {
                            shouldDisplayChat = false;
                        }
                    }

                    if (!shouldDisplayChat) {
                        // Skip this chat as it's hidden for the current user
                        continue;
                    }

                    // Get the last message from messages subcollection and sort by timestamp
                    Query messagesQuery = documentSnapshot.getReference().collection("messages")
                            .orderBy("timestamp", Query.Direction.DESCENDING).limit(1);

                    messagesQuery.get().addOnSuccessListener(messagesQuerySnapshot -> {
                        if (!messagesQuerySnapshot.isEmpty()) {
                            noChat.setVisibility(View.GONE);
                            DocumentSnapshot lastMessageSnapshot = messagesQuerySnapshot.getDocuments().get(0);
                            String lastMessage = lastMessageSnapshot.getString("message");
                            Date lastMessageTimestamp = lastMessageSnapshot.getDate("timestamp");

                            ChatListHandler chatListItem = new ChatListHandler();
                            chatListItem.setParticipant1(isParticipant1);
                            chatListItem.setCurrentUser(userID);
                            chatListItem.setOtherUser(otherUser);
                            chatListItem.setName(otherParticipantName);
                            chatListItem.setJobTitle(chatTitle);
                            chatListItem.setLastMessage(lastMessage);
                            chatListItem.setLastMessageTimestamp(lastMessageTimestamp);

                            chatList.add(chatListItem);
                            // Sort the chatList based on lastMessageTimestamp
                            chatList.sort((o1, o2) -> o2.getLastMessageTimestamp().compareTo(o1.getLastMessageTimestamp()));
                            chatAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.getSupportActionBar().setTitle("Chats");
        }
    }

    private void initRecyclerView(View rootView) {
        recyclerView = rootView.findViewById(R.id.chatListRecyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        chatList = new ArrayList<>();
        chatAdapter = new ChatListAdapter(getContext(), chatList);
        recyclerView.setAdapter(chatAdapter);
    }

    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        fStore = FirebaseFirestore.getInstance();
        userID = currentUser.getUid();
    }
}
