package com.nicefontaine.matcha.data.sources;


import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.nicefontaine.matcha.data.Shapes;
import com.nicefontaine.matcha.data.Zone;
import com.nicefontaine.matcha.network.ShapeService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ShapeRemoteDataSource implements ShapeDataSource {

    private static ShapeRemoteDataSource instance;
    private final ShapeService shapeService;

    public static ShapeRemoteDataSource getInstance(@NonNull ShapeService shapeService) {
        if (instance == null) {
            instance = new ShapeRemoteDataSource(shapeService);
        }
        return instance;
    }

    private ShapeRemoteDataSource(@NonNull ShapeService shapeService) {
        this.shapeService = shapeService;
    }

    @Override
    public void getShapes(@NonNull ZonesCallback callback) {
        Call<Shapes> call = shapeService.getShapes();
        call.enqueue(new Callback<Shapes>() {

            @Override
            public void onResponse(Call<Shapes> call, Response<Shapes> response) {
                if (response != null && response.isSuccessful()) {
                    Map<String, List<LatLng>> zones = new HashMap<>();
                    Zone a = response.body().zoneA;
                    Zone b = response.body().zoneB;
                    zones.put("VBB_A", a.getCoordinates());
                    zones.put("VBB_B", b.getCoordinates());
                    callback.onZones(zones);
                } else {
                    callback.onError();
                }
            }

            @Override
            public void onFailure(Call<Shapes> call, Throwable t) {
                callback.onError();
            }
        });
    }
}
