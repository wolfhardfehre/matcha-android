package com.nicefontaine.matcha.data.sources;


import android.support.annotation.NonNull;

import com.nicefontaine.matcha.network.Place;

import java.util.List;


public interface LocationDataSource {

    interface LocationCallback {

        void onLocation(List<Place> locations);

        void onError();
    }

    void getLocation(@NonNull String query, @NonNull LocationCallback callback);
}