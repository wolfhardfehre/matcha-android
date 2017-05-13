package com.nicefontaine.matcha.data.sources;


import android.support.annotation.NonNull;

import com.nicefontaine.matcha.network.Place;
import com.nicefontaine.matcha.network.NominatimService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class LocationRemoteDataSource implements LocationDataSource {

    private static LocationRemoteDataSource instance;
    private final NominatimService nominatimService;

    public static LocationRemoteDataSource getInstance(@NonNull NominatimService nominatimService) {
        if (instance == null) {
            instance = new LocationRemoteDataSource(nominatimService);
        }
        return instance;
    }

    private LocationRemoteDataSource(@NonNull NominatimService nominatimService) {
        this.nominatimService = nominatimService;
    }

    @Override
    public void getLocation(@NonNull String query, @NonNull final LocationCallback callback) {
        Call<List<Place>> call = nominatimService.getLocations("json", query);
        call.enqueue(new Callback<List<Place>>() {

            @Override
            public void onResponse(Call<List<Place>> call, Response<List<Place>> response) {
                if (response != null && response.isSuccessful()) {
                    List<Place> locations = response.body();
                    callback.onLocation(locations);
                } else {
                    callback.onError();
                }
            }

            @Override
            public void onFailure(Call<List<Place>> call, Throwable t) {
                callback.onError();
            }
        });
    }
}
