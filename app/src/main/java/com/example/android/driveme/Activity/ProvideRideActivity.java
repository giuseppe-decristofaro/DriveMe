package com.example.android.driveme.Activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.navigation.NavigationView;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.android.driveme.R;
import com.example.android.driveme.Utility.Ride;
import com.example.android.driveme.Utility.User;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class ProvideRideActivity extends AppCompatActivity implements OnMapReadyCallback,
                                                                        TimePickerDialog.OnTimeSetListener,
                                                                        GoogleApiClient.OnConnectionFailedListener,
                                                                        GoogleApiClient.ConnectionCallbacks,
                                                                        com.google.android.gms.location.LocationListener{

    private FirebaseUser mUser;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference rideRef;
    private DatabaseReference userRef;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference picStorage;
    private User userLogged;
    private String userID;

    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private MapFragment mapFragment;
    private MarkerOptions start;
    private MarkerOptions end;
    private MarkerOptions myPositionMarker;
    private boolean mapReady = false;
    private static int numMarker = 0;
    private LocationManager locationManager;
    private boolean firstLocation = true;
    private Location actualLocation;
    private LocationRequest locationRequest;

    private DrawerLayout mDrawerLayout;

    private Button btnProvideRide;
    private TextView nameText;
    private TextView txtDate;
    private TextView txtVehicle;
    private TextView txtPassengers;
    private CircleImageView profilePic;

    private Spinner spinnerVehicle;
    private Spinner spinnerNumPassengers;

    private String hourChosen = "00";
    private String minuteChosen = "00";
    private int hourSet;
    private int minuteSet;
    private int numPassengersChosen;
    private String vehicleChosen;

    private double startPositionLatitude = 0;
    private double startPositionLongitude = 0;
    private double endPositionLatitude = 0;
    private double endPositionLongitude = 0;

    //private String[] numPassengers = {"1", "2", "3", "4", "5", "6"};
    private String[] numPassengers = {"1", "2", "3", "4", "5", "6+"};

    private String[] vehicles = {"Auto", "Moto", "Altro"};

    final static int PERMISSION_ALL = 1;
    final static String[] PERMISSIONS = {android.Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION};
    private static final int RC_PHOTO_PICKER = 2;

    /**
     * Metodo che istanzia gli oggetti al momento della creazione dell'Activity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provide_ride);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.baseline_list_black_18dp);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        //Prendo l'UID dell'utente corremte
        userID = mUser.getUid();

        mDatabase = FirebaseDatabase.getInstance();
        rideRef = mDatabase.getReference().child("rides");
        userRef = mDatabase.getReference().child("users").child(userID);

        userID = mAuth.getCurrentUser().getUid();
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

        txtDate = (TextView) findViewById(R.id.time_text_view);
        txtDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(ProvideRideActivity.this, ProvideRideActivity.this,
                        hourSet, minuteSet, DateFormat.is24HourFormat(ProvideRideActivity.this));
                timePickerDialog.show();
            }
        });


        txtVehicle = (TextView) findViewById(R.id.vehicle_choice);
        txtVehicle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(ProvideRideActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                View convertView = (View) inflater.inflate(R.layout.custom, null);
                alertDialog.setView(convertView);
                alertDialog.setTitle("Scegli il veicolo");
                ListView lv = (ListView) convertView.findViewById(R.id.listView1);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(ProvideRideActivity.this ,android.R.layout.simple_list_item_1, vehicles);
                lv.setAdapter(adapter);
                alertDialog.show();
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        txtVehicle.setText("" + vehicles[position]);
                        vehicleChosen = vehicles[position];
                    }
                });
            }
        });

        txtPassengers = (TextView) findViewById(R.id.passengers_choice);
        txtPassengers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(ProvideRideActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                View convertView = (View) inflater.inflate(R.layout.custom, null);
                alertDialog.setView(convertView);
                alertDialog.setTitle("Scegli il numero di passeggeri");
                ListView lv = (ListView) convertView.findViewById(R.id.listView1);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(ProvideRideActivity.this ,android.R.layout.simple_list_item_1, numPassengers);
                lv.setAdapter(adapter);
                alertDialog.show();
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        txtPassengers.setText("" + numPassengers[position]);
                        numPassengersChosen = position + 1;
                    }
                });

            }
        });

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
                        // For example, swap UI fragments here
                        switch(menuItem.getItemId()){

                            case R.id.nav_request:{
                                Intent requestPage = new Intent(ProvideRideActivity.this, RequestActivity.class);
                                startActivity(requestPage);
                                finish();
                                break;
                            }

                            case R.id.nav_sign_out:{
                                FirebaseAuth.getInstance().signOut();
                                Intent mainPage = new Intent(ProvideRideActivity.this, MainActivity.class);
                                startActivity(mainPage);
                                finish();
                                break;
                            }

                            case R.id.nav_rides_in_progress:{
                                Intent ridesPage = new Intent(ProvideRideActivity.this, WaitingConfirmRideActivity.class);
                                startActivity(ridesPage);
                                finish();
                                break;
                            }

                            case R.id.nav_rides_accepted:{
                                Intent acceptedRidesPage = new Intent(ProvideRideActivity.this, ConfirmedRideActivity.class);
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

        btnProvideRide = (Button) findViewById(R.id.provide_ride_button);
        btnProvideRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try{
                    startPositionLatitude = start.getPosition().latitude;
                    endPositionLatitude = end.getPosition().latitude;
                    endPositionLongitude = end.getPosition().longitude;
                    startPositionLongitude = start.getPosition().longitude;
                }
                catch(NullPointerException e){
                    Toast.makeText(ProvideRideActivity.this, "Inserisci correttamente il punto di partenza e la zona di arrivo!", Toast.LENGTH_LONG).show();
                }

                if(controlloDati(startPositionLatitude, startPositionLongitude, endPositionLatitude, endPositionLongitude,
                                    hourChosen, minuteChosen, vehicleChosen, numPassengersChosen)) {
                    Ride ride = new Ride(startPositionLatitude, startPositionLongitude,
                            endPositionLatitude, endPositionLongitude,
                            userLogged, hourChosen, minuteChosen, vehicleChosen, numPassengersChosen);

                    rideRef.push().setValue(ride);

                    Intent mainPage = new Intent(ProvideRideActivity.this, UserPageActivity.class);
                    startActivity(mainPage);
                    finish();
                }
            }
        });

        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.provide_map);
        mapFragment.getMapAsync(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        googleApiClient.disconnect();
    }

    @Override
    public void onBackPressed() {
        Intent backPage = new Intent(ProvideRideActivity.this, UserPageActivity.class);
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

    public boolean controlloDati(double startLatitude, double startLongitude, double endLatitude, double endLongitude,
                                    String hour, String minute, String vehicle, int numPassenger){
        try {
            if (!vehicleChosen.isEmpty()) {
                return true;
            }
        }
        catch(NullPointerException e){
            e.printStackTrace();
            Toast.makeText(ProvideRideActivity.this, "Non hai ancora inserito un veicolo!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    //Controls if the GPS is enabled or not and ask if the user wants to enable it
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Il tuo GPS è disabilitato. Vuoi abilitarlo?")
                .setCancelable(false)
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        String hourTxt = "";
        String minuteTxt = "";
        if(hourOfDay<10) {
            hourTxt = "0";
            hourTxt = hourTxt.concat(Integer.toString(hourOfDay));
        }
        else
            hourTxt = hourTxt.concat(Integer.toString(hourOfDay));
        if(minute<10) {
            minuteTxt = "0";
            minuteTxt = minuteTxt.concat(Integer.toString(minute));
        }
        else
            minuteTxt = Integer.toString(minute);

        hourChosen = hourTxt;
        minuteChosen = minuteTxt;
        txtDate.setText(hourTxt + ":" + minuteTxt);
    }

    /**
     * Funzione che implementa le azioni da poter eseguire nella mappa dopo che questa è attiva e utilizzabile.
     * Una volta attiva, è possibile inserire due {@link MarkerOptions} che indicheranno un punto di partenza ed una zona di arrivo.
     * @param googleMap {@link GoogleMap}: la mappa che viene attivata
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mapReady=true;

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(numMarker==0){
                    start = new MarkerOptions()
                            .position(latLng)
                            .title("Partenza");
                    mMap.addMarker(start);
                    numMarker++;
                }
                else if(numMarker==1){
                    end = new MarkerOptions()
                            .position(latLng)
                            .title("Arrivo");
                    mMap.addMarker(end);
                    mMap.addCircle(new CircleOptions()
                            .center(end.getPosition())
                            .radius(250)
                            .strokeColor(Color.BLUE)
                            .fillColor(Color.argb(64, 0, 0, 255)));
                    numMarker++;
                }
                else {
                    mMap.clear();
                    start = null;
                    end = null;
                    mMap.addMarker(myPositionMarker);
                    numMarker = 0;
                }
            }
        });
    }

    /**
     * Metodo che viene chiamato automaticamente dal sistema quando la posizione dell'utente cambia.
     * La {@link GoogleMap} viene aggiornata effettuando una cancellazione di tutti i {@link MarkerOptions},
     * che verranno subito dopo reinseriti negli stessi punti, tranne quello relativo alla posizione corrente
     * che verrà collocato nella nuova posizione.
     * @param location {@link Location}: la posizione calcolata dal GPS.
     */
    @Override
    public void onLocationChanged(Location location) {

        LatLng myPosition = new LatLng(location.getLatitude(), location.getLongitude());
        actualLocation = location;

        myPositionMarker = new MarkerOptions();
        myPositionMarker.position(myPosition)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

        if (firstLocation) {
            CameraPosition userCameraPosition = CameraPosition.builder()
                    .target(myPosition)
                    .zoom((float)(16.3))
                    .build();
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(userCameraPosition));
            firstLocation = false;
        }

        mMap.clear();

        if(start!=null)
            mMap.addMarker(start);
        if(end!=null) {
            mMap.addMarker(end);
            mMap.addCircle(new CircleOptions()
                    .center(end.getPosition())
                    .radius(250)
                    .strokeColor(Color.BLUE)
                    .fillColor(Color.argb(64, 0, 0, 255)));
        }

        mMap.addMarker(myPositionMarker);
    }

    /**
     * Metodo per il controllo dei permessi di localizzazione forniti all'applicazione
     * @return {@link Boolean}: <b>TRUE</b> se i permessi sono garantiti, <b>FALSE</b> altrimenti
     */
    private boolean isPermissionGranted() {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED || checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            //Permessi già concessi all'applicazione
            return true;
        } else {
            //Permessi non concessi all'applicazione
            return false;
        }
    }

    private void showAlert(final int status) {
        String message, title, btnText;
        if (status == 1) {
            message = "Le tue impostazioni di localizzazione sono spente.\nPer favore, attivale per " +
                    "usare l'applicazione";
            title = "Attivazione impostazioni";
            btnText = "Impostazioni di localizzazione";
        } else {
            message = "Permetti a quest'app di accedere alla tua posizione!";
            title = "Permesso accessi";
            btnText = "Permetti";
        }

        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setCancelable(false);
        dialog.setTitle(title)
                .setMessage(message)
                .setPositiveButton(btnText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        if (status == 1) {
                            Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(myIntent);
                        } else
                            requestPermissions(PERMISSIONS, PERMISSION_ALL);
                    }
                })
                .setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent returnUserPage = new Intent(ProvideRideActivity.this, UserPageActivity.class);
                        startActivity(returnUserPage);
                        finish();
                    }
                });
        dialog.show();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);

        if (Build.VERSION.SDK_INT >= 23 && !isPermissionGranted())
            requestPermissions(PERMISSIONS, PERMISSION_ALL);
        if (!isLocationEnabled())
            showAlert(1);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void requestLocation() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
        String provider = locationManager.getBestProvider(criteria, true);

        if (Build.VERSION.SDK_INT >= 23 && !isPermissionGranted())
            requestPermissions(PERMISSIONS, PERMISSION_ALL);
        if (!isLocationEnabled())
            showAlert(1);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(provider, 1000, 10, (LocationListener) this);
    }

    private boolean isLocationEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
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