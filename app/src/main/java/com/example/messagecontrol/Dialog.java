package com.example.messagecontrol;


import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

public class Dialog extends AppCompatDialogFragment {//κλάσση για τον pop up παράθυρο που καλείται όταν ο χρήστης θέλει να προσθέσει κάποιο κωδικό.

    private EditText editText;

    private ExampleDialogListener listener;
    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_dialog, null);
        builder.setView(view)
                .setTitle("Εισαγωγή Κωδικού Μετακίνησης")
                .setNegativeButton("Ακύρωση", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .setPositiveButton("Εισαγωγή", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try{
                            int addNumber = Integer.parseInt(editText.getText().toString());
                            listener.applyTexts(addNumber);
                        }catch(Exception e) {
                            Toast.makeText(getContext(),"Παρακαλώ Εισάγεται Ακέραιο Αριθμό!",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        editText = view.findViewById(R.id.addNumber);

        return builder.create();
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (ExampleDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    "must implement ExampleDialogListener");
        }
    }
    public interface ExampleDialogListener {
        void applyTexts(int addNumber);
    }
}