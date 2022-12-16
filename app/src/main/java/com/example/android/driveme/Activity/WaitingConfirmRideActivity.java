package com.example.android.driveme.Activity;

import android.content.Intent;
import android.net.Uri;
import androidx.annotation.NonNull;
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
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.driveme.Utility.RequestRide;
import com.example.android.driveme.R;
import com.example.android.driveme.Utility.User;
import com.example.android.driveme.Adapter.WaitingConfirmRideAdapter;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class WaitingConfirmRideActivity extends AppCompatActivity {

    private FirebaseUser mUser;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference userRef;
    private DatabaseReference requestRef;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference picStorage;
    private User userLogged;
    private String userID;
    private String name;
    private String surname;
    private String email;

    private TextView nameText;
    private DrawerLayout mDrawerLayout;
    private ListView waitinConfirmRideListView;
    private CircleImageView profilePic;

    private WaitingConfirmRideAdapter waitingConfirmRideAdapter;
    private ArrayList<RequestRide> waitingConfirmRide = new ArrayList<>();
    private ArrayList<String> keyRide = new ArrayList<>();

    private static final int RC_PHOTO_PICKER = 2;

    /**
     * Metodo che istanzia gli oggetti al momento della creazione dell'Activity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_in_progress);

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

                name = userLogged.getName();
                surname = userLogged.getSurname();
                email = userLogged.getEmail();

                //Aggiorno l'interfaccia con i dati dell'utente
                NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
                nameText = (TextView) navigationView.getHeaderView(0).findViewById(R.id.user_data_text_view);
                //nameText.setText("Welcome " + name + " " + surname + "! La tua email è " + email);
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
                                Intent requestPage = new Intent(WaitingConfirmRideActivity.this, RequestActivity.class);
                                startActivity(requestPage);
                                finish();
                                break;
                            }

                            case R.id.nav_sign_out:{
                                FirebaseAuth.getInstance().signOut();
                                Intent mainPage = new Intent(WaitingConfirmRideActivity.this, MainActivity.class);
                                startActivity(mainPage);
                                finish();
                                break;
                            }

                            case R.id.nav_rides_in_progress:{
                                Intent ridesPage = new Intent(WaitingConfirmRideActivity.this, WaitingConfirmRideActivity.class);
                                startActivity(ridesPage);
                                finish();
                                break;
                            }

                            case R.id.nav_rides_accepted:{
                                Intent acceptedRidesPage = new Intent(WaitingConfirmRideActivity.this, ConfirmedRideActivity.class);
                                startActivity(acceptedRidesPage);
                                finish();
                                break;
                            }
                        }

                        return true;
                    }
                });

        //Istanza di FirebaseStorage
        mFirebaseStorage = FirebaseStorage.getInstance();
        picStorage = mFirebaseStorage.getReference().child("users_pic/").child(userID);
        profilePic = (CircleImageView) navigationView.getHeaderView(0).findViewById(R.id.profile_pic);

        Glide.with(getApplicationContext())
                .using(new FirebaseImageLoader())
                .load(picStorage)
                .into(profilePic);

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
            }
        });

        requestRef = mDatabase.getReference().child("requestdrive");
        requestRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                RequestRide ride;
                for(DataSnapshot snap : dataSnapshot.getChildren()) {
                    if (snap.getValue(RequestRide.class).getPassenger().getUID().equals(userID)) {
                        ride = snap.getValue(RequestRide.class);
                        Log.e("REQUESTDRIVE", "key: " + snap.getKey() + "---passenger: " + ride.getPassenger().getName());
                        keyRide.add(snap.getKey());
                        waitingConfirmRide.add(ride);
                    }
                }
                waitingConfirmRideAdapter = new WaitingConfirmRideAdapter(WaitingConfirmRideActivity.this, R.layout.request_item, waitingConfirmRide);
                waitinConfirmRideListView.setAdapter(waitingConfirmRideAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        waitinConfirmRideListView = (ListView) findViewById(R.id.ride_in_progress_list_view);
        waitingConfirmRideAdapter = new WaitingConfirmRideAdapter(WaitingConfirmRideActivity.this, R.layout.request_item, waitingConfirmRide);
        waitinConfirmRideListView.setAdapter(waitingConfirmRideAdapter);
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
    public void onBackPressed() {
        Intent backPage = new Intent(WaitingConfirmRideActivity.this, UserPageActivity.class);
        startActivity(backPage);
        finish();
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
