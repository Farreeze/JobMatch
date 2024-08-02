package com.example.jobmatch;

import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SentApplicationHolder extends RecyclerView.ViewHolder {

    TextView posterName, appliedJobTitle, jobDescription, jobRate, recruiterRep, numberOfRating;

    Button cancelApplicationBtn;

    ProgressBar progressBar;

    public SentApplicationHolder(@NonNull View itemView) {
        super(itemView);

        posterName = itemView.findViewById(R.id.SA_posterName);
        appliedJobTitle = itemView.findViewById(R.id.SA_jobTitle);
        jobDescription = itemView.findViewById(R.id.SA_jobDescription);
        jobRate = itemView.findViewById(R.id.SA_jobRate);
        recruiterRep = itemView.findViewById(R.id.SA_reputation);
        numberOfRating = itemView.findViewById(R.id.SA_numRating);

        cancelApplicationBtn = itemView.findViewById(R.id.SA_cancelButton);

        progressBar = itemView.findViewById(R.id.sentApplication_progressbar);

    }

}
