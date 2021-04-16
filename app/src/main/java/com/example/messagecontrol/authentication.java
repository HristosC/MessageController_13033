package com.example.messagecontrol;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

public class authentication extends AppCompatActivity { // σε αυτό το java αρχείο υλοποιηούμε το
    // κομμάτι όπου ο χρήστης επιθυμεί να δημιουργήσει έναν καινούργιο λογαριασμό για την εφαρμογή
    // αυτή.
    //Επίσης αυτό είναι και το πρώτο activity που βλέπει ο χρήστης με το που ανοίγει την εφαρμογή.
    Button registerButton;
    TextView textView;
    EditText email,password1,password2,fullName,address;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);
        //από κάνουμε initialize κάποιες μεταβλητές ώστε να επικοινωνήσουμε με τα views του activity
        registerButton = (Button) findViewById(R.id.registerButton);
        textView = (TextView) findViewById(R.id.textView);
        email = (EditText) findViewById(R.id.email);
        password1 = (EditText)findViewById(R.id.password1);
        password2 = (EditText)findViewById(R.id.password2);
        fullName = (EditText)findViewById(R.id.fullName);
        address = (EditText)findViewById(R.id.address);
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        String text = "Έχετε ήδη λογαριασμό; Πατήστε εδώ!";
        SpannableString ss = new SpannableString(text);
        ClickableSpan clickableSpan1 = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                startActivity(new Intent(getApplicationContext(), login.class));//ανοίγουμε το activity
                //login
                finish();
            }
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.BLUE);
                ds.setUnderlineText(false);
            }
        };
        ss.setSpan(clickableSpan1, 30, 34, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); //στο κείμενο
        //της μεταβλητής text ο χρήστης πατάει το "κουμπί" "εδώ" καθώς θέλει να συνδεθεί με τον λογαριασμό
        //που έχει ήδη στην βάση μας.
        textView.setText(ss);
        textView.setMovementMethod(LinkMovementMethod.getInstance());

        if(fAuth.getCurrentUser() != null){//εαν ο χρήστης είναι ήδη συνδεδεμένος τότε τον καθοδηγούμε στο
            //MainActivity δηλαδή στο παράθυρο όπου μπορεί να πραγματοποιήσει την αποστολή sms
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }


        registerButton.setOnClickListener(new View.OnClickListener()
        { //εαν ο χρήστης πατήσει το κουμπί ώστε να κάνει εγγραφή του λογαριασμού που δήλωσε τότε
            //πραγματοποιούναι ορισμένοι έλεγχοι.
            @Override
            public void onClick(View v)
            {
                String emailS = email.getText().toString().trim();
                String password1S = password1.getText().toString().trim();
                String password2S = password2.getText().toString().trim();
                String fullNameS = fullName.getText().toString();
                String addressS = address.getText().toString();

                if( TextUtils.isEmpty(emailS) && TextUtils.isEmpty(password1S) && TextUtils.isEmpty(fullNameS) && TextUtils.isEmpty(addressS)){
                    email.setError("Το email είναι απαραίτητο.");
                    password1.setError("Ο κωδικός είναι απαραίτητος.");
                    fullName.setError("Το όνομα είναι απαραίτητο.");
                    address.setError("Η διεύθυνση είναι απαραίτητη.");
                }
                if( TextUtils.isEmpty(emailS) && TextUtils.isEmpty(password1S) && TextUtils.isEmpty(fullNameS) && TextUtils.isEmpty(addressS)){
                    email.setError("Το email είναι απαραίτητο.");
                    password1.setError("Ο κωδικός είναι απαραίτητος.");
                    fullName.setError("Το όνομα είναι απαραίτητο.");
                    address.setError("Η διεύθυνση είναι απαραίτητη.");
                }
                if(TextUtils.isEmpty(emailS)){ //εαν ο χρήστης αφήσει κενό το email
                    email.setError("Το email είναι απαραίτητο.");
                    return;
                }

                if(TextUtils.isEmpty(password1S)){//εαν ο χρήστης αφήσει κενό το password
                    password1.setError("Ο κωδικός είναι απαραίτητος.");
                    return;
                }

                if(TextUtils.isEmpty(fullNameS)){//εαν ο χρήστης αφήσει κενό το πεδίο όπου πρέπει να
                    //συμπληρώσει το όνομα του.
                    fullName.setError("Το όνομα είναι απαραίτητο.");
                    return;
                }

                if(TextUtils.isEmpty(addressS)){//εαν ο χρήστης αφήσει κενό το πεδίο όπου πρέπει να
                    //συμπληρώσει την διεύθυνση του.
                    address.setError("Η διεύθυνση είναι απαραίτητη.");
                    return;
                }

                if(password1S.length() < 6){ //Εαν ο κωδικός είναι μικρότερος των 6 χαρακτήρων
                    password1.setError("Ο κωδικός πρέπει να είναι άνω των 6 χαρακτήρων!");
                    return;
                }
                if(!password1S.equals(password2S)){//εαν οι δυο κωδικοί δεν ταιριάζουν.
                    password1.setError("Οι κωδικοί πρέπει να ταιριάζουν!");
                    password2.setError("Οι κωδικοί πρέπει να ταιριάζουν!");
                    return;
                }

                fAuth.createUserWithEmailAndPassword(emailS,password1S).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){ //εαν η διαδικασία είναι επιτυχής τότε ο χρήστης δημιουργείται και επίσης
                            //το όνομα του και η διεύθυνση του αποθηκεύεται στην firestore. Τα στοιχεία αυτά δεν ζητούνται από την ίδια την εργασία
                            //αλλά είναι ένα έξτρα βήμα ώστε ο
                            userID = fAuth.getCurrentUser().getUid(); //εδώ παίρνουμε το ID του χρήστη
                            DocumentReference documentReference = fStore.collection("users").document(userID); //φτιάχνουμε ένα document
                            //με βάση το userID και αποθηκεύουμε σε αυτό το ονομα του και την διεύθυνση του
                            Map<String,Object> user = new HashMap<>();
                            user.put("fName",fullNameS);
                            user.put("address",addressS);
                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(authentication.this,"Ο λογαριασμός δημιουργήθηκε επιτυχώς!",Toast.LENGTH_LONG).show();
                                }
                            });
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        }else{
                            try
                            {
                                throw task.getException();
                            }
                            catch(Exception existEmail){//εαν ο χρήστης προσπαθεί να δημιουργήσει λογαριασμό με email που χρησιμοποιείται ήδη.
                                Toast.makeText(authentication.this,"Το email αυτό χρησιμοποιείται ήδη!",Toast.LENGTH_SHORT).show();
                            }
                            Toast.makeText(authentication.this,"Σφάλμα! Ο λογαριασμός δεν δημιουργήθηκε!",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

        });
    }
}