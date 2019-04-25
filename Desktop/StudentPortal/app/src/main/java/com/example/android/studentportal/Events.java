package com.example.android.studentportal;

public class Events {
    public String uid, time, date, eventimage, description, profileimage, fullname, startdate, enddate;

    public Events(){

    }
    public Events(String uid, String time, String date, String eventimage, String description, String profileimage, String fullname) {
        this.uid = uid;
        this.time = time;
        this.date = date;
        this.eventimage = eventimage;
        this.description = description;
        this.profileimage = profileimage;
        this.fullname = fullname;
        this.startdate = startdate;
        this.enddate = enddate;
    }
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getEventimage() {
        return eventimage;
    }

    public void setEventimage(String eventimage) {
        this.eventimage = eventimage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getStartdate() {
        return startdate;
    }

    public void setStartdate(String startdate) {
        this.startdate = startdate;
    }

    public String getEnddate() {
        return enddate;
    }

    public void setEnddate(String enddate) {
        this.enddate = enddate;
    }
}


