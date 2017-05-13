package com.nicefontaine.matcha.network;


import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class TicketResponse {

    @SerializedName("tickets")
    public ArrayList<Ticket> tickets;

    public class Ticket {

        @SerializedName("name")
        public String name;

        @SerializedName("price")
        public int price;

        @SerializedName("duration")
        public String duration;

        @SerializedName("person_number")
        public int people;

        @SerializedName("modes")
        public ArrayList<String> modes;

        public String getPrice() {
            return String.format("%s €", new DecimalFormat("0.00").format((price / 100D)));
        }

        public String getPeople() {
            return String.format(people == 1 ? "%s pers." : "%s ppl.", people);
        }

        @Override
        public String toString() {
            return new Gson().toJson(this);
        }
    }
}
