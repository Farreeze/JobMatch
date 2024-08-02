package com.example.jobmatch;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;

import java.util.List;
import java.util.Objects;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageHolder> {

    private Context context;
    private List<ChatMessageHandler> items;
    private String currentUserId;  // Store the ID of the current user

    public ChatMessageAdapter(Context context, List<ChatMessageHandler> items, String currentUserId) {
        this.context = context;
        this.items = items;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public ChatMessageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ChatMessageHolder(LayoutInflater.from(context).inflate(R.layout.chat_message_model, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ChatMessageHolder holder, int position) {
        ChatMessageHandler message = items.get(position);

        // Display the sender's name based on the information stored in the ChatMessageHandler object
        holder.nameOfSender.setText(message.getNameOfSender());

        holder.chatMessage.setText(message.getChatMessage());

        Timestamp currentTime = Timestamp.now();
        Timestamp messageTimestamp = message.getTimestamp();

        if (messageTimestamp == null) {
            return;
        }

        long differenceInMillis = currentTime.toDate().getTime() - messageTimestamp.toDate().getTime();

        long seconds = differenceInMillis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        String timeDifference;

        if (days > 0) {
            timeDifference = days + " day" + (days > 1 ? "s" : "") + " ago";
        } else if (hours > 0) {
            timeDifference = hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        } else if (minutes > 0) {
            timeDifference = minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
        } else {
            timeDifference = seconds + " second" + (seconds > 1 ? "s" : "") + " ago";
        }

        holder.chatTime.setText(timeDifference);

        String senderUID = message.getSenderUID();

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) holder.nameOfSender.getLayoutParams();
        RelativeLayout.LayoutParams layoutParams1 = (RelativeLayout.LayoutParams) holder.chatMessage.getLayoutParams();
        RelativeLayout.LayoutParams layoutParams2 = (RelativeLayout.LayoutParams) holder.chatTime.getLayoutParams();


        layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_END);
        layoutParams1.removeRule(RelativeLayout.ALIGN_PARENT_END);
        layoutParams2.removeRule(RelativeLayout.ALIGN_PARENT_START);



        if (Objects.equals(senderUID, currentUserId)) {
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);
            layoutParams1.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);
            layoutParams2.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
            holder.messageType.setBackgroundResource(R.drawable.sent_message_background);
        } else {
            holder.messageType.setBackgroundResource(R.drawable.received_message_background);
        }

        holder.nameOfSender.setLayoutParams(layoutParams);
        holder.chatMessage.setLayoutParams(layoutParams1);
        holder.chatTime.setLayoutParams(layoutParams2);

    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<ChatMessageHandler> updatedList) {
        items.clear();
        items.addAll(updatedList);
        notifyDataSetChanged();
    }

    public String getChatTime() {



        String chatTime = null;

        return chatTime;
    }

}
