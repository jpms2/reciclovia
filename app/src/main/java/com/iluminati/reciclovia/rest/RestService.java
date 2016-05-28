package com.iluminati.reciclovia.rest;

import com.iluminati.reciclovia.model.Geojson;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by jpms2 on 27/05/2016.
 */
public interface RestService {
    @GET("2015-06-19T17%3A58%3A53.333Z/ciclo-faixa2-0.geojson")
    Call<Geojson> getGeojson();

}
