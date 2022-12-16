package com.example.android.driveme.Activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import com.google.android.material.navigation.NavigationView;
import androidx.core.app.ActivityCompat;
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
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.driveme.Adapter.ConfirmedRideAdapter;
import com.example.android.driveme.Utility.RequestRide;
import com.example.android.driveme.R;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Classe che implementa l'Activity contenente i passaggi confermati visti dall'utente come conducente e come passeggero
 */
public class ConfirmedRideActivity extends AppCompatActivity {

    private FirebaseUser mUser;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference userRef;
    private DatabaseReference acceptedRideRef;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference picStorage;
    private User userLogged;
    private String userID;
    private String name;
    private String surname;
    private String email;

    private TextView nameText;
    private DrawerLayout mDrawerLayout;
    private ListView confirmedRideAsDriverListView;
    private ListView confirmedRideAsPassengerListView;
    private CircleImageView profilePic;

    private ConfirmedRideAdapter confirmedRideAsDriverAdapter;
    private ConfirmedRideAdapter confirmedRideAsPassengerAdapter;

    private ArrayList<RequestRide> confirmedAsPassenger = new ArrayList<>();
    private ArrayList<RequestRide> confirmedAsDriver = new ArrayList<>();
    private ArrayList<RequestRide> confirmedRide = new ArrayList<>();
    private ArrayList<String> keyRideAsPassenger = new ArrayList<>();
    private ArrayList<String> keyRideAsDriver = new ArrayList<>();

    final static int PERMISSION_ALL = 1;
    final static String[] PERMISSIONS = {Manifest.permission.CALL_PHONE};
    private static final int RC_PHOTO_PICKER = 2;

    /**
     * Metodo che istanzia gli oggetti al momento della creazione dell'Activity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmed_ride);

        //Inizializzo l'utente corrente
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        //Prendo l'UID dell'utente corremte
        userID = mUser.getUid();

        //Istanzio un nodo del database. Questo prende l'utente UID dell'utente loggato per poi prelavare i dati personali.
        mDatabase = FirebaseDatabase.getInstance();
        userRef = mDatabase.getReference().child("users").child(userID);


        //Con questo listener prelevo i dati personali dell'utente dal database per mostrarli a video nella TextView presente nella barra laterale di navigazione
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
                        switch (menuItem.getItemId()) {

                            case R.id.nav_request: {
                                Intent requestPage = new Intent(ConfirmedRideActivity.this, RequestActivity.class);
                                startActivity(requestPage);
                                finish();
                                break;
                            }

                            case R.id.nav_sign_out: {
                                FirebaseAuth.getInstance().signOut();
                                Intent mainPage = new Intent(ConfirmedRideActivity.this, MainActivity.class);
                                startActivity(mainPage);
                                finish();
                                break;
                            }

                            case R.id.nav_rides_in_progress: {
                                Intent ridesPage = new Intent(ConfirmedRideActivity.this, WaitingConfirmRideActivity.class);
                                startActivity(ridesPage);
                                finish();
                                break;
                            }

                            case R.id.nav_rides_accepted: {
                                Intent acceptedRidesPage = new Intent(ConfirmedRideActivity.this, ConfirmedRideActivity.class);
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

        acceptedRideRef = mDatabase.getReference().child("acceptedRequest");
        acceptedRideRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                RequestRide request;
                int i = 0;
                for (DataSnapshot snap : dataSnapshot.getChildren()) {
                    request = snap.getValue(RequestRide.class);
                    Log.e("CONFIRMRIDE, KEY", "" + snap.getKey());
                    Log.e("CONFIRMRIDE, HOUR RIDE", "" + snap.getValue(RequestRide.class).getRide().getHour());
                    i++;
                    if (snap.getValue(RequestRide.class).getPassenger().getUID().equals(userID)) {
                        keyRideAsPassenger.add(snap.getKey());
                        confirmedAsPassenger.add(snap.getValue(RequestRide.class));
                    } else if (snap.getValue(RequestRide.class).getRide().getRider().getUID().equals(userID)) {
                        keyRideAsDriver.add(snap.getKey());
                        confirmedAsDriver.add(snap.getValue(RequestRide.class));
                    }
                }

                confirmedRideAsDriverAdapter = new ConfirmedRideAdapter(ConfirmedRideActivity.this, R.layout.request_item, confirmedAsDriver, true);
                confirmedRideAsPassengerAdapter = new ConfirmedRideAdapter(ConfirmedRideActivity.this, R.layout.request_item, confirmedAsPassenger, false);

                confirmedRideAsDriverListView.setAdapter(confirmedRideAsDriverAdapter);
                confirmedRideAsPassengerListView.setAdapter(confirmedRideAsPassengerAdapter);

                confirmedRideAsDriverListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        final int pos = position;

                        AlertDialog.Builder builder = new AlertDialog.Builder(ConfirmedRideActivity.this);
                        builder.setMessage("Cosa vuoi fare?");
                        builder.setPositiveButton("LASCIA UN FEEDBACK", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                Dialog ratingDialog = new Dialog(ConfirmedRideActivity.this);
                                ratingDialog.setContentView(R.layout.rating_dialog);


                                RatingBar ratingBar = (RatingBar) ratingDialog.findViewById(R.id.rating_bar);
                                TextView ratingTxt = (TextView) ratingDialog.findViewById(R.id.message_rating);
                                ratingTxt.setText("FEEDBACK");
                                ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                                    @Override
                                    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {

                                    }
                                });
                                ratingBar.setRating(5);
                                ratingDialog.show();

                            }
                        });

                        builder.setNegativeButton("CONTATTA", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (Build.VERSION.SDK_INT >= 23 && !isPermissionGranted())
                                    requestPermissions(PERMISSIONS, PERMISSION_ALL);

                                Intent callUser = new Intent(Intent.ACTION_DIAL);
                                callUser.setData(Uri.parse("tel:123456789"));
                                callUser.putExtra(Intent.EXTRA_PHONE_NUMBER, confirmedAsDriver.get(pos).getPassenger().getPhone());

                                if (ActivityCompat.checkSelfPermission(ConfirmedRideActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                    // TODO: Consider calling
                                    //    ActivityCompat#requestPermissions
                                    // here to request the missing permissions, and then overriding
                                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                    //                                          int[] grantResults)
                                    // to handle the case where the user grants the permission. See the documentation
                                    // for ActivityCompat#requestPermissions for more details.
                                    return;
                                }

                                startActivity(callUser);
                            }
                        });

                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }
                });

                confirmedRideAsPassengerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        final int pos = position;

                        AlertDialog.Builder builder = new AlertDialog.Builder(ConfirmedRideActivity.this);
                        builder.setMessage("Cosa vuoi fare?");
                        builder.setPositiveButton("CONTATTA", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (Build.VERSION.SDK_INT >= 23 && !isPermissionGranted())
                                    requestPermissions(PERMISSIONS, PERMISSION_ALL);

                                Intent callUser = new Intent(Intent.ACTION_DIAL);
                                callUser.setData(Uri.parse("tel:123456789"));
                                callUser.putExtra(Intent.EXTRA_PHONE_NUMBER, confirmedAsPassenger.get(pos).getPassenger().getPhone());

                                if (ActivityCompat.checkSelfPermission(ConfirmedRideActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                    // TODO: Consider calling
                                    //    ActivityCompat#requestPermissions
                                    // here to request the missing permissions, and then overriding
                                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                    //                                          int[] grantResults)
                                    // to handle the case where the user grants the permission. See the documentation
                                    // for ActivityCompat#requestPermissions for more details.
                                    return;
                                }
                                startActivity(callUser);

                            }
                        });
                        builder.setNegativeButton("LASCIA FEEDBACK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                Dialog ratingDialog = new Dialog(ConfirmedRideActivity.this);
                                ratingDialog.setContentView(R.layout.rating_dialog);

                                RatingBar ratingBar = (RatingBar) ratingDialog.findViewById(R.id.rating_bar);
                                TextView ratingTxt = (TextView) ratingDialog.findViewById(R.id.message_rating);
                                ratingTxt.setText("FEEDBACK");
                                ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                                    @Override
                                    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {

                                    }
                                });
                                ratingBar.setRating(5);
                                ratingDialog.show();
                            }
                        });

                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        confirmedRideAsDriverListView = (ListView) findViewById(R.id.confirmed_ride_as_driver_list_view);
        confirmedRideAsPassengerListView = (ListView) findViewById(R.id.confirmed_ride_as_passenger_list_view);

        confirmedRideAsDriverAdapter = new ConfirmedRideAdapter(ConfirmedRideActivity.this, R.layout.request_item, confirmedAsDriver, true);
        confirmedRideAsPassengerAdapter = new ConfirmedRideAdapter(ConfirmedRideActivity.this, R.layout.request_item, confirmedAsPassenger, false);

        confirmedRideAsDriverListView.setAdapter(confirmedRideAsDriverAdapter);
        confirmedRideAsPassengerListView.setAdapter(confirmedRideAsPassengerAdapter);
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
        Intent backPage = new Intent(ConfirmedRideActivity.this, UserPageActivity.class);
        startActivity(backPage);
        finish();
    }


    private boolean isPermissionGranted() {
        if (checkSelfPermission(Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED){
            Log.v("mylog", "Permission is granted");
            return true;
        } else {
            Log.v("mylog", "Permission not granted");
            return false;
        }
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
