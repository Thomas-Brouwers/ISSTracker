package com.example.tmbro.isstracker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.Task;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.google.android.gms.location.Geofence.NEVER_EXPIRE;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {
    private final static int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 0;
    private final static float DEFAULT_ZOOM = 18f;
    private final static String KEY_LOCATION = "LOCATION";
    private final static String KEY_CAMERA_POSITION = "CAMERA_POSITION";
    public final static String KEY_ROUTE = "ROUTE";
    private final static int ZOOM_THRESHOLD = 10;

    private boolean fresh = true;
    private GoogleMap mMap;
    private Location lastKnownLocation = null;
    private CameraPosition cameraPosition = null;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LatLng defaultLocation = new LatLng(32.676149, -117.157703);
    private SparseArray<Marker> visibleMarkers = new SparseArray<>();
    private LocationCallback locationCallback;
    private List<List<LatLng>> route = new ArrayList<>();
    private Polyline lineToVisit;
    private Polyline lineVisited;
    private GeofencingClient mGeofencingClient;
    private ArrayList<Geofence> mGeofenceList;
    private PendingIntent mGeofencePendingIntent;
    private GoogleApiClient googleApiClient;

    private LatLng northEastBound = null;
    private LatLng southWestBound = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            fresh = false;
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }
        mGeofenceList = new ArrayList<>();


        //setContentView(R.layout.activity_map);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        /*SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);*/

        mGeofencingClient = LocationServices.getGeofencingClient(this);

        LocalBroadcastManager lbc = LocalBroadcastManager.getInstance(this);
        GoogleReceiver receiver = new GoogleReceiver(this);
        lbc.registerReceiver(receiver, new IntentFilter("googlegeofence"));

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                onLocationChanged(locationResult.getLastLocation());
            }
        };

        createGoogleApi();
    }

    private void createGoogleApi() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
        outState.putParcelable(KEY_LOCATION, lastKnownLocation);
        super.onSaveInstanceState(outState);
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
    protected void onResume() {
        registerLocationUpdates();
        super.onResume();
    }

    @Override
    protected void onPause() {
        deregisterLocationUpdates();
        super.onPause();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this::onMarkerClick);


        if (fresh) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
            getLocationPermission();
        }
        updateLocationUI();
        getDeviceLocation();


        try {
            if (hasLocationPermission() && fresh) {
                mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                        .addOnSuccessListener(this, aVoid -> {
                            Log.d("SUC", "succes");
                        })
                        .addOnFailureListener(this, e -> {
                            Log.d("FAI", "failure");
                        });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }

    }


    private void addMarker(/*POI poi*/) {
        LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;

        /*if (bounds.contains(new LatLng(poi.getLatitude(), poi.getLongitude())) && mMap.getCameraPosition().zoom >= ZOOM_THRESHOLD) {
            if (visibleMarkers.get(poi.getNumber()) == null) {
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .title(poi.getName())
                        .position(new LatLng(poi.getLatitude(), poi.getLongitude())));
                marker.setTag(poi.getNumber());
                visibleMarkers.put(poi.getNumber(), marker);

            }
        } else {
            if (visibleMarkers.get(poi.getNumber()) != null) {
                visibleMarkers.get(poi.getNumber()).remove();
                visibleMarkers.remove(poi.getNumber());
            }
        }
        mGeofenceList.add(new Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId(String.valueOf(poi.getNumber()))

                .setCircularRegion(
                        poi.getLatitude(),
                        poi.getLongitude(),
                        30
                )
                .setExpirationDuration(NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build());*/
    }


    private void getDeviceLocation() {
        try {
            if (hasLocationPermission()) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, (task) -> {
                    if (task.isSuccessful()) {
                        lastKnownLocation = task.getResult();
                        if (cameraPosition == null && lastKnownLocation != null)
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(lastKnownLocation.getLatitude(),
                                            lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                    } else {
                        if (cameraPosition == null)
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                        mMap.getUiSettings().setMyLocationButtonEnabled(false);
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void updateLocationUI() {
        try {
            if (hasLocationPermission()) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    @SuppressLint("MissingPermission")
    private void registerLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000)
                .setSmallestDisplacement(10)
                .setMaxWaitTime(1000)
                .setFastestInterval(10000);

        if (hasLocationPermission() && locationCallback != null)
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void deregisterLocationUpdates() {
        if (locationCallback != null)
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    public void onLocationChanged(Location lastLocation) {
        Log.i("MAP", "Location Changed");
        lastKnownLocation = lastLocation;

        }


    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void getLocationPermission() {
        if (!hasLocationPermission()) {
            ActivityCompat.requestPermissions(this,
                    new String[]{(android.Manifest.permission.ACCESS_FINE_LOCATION)},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateLocationUI();
                    getDeviceLocation();
                }
            }
        }
    }

    public boolean onMarkerClick(Marker marker) {
        /*Bundle args = new Bundle();
        args.putInt(POIFragment.KEY_POI, (Integer) marker.getTag());
        POIFragment poiFragment = new POIFragment();
        poiFragment.setArguments(args);
        poiFragment.show(getSupportFragmentManager(), "POI");*/
        return true;
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
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
            /*Bundle args = new Bundle();
            args.putInt(POIFragment.KEY_POI, intent.getIntExtra("ID", 0));
            POIFragment poiFragment = new POIFragment();
            poiFragment.setArguments(args);
            poiFragment.show(getSupportFragmentManager(), "POI");*/
        }
    }
}

