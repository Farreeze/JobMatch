package com.example.jobmatch;

import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AssignedJobHolder extends RecyclerView.ViewHolder {

    TextView recruiterName, jobTitle, jobRate, jobDescription, recruiterRep, schedule, numberOfRating;

    Button chatBtn, cancelBtn;

    ProgressBar progressBar;


    public AssignedJobHolder(@NonNull View itemView) {
        super(itemView);

        recruiterName = itemView.findViewById(R.id.recruiter_name);
        recruiterRep = itemView.findViewById(R.id.recruiter_reputation);
        jobTitle = itemView.findViewById(R.id.aj_jobTitle);
        jobRate = itemView.findViewById(R.id.aj_jobRate);
        jobDescription = itemView.findViewById(R.id.aj_jobDescription);
        numberOfRating = itemView.findViewById(R.id.AJ_numRating);
        schedule = itemView.findViewById(R.id.ajSched);

        chatBtn = itemView.findViewById(R.id.chat_poster_btn);
        cancelBtn = itemView.findViewById(R.id.aj_cancelBtn);

        progressBar = itemView.findViewById(R.id.AJ_PB);


    }
}
