package com.nicefontaine.matcha.utils;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.content.ContextCompat;


public class LocationUtils {

    private static final float ACCURACY_THRESHOLD = 100;
    private static final long TIME_THRESHOLD = 30 * 1000;
    private static final long TWO_MINUTES_MS = 2 * 60 * 1000;

    public static boolean checkLocationServices(LocationManager locationManager) {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static boolean checkLocationPermission(Context context) {
        int permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionCheck == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isLocationGoodEnough(Location location) {
        return isLocationStillValide(location) && location.getAccuracy() < ACCURACY_THRESHOLD;
    }

    private static boolean isLocationStillValide(Location location) {
        long currentTime = System.currentTimeMillis();
        return location != null && currentTime - location.getTime() < TIME_THRESHOLD;
    }

    public static boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (location == null) return false;
        if (currentBestLocation == null) return true;

        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES_MS;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES_MS;
        boolean isNewer = timeDelta > 0;

        if (isSignificantlyNewer) {
            return true;
        } else if (isSignificantlyOlder) {
            return false;
        }

        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    private static boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) return provider2 == null;
        return provider1.equals(provider2);
    }
}
