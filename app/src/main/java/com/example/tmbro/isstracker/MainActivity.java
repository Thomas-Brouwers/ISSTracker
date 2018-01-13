package com.example.tmbro.isstracker;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    public static final String USER_HOME = "USER_HOME";
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 1;
    Geocoder geocoder;
    List<Address> addresses;

    double latitude;
    double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences.Editor editor = getSharedPreferences(USER_HOME, MODE_PRIVATE).edit();
        SharedPreferences reader = getSharedPreferences(USER_HOME, MODE_PRIVATE);

        final TextView textView = findViewById(R.id.updateTxt);

        float readlat = reader.getFloat("lat", 1);
        float readlon = reader.getFloat("lon", 1);

        latitude = readlat;
        longitude = readlon;
        textView.setText("Je thuislocatie is nu: " + reader.getString("place", "Please update below"));

        final Button saveButton = findViewById(R.id.saveButton);

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Context context = this;
        Activity activity = this;

        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {

                    try {
                        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        longitude = location.getLongitude();
                        latitude = location.getLatitude();

                        geocoder = new Geocoder(context, Locale.getDefault());
                        addresses = geocoder.getFromLocation(latitude, longitude, 1);

                        editor.putFloat("lat", (float)latitude);
                        editor.putFloat("lon", (float)longitude);
                        editor.putString("place", addresses.get(0).getLocality());
                        editor.apply();

                        textView.setText("Je thuislocatie is nu: " + addresses.get(0).getLocality());

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    ActivityCompat.requestPermissions(activity,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            MY_PERMISSIONS_REQUEST_LOCATION);
                }
            }
        });

        final Button button = findViewById(R.id.mapButton);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), MapActivity.class);

                intent.putExtra("USER_LON", longitude);
                intent.putExtra("USER_LAT", latitude);
                startActivity(intent);
            }
        });
    }
}
