package com.nicefontaine.matcha.data.sources;


import android.support.annotation.NonNull;

import com.nicefontaine.matcha.data.Coordinate;
import com.nicefontaine.matcha.network.Ticket;

import java.util.List;


public interface TicketDataSource {

    interface TicketCallback {

        void onTickets(List<Ticket> tickets);

        void onError();
    }

    void getTickets(@NonNull List<Coordinate> coordinates, @NonNull TicketCallback callback);
}
