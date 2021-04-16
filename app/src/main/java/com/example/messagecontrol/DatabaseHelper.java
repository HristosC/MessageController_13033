package com.example.messagecontrol;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelped";

    private static final String TABLE_NAME = "DatabaseHelper"; //Το όνομα του table
    private static final String COL0 = "ID"; //Το id που βρίσκεται η καταχώρηση μας και αυξανεται αυτόματα κάθε φορα που εισάγονται νεα δεδομένα στο table μας
    private static final String COL1 = "NUMBER"; //η στήλη που που βρίσκονται οι κωδικοί μετακίνησης



    public DatabaseHelper(Context context){
        super(context, TABLE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL1 + " INTEGER)" ;
        db.execSQL(createTable); //δημιουργούμε το table μας με βάση το String createTable



    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { //απαραίτητη μέθοδος που χρειάζεται το databasehelper σε περίπτωση που κάνουμε κάποια αλλαγή γενικά στο table μας ή στην βάση μας.
        //όπως πχ να
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean addData(int number) {//μέθοδος που παίρνουμε τα δεδομένα και τα
        // παιρνάμε ως παράμετρους ώστε να τα βάλουμε στο table μας

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL1, number);




        long result = db.insert(TABLE_NAME, null, contentValues);

        if (result== -1 ){
            return false;
        }
        else{
            return true;
        }
    }

    public void deleteData(int number){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "DELETE FROM "+ TABLE_NAME + " WHERE NUMBER = "+ number;
        db.execSQL(query);
    }



    public Cursor getData(){ //μέθοδος ώστε να εμφανίσουμε τα δεδόμενα μας στο spinner
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + COL1 + " ASC";
        Cursor data = db.rawQuery(query, null);
        return data;
    }

    public  boolean CheckIsDataAlreadyInDBorNot(int fieldValue) {//ελέγχουμε εάν ο κωδικός βρίσκεται μέσα στην βάση μας.
        SQLiteDatabase db = this.getWritableDatabase();
        String Query = "Select * from " + TABLE_NAME + " where " + COL1 + " = " + fieldValue;
        Cursor cursor = db.rawQuery(Query, null);
        if(cursor.getCount() <= 0){
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

}
