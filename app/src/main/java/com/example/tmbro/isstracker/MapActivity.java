package com.example.tmbro.isstracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.example.tmbro.isstracker.R;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;


import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.google.android.gms.location.Geofence.NEVER_EXPIRE;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final int  MY_PERMISSIONS_REQUEST_LOCATION = 1;
    int off = 0;
    public GoogleMap mMap;
    UpdateThread th;
    private GeofencingClient mGeofencingClient;
    private  Geofence geofence;
    private PendingIntent mGeofencePendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);


        mGeofencingClient = LocationServices.getGeofencingClient(this);

        LocalBroadcastManager lbc = LocalBroadcastManager.getInstance(this);
        GoogleReceiver receiver = new GoogleReceiver(this);
        lbc.registerReceiver(receiver, new IntentFilter("googlegeofence"));

        Log.d("CREATION", "Thread might run");
         th = new UpdateThread(this.getApplicationContext());
        th.execute();

        try {
            off = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        if(off==0){
            Intent onGPS = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(onGPS);
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        /*Polyline line = mMap.addPolyline(new PolylineOptions()
                .add(new LatLng(51.5, -0.1), new LatLng(40.7, -74.0))
                .width(5)
                .color(Color.RED));*/

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
        }
        geofence = new Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId("ISS")

                .setCircularRegion(
                        0,
                        0,
                        1
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build();

        Timer t = new Timer();

            TimerTask task = new TimerTask(){

                @Override
                public void run(){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            List<Double> lat = th.lat();
                            List<Double> lon = th.lon();
                            Log.d("COÖRDS", "Lat: " + lat + " Lon: " + lon);
                            mMap.clear();
                            for (int i = 0; i < lat.size(); i++) {

                                mMap.addMarker(new MarkerOptions().position(new LatLng(lat.get(i), lon.get(i))).title("Hier is een spacestation."));

                            }
                            if (!lat.isEmpty() || !lon.isEmpty()) {
                                geofence = new Geofence.Builder()
                                        // Set the request ID of the geofence. This is a string to identify this
                                        // geofence.
                                        .setRequestId("ISS")

                                        .setCircularRegion(
                                                lat.get(4),
                                                lon.get(4),
                                                3000000
                                        )
                                        .setExpirationDuration(Geofence.NEVER_EXPIRE)
                                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                                        .build();
                            }
                            try {
                                    mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent());

                            } catch (SecurityException e) {
                                Toast toast = Toast.makeText(getApplicationContext(),"exception",Toast.LENGTH_SHORT);
                                toast.show();
                                e.printStackTrace();
                            }
                        }
                    });
                }
            };

        t.scheduleAtFixedRate(task, 0, 1200);

       /* try {
            if (!mGeofenceList.isEmpty()) {
                mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                        .addOnSuccessListener(this, aVoid -> {
                            Toast toast = Toast.makeText(this,"succes",Toast.LENGTH_SHORT);
                            toast.show();
                        })
                        .addOnFailureListener(this, e -> {
                            Toast toast = Toast.makeText(this,"failure",Toast.LENGTH_SHORT);
                            toast.show();
                        });
            }
        } catch (SecurityException e) {
            Toast toast = Toast.makeText(this,"exception",Toast.LENGTH_SHORT);
            toast.show();
            e.printStackTrace();
        }*/

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    mMap.setMyLocationEnabled(true);

                } else {

                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(getApplicationContext(), "You won't be able to see your location without the asked permission.", duration);
                    toast.show();
                }
                return;
            }
        }
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofence(geofence);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }

    class GoogleReceiver extends BroadcastReceiver {

        MapActivity mActivity;

        public GoogleReceiver(Activity activity) {
            mActivity = (MapActivity) activity;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle args = new Bundle();

        }
    }
}


