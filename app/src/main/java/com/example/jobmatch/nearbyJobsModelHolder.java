package com.example.jobmatch;

import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class nearbyJobsModelHolder extends RecyclerView.ViewHolder {

    TextView jobTitle, jobDescription, jobRate, distance, posterName, posterRep, jobCategory,
    dateRange, jobType, numberOfWorker, numberOfRating;

    Button chatBtn, applyBtn;

    ProgressBar nearbyJobsModelPb;



    public nearbyJobsModelHolder(@NonNull View itemView) {
        super(itemView);

        jobTitle = itemView.findViewById(R.id.nJobTitle_tv);
        jobDescription = itemView.findViewById(R.id.nJobDescription_tv);
        jobRate = itemView.findViewById(R.id.nJobRate_tv);
        posterName = itemView.findViewById(R.id.posterName_tv);
        posterRep = itemView.findViewById(R.id.posterRep_tv);
        distance = itemView.findViewById(R.id.distance_tv);
        chatBtn = itemView.findViewById(R.id.chat_button);
        applyBtn = itemView.findViewById(R.id.apply_button);
        nearbyJobsModelPb = itemView.findViewById(R.id.nearbyJobsModel_pb);
        jobCategory = itemView.findViewById(R.id.nJobCateg);
        dateRange = itemView.findViewById(R.id.nDate);
        jobType = itemView.findViewById(R.id.nJobType);
        numberOfWorker = itemView.findViewById(R.id.nNumberOfWorker);
        numberOfRating = itemView.findViewById(R.id.NJNumOfRating);

    }

}
