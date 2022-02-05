package com.example.maps_and_location;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

//This is foregroundService for Location
public class LocationService extends Service {
    public LocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}