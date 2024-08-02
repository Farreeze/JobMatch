package com.example.jobmatch;

public class PostedJobsModelHandler {

    String jobTitle, jobDescription, jobRate, assignedUserName, assignedUserEmail, assignedUserContact, assignedUserID, assignedUserSched;
    String documentId, dateRange, jobType, numberOfWorker;

    String currentUserID, currentUserName;

    public PostedJobsModelHandler(String jobTitle, String jobDescription, String jobRate) {
        this.jobTitle = jobTitle;
        this.jobDescription = jobDescription;
        this.jobRate = jobRate;
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

    public String getAssignedUserSched() {
        return assignedUserSched;
    }

    public void setAssignedUserSched(String assignedUserSched) {
        this.assignedUserSched = assignedUserSched;
    }

    public String getCurrentUserID() {
        return currentUserID;
    }

    public void setCurrentUserID(String currentUserID) {
        this.currentUserID = currentUserID;
    }

    public String getCurrentUserName() {
        return currentUserName;
    }

    public void setCurrentUserName(String currentUserName) {
        this.currentUserName = currentUserName;
    }

    public String getAssignedUserID() {
        return assignedUserID;
    }

    public void setAssignedUserID(String assignedUserID) {
        this.assignedUserID = assignedUserID;
    }

    public String getAssignedUserName() {
        return assignedUserName;
    }

    public void setAssignedUserName(String assignedUserName) {
        this.assignedUserName = assignedUserName;
    }

    public String getAssignedUserEmail() {
        return assignedUserEmail;
    }

    public void setAssignedUserEmail(String assignedUserEmail) {
        this.assignedUserEmail = assignedUserEmail;
    }

    public String getAssignedUserContact() {
        return assignedUserContact;
    }

    public void setAssignedUserContact(String assignedUserContact) {
        this.assignedUserContact = assignedUserContact;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
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

}
