package com.example.jobmatch;

import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ChatMessageHolder extends RecyclerView.ViewHolder {

    TextView nameOfSender, chatMessage, chatTime;
    RelativeLayout messageType;

    public ChatMessageHolder(@NonNull View itemView) {
        super(itemView);

        chatMessage = itemView.findViewById(R.id.chat_message);
        nameOfSender = itemView.findViewById(R.id.nameOfSender);
        chatTime = itemView.findViewById(R.id.chat_time);
        messageType = itemView.findViewById(R.id.messageType);

    }
}
