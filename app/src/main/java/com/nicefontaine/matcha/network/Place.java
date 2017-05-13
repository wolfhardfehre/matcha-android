package com.nicefontaine.matcha.network;


import com.google.gson.annotations.SerializedName;

public class Place {

    @SerializedName("lat")
    public double latitude;

    @SerializedName("lon")
    public double longitude;
}
