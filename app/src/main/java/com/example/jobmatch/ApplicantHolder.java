package com.example.jobmatch;

import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ApplicantHolder extends RecyclerView.ViewHolder {

    TextView applicantName, jobTitle, applicantAge,
            applicantAddress, applicantContact, applicantEmail,
            applicantGender, applicantRep, numberOfRatings;
    Button declineButton, acceptButton;

    ProgressBar progressBar;

    RelativeLayout MrelativeLayout, relativeLayout;

    public ApplicantHolder(@NonNull View itemView) {
        super(itemView);

        applicantName = itemView.findViewById(R.id.applicant_name);
        applicantRep = itemView.findViewById(R.id.applicant_reputation);
        applicantGender = itemView.findViewById(R.id.applicant_gender);
        jobTitle = itemView.findViewById(R.id.applicant_jobTitle);
        applicantAge = itemView.findViewById(R.id.applicant_age);
        applicantAddress = itemView.findViewById(R.id.applicant_address);
        applicantContact = itemView.findViewById(R.id.applicant_contact);
        applicantEmail = itemView.findViewById(R.id.applicant_email);
        declineButton = itemView.findViewById(R.id.applicant_declineBtn);
        acceptButton = itemView.findViewById(R.id.applicant_acceptBtn);
        progressBar = itemView.findViewById(R.id.applicant_progressbar);
        MrelativeLayout = itemView.findViewById(R.id.AL_MrelativeLayout);
        relativeLayout = itemView.findViewById(R.id.AL_dropdownDetails);
        numberOfRatings = itemView.findViewById(R.id.AL_numRatings);



    }
}
