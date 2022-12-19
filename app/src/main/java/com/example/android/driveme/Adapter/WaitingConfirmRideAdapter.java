package com.example.android.driveme.Adapter;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.driveme.R;
import com.example.android.driveme.Utility.RequestRide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class WaitingConfirmRideAdapter extends ArrayAdapter {

    private FirebaseStorage mFirebaseStorage;
    private StorageReference picStorage;

    public WaitingConfirmRideAdapter(Context context, int resource, List<RequestRide> requestRides){
        super(context, resource, requestRides);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.request_item, parent, false);
        }

        RequestRide requestRide = (RequestRide) getItem(position);

        //Istanza di FirebaseStorage
        mFirebaseStorage = FirebaseStorage.getInstance();
        picStorage = mFirebaseStorage.getReference().child("users_pic/").child(requestRide.getRide().getRider().getUID());

        TextView passengerName = (TextView) convertView.findViewById(R.id.passenger_name_text_view);
        passengerName.setText(requestRide.getRide().getRider().getName() + " " + requestRide.getRide().getRider().getSurname());
        passengerName.setVisibility(View.VISIBLE);

        TextView passengerPoints = (TextView) convertView.findViewById(R.id.points_passenger_text_view);
        passengerPoints.setText("Punti: " + requestRide.getRide().getRider().getPoints());
        passengerPoints.setVisibility(View.VISIBLE);

        TextView hourRide = (TextView) convertView.findViewById(R.id.ride_hour_request);
        hourRide.setText("Orario: " + requestRide.getRide().getHour() + ":" + requestRide.getRide().getMinute());
        hourRide.setVisibility(View.VISIBLE);

        TextView vehicleRequest = (TextView) convertView.findViewById(R.id.vehicle_request);
        vehicleRequest.setText("Veicolo: " + requestRide.getRide().getVehicle());
        vehicleRequest.setVisibility(View.VISIBLE);

        CircleImageView imageView = (CircleImageView) convertView.findViewById(R.id.profile_pic_request_item);

        boolean driverHasPhoto = requestRide.getRide().getRider().getPhotoUrl()!= null;
        if (driverHasPhoto) {
            Glide.with(imageView.getContext())
                    .load(picStorage)
                    .into(imageView);
            imageView.setVisibility(View.VISIBLE);
        } else {
            imageView.setVisibility(View.GONE);
        }

        return convertView;
    }


}
