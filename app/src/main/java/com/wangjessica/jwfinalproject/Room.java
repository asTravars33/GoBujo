package com.wangjessica.jwfinalproject;

import com.google.firebase.database.DatabaseReference;

public class Room {

    // Basic info
    private String title;
    private int capacity;

    // Firebase
    private String roomKey;
    private DatabaseReference roomRef;

    // Constructor
    public Room(String title, int capacity, String roomKey){
        this.title = title;
        this.capacity = capacity;
        this.roomKey = roomKey;
    }

    // Getters and setters
    public String getTitle(){
        return title;
    }
    public int getCapacity(){ return capacity; }
    public String getRoomKey(){ return roomKey; }
}
