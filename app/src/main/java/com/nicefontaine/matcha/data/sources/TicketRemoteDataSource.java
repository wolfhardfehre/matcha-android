package com.nicefontaine.matcha.data.sources;


import android.support.annotation.NonNull;

import com.nicefontaine.matcha.data.Coordinate;
import com.nicefontaine.matcha.network.MatchaService;
import com.nicefontaine.matcha.network.Ticket;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class TicketRemoteDataSource implements TicketDataSource {

    private static TicketRemoteDataSource instance;
    private final MatchaService matchaService;

    public static TicketRemoteDataSource getInstance(@NonNull MatchaService matchaService) {
        if (instance == null) {
            instance = new TicketRemoteDataSource(matchaService);
        }
        return instance;
    }

    private TicketRemoteDataSource(@NonNull MatchaService matchaService) {
        this.matchaService = matchaService;
    }

    @Override
    public void getTickets(@NonNull List<Coordinate> coordinates,
                           @NonNull final TicketDataSource.TicketCallback callback) {
        Call<List<Ticket>> call = matchaService.getTickets();
        call.enqueue(new Callback<List<Ticket>>() {

            @Override
            public void onResponse(Call<List<Ticket>> call, Response<List<Ticket>> response) {
                if (response != null && response.isSuccessful()) {
                    List<Ticket> tickets = response.body();
                    callback.onTickets(tickets);
                } else {
                    callback.onError();
                }
            }

            @Override
            public void onFailure(Call<List<Ticket>> call, Throwable t) {
                callback.onError();
            }
        });
    }
}
