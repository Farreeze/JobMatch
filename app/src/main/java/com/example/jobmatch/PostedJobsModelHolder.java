package com.example.jobmatch;

import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PostedJobsModelHolder extends RecyclerView.ViewHolder {

    TextView jobTitle, jobDescription, jobRate, assignedUserName, assignedUserEmail, assignedUserContact, assignedUserSched,
    dateRange, jobType, numberOfWorker;
    Button deleteBtn, chatBtn, finishBtn, setSchedBtn, openSchedSetter, reminderBtn;
    ProgressBar progressBar;
    Spinner spinner;
    LinearLayout linearLayout;

    RelativeLayout MrelativeLayout, relativeLayout;

    public PostedJobsModelHolder(@NonNull View itemView) {
        super(itemView);

        jobTitle = itemView.findViewById(R.id.jobTitle_tv);
        jobDescription = itemView.findViewById(R.id.jobDescription_tv);
        jobRate = itemView.findViewById(R.id.jobRate_tv);
        dateRange = itemView.findViewById(R.id.pj_dateRange);
        jobType = itemView.findViewById(R.id.pj_jobType);
        numberOfWorker = itemView.findViewById(R.id.pj_numberOfWorkers);

        assignedUserName = itemView.findViewById(R.id.assigned_user_name);
        assignedUserEmail = itemView.findViewById(R.id.assigned_user_email);
        assignedUserContact = itemView.findViewById(R.id.assigned_user_contact);
        assignedUserSched = itemView.findViewById(R.id.assigned_user_schedule);

        deleteBtn = itemView.findViewById(R.id.deleteJob_btn);
        finishBtn = itemView.findViewById(R.id.finishJob_btn);
        chatBtn = itemView.findViewById(R.id.chat_assigned_user_btn);
        setSchedBtn = itemView.findViewById(R.id.set_schedule_btn);
        openSchedSetter = itemView.findViewById(R.id.openSchedSetter);
        reminderBtn = itemView.findViewById(R.id.reminderBtn);

        linearLayout = itemView.findViewById(R.id.schedLayout);

        spinner = itemView.findViewById(R.id.daySpinner);

        progressBar = itemView.findViewById(R.id.postedJobs_progressbar);

        MrelativeLayout = itemView.findViewById(R.id.postedJobs_model);
        relativeLayout = itemView.findViewById(R.id.postedJobs_details);


    }

}
