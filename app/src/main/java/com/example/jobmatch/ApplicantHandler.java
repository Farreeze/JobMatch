package com.example.jobmatch;

public class ApplicantHandler {



    String applicantName, jobTitle, applicantAge,
            applicantAddress, applicantContact, applicantEmail,
            applicantGender;

    String documentID;

    Double applicantRep;

    int numberOfRatings;


    public ApplicantHandler
            (String applicantName, String jobTitle, String applicantAge,
             String applicantAddress, String applicantContact, String applicantEmail,
             String applicantGender, Double applicantRep) {

        this.applicantName = applicantName;
        this.jobTitle = jobTitle;
        this.applicantAge = applicantAge;
        this.applicantAddress = applicantAddress;
        this.applicantContact = applicantContact;
        this.applicantEmail = applicantEmail;
        this.applicantGender = applicantGender;
        this.applicantRep = applicantRep;

    }

    public int getNumberOfRatings() {
        return numberOfRatings;
    }

    public void setNumberOfRatings(int numberOfRatings) {
        this.numberOfRatings = numberOfRatings;
    }

    public double getApplicantRep() {
        return applicantRep;
    }

    public void setApplicantRep(double applicantRep) {
        this.applicantRep = applicantRep;
    }

    public String getDocumentID() {
        return documentID;
    }

    public void setDocumentID(String documentID) {
        this.documentID = documentID;
    }

    public String getApplicantGender() {
        return applicantGender;
    }

    public void setApplicantGender(String applicantGender) {
        this.applicantGender = applicantGender;
    }

    public String getApplicantName() {
        return applicantName;
    }

    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getApplicantAge() {
        return applicantAge;
    }

    public void setApplicantAge(String applicantAge) {
        this.applicantAge = applicantAge;
    }

    public String getApplicantAddress() {
        return applicantAddress;
    }

    public void setApplicantAddress(String applicantAddress) {
        this.applicantAddress = applicantAddress;
    }

    public String getApplicantContact() {
        return applicantContact;
    }

    public void setApplicantContact(String applicantContact) {
        this.applicantContact = applicantContact;
    }

    public String getApplicantEmail() {
        return applicantEmail;
    }

    public void setApplicantEmail(String applicantEmail) {
        this.applicantEmail = applicantEmail;
    }
}
