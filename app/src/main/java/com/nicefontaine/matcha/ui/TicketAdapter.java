package com.nicefontaine.matcha.ui;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nicefontaine.matcha.R;
import com.nicefontaine.matcha.network.Ticket;

import java.util.List;


public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketHolder> {

    private LayoutInflater inflater;
    private List<Ticket> tickets;
    private SelectedCallback listener;

    interface SelectedCallback {
        void onSelected(Ticket ticket);
    }

    public TicketAdapter(Context context, SelectedCallback listener, List<Ticket> tickets) {
        this.inflater = LayoutInflater.from(context);
        this.listener = listener;
        setTickets(tickets);
    }

    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets;
    }

    @Override
    public TicketAdapter.TicketHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.ticket_card, parent, false);
        return new TicketAdapter.TicketHolder(view);
    }

    @Override
    public void onBindViewHolder(TicketAdapter.TicketHolder holder, int position) {
        final Ticket ticket = tickets.get(position);
        holder.ticketName.setText(ticket.name);
        holder.ticketPrice.setText(ticket.getPrice());
        holder.timeValidity.setText(ticket.duration);
        holder.peopleValidity.setText(ticket.getPeople());
        holder.layout.setOnClickListener(v -> listener.onSelected(tickets.get(position)));
    }

    @Override
    public int getItemCount() {
        return tickets.size();
    }

    class TicketHolder extends RecyclerView.ViewHolder {

        private TextView ticketName;
        private TextView ticketPrice;
        private TextView timeValidity;
        private TextView peopleValidity;
        private LinearLayout layout;

        TicketHolder(View itemView) {
            super(itemView);
            ticketName = (TextView) itemView.findViewById(R.id.ticket_name);
            ticketPrice = (TextView) itemView.findViewById(R.id.ticket_price);
            timeValidity = (TextView) itemView.findViewById(R.id.time_validity);
            peopleValidity = (TextView) itemView.findViewById(R.id.people_validity);
            layout = (LinearLayout) itemView.findViewById(R.id.bottom_layout);
        }
    }
}

