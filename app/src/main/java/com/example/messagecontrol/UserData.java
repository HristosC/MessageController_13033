package com.example.messagecontrol;

public class UserData { //κλάση ώστε να πάρουμε τα στοιχεία που χρειαζόμαστε ώστε να τα περασουμε
    //σε node στην Real-Time database της firebase 
    private String timestamp;
    private String latitude;
    private String longitude;
    private String item;

    public UserData(){
        //this constructor is required
    }

    public UserData( String item, String latitude,String longitude,String timestamp) {
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.item = item;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }
    public String getItem(){
        return item;
    }


}
