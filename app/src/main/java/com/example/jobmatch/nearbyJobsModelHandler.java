package com.example.jobmatch;

public class nearbyJobsModelHandler {

    String jobTitle, jobDescription, jobRate, distance, posterName, jobID, posterUserID, jobCategory;
    String documentId, dateRange, jobType, numberOfWorker;
    Double posterRep;
    int numberOfRatings;

    public nearbyJobsModelHandler(String jobTitle, String jobDescription, String jobRate,
                                  String distance, String posterName, String jobID, String posterUserID, Double posterRep) {
        this.distance = distance;
        this.posterName = posterName;
        this.jobTitle = jobTitle;
        this.jobDescription = jobDescription;
        this.jobRate = jobRate;
        this.jobID = jobID;
        this.posterUserID = posterUserID;
        this.posterRep = posterRep;
    }

    public int getNumberOfRatings() {
        return numberOfRatings;
    }

    public void setNumberOfRatings(int numberOfRatings) {
        this.numberOfRatings = numberOfRatings;
    }

    public String getDateRange() {
        return dateRange;
    }

    public void setDateRange(String dateRange) {
        this.dateRange = dateRange;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public String getNumberOfWorker() {
        return numberOfWorker;
    }

    public void setNumberOfWorker(String numberOfWorker) {
        this.numberOfWorker = numberOfWorker;
    }

    public String getJobCategory() {
        return jobCategory;
    }

    public void setJobCategory(String jobCategory) {
        this.jobCategory = jobCategory;
    }

    public double getPosterRep() {
        return posterRep;
    }

    public void setPosterRep(double posterRep) {
        this.posterRep = posterRep;
    }

    public String getPosterUserID() {
        return posterUserID;
    }

    public void setPosterUserID(String posterUserID) {
        this.posterUserID = posterUserID;
    }

    public String getJobID() {
        return jobID;
    }

    public void setJobID(String jobID) {
        this.jobID = jobID;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public String getJobRate() {
        return jobRate;
    }

    public void setJobRate(String jobRate) {
        this.jobRate = jobRate;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getPosterName() {
        return posterName;
    }

    public void setPosterName(String posterName) {
        this.posterName = posterName;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }


}
