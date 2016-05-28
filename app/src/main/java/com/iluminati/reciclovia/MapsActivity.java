package com.iluminati.reciclovia;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.github.jonaslins.fiware.FiwareOrionClient;
import com.github.jonaslins.fiware.request.FiwareCallback;
import com.github.jonaslins.fiware.request.model.Attribute;
import com.github.jonaslins.fiware.request.model.ContextResponse;
import com.github.jonaslins.fiware.request.model.Entity;
import com.github.jonaslins.fiware.request.model.FiwareResponse;
import com.github.jonaslins.fiware.request.model.QueryContext;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.iluminati.reciclovia.model.Feature;
import com.iluminati.reciclovia.model.Geojson;
import com.iluminati.reciclovia.rest.RestController;
import com.iluminati.reciclovia.rest.RestService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, Callback<Geojson> {

    private GoogleMap mMap;
    private String SERVER_ADDRESS = "http://130.206.119.206:1026";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapsActivity.this ,PreRouteActivity.class);
                startActivity(intent);
             }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng hellcife = new LatLng(-8.052380, -34.880191);
        mMap.addMarker(new MarkerOptions().position(hellcife).title("Estamos aqui"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hellcife, 15));

        carregarCiclovias();
        carregarPontosTuristicos();
        carregarPontosBikePE();
    }

    private void carregarCiclovias(){
        RestController restController = RestController.getInstance();
        RestService restService = restController.createService("http://dados.recife.pe.gov.br/storage/f/");
        restService.getGeojson().enqueue(this);
    }


    @Override
    public void onResponse(Call<Geojson> call, Response<Geojson> response) {
        Geojson geojson = response.body();

        for (Feature feature: geojson.features) {
            List<List<Double>> coordinates = feature.geometry.coordinates;
            PolylineOptions polylineOptions = new PolylineOptions().width(5).color(ContextCompat.getColor(this, R.color.blue));
            for (List<Double> coordLatLng: coordinates) {
                LatLng latLng = new LatLng(coordLatLng.get(1), coordLatLng.get(0));
                polylineOptions.add(latLng);
            }
            mMap.addPolyline(polylineOptions);
        }
    }

    @Override
    public void onFailure(Call<Geojson> call, Throwable t) {
        String asdjisa;
    }

    public void carregarPontosBikePE(){

        Entity entity = new Entity("Bike", ".*", true);
        QueryContext queryContext = new QueryContext();
        queryContext.addEntity(entity);

        //Asynchronous Request
        FiwareOrionClient.serverAddress(SERVER_ADDRESS)
                .queryContext(queryContext)
                .asyncRequest(new FiwareCallback() {
                    @Override
                    public void onResponseSuccess(FiwareResponse fiwareResponse, String rawBody) {
                        List<ContextResponse> contextResponses = fiwareResponse.getContextResponses();

                        for (ContextResponse contextResponse: contextResponses) {
                            Attribute attribute = contextResponse.getContextElement().getAttributes().get(0);
                            String[] values = attribute.getValue().split(",");
//                            mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(values[1]), Double.parseDouble(values[0]))));
                            mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(values[1]), Double.parseDouble(values[0]))).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_directions_bike_black_36dp)));

                        }
                    }

                    @Override
                    public void onResponseFailure(Throwable t) {

                    }
                });
    }

    public void carregarPontosTuristicos(){

        Entity entity = new Entity("Ponto", ".*", true);
        QueryContext queryContext = new QueryContext();
        queryContext.addEntity(entity);

        //Asynchronous Request
        FiwareOrionClient.serverAddress(SERVER_ADDRESS)
                .queryContext(queryContext)
                .asyncRequest(new FiwareCallback() {
                    @Override
                    public void onResponseSuccess(FiwareResponse fiwareResponse, String rawBody) {
                        List<ContextResponse> contextResponses = fiwareResponse.getContextResponses();

                        for (ContextResponse contextResponse: contextResponses) {
                            Attribute attribute = contextResponse.getContextElement().getAttributes().get(0);
                            String[] values = attribute.getValue().split(",");
                            mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(values[1]), Double.parseDouble(values[0]))).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_account_balance_black_36dp)));
                        }
                    }

                    @Override
                    public void onResponseFailure(Throwable t) {

                    }
                });
    }

}
