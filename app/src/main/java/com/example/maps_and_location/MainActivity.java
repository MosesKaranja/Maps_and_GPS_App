package com.example.maps_and_location;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int FAST_UPDATE_INTERVAL = 2;
    private static final int DEFAULT_UPDATE_INTERVAL = 2;
    private static final int PERMISSIONS_FINE_LOCATION = 99;
    TextView tv_lat, tv_lon, tv_altitude, tv_address, tv_accuracy, tv_speed, tv_sensor, tv_updates, tv_Satellites, tv_waypointCounts;

    Switch sw_locationsupdates, sw_gps;

    //Variable to remember if we are tracking location or not
    boolean updateOn = false;

    //current location
    Location currentLocation;

    //List of saved locations
    List<Location> savedLocationsList;

    //Google API for accessing locations
    FusedLocationProviderClient fusedLocationProviderClient;

    //This is a config file that affects the FusedLocationProviderClient
    LocationRequest locationRequest;

    LocationCallback locationCallback;

    Button new_waypoint, show_waypoint_list;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setTitle("Location Activity");

        tv_lat = findViewById(R.id.tv_lat);
        tv_lon = findViewById(R.id.tv_lon);
        tv_altitude = findViewById(R.id.tv_altitude);
        tv_accuracy = findViewById(R.id.tv_accuracy);
        tv_speed = findViewById(R.id.tv_speed);
        tv_sensor = findViewById(R.id.tv_sensor);
        tv_updates = findViewById(R.id.tv_updates);
        tv_Satellites = findViewById(R.id.tv_Satellites);
        tv_address = findViewById(R.id.tv_address);
        tv_waypointCounts = findViewById(R.id.tv_countOfCrumbs);

        sw_gps = findViewById(R.id.sw_gps);
        sw_locationsupdates = findViewById(R.id.sw_locationsupdates);

        new_waypoint = findViewById(R.id.new_waypoint);
        show_waypoint_list = findViewById(R.id.show_waypoint_list);

        locationRequest = LocationRequest.create()
                .setInterval(1000 * DEFAULT_UPDATE_INTERVAL)
                .setFastestInterval(1000 * FAST_UPDATE_INTERVAL)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(1000 * DEFAULT_UPDATE_INTERVAL);
        //Max wait time is not necessary and also setFastestInterval is not necessary.

        new_waypoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Get the gps location

                //Add the new location to the global list
                MyApplication myApplication = (MyApplication) getApplicationContext();
                savedLocationsList = myApplication.getMyLocation();
                savedLocationsList.add(currentLocation);

                //Show the number of waypoints
                tv_waypointCounts.setText(Integer.toString(savedLocationsList.size()));

            }
        });

        show_waypoint_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this,ShowSavedLocationsList.class);
                startActivity(intent);

            }
        });

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                Location location = locationResult.getLastLocation();

                Log.i("GotLocation","GotLocation ===>");

                updateUIValues(location);

                //Add the new location to the global list
                MyApplication myApplication = (MyApplication) getApplicationContext();
                savedLocationsList = myApplication.getMyLocation();
                savedLocationsList.add(location);

                //Show the number of waypoints
                tv_waypointCounts.setText(Integer.toString(savedLocationsList.size()));

            }

            @Override
            public void onLocationAvailability(@NonNull LocationAvailability locationAvailability) {
                super.onLocationAvailability(locationAvailability);

                if (locationAvailability.isLocationAvailable()) {
                    Log.i("LocationAvailable", "Location is available");

                } else {
                    Log.i("Location_Not_Available", "Location is not available");

                }
            }
        };

        sw_gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sw_gps.isChecked()) {
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    tv_sensor.setText("Using GPS Sensors");

                } else {
                    locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                    tv_sensor.setText("Using Towers + WIFI");

                }

            }
        });

        sw_locationsupdates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sw_locationsupdates.isChecked()) {
                    //Turn on location tracking
                    Log.i("InHere","Launched In Here. ");
                    startLocationUpdates();

                } else {
                    //Stop Location Updates
                    stopLocationUpdates();
                }
            }
        });

        updateGPS();


    }
    //End of OnCreate

    private void startLocationUpdates() {
        tv_updates.setText("Location is being tracked");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            //return;
            Toast.makeText(this, "Location Permission is required", Toast.LENGTH_SHORT).show();
            //Add Code below here to ask for that permission
        }
        else{
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
            updateGPS();
        }


    }

    private void stopLocationUpdates(){
        tv_updates.setText("Location is NOT being Trackesd");
        tv_lat.setText("Not Tracking Location");
        tv_lon.setText("Not Tracking Location");
        tv_speed.setText("Not Tracking Location");
        tv_address.setText("Not Tracking Location");
        tv_accuracy.setText("Not Tracking Location");
        tv_altitude.setText("Not Tracking Location");
        tv_sensor.setText("Not Tracking Location");

        fusedLocationProviderClient.removeLocationUpdates(locationCallback);

    }




    private void updateGPSUsingGetCurrentLocation(){



        //Get Permissions from the user to track GPS
        //Get the current location from the fused client
        //Update the UI - i.e. set all properties in theur associated text view items
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            CancellationTokenSource cts = new CancellationTokenSource();
            CancellationToken tokenForCancel =  cts.getToken();

            fusedLocationProviderClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY,tokenForCancel).addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    Toast.makeText(MainActivity.this, "Yeah yeye", Toast.LENGTH_SHORT).show();
                    if (location == null){
                        Toast.makeText(MainActivity.this, "Failed To Get Location Again", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        updateUIValues(location);

                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this, "Getting Location Has Failed", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            });

        }
        else{
            //Permission not granted yet.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);

            }

        }

    }

    private void updateGPS(){
        //Get Permissions from the user to track GPS
        //Get the current location from the fused client
        //Update the UI - i.e. set all properties in theur associated text view items
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    //We got permissions. Put the values of location, XXX into the UI components

                    if (location == null){
                        //Toast.makeText(MainActivity.this, "Location Object is Null", Toast.LENGTH_SHORT).show();
                        updateGPSUsingGetCurrentLocation();
                    }
                    else{
                        updateUIValues(location);

                        currentLocation = location;

                    }


                }
            });

        }
        else{
            //Permission not granted yet.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);
            }

        }

    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //Request code is what permission we have requested in our case 99

        switch (requestCode){
            case PERMISSIONS_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    updateGPS();

                }
                else{
                    Toast.makeText(this, "This app requires permission.", Toast.LENGTH_SHORT).show();
                    finish();

                }

                break;
        }


    }


    private void updateUIValues(Location location) {
        //Update All TextView Objects with new location
        Log.i("UpdatedUI","Updated UI =)=======>");
        tv_lat.setText(String.valueOf(location.getLatitude()));
        tv_lon.setText(String.valueOf(location.getLongitude()));
        tv_accuracy.setText(String.valueOf(location.getAccuracy()));

        if (location.hasAltitude()){
            tv_altitude.setText(String.valueOf(location.getAltitude()));
        }
        else{
            tv_altitude.setText("Device Cant Get Altitude");
        }

        if (location.hasSpeed()){
            tv_speed.setText(String.valueOf(location.getSpeed()));
        }
        else{
            tv_speed.setText("Device Cant Get Speed");
        }

        Geocoder geocoder = new Geocoder(MainActivity.this);

        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

            String myGeocode = addresses.get(0).getCountryName() + addresses.get(0).getCountryCode() + "\n" + addresses.get(0).getAddressLine(0) + "\n" +addresses.get(0).getPhone();
            tv_address.setText(myGeocode);

        }
        catch(Exception e){
            tv_address.setText("Unable to Get Street Address");
            e.printStackTrace();

        }






    }

    /**
     * {@inheritDoc}
     * <p>
     * Dispatch onPause() to fragments.
     */
    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();

    }
}