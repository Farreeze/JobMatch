package com.example.jobmatch;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ChatListHolder extends RecyclerView.ViewHolder {

    TextView JobTitle, name, lastMessage;
    ImageView hideChat;
    RelativeLayout openChat;

    public ChatListHolder(@NonNull View itemView) {
        super(itemView);

        name = itemView.findViewById(R.id.NameLabel);
        JobTitle = itemView.findViewById(R.id.JobLabel);
        lastMessage = itemView.findViewById(R.id.MessageLabel);
        hideChat = itemView.findViewById(R.id.hideChatBtn);
        openChat = itemView.findViewById(R.id.chatItem);
    }
}
