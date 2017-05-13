package com.nicefontaine.matcha.network;


import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;


public interface NominatimService {

    @GET("/search")
    Call<List<Place>> getLocations(@Query("format") String format,
                                   @Query("q") String q);
}
