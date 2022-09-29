package com.example.android.driveme.Activity;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.driveme.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private final static String MAIN_ACTIVITY = "MAIN_ACTIVITY";
    private FirebaseAuth myAuth; //For the authentication in the database
    private FirebaseUser userLogged; //For save the user that is already logged;

    private TextView registerText;
    private EditText emailText;
    private EditText passwordText;
    private Button btnEnter;

    private int RC_SIGN_IN = 1;
    private String TAG = "GOOGLE SIGN IN";


    /**
     * Metodo che istanzia gli oggetti al momento della creazione dell'Activity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myAuth = FirebaseAuth.getInstance();

        emailText = (EditText) findViewById(R.id.email_edit_view);
        passwordText = (EditText) findViewById(R.id.password_edit_view);
        registerText = (TextView) findViewById(R.id.register_text_view);
        btnEnter = (Button) findViewById(R.id.enter_button);

        btnEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailText.getText().toString().trim();
                String password = passwordText.getText().toString().trim();

                //Controllo se le credenziali inserite dall'utente sono valide. Se si, cerco di prelevarle dal database
                if(controlloCredenziali(email, password)){
                    //Chiamo la funzione che cerca nel database l'email e la password e aggiungo un listener al completamento
                    //sia per confermare il corretto login che per aprire la nuova activity.
                    //In caso di fallimento dovuto ad un errore (email/password errate o errori vari), l'utente viene avvisato.
                    myAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    //Controllo che il login avvenga con successo
                                    if (task.isSuccessful()) {
                                        //Autenticazione avvenuta con successo

                                        //Poichè il login è avvenuto con successo, avvio l'activity della main page utente
                                        Intent userPage = new Intent(MainActivity.this, UserPageActivity.class);
                                        startActivity(userPage);
                                        finish();
                                    } else {
                                        //Se il login ha fallito, avviso l'utente
                                        Toast.makeText(MainActivity.this, "Credenziali errate. Riprova.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });

        registerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerPage = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(registerPage);
                finish();
            }
        });

    }

    public boolean controlloCredenziali(String email, String password){
        if(email.trim().equals("")){
            Toast.makeText(MainActivity.this, "Per favore, inserisci una email", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(password.equals("")){
            Toast.makeText(MainActivity.this,"Per favore, inserisci una password", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(password.length()<6){
            Toast.makeText(MainActivity.this,"Per favore, inserisci una password con più di 6 caratteri", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


}
