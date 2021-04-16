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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class login extends AppCompatActivity {
    TextView textView;
    EditText email,password;
    Button login;
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) { //για να φτιάξουμε το activity του παραθύρου που ο χρήστης
        //απλά θα θέλει να συνδεθεί σε έναν λογαριασμό που έχει ήδη δημιουργήσει.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        textView = (TextView) findViewById(R.id.textView);
        email = (EditText) findViewById(R.id.email);
        password = (EditText)findViewById(R.id.password);
        login = (Button)findViewById(R.id.loginButton);
        fAuth = FirebaseAuth.getInstance();

        String text = "Δεν έχετε λογαριασμό; Πατήστε εδώ!";
        SpannableString ss = new SpannableString(text);
        ClickableSpan clickableSpan1 = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                startActivity(new Intent(getApplicationContext(), authentication.class));//εάν ο χρήστης
                //πατήσει στο κείμενο του text το "εδώ" τότε τον μεταφέρουμε στο activity που δημιουργεί
                //λογαριασμο
                finish();
            }
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.BLUE);
                ds.setUnderlineText(false);
            }
        };
        ss.setSpan(clickableSpan1, 30, 34, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(ss);
        textView.setMovementMethod(LinkMovementMethod.getInstance());

        login.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String emailS = email.getText().toString().trim();
                String password1S = password.getText().toString().trim();

                if(TextUtils.isEmpty(emailS)){
                    email.setError("To email είναι υποχρεωτικό.");//ειδοποιούμε τον χρήστη ότι πρέπει να εισάγει κάποιο email.
                    return;
                }

                if(TextUtils.isEmpty(password1S)){
                    password.setError("Ο κωδικός είναι υποχρεωτικός.");
                    return;
                }

                if(password1S.length() < 6){
                    password.setError("Ο κωδικός πρέπει να είναι άνω των 6 χαρακτήρων");
                    return;
                }

                fAuth.signInWithEmailAndPassword(emailS,password1S).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(login.this,"Συγχαρητήρια! Συνδεθήκατε!",Toast.LENGTH_LONG).show();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        }
                        else{
                            Toast.makeText(login.this,"Σφάλμα!Λάθος στοιχεία!",Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }

        });
    }


}