package com.example.jobmatch;

import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class HistoryHolder extends RecyclerView.ViewHolder {

    TextView jobTitle, recruiterName, applicantName, status, date, plusRep, minusRep;

    LinearLayout ratingLayout;

    Button openRateBtn, giveRateBtn;

    RatingBar ratingBar;

    public HistoryHolder(@NonNull View itemView) {
        super(itemView);

        date = itemView.findViewById(R.id.date);
        recruiterName = itemView.findViewById(R.id.HRecruiter_name);
        applicantName = itemView.findViewById(R.id.HApplicant_name);
        jobTitle = itemView.findViewById(R.id.HjobTitle);
        status = itemView.findViewById(R.id.HStatus);

        plusRep = itemView.findViewById(R.id.plus10Label);
        minusRep = itemView.findViewById(R.id.minus10Label);

        ratingLayout = itemView.findViewById(R.id.ratingLayout);

        openRateBtn = itemView.findViewById(R.id.OpenRateBtn);

        giveRateBtn = itemView.findViewById(R.id.giveRateBtn);

        ratingBar = itemView.findViewById(R.id.RatingStars);

    }

}
