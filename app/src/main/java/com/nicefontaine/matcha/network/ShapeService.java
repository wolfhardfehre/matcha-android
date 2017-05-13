package com.nicefontaine.matcha.network;


import com.nicefontaine.matcha.data.Shapes;

import retrofit2.Call;
import retrofit2.http.GET;


public interface ShapeService {

    @GET("/shapes")
    Call<Shapes> getShapes();
}
