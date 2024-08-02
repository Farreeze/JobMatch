package com.example.jobmatch;

public class SentApplicationHandler {
    String posterName, appliedJobTitle, jobDescription, jobRate, appliedJobID, posterUID;

    String documentID;

    Double recruiterRep;

    int numberOfRating;

    public SentApplicationHandler
            (String posterName, Double recruiterRep, String appliedJobTitle, String jobDescription, String jobRate, String appliedJobID, String posterUID) {
        this.posterName = posterName;
        this.recruiterRep = recruiterRep;
        this.appliedJobTitle = appliedJobTitle;
        this.jobDescription = jobDescription;
        this.jobRate = jobRate;
        this.appliedJobID = appliedJobID;
        this.posterUID = posterUID;
    }

    public int getNumberOfRating() {
        return numberOfRating;
    }

    public void setNumberOfRating(int numberOfRating) {
        this.numberOfRating = numberOfRating;
    }

    public double getRecruiterRep() {
        return recruiterRep;
    }

    public void setRecruiterRep(double recruiterRep) {
        this.recruiterRep = recruiterRep;
    }

    public String getDocumentID() {
        return documentID;
    }

    public void setDocumentID(String documentID) {
        this.documentID = documentID;
    }

    public String getPosterUID() {
        return posterUID;
    }

    public void setPosterUID(String posterUID) {
        this.posterUID = posterUID;
    }

    public String getAppliedJobID() {
        return appliedJobID;
    }

    public void setAppliedJobID(String appliedJobID) {
        this.appliedJobID = appliedJobID;
    }

    public String getJobRate() {
        return jobRate;
    }

    public void setJobRate(String jobRate) {
        this.jobRate = jobRate;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public String getPosterName() {
        return posterName;
    }

    public void setPosterName(String posterName) {
        this.posterName = posterName;
    }

    public String getAppliedJobTitle() {
        return appliedJobTitle;
    }

    public void setAppliedJobTitle(String appliedJobTitle) {
        this.appliedJobTitle = appliedJobTitle;
    }

}
