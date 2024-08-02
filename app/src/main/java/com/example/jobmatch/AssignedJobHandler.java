package com.example.jobmatch;

public class AssignedJobHandler {

    String posterName, jobTitle, jobRate, jobDescription, schedule;

    String jobID, posterID;

    Double recruiterRep;

    int numberOfRating;

    public AssignedJobHandler(String posterName, double recruiterRep, String jobTitle, String jobRate, String jobDescription) {
        this.posterName = posterName;
        this.jobTitle = jobTitle;
        this.jobRate = jobRate;
        this.jobDescription = jobDescription;
        this.recruiterRep = recruiterRep;
    }

    public int getNumberOfRating() {
        return numberOfRating;
    }

    public void setNumberOfRating(int numberOfRating) {
        this.numberOfRating = numberOfRating;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public String getPosterID() {
        return posterID;
    }

    public void setPosterID(String posterID) {
        this.posterID = posterID;
    }

    public String getJobID() {
        return jobID;
    }

    public void setJobID(String jobID) {
        this.jobID = jobID;
    }

    public double getRecruiterRep() {
        return recruiterRep;
    }

    public void setRecruiterRep(double recruiterRep) {
        this.recruiterRep = recruiterRep;
    }

    public String getPosterName() {
        return posterName;
    }

    public void setPosterName(String posterName) {
        this.posterName = posterName;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
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


}
