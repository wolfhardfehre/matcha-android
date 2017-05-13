package com.nicefontaine.matcha.data;


import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;


public class Zone {

    @SerializedName("coordinates")
    public ArrayList<ArrayList<ArrayList<Double>>> coordinates;

    public List<LatLng> getCoordinates() {
        List<LatLng> latLngs = new ArrayList<>();
        for (List<ArrayList<Double>> list : coordinates) {
            for (List<Double> coords : list) {
                latLngs.add(new LatLng(coords.get(1), coords.get(0)));
            }
        }
        return latLngs;
    }
}
