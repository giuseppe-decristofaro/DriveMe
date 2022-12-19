package com.example.android.driveme.Activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.android.driveme.Utility.RequestRide;
import com.example.android.driveme.R;
import com.example.android.driveme.Adapter.RequestAdapter;
import com.example.android.driveme.Utility.User;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestActivity extends AppCompatActivity {

    private FirebaseUser mUser;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference userRef;
    private DatabaseReference requestRef;
    private DatabaseReference acceptedRequests;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference picStorage;
    private String userID;
    private User userLogged;

    private DrawerLayout mDrawerLayout;
    private TextView nameText;
    private ListView requestListView;
    private CircleImageView profilePic;

    private RequestAdapter requestAdapter;
    private ArrayList<RequestRide> requests = new ArrayList<>();
    private ArrayList<String> keyRequests = new ArrayList<>();

    private static final int RC_PHOTO_PICKER = 2;

    /**
     * Metodo che istanzia gli oggetti al momento della creazione dell'Activity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);

        //Inizializzo l'utente corrente
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        //Prendo l'UID dell'utente corremte
        userID = mUser.getUid();

        //Istanzio un nodo del database. Questo prende l'utente UID dell'utente loggato per poi prelavare i dati personali.
        mDatabase = FirebaseDatabase.getInstance();
        userRef = mDatabase.getReference().child("users").child(userID);


        //Con questo listener prelevo i dati personali dell'utente dal database per mostrarli a video.
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userLogged = dataSnapshot.getValue(User.class);

                Log.e("addValueEventListener", "\nName: " + userLogged.getName() + "\nSurname: " + userLogged.getSurname() +
                        "\nEmail: " + userLogged.getEmail());

                NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
                nameText = (TextView) navigationView.getHeaderView(0).findViewById(R.id.user_data_text_view);
                //Aggiorno l'interfaccia con i dati dell'utente
                //nameText = (TextView) findViewById(R.id.user_data_text_view);

                nameText.setText("Bentornato " + userLogged.getName() + " " + userLogged.getSurname() +
                        "!\n" + userLogged.getEmail());

                userRef.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.baseline_list_black_18dp);

        mDrawerLayout = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();

                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments herea
                        switch(menuItem.getItemId()){

                            case R.id.nav_request:{
                                Intent requestPage = new Intent(RequestActivity.this, RequestActivity.class);
                                startActivity(requestPage);
                                finish();
                                break;
                            }

                            case R.id.nav_sign_out:{
                                FirebaseAuth.getInstance().signOut();
                                Intent mainPage = new Intent(RequestActivity.this, MainActivity.class);
                                startActivity(mainPage);
                                finish();
                                break;
                            }

                            case R.id.nav_rides_in_progress:{
                                Intent ridesPage = new Intent(RequestActivity.this, WaitingConfirmRideActivity.class);
                                startActivity(ridesPage);
                                finish();
                                break;
                            }

                            case R.id.nav_rides_accepted:{
                                Intent acceptedRidesPage = new Intent(RequestActivity.this, ConfirmedRideActivity.class);
                                startActivity(acceptedRidesPage);
                                finish();
                                break;
                            }
                        }

                        return true;
                    }
                });

        profilePic = (CircleImageView) navigationView.getHeaderView(0).findViewById(R.id.profile_pic);

        //Istanza di FirebaseStorage
        mFirebaseStorage = FirebaseStorage.getInstance();
        picStorage = mFirebaseStorage.getReference().child("users_pic");


        try {
            File localFile = File.createTempFile("temp", ".jpg");

            picStorage.child("DEFAULT_PROFILE_PIC.jpg").getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                    profilePic.setImageBitmap(bitmap);
                }
            });

            picStorage.child(userID).getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                    profilePic.setImageBitmap(bitmap);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
            }
        });

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        //Prendo l'UID dell'utente corrente
        userID = mUser.getUid();

        mDatabase = FirebaseDatabase.getInstance();

        acceptedRequests = mDatabase.getReference().child("acceptedRequest");

        requestListView = (ListView) findViewById(R.id.request_list_view);

        userRef = mDatabase.getReference().child("users").child(userID);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userLogged = dataSnapshot.getValue(User.class);
                Log.e("userLogged", userLogged.getEmail());
                nameText = (TextView) findViewById(R.id.user_data_text_view);
                //nameText.setText("Welcome " + name + " " + surname + "! La tua email è " + email);
                nameText.setText("Bentornato " + userLogged.getName() + " " + userLogged.getSurname() +
                        "!\n" + userLogged.getEmail());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        requestAdapter = new RequestAdapter(RequestActivity.this, R.layout.request_item, requests);
        requestListView.setAdapter(requestAdapter);

        requestRef = mDatabase.getReference().child("requestdrive");
        requestRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                RequestRide request;
                int i=0;
                //Effettuo un'istantanea dei valori nel database a cui punta requestRef
                for(DataSnapshot snap: dataSnapshot.getChildren()){
                    request = snap.getValue(RequestRide.class);
                    //Prelevo tutte le richieste che contengono l'UID dell'utente connesso
                    if(request.getRide().getRider().getUID().equals(userID)){
                        keyRequests.add(snap.getKey());
                        requests.add(request);
                    }
                }
                //Aggiungo le richieste alla lista che le mostrerà
                requestAdapter = new RequestAdapter(RequestActivity.this, R.layout.request_item, requests);
                requestListView.setAdapter(requestAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                requestAdapter = new RequestAdapter(RequestActivity.this, R.layout.request_item, requests);
                requestListView.setAdapter(requestAdapter);
            }
        });



        requestListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final int pos = position;

                AlertDialog.Builder builder = new AlertDialog.Builder(RequestActivity.this);
                builder.setMessage("Cosa vuoi fare?");

                builder.setPositiveButton("CONFERMA", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(RequestActivity.this, "Grazie!", Toast.LENGTH_SHORT).show();
                        RequestRide requestRide = requestAdapter.getItem(pos);
                        acceptedRequests.push().setValue(requestRide);
                        requests.clear();
                        //requests.remove(requestRide);
                        requestRef.child(keyRequests.get(pos)).removeValue();
                        keyRequests.clear();
                        requestAdapter = new RequestAdapter(RequestActivity.this, R.layout.request_item, requests);
                        requestListView.setAdapter(requestAdapter);

                    }
                });

                builder.setNegativeButton("RIFIUTA", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(RequestActivity.this, "Sei una brutta persona.", Toast.LENGTH_SHORT).show();
                        requests.clear();
                        requestRef.child(keyRequests.get(pos)).removeValue();
                        keyRequests.clear();
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });


    }

    @Override
    public void onBackPressed() {
        Intent backPage = new Intent(RequestActivity.this, UserPageActivity.class);
        startActivity(backPage);
        finish();
    }

    /**
     * In base all'oggetto cliccato dall'utente nel Menu, esegue un'azione diversa
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:{
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            }

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK){

            final Uri selectedImageUri = data.getData();
            picStorage.child(selectedImageUri.getLastPathSegment());

            picStorage.putFile(selectedImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    final String downloadUrl = picStorage.getDownloadUrl().toString();
                    userRef.child("photoUrl").setValue(downloadUrl);
                    profilePic.setImageURI(selectedImageUri);
                }
            });
        }
    }
}
