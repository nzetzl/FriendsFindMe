package com.example.friendsfind.friendsfindme;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import java.io.UnsupportedEncodingException;

import java.net.URLEncoder;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;


/**
 * Created by nicholaszetzl on 10/17/15.
 */

public class MyLocationManager implements LocationListener, Runnable {

    public static String longString;
    public static String latString;
    public static double latitude = 0.0;
    public static double longitude = 0.0;

    @Override
    public void onLocationChanged(Location location) {
        longitude = location.getLongitude();
        latitude = location.getLatitude();
        longString = Double.toString(longitude);
        latString = Double.toString(latitude);
        Log.w("high", longString);
        Log.w("low", latString);
        get();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    public String get() {
        String urlName = "http://www.friendsfind.me/";
        String s = "";
        try {
            s = URLEncoder.encode(Arrays.toString(MainActivity.friendNumbers), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.v("high", String.valueOf(e));
        }
        //builds url to send get request
        String request = urlName + "api?number=" + MainActivity.phoneNumber + "&lat=" + latString + "&long=" +
                longString + "&friends=" + s;
        String returnValue = "";
        try {
            returnValue = new GetRequest().execute(request).get();
        } catch (ExecutionException e) {
            Log.v("high", String.valueOf(e));
        } catch (InterruptedException e) {
            Log.v("high", String.valueOf(e));
        }
        return returnValue;
    }

    @Override
    public void run() {
        String urlName = "http://www.friendsfind.me/";
        while(true) {
            try {
                get();
            } catch (Exception e) {
                Log.v("high", String.valueOf(e));
            }

        }
    }

}
