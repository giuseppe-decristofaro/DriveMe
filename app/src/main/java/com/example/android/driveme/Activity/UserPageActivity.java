package com.example.android.driveme.Activity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.driveme.R;
import com.example.android.driveme.Utility.User;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
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


public class UserPageActivity extends AppCompatActivity{

    private FirebaseUser mUser;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference picStorage;
    private DatabaseReference userRef;
    private User userLogged;
    private String userID;
    private String name;
    private String surname;
    private String email;

    //private RidesMapFragment ridesMapFragment;
    //private ButtonFragment buttonFragment;
    private Button btnCercaPassaggio;
    private Button btnFornisciPassaggio;
    private TextView nameText;
    private CircleImageView profilePic;

    private DrawerLayout mDrawerLayout;

    private static final int RC_PHOTO_PICKER = 2;

    /**
     * Metodo che istanzia gli oggetti al momento della creazione dell'Activity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_page);

        //Inizializzo l'utente corrente
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        //Prendo l'UID dell'utente corremte
        userID = mUser.getUid();

        //Istanzio un nodo del database. Questo prende l'utente UID dell'utente loggato per poi prelavare i dati personali.
        mDatabase = FirebaseDatabase.getInstance();
        userRef = mDatabase.getReference().child("users").child(userID);
        Log.e("USERPAGE", "UID: " + userID);


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

                nameText = (TextView) findViewById(R.id.user_data_text_view);

                //nameText.setText("Welcome " + name + " " + surname + "! La tua email è " + email);
                nameText.setText("Bentornato " + userLogged.getName() + " " + userLogged.getSurname() +
                        "!\n" + userLogged.getEmail());
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

                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/jpeg");
                        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                        startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);


                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments herea
                        switch(menuItem.getItemId()){

                            case R.id.nav_home:{
                                Intent homePage = new Intent(UserPageActivity.this, UserPageActivity.class);
                                startActivity(homePage);
                                finish();
                                break;
                            }

                            case R.id.nav_request:{
                                Intent requestPage = new Intent(UserPageActivity.this, RequestActivity.class);
                                startActivity(requestPage);
                                finish();
                                break;
                            }

                            case R.id.nav_sign_out:{
                                FirebaseAuth.getInstance().signOut();
                                Intent mainPage = new Intent(UserPageActivity.this, MainActivity.class);
                                startActivity(mainPage);
                                finish();
                                break;
                            }

                            case R.id.nav_rides_in_progress:{
                                Intent ridesPage = new Intent(UserPageActivity.this, WaitingConfirmRideActivity.class);
                                startActivity(ridesPage);
                                finish();
                                break;
                            }

                            case R.id.nav_rides_accepted:{
                                Intent acceptedRidesPage = new Intent(UserPageActivity.this, ConfirmedRideActivity.class);
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
        picStorage = mFirebaseStorage.getReference().child("users_pic/").child(userID);

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

        btnCercaPassaggio = (Button) findViewById(R.id.button_cerca_passaggio);
        btnFornisciPassaggio = (Button) findViewById(R.id.button_fornisci_passaggio);

        btnCercaPassaggio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent searchRideIntent = new Intent(UserPageActivity.this, SearchRideActivity.class);
                startActivity(searchRideIntent);
                finish();
            }
        });

        btnFornisciPassaggio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent provideRide = new Intent(UserPageActivity.this, ProvideRideActivity.class);
                startActivity(provideRide);
                finish();
            }
        });

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
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    profilePic.setImageDrawable(Drawable.createFromPath(String.valueOf(R.drawable.baseline_face_black_18dp)));
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent loginPage = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(loginPage);
        finish();
    }
}
