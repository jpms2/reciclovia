package com.iluminati.reciclovia.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Feature {

    @SerializedName("type")
    @Expose
    public String type;
    @SerializedName("id")
    @Expose
    public Integer id;
    @SerializedName("properties")
    @Expose
    public Properties properties;
    @SerializedName("geometry")
    @Expose
    public Geometry geometry;

}
