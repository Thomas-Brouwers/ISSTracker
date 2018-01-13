package com.example.tmbro.isstracker;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 1;
    Geocoder geocoder;
    List<Address> addresses;

    double latitude;
    double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView textView = findViewById(R.id.headTxt);
        final Button button = findViewById(R.id.button2);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), MapActivity.class);
                startActivity(intent);
            }
        });

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
                        double longitude = location.getLongitude();
                        double latitude = location.getLatitude();

                        geocoder = new Geocoder(context, Locale.getDefault());
                        addresses = geocoder.getFromLocation(latitude, longitude, 1);

                        String address = addresses.get(0).getAddressLine(0);
                        String area = addresses.get(0).getLocality();
                        String city = addresses.get(0).getAdminArea();
                        String country = addresses.get(0).getCountryName();
                        String postalcode = addresses.get(0).getPostalCode();

                        textView.setText(address + area + city + country + postalcode);

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
    }
}
