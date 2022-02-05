package com.example.maps_and_location;

import android.app.Application;
import android.location.Location;

import java.util.ArrayList;
import java.util.List;

public class MyApplication extends Application {

    private static MyApplication singleton;

    public static MyApplication getSingleton() {
        return singleton;
    }

    public List<Location> getMyLocation() {
        return myLocation;
    }



    private List<Location> myLocation;

    public MyApplication getInstance(){
        return singleton;

    }

    @Override
    public void onCreate() {
        super.onCreate();

        singleton = this;

        myLocation = new ArrayList<>();

    }
}
