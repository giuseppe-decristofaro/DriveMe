package com.example.android.driveme.Activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.navigation.NavigationView;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
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
import com.example.android.driveme.Utility.Ride;
import com.example.android.driveme.Adapter.RideAdapter;
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

import java.text.DecimalFormat;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchRideActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        com.google.android.gms.location.LocationListener {

    private static final double LOCAL_PI = 3.1415926535897932385;
    static double PI_RAD = Math.PI / 180.0;
    final static int PERMISSION_ALL = 1;
    final static String[] PERMISSIONS = {android.Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION};

    private FirebaseUser mUser;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference rideRef;
    private DatabaseReference userRef;
    private DatabaseReference contactRef;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference picStorage;
    private User userLogged;
    private User riderChosen;
    private String userID;

    private DrawerLayout mDrawerLayout;
    private TextView nameText;
    private TextView provaTxt;
    private ListView rideListView;
    private RideAdapter rideAdapter;

    private GoogleMap mMap;
    private MapFragment mapFragment;
    private boolean mapReady = false;
    private boolean firstLocation = true;
    private Location actualLocation = new Location(LocationManager.GPS_PROVIDER);
    private static int numMarker = 0;
    private MarkerOptions destination;
    private MarkerOptions myPositionMarker;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private LocationManager locationManager;
    private ArrayList<Ride> rides = new ArrayList<>();
    private Ride rideChosen;

    private CircleImageView profilePic;
    private static final int RC_PHOTO_PICKER = 2;


    /**
     * Metodo che istanzia gli oggetti al momento della creazione dell'Activity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_ride);

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

        //provaTxt = (TextView) findViewById(R.id.txt_view_prova_position);


        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        //Prendo l'UID dell'utente corremte
        userID = mUser.getUid();

        mDatabase = FirebaseDatabase.getInstance();
        rideRef = mDatabase.getReference().child("rides");
        userRef = mDatabase.getReference().child("users").child(userID);
        contactRef = mDatabase.getReference().child("requestdrive");

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
                        switch(menuItem.getItemId()) {

                            case R.id.nav_request: {
                                Log.e("NAVIGATIONVIEW", "Ho cliccato NavigationView");
                                Intent requestPage = new Intent(SearchRideActivity.this, RequestActivity.class);
                                startActivity(requestPage);
                                finish();
                                break;
                            }

                            case R.id.nav_sign_out: {
                                FirebaseAuth.getInstance().signOut();
                                Intent mainPage = new Intent(SearchRideActivity.this, MainActivity.class);
                                startActivity(mainPage);
                                finish();
                                break;
                            }

                            case R.id.nav_rides_in_progress: {
                                Intent ridesPage = new Intent(SearchRideActivity.this, WaitingConfirmRideActivity.class);
                                startActivity(ridesPage);
                                finish();
                                break;
                            }

                            case R.id.nav_rides_accepted: {
                                Intent acceptedRidesPage = new Intent(SearchRideActivity.this, ConfirmedRideActivity.class);
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

        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.search_map);
        mapFragment.getMapAsync(this);

        rideListView = (ListView) findViewById(R.id.ride_list_view);
        rideAdapter = new RideAdapter(SearchRideActivity.this, R.layout.ride_item, rides);

        rideListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                rideChosen = rides.get(position);

                AlertDialog.Builder builder = new AlertDialog.Builder(SearchRideActivity.this);
                builder.setMessage("Vuoi richiedere il passaggio?");
                builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(SearchRideActivity.this, "Grazie!", Toast.LENGTH_SHORT).show();
                        RequestRide requestRide = new RequestRide(userLogged, rideChosen);
                        contactRef.push().setValue(requestRide);
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();

            }
        });
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

    /**
     * Funzione che implementa le azioni da poter eseguire nella mappa dopo che questa è attiva e utilizzabile.
     * Una volta attiva, è possibile inserire un {@link MarkerOptions} che indicherà la meta.
     * @param googleMap {@link GoogleMap}: la mappa che viene attivata
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mapReady = true;

        //Implementa le risposte ai click sulla mappa
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (numMarker != 0) {
                    mMap.clear();
                    mMap.addMarker(myPositionMarker);
                    mMap.addCircle(new CircleOptions()
                            .center(myPositionMarker.getPosition())
                            .radius(150)
                            .strokeColor(Color.BLUE)
                            .fillColor(Color.argb(64, 0, 0, 255)));
                    numMarker--;
                }
                destination = new MarkerOptions()
                        .position(latLng);
                mMap.addMarker(destination);
                numMarker++;

                rides.clear();
                rideListView.setAdapter(rideAdapter);

                //Ogni volta che l'utente cambia la meta, la lista dei conducenti disponibili viene ricaricata e aggiornata.
                rideRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Ride ride = new Ride();
                        for (DataSnapshot snap : dataSnapshot.getChildren()) {
                            ride = snap.getValue(Ride.class);
                            //Confronto i dati delle corse e verifico che il passeggero non sia anche conducente
                            if(isInTheArea(ride) && sameDestination(ride) && !ride.getRider().getUID().equals(userID))
                                rides.add(ride);
                        }

                        rideListView.setAdapter(rideAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

    }
    /**
     * Metodo che controlla se il conducente di una corsa parta nella zona in cui si trova il passeggero.
     * @param ride {@link Ride}: la corsa con cui viene effettuato il confronto
     * @return un {@link Boolean}: <b>TRUE</b> se il punto di partenza del conducente si trova nell'area del passeggero, <b>FALSE</b> altrimenti
     */
    boolean isInTheArea(Ride ride) {
        if(ride==null)
            return false;

        //Distanza tra la posizione attuale dell'utente e il generico passaggio
        double calculationByDistance = CalculationByDistance(new LatLng(actualLocation.getLatitude(), actualLocation.getLongitude()),
                                                             new LatLng(ride.getStartRideLatitude(), ride.getStartRideLongitude()));

        //Controlla che la distanza sia inferiore a 150 metri
        if (calculationByDistance <= 0.150)
            return true;

        return false;
    }

    /**
     * Metodo che controlla se la meta specificata dal passeggero si trova nell'area di destinazione del conducente.
     * @param ride {@link Ride}: la corsa con cui viene effettuato il confronto
     * @return un {@link Boolean}: <b>TRUE</b> se la meta del passeggero è nell'area di destionazione del conducente, <b>FALSE</b> altrimenti
     */
    boolean sameDestination(Ride ride){
        if(ride==null || destination==null)
            return false;

        double calculationByDistance = CalculationByDistance(new LatLng(destination.getPosition().latitude, destination.getPosition().longitude),
                new LatLng(ride.getEndRideLatitude(), ride.getEndRideLongitude()));

        //Controlla che la distanza sia inferiore a 250 metri
        if(calculationByDistance <= 0.250)
            return true;

        return false;
    }

    /**
     * Metodo per il calcolo in chilometri della distanza aerea tra due posizioni indicate da latitudine e longitudine
     * @param StartP {@link LatLng}: latitudine e longitudine della prima posizione.
     * @param EndP {@link LatLng}: latitudine e longitudine della seconda posizione.
     * @return un {@link Double} che indica la distanza aerea in chilometri che intercorre tra le due posizioni.
     */
    public double CalculationByDistance(LatLng StartP, LatLng EndP) {
        int Radius=6371;//radius of earth in Km
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2-lat1);
        double dLon = Math.toRadians(lon2-lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult= Radius*c;
        double km=valueResult/1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec =  Integer.valueOf(newFormat.format(km));
        double meter=valueResult%1000;
        int  meterInDec= Integer.valueOf(newFormat.format(meter));
        Log.i("Radius Value",""+valueResult+"   KM  "+kmInDec+" Meter   "+meterInDec);

        return Radius * c;
    }

    @Override
    public void onBackPressed() {
        Intent backPage = new Intent(SearchRideActivity.this, UserPageActivity.class);
        finish();
        startActivity(backPage);
    }

    private boolean isPermissionGranted() {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED || checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Log.v("mylog", "Permission is granted");
            return true;
        } else {
            Log.v("mylog", "Permission not granted");
            return false;
        }
    }

    private void showAlert(final int status) {
        String message, title, btnText;
        if (status == 1) {
            message = "Le tue impostazioni di localizzazione sono spente.\nPer favore, attivale per " +
                    "usare questa app";
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
                        Intent returnUserPage = new Intent(SearchRideActivity.this, UserPageActivity.class);
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

    /**
     * Metodo che viene chiamato automaticamente dal sistema quando la posizione dell'utente cambia.
     * La {@link GoogleMap} viene aggiornata effettuando una cancellazione di tutti i {@link MarkerOptions},
     * che verranno subito dopo reinseriti negli stessi punti, tranne quello relativo alla posizione corrente
     * che verrà collocato nella nuova posizione.
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {

        LatLng myPosition = new LatLng(location.getLatitude(), location.getLongitude());
        actualLocation.set(location);
        actualLocation.setLatitude(myPosition.latitude);
        actualLocation.setLongitude(myPosition.longitude);

        myPositionMarker = new MarkerOptions();
        myPositionMarker.position(myPosition).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

        mMap.clear();

        if(destination!=null)
            mMap.addMarker(destination);

        mMap.addMarker(myPositionMarker);
        mMap.addCircle(new CircleOptions()
                .center(myPositionMarker.getPosition())
                .radius(150)
                .strokeColor(Color.BLUE)
                .fillColor(Color.argb(64, 0, 0, 255)));

        if (firstLocation) {
            CameraPosition userCameraPosition = CameraPosition.builder()
                    .target(myPosition)
                    .zoom((float)(16.3))
                    .build();
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(userCameraPosition));
            firstLocation = false;
        }
    }

    private boolean isLocationEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
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
