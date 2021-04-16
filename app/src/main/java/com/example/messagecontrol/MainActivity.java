package com.example.messagecontrol;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import org.w3c.dom.Document;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static java.lang.Integer.parseInt;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener,
        Dialog.ExampleDialogListener {
    Spinner s; //μεταβλητή ώστε να χειριστούμε το spinner που βρίσκεται μεσα στο activity.
    SQLiteDatabase db;
    DatabaseHelper mDatabaseHelper;//για την sqlite μεταβλητή που υα χρησιμοποιήσουμε αργότερα.
    Button button1, button2, button3, voiceButton; //μεταβλητή για τα κουμπιά που υπάρχουν.
    String item; //μεταβλητή τύπου String με την οποία θα αποθηκεύουμε τον κωδικό που έχει επιλέξει
    // ο χρήστης είτε με φωνητική εντολή είτε με το spinner
    FirebaseUser user; //μεταβλητή για παίρνουμε τα στοιχεία του χρήστη από το firebase
    FirebaseDatabase fdb;   //μεταβλητή για την real time database της firebase
    DatabaseReference root; // μεταβλητή reference για την firebase
    FirebaseFirestore fStore; //μεταβλητή για την firebase firestone ώστε να αποθηκεύσουμε στοιχεία
    Calendar calendar; // μεταβλητή calendar που θα χρησιμοποιηθεί για το timestamp
    SimpleDateFormat simpleDateFormat; //μεταβλητή simpleDateFormat που θα χρησιμοποιηθεί για το timestamp
    EditText address, fullName; // μεταβλητές editext ώστε ο χρήστης να μπορεί να τροποποιήσει αργότερα στοιχεία.
    LocationManager lm ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        s = (Spinner) findViewById(R.id.spinner01); //από εδώ κάνουμε initialize μεταβλητές.
        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);
        voiceButton = (Button) findViewById(R.id.voiceButton);
        fullName = (EditText) findViewById(R.id.onoma);
        address = (EditText) findViewById(R.id.addr);

        mDatabaseHelper = new DatabaseHelper(this); //κάνουμε initialize την μεταβλητή ώστε να δημιουργήσουμε καινούργιο αντικείμενο DatabaseHelper

        user = FirebaseAuth.getInstance().getCurrentUser(); //εδώ παίρνουμε τον χρήστη που είναι συνδεδεμένος
        String userID = user.getUid(); //παίρνουμε το id του χρήστη που βρίσκεται στην βάση της firebase
        fStore = FirebaseFirestore.getInstance(); //μεταβλητή ώστε να πάρουμε στοιχεία από το firebase cloud firestore * έξτρα από την εργασία αυτό *

        DocumentReference documentReference = fStore.collection("users").document(userID); //εδώ παίρνουμε στοιχεία από το cloud firestore ώστε να πάρουμε δεδομένα από το συγκεκριμένο collection users
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                address.setText(documentSnapshot.getString("address")); //παίρνουμε δεδομένα από το document "address"
                fullName.setText(documentSnapshot.getString("fName")); //παίρνουμε δεδομένα από το document "fName"
            }
        });


        firstTime(); // καλούμε την μέθοδο firstTime()
        populateListView(); // καλούμε την μέθοδο populateListView()

        button1.setOnClickListener(new View.OnClickListener() { // listener ώστε όταν ο χρήστης πατάει το συγκεκριμένο κουμπί να εκτελούνται οι παρακάτω εντολές
            @Override
            public void onClick(View v) {
                if (s.getCount() == 0) {//προειδοποίηση εάν είναι κενό το spinner
                    Toast.makeText(getApplicationContext(), "Η λίστα είναι κενή , δεν " +
                            "μπορεί να πραγματοποιηθεί διαγραφή!", Toast.LENGTH_LONG).show();

                } else { //άμα δεν είναι κενό το spinner τότε εκτελούνται οι συγκεκριμένες εντολές

                    int number = parseInt(item);
                    mDatabaseHelper.deleteData(number);
                    populateListView();
                    Toast.makeText(getApplicationContext(), "Ο κωδικός διαγράφθηκε " +
                            "επιτυχώς!", Toast.LENGTH_SHORT).show();
                }
            }

        });

        button2.setOnClickListener(new View.OnClickListener() { //εαν ο χρήστης πατήσε το κουμπί αυτό ( Είναι το κουμπί
            // "ΠΡΟΣΘΕΣΗ" ώστε να προσθέσει δικό του κωδικό ) τότε εκτελέιται η μέθοδος openDialog()
            @Override
            public void onClick(View v) {
                openDialog();
            }

        });

        voiceButton.setOnClickListener(new View.OnClickListener() { //το κουμπί που όταν το
            // πατάει ο χρήστης ακούγεται η φωνή του
            //και γίνονται ενέργειες βάση του τι λέει.
            public void onClick(View v) {
                speak(); //εαν περάσει από όλους τους ελέγχους τότε καλούμε την μέθοδο speak()
            }

        });


    }


    public void openDialog() {
        Dialog dialog = new Dialog(); //δημιουργουμε ένα αντικείμενο Dialog
        dialog.show(getSupportFragmentManager(), "example Dialog");
    }

    private void populateListView() {  //με αυτή την μέθοδο γεμίζουμε το spinner μας μέσω της
        // sqlite βάσης μας που μέσα περιέχει τους κωδικους που ο χρήστης μπορεί να στείλει στο 13033

        Cursor data = mDatabaseHelper.getData();
        ArrayList<String> listData = new ArrayList<>();
        while (data.moveToNext()) {
            //get the value from the database
            //then add it to the ArrayList
            listData.add(data.getString(1));
        }
        ArrayAdapter adapter = new ArrayAdapter(
                this, android.R.layout.simple_list_item_1, listData);
        s.setAdapter(adapter);
        s.setOnItemSelectedListener(this);
    }

    public void AddData(int number) { // μέθοδος που στέλνουμε τον κωδικό που έχει
        //βάλει ο χρήστης μέσα στην βάση μας.
        boolean insertData = mDatabaseHelper.addData(number);

        final String PREFS_NAME = "MyPrefsFile";
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        if (settings.getBoolean("my_first_time", true)){

        }
        else{
            if (insertData) {
                Toast.makeText(this,
                        "O κωδικός εισάχθηκε επιτυχώς!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Υπήρξε κάποιο σφάλμα στην εισαγωγή του κωδικού!",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }


    public void firstTime() { //εαν είναι η πρώτη φορά που επισκέπτεται ο χρήστης την εφαρμογή τότε
        //βάζουμε τους βασικούς κωδικούς (1 εώς 6) στην βάση μας sqlite
        final String PREFS_NAME = "MyPrefsFile";
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

        if (settings.getBoolean("my_first_time", true)) {
            for(int i = 1; i<7; i++){
                AddData(i);
            }

            settings.edit().putBoolean("my_first_time", false).commit();

        }
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { //εάν ο χρήστης διαλέξει κάποιο κωδικό από τον spinner
        //τότε αποθηκεύουμε την επιλογή του σε μια μεταβλητή τύπου int "item"
        item = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void applyTexts(int addNumber) { //μέθοδος ώστε να πάρουμε τον αριθμό που εισάγει ο χρήστης από το pop up dialog Που εμφανίζεται.

        if (mDatabaseHelper.CheckIsDataAlreadyInDBorNot(addNumber)) {//εαν ο κωδικός υπάρχει ήδη στην sqlite βάση μας τότε τον ειδοποιούμε ότι ο κωδικός αυτός υπάρχει και δεν θα εισαχθεί
            Toast.makeText(this, "Ο κωδικός που εισάγατε υπάρχει ήδη και δεν θα εισαχθεί.", Toast.LENGTH_SHORT).show();
        } else { //αλλιώς βάζουμε τον αριθμό αυτό και ανανεώνουμε τον spinner μας
            AddData(addNumber);
            populateListView();
        }
    }

    private void speak() { //Η μέθοδος που καλείται όταν πατάει ο χρήστης το κουμπί Speech Recognition
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH); //οτι ο ομιλούμενη γλώσσα είναι στα αγγλικά
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Πες τον Κωδικό!"); //μήνυμα που λέμε στο χρήστη τι να κάνει.
        try {
            startActivityForResult(intent, 1000);
        } catch (Exception e) {

        }
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) { //σε αυτή την μέθοδο γίνονται οι δράσεις
        //με βαση το τι λεει ο χρήστης
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 1000: {
                boolean flag = false;
                if (resultCode == RESULT_OK && null != data) {//στις παρακάτω περιπτώσεις εξετάζουμε εάν ο χρήστης στην φωνητική του εντολή
                    //έχει δώσει ολόκληρη πρόταση (πχ "εκτελεσε τον κωδικο 1000") ή εάν έχει δώσει απλά έναν αριθμό (πχ απλά να πεί "1000")
                    //εαν ο χρήστης δώσει παραπάνω από μια λέξη τότε η λέξη σπάει σε πίνακα τύπου String
                    //εάν ο χρήστης δώσει μόνο 1 λέξη τότε ο πίνακας έχει μόνο 1 στοιχείο.
                    //ανάλογα με το τι είναι εξετάζουμε εάν βρίσκεται μέσα στην πρόταση ή στην λέξη , κάποιος από τους κωδικούς
                    //που βρίσκονται μέσα στην βάση μας.
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    Adapter adapter = s.getAdapter();
                    String[] splited;
                    String fullname = fullName.getText().toString();
                    String addr = address.getText().toString();

                    if (result.get(0).contains("Προσθήκη") || result.get(0).contains("Add") || result.get(0).contains("προσθήκη") || result.get(0).contains("add")){
                        openDialog();
                    }
                    else{
                        if(s.getCount()==0){
                            Toast.makeText(getApplicationContext(), "Πρέπει πρώτα να υπάρχουν " +
                                            "επιλογές ώστε να πραγματοποιήσετε αποστολή ή διαγραφή",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else{
                            if (result.get(0).contains(" ")) {
                                splited = result.get(0).split("\\s+");
                            } else {
                                splited = new String[]{result.get(0)};
                            }
                            int n = adapter.getCount();
                            int k = splited.length;
                            for (int j = 0; j < k; j++) {
                                if (flag) {
                                    break;
                                }
                                for (int i = 0; i < n; i++) {

                                    if (splited[j].equals(adapter.getItem(i).toString())) {
                                        if (fullname.isEmpty() && addr.isEmpty()) { //εαν έχει αφήσει κενά τα πεδία με το όνομα και την διεύθυνση.
                                            fullName.setError("Πρέπει να συμπληρωθεί!");
                                            address.setError("Πρέπει να συμπληρωθεί!");
                                        } else if (fullname.isEmpty()) {
                                            fullName.setError("Πρέπει να συμπληρωθεί!");
                                        } else if (addr.isEmpty()) {
                                            address.setError("Πρέπει να συμπληρωθεί!");
                                        } else{
                                            item = adapter.getItem(i).toString(); //εαν ο κωδικός αυτός υπάρχει μέσα στον spinner τότε
                                            //τον στέλνουμε στην μέθοδο sendSMS όπου στέλνετε το sms ύστερα από κατάλληλους ελέγχους
                                            sendSMS(null);
                                        }
                                        flag = true;
                                        break;//εαν βρεθεί ο κωδικός σπάμε την for loop
                                    }
                                    else if (splited[j].contains("Διαγραφή") || splited[j].contains("delete") || splited[j].contains("Delete") || splited[j].contains("διαγραφή")){

                                        int number = parseInt(item);
                                        mDatabaseHelper.deleteData(number);
                                        populateListView();
                                        Toast.makeText(getApplicationContext(), "Ο κωδικός διαγράφθηκε " +
                                                "επιτυχώς!", Toast.LENGTH_SHORT).show();
                                        flag = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void logout(View view) {// ο χρήστης με αυτή την μέθοδο πατώντας το κατάλληλο κουμπί αποσυνδέεται
        FirebaseAuth.getInstance().signOut();//logout , o χρήστης αποσυνδέεται
        startActivity(new Intent(getApplicationContext(), login.class)); //ξεκινάμε το activity login
        finish();
    }

    private boolean checkAndRequestPermissions() { // εδώ ειναι η μέθοδος που πραγματοποιούμε τους
        // κατάλληλους ελέγχους εάν ο χρήστης θέλει να στείλει μήνυμα και εάν έχει δώσει τα κατάλληλα
        // δικαιώματα στην εφαρμογή
        int permissionSendMessage = ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS);
        int locationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (permissionSendMessage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.SEND_SMS);
        }   
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 1);
            return false;
        }
        return true;

    }

    public void sendSMS(View view) { //σε αυτή την μέθοδο στέλνουμε το μήνυμα sms ανάλογα με τον κωδικό που έχει επιλέξει ο χρήστης.
        if (!isLocationEnabled(this)){
            Toast.makeText(this,"Ενεργοποιήστε την τοποθεσία.",Toast.LENGTH_SHORT).show();
        }
        else if (s.getCount() == 0) {// εαν ο spinner είναι άδειος τότε εμφανίζεται προειδοποιητικό μήνυμα!
            Toast.makeText(this, "Πρέπει πρώτα να έχετε επιλογές ώστε να στείλετε μήνυμα!", Toast.LENGTH_SHORT).show();
        }
        else {
            String fullname = fullName.getText().toString();
            String addr = address.getText().toString();
            if (fullname.isEmpty() && addr.isEmpty()) {
                fullName.setError("Πρέπει να συμπληρωθεί!");
                address.setError("Πρέπει να συμπληρωθεί!");
            } else if (fullname.isEmpty()) {
                fullName.setError("Πρέπει να συμπληρωθεί!");
            } else if (addr.isEmpty()) {
                address.setError("Πρέπει να συμπληρωθεί!");
            } else {
                if (checkAndRequestPermissions()) { //εαν όλες οι συνθήκες ικανοποιούται και οι άδειες έχουν δωθεί τότε καλούμε την μέθοδο getCurrentLocation ,
                    //με την οποία παίρνουμε την θέση του χρήστη που βρίσκεται την στιγμή που στέλνει το μήνυμα.
                    getCurrentLocation();
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage("13033", null, item + " " + fullname.toUpperCase() + " " + addr.toUpperCase(), null, null);
                    Toast.makeText(this, "Το μήνυμα στάλθηκε επιτυχώς!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Πρέπει να αποδεχτείτε τις κατάλληλες άδειες ώστε να στείλετε μήνυμα!", Toast.LENGTH_SHORT).show();
                }

            }
        }
    }


    @SuppressLint("MissingPermission") // αγνοούμε τα permissions που χρειάζονται επειδή
    //ο μόνος τρόπος να εκτελεστεί η συγκεκριμένη μέθοδος είναι εάν βγει true η μέθοδος checkAndRequestPermissions().
    //Η μέθοδος που αναφέρουμε είναι .
    private void getCurrentLocation() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        LocationServices.getFusedLocationProviderClient(MainActivity.this).requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                String latitude;
                String longitude;
                super.onLocationResult(locationResult);
                LocationServices.getFusedLocationProviderClient(MainActivity.this).removeLocationUpdates(this);
                if (locationResult != null && locationResult.getLocations().size() > 0) {
                    int latestLocationIndex = locationResult.getLocations().size() - 1;
                    latitude =
                            String.valueOf(locationResult.getLocations().get(latestLocationIndex).getLatitude());
                    longitude =
                            String.valueOf(locationResult.getLocations().get(latestLocationIndex).getLongitude());
                } else { // εαν δεν μπορούμε να λαβούμε την τοποθεσία του χρήστη τοτέ παιρνάμε τις τιμές "null"
                    latitude = "null";
                    longitude = "null";
                }
                calendar = Calendar.getInstance();
                simpleDateFormat = new SimpleDateFormat("EEEE , dd-MMM-yyyy hh:mm:ss a");
                String dateTime = simpleDateFormat.format(calendar.getTime()); //παίρνουμε την τωρινή ώρα και ημερομηνία.
                fdb = FirebaseDatabase.getInstance("https://messagecontrol-6990e-default-rtdb.firebaseio.com/");
                root = FirebaseDatabase.getInstance().getReference(user.getUid().toString());//εδώ πάιρνουμε το userID που βρίσκεται μέσα στο firebase και φτιάχνουμε ένα node για τον συγκεκριμένο χρήστη.
                String id = root.push().getKey(); //παίρνουμε τον τίτλο για το node που δημιουργείται
                //επειδή ο χρήστης έστειλε μήνυμα , αυτό το node θα μπεί ως παιδί στο parent node ,
                //που έχει ως τίλτο το userID του χρήστη.
                UserData userData = new UserData(item, latitude, longitude, dateTime);
                root.child(id).setValue(userData);//data σχετικά με την στιγμή που έστειλε το μήνυμα ,
                // δηλαδή το dateTime , το longitude , to latitude και τον κωδικό που έστειλε που αυτά
                //παιρνιούνται ως δεδομένα στο child node μας.
            }
        }, Looper.getMainLooper());
    }

    public static boolean isLocationEnabled(Context context) { //με αυτή την μέθοδο βλέπουμε εαν ειναι ανοιχτη η τοποθεσία στο κινητό του χρήστη.
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        }else{
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

}