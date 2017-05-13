package com.nicefontaine.matcha.network;


import retrofit2.Call;
import retrofit2.http.GET;

public interface MatchaService {

    @GET("/test")
    Call<TicketResponse> getTickets();
}
