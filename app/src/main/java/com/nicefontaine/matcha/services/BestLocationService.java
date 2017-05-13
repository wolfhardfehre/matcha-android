package com.nicefontaine.matcha.services;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.nicefontaine.matcha.MatchaApp;
import com.nicefontaine.matcha.data.DefaultEventBus;
import com.nicefontaine.matcha.data.OnLocationEvent;
import com.nicefontaine.matcha.utils.LocationUtils;

import javax.inject.Inject;

import timber.log.Timber;

import static com.nicefontaine.matcha.utils.LocationUtils.checkLocationPermission;


public class BestLocationService extends Service implements
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final long FIVE_SECONDS_MS = 5 * 1000;

    private GoogleApiClient googleApiClient;
    private LocationManager locationManager;
    private Location bestLocation;
    private long startTime;

    @Inject DefaultEventBus eventBus;

    @Override
    public void onCreate() {
        super.onCreate();
        ((MatchaApp) getApplicationContext()).getAppComponent().inject(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        googleApiClient = getApiClient();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        requestLocation();
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        googleApiClient.disconnect();
        super.onDestroy();
    }

    private void requestLocation() {
        checkLastLocationByProvider(LocationManager.NETWORK_PROVIDER);
        checkLastLocationByProvider(LocationManager.GPS_PROVIDER);
        if (LocationUtils.isLocationGoodEnough(bestLocation)) {
            broadcastLocationEvent();
        } else {
            googleApiClient.connect();
            startTime = System.currentTimeMillis();
        }
    }

    private void checkLastLocationByProvider(String provider) {
        if (!checkLocationPermission(this)) return;
        Location location = locationManager.getLastKnownLocation(provider);
        if (LocationUtils.isBetterLocation(location, bestLocation)) {
            bestLocation = location;
        }
    }


    private void broadcastLocationEvent() {
        disconnectGoogleApiClient();
        eventBus.post(new OnLocationEvent(bestLocation));
    }

    private void disconnectGoogleApiClient() {
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    private GoogleApiClient getApiClient() {
        return new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    private LocationRequest getLocationRequest() {
        return LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(1000)
                .setFastestInterval(1000);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        boolean isGranted = checkLocationPermission(this);
        if (isGranted) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    googleApiClient,
                    getLocationRequest(),
                    this
            );
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Timber.d("onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Timber.d("onConnectionFailed");
    }

    @Override
    public void onLocationChanged(Location location) {
        if (LocationUtils.isBetterLocation(location, bestLocation)) {
            bestLocation = location;
        }

        if (LocationUtils.isLocationGoodEnough(bestLocation)) {
            broadcastLocationEvent();
        } else if (System.currentTimeMillis() - startTime > FIVE_SECONDS_MS) {
            broadcastLocationEvent();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
