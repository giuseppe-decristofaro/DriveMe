package com.example.android.driveme.Activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.driveme.Utility.User;
import com.example.android.driveme.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.UUID;


public class RegisterActivity extends AppCompatActivity {

    private static final String REGISTER_ACTIVITY = "EmailPassword";
    private EditText nameText;
    private EditText surnameText;
    private EditText emailText;
    private EditText passwordText;
    private EditText phoneText;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseDatabase mDatabase;
    private DatabaseReference userRef;
    private User userRegistered = new User();
    private String userID;

    /**
     * Metodo che istanzia gli oggetti al momento della creazione dell'Activity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mDatabase = FirebaseDatabase.getInstance();
        userRef = mDatabase.getReference().child("users");

        nameText = (EditText) findViewById(R.id.name_register_edit_view);
        surnameText = (EditText) findViewById(R.id.surname_register_edit_view);
        emailText = (EditText) findViewById(R.id.email_register_edit_view);
        passwordText = (EditText) findViewById(R.id.password_register_edit_view);
        phoneText = (EditText) findViewById(R.id.phone_edit_view);

        Button btnRegistrati = (Button) findViewById(R.id.register_button);

        btnRegistrati.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                userRegistered.setEmail(emailText.getText().toString());
                String password = passwordText.getText().toString();
                userRegistered.setName(nameText.getText().toString());
                userRegistered.setSurname(surnameText.getText().toString());
                userRegistered.setPhone(phoneText.getText().toString());
                userRegistered.setPoints(0.0);

                if(controlloCredenziali(userRegistered.getName(), userRegistered.getSurname(), userRegistered.getEmail(), password,
                                        userRegistered.getPhone())){

                    //Creo le istanze di autenticazione, database e riferimento a database(questo serve per accedere ai nodi del database)
                    mAuth = FirebaseAuth.getInstance();
                    Log.d("RegisterActivity", "mAuth is" + mAuth);

                    if(mAuth != null) {

                        mAuth.createUserWithEmailAndPassword(userRegistered.getEmail(), password)
                                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            //Registrazione avvenuta con successo.

                                            //Prendo l'UID dell'utente corrente
                                            userID = mAuth.getCurrentUser().getUid();
                                            Log.d("RegisterActivity", "userID is" + userID);

                                            userRegistered = new User(userID, nameText.getText().toString(), surnameText.getText().toString(),
                                                    emailText.getText().toString(), phoneText.getText().toString(), 0.0);

                                            //Il nuovo utente viene inserito nel database .
                                            userRef.child(userID).setValue(userRegistered);

                                            //Apro la nuova activity con l'utente già autenticato.
                                            Intent userPage = new Intent(RegisterActivity.this, UserPageActivity.class);
                                            startActivity(userPage);
                                            finish();

                                        } else {
                                            //Se la registrazione fallisce, avviso l'utente
                                            Toast.makeText(RegisterActivity.this, "Ops! C'è stato un errore! Ti preghiamo di riprovare",
                                                    Toast.LENGTH_SHORT).show();
                                            Log.e("RegisterActivity", "onComplete: Failed=" + task.getException().getMessage());
                                        }
                                    }
                                });
                    }
                }
            }
        });
    }


    @Override
    public void onBackPressed() {
        Intent mainPage = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(mainPage);
        finish();
    }

    /**
     * Funzione per il controllo del corretto inserimento delle credenziali da parte dell'utente in fase di login e/o registrazione
     * @param nome {@link String}: il nome dell'utente
     * @param cognome {@link String}: il cognome dell'utente
     * @param email {@link String}: l'email dell'utente
     * @param password {@link String}: la password dell'utente
     * @return a {@link Boolean}: <b>TRUE</b> se l'utente ha inserito correttamente le credenziali, <b>FALSE</b> altrimenti.
     */
    public boolean controlloCredenziali(String nome, String cognome, String email, String password, String phone){

        if(nome.trim().equals("")){
            Toast.makeText(RegisterActivity.this, "Per favore, inserisci un nome", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(cognome.trim().equals("")){
            Toast.makeText(RegisterActivity.this, "Per favore, inserisci un cognome", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(email.trim().equals("")){
            Toast.makeText(RegisterActivity.this, "Per favore, inserisci una email", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(password.equals("")){
            Toast.makeText(RegisterActivity.this,"Per favore, inserisci una password", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(password.length()<6){
            Toast.makeText(RegisterActivity.this,"Per favore, inserisci una password con più di 6 caratteri", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(phone.trim().equals("")){
            Toast.makeText(RegisterActivity.this, "Per favore, inserisci il numero di telefono", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
