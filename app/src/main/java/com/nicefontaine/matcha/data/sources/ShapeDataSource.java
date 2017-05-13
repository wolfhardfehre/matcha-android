package com.nicefontaine.matcha.data.sources;


import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.Map;


public interface ShapeDataSource {

    interface ZonesCallback {

        void onZones(Map<String, List<LatLng>> zones);

        void onError();
    }

    void getShapes(@NonNull ZonesCallback callback);
}
