package com.nicefontaine.matcha.utils;


import android.support.annotation.NonNull;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;


public class MapDrawUtils {

    private static final double DELTA = 0.001;

    public static void clearMarkers(List<Marker> markers) {
        for (Marker marker : markers) {
            marker.remove();
        }
    }

    @NonNull
    public static Polyline drawPolyline(GoogleMap map, List<LatLng> coordinates, int color) {
        return map.addPolyline(
                new PolylineOptions()
                        .color(color)
                        .width(10)
                        .addAll(coordinates)
                        .zIndex(1)
        );
    }

    @NonNull
    public static Marker drawMarker(GoogleMap map, LatLng coordinate) {
        return map.addMarker(
                new MarkerOptions()
                        .position(coordinate)
                        .anchor(0.5F, 1.0F)
        );
    }

    public static LatLngBounds buildBBox(final List<LatLng> latLngs) {
        LatLngBounds.Builder bBox = new LatLngBounds.Builder();
        for (LatLng latLng : latLngs) {
            buildPointBBox(bBox, latLng);
        }
        return bBox.build();
    }

    private static void buildPointBBox(LatLngBounds.Builder bBox, LatLng latLng) {
        bBox.include(new LatLng(latLng.latitude + DELTA, latLng.longitude + DELTA));
        bBox.include(new LatLng(latLng.latitude - DELTA, latLng.longitude - DELTA));
    }
}
