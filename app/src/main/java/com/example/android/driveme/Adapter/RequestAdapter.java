package com.example.android.driveme.Adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.driveme.R;
import com.example.android.driveme.Utility.RequestRide;
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

public class RequestAdapter extends ArrayAdapter<RequestRide>{

    private FirebaseDatabase mDatabase;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference picStorage;

    public RequestAdapter(Context context, int resource, List<RequestRide> requestRides){
        super(context, resource, requestRides);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.request_item, parent, false);
        }

        RequestRide requestRide = getItem(position);

        TextView passengerName = (TextView) convertView.findViewById(R.id.passenger_name_text_view);
        passengerName.setText(requestRide.getPassenger().getName() + " " + requestRide.getPassenger().getSurname());

        TextView passengerPoints = (TextView) convertView.findViewById(R.id.points_passenger_text_view);
        passengerPoints.setText("Punti: " + requestRide.getPassenger().getPoints());

        passengerName.setVisibility(View.VISIBLE);
        passengerPoints.setVisibility(View.VISIBLE);

        TextView hourRide = (TextView) convertView.findViewById(R.id.ride_hour_request);
        hourRide.setText("Orario: " + requestRide.getRide().getHour() + ":" + requestRide.getRide().getMinute());
        hourRide.setVisibility(View.VISIBLE);

        TextView vehicleRequest = (TextView) convertView.findViewById(R.id.vehicle_request);
        vehicleRequest.setText("Veicolo: " + requestRide.getRide().getVehicle());
        vehicleRequest.setVisibility(View.VISIBLE);

        CircleImageView imageView = (CircleImageView) convertView.findViewById(R.id.profile_pic_request_item);

        boolean passengerHasPhoto = requestRide.getPassenger().getPhotoUrl()!= null;
        if (passengerHasPhoto) {
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

                picStorage.child(requestRide.getPassenger().getUID()).getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
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
