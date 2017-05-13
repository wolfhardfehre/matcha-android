package com.nicefontaine.matcha.network;


import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;


public interface MatchaService {

    @GET("/tickets")
    Call<List<Ticket>> getTickets();
}
