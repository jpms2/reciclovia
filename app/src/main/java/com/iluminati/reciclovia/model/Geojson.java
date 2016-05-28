package com.iluminati.reciclovia.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Geojson {

    @SerializedName("type")
    @Expose
    public String type;
    @SerializedName("features")
    @Expose
    public List<Feature> features = new ArrayList<Feature>();

}