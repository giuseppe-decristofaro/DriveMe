package com.example.android.driveme.Adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.driveme.R;
import com.example.android.driveme.Utility.Ride;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class RideAdapter extends ArrayAdapter<Ride> {

    private FirebaseStorage mFirebaseStorage;
    private StorageReference picStorage;

    public RideAdapter(Context context, int resource, List<Ride> rides){
        super(context, resource, rides);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.ride_item, parent, false);
        }
        Ride ride = getItem(position);

        TextView nameRiderView = (TextView) convertView.findViewById(R.id.name_rider_text_ride_item);
        nameRiderView.setText(ride.getRider().getName() + " " + ride.getRider().getSurname());
        nameRiderView.setVisibility(View.VISIBLE);

        TextView vehicle = (TextView) convertView.findViewById(R.id.vehicle__text_ride_item);
        vehicle.setText("Tipo Veicolo: " + ride.getVehicle());
        vehicle.setVisibility(View.VISIBLE);

        TextView points = (TextView) convertView.findViewById(R.id.points_rider_text_ride_item);
        points.setText("Punti: " + ride.getRider().getPoints());
        points.setVisibility(View.VISIBLE);

        TextView hourRide = (TextView) convertView.findViewById(R.id.hour_text_ride_item);
        hourRide.setText("Orario: " + ride.getHour() + "." + ride.getMinute());
        hourRide.setVisibility(View.VISIBLE);

        CircleImageView imageView = (CircleImageView) convertView.findViewById(R.id.profile_pic_ride_item);

        boolean driverHasPhoto = ride.getRider().getPhotoUrl() != null;
        if (driverHasPhoto) {
            //Istanza di FirebaseStorage
            mFirebaseStorage = FirebaseStorage.getInstance();
            picStorage = mFirebaseStorage.getReference().child("users_pic");


            try {
                File localFile = File.createTempFile("temp", ".jpg");

                picStorage.child("DEFAULT_PROFILE_PIC.jpg").getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                        imageView.setImageBitmap(bitmap);
                    }
                });

                picStorage.child(ride.getRider().getUID()).getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                        imageView.setImageBitmap(bitmap);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }

            imageView.setVisibility(View.VISIBLE);
        } else {
            imageView.setVisibility(View.GONE);
        }

        return convertView;
    }

}
