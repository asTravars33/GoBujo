package com.wangjessica.jwfinalproject;

import com.google.firebase.database.DatabaseReference;

public class Journal {

    // Basic info
    private String title;
    private int coverImg;

    // Firebase
    private String journalKey;
    private DatabaseReference journalRef;

    // Constructor
    public Journal(String title, int coverImg, String journalKey){
        this.title = title;
        this.coverImg = coverImg;
        this.journalKey = journalKey;
    }

    // Getters and setters
    public String getTitle(){
        return title;
    }
    public int getCoverImg(){
        return coverImg;
    }
    public String getJournalKey(){
        return journalKey;
    }
}
