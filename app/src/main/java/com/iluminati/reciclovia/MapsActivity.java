package com.iluminati.reciclovia;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.github.jonaslins.fiware.FiwareOrionClient;
import com.github.jonaslins.fiware.request.FiwareCallback;
import com.github.jonaslins.fiware.request.model.Attribute;
import com.github.jonaslins.fiware.request.model.ContextElement;
import com.github.jonaslins.fiware.request.model.ContextResponse;
import com.github.jonaslins.fiware.request.model.Entity;
import com.github.jonaslins.fiware.request.model.FiwareResponse;
import com.github.jonaslins.fiware.request.model.QueryContext;
import com.github.jonaslins.fiware.request.model.UpdateAction;
import com.github.jonaslins.fiware.request.model.UpdateContext;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.iluminati.reciclovia.model.Bike;
import com.iluminati.reciclovia.model.Feature;
import com.iluminati.reciclovia.model.Geojson;
import com.iluminati.reciclovia.model.Ponto;
import com.iluminati.reciclovia.rest.RestController;
import com.iluminati.reciclovia.rest.RestService;
import com.iluminati.reciclovia.util.MapUtil;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, Callback<Geojson>,DirectionCallback {

    private GoogleMap mMap;
    private String SERVER_ADDRESS = "http://130.206.119.206:1026";

    private ArrayList<Ponto> pontos= new ArrayList<>();
    private ArrayList<Bike> bikes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        if(getIntent().getStringExtra("1")!=null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Context context = view.getContext();
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Sua viagem foi agradável?")
                            .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    ContextElement contextElement = new ContextElement("Rota", "63", false);
                                    contextElement.addAttribute(new Attribute("likes", "integer", "0"));
                                    UpdateContext updateContext = new UpdateContext();
                                    updateContext.setUpdateAction(UpdateAction.APPEND);
                                    updateContext.addContextElement(contextElement);

                                    //Asynchronous Request
                                    FiwareOrionClient.serverAddress(SERVER_ADDRESS)
                                            .updateContext(updateContext).asyncRequest(new FiwareCallback() {
                                        @Override
                                        public void onResponseSuccess(FiwareResponse fiwareResponse, String rawBody) {
                                                String algo=rawBody;
                                        }

                                        @Override
                                        public void onResponseFailure(Throwable t) {

                                        }
                                    });
                                    Intent intent = new Intent(MapsActivity.this,MapsActivity.class);
                                    startActivity(intent);
                                }
                            }).setNegativeButton("Não", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(MapsActivity.this,MapsActivity.class);
                            startActivity(intent);
                        }
                    })
                    ;
                    builder.create().show();
                }
            });
        }
        else
        {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MapsActivity.this, RouteSelection.class);
                    startActivity(intent);
                }
            });
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        if(getIntent().getStringExtra("1")!=null)
        ((FloatingActionButton) findViewById(R.id.fab)).setImageResource(R.drawable.ic_star_rate_white_18dp);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng hellcife = new LatLng(-8.060377, -34.872749);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hellcife, 14.5f));

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
            if(getIntent().getStringExtra("1")==null)mMap.addPolyline(polylineOptions);
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
                            String coord = attribute.getValue();
                            bikes.add(new Bike(coord));
                            String[] values = attribute.getValue().split(",");
//                            mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(values[1]), Double.parseDouble(values[0]))));
                            if(getIntent().getStringExtra("1")==null) mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(values[1]), Double.parseDouble(values[0]))).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_directions_bike_black_36dp)));

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

//        //Asynchronous Request
//        FiwareOrionClient.serverAddress(SERVER_ADDRESS)
//                .updateContext(updateContext)

        //Asynchronous Request
        FiwareOrionClient.serverAddress(SERVER_ADDRESS)
                .queryContext(queryContext)
                .asyncRequest(new FiwareCallback() {
                    @Override
                    public void onResponseSuccess(FiwareResponse fiwareResponse, String rawBody) {
                        List<ContextResponse> contextResponses = fiwareResponse.getContextResponses();

                        for (ContextResponse contextResponse : contextResponses) {
                            Attribute attribute = contextResponse.getContextElement().getAttributes().get(0);
                            Attribute name_attr = contextResponse.getContextElement().getAttributes().get(1);
                            String name = name_attr.getValue();
                            String coord = attribute.getValue();
                            pontos.add(new Ponto(name, coord));
                            String[] values = attribute.getValue().split(",");
                            if (getIntent().getStringExtra("1") == null)
                                mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(values[1]), Double.parseDouble(values[0]))).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_account_balance_black_36dp)).title(name));
                        }

                        final String routeId = getIntent().getStringExtra("1");
                        if (routeId != null) {
                            if (bikes.size() > 0 && pontos.size() > 0)
                                carregarRota(routeId);
                            else {
                                new Handler(getApplicationContext().getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            Thread.sleep(1500);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        carregarRota(routeId);
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onResponseFailure(Throwable t) {

                    }
                });
    }

    public String getPontoCoord(String name)
    {
        String retorno = "";
        for(int i = 0; i < pontos.size(); i++)
        {
            if(pontos.get(i).getName().equals(name))
            {
                return pontos.get(i).getCoord();
            }
        }
        return retorno;
    }

    public Location pontoMaisProx(Location loc)
    {
        float aux = 999999999;
        Location auxLoc = null;
        for(int i = 0; i < bikes.size(); i++)
        {
            Location bikeloc = new Location("Bike");
            String [] coords = bikes.get(i).getCoord().split(",");
            bikeloc.setLongitude(Double.parseDouble(coords[0]));
            bikeloc.setLatitude(Double.parseDouble(coords[1]));
            if(loc.distanceTo(bikeloc) < aux )
            {
                aux = loc.distanceTo(bikeloc);
                auxLoc = bikeloc;
            }
        }
        return auxLoc;
    }

    public void carregarRota(String routeIdStr){
        int routeId = Integer.parseInt(routeIdStr);
        switch (routeId)
        {
            case 1:

                Location mz = new Location("Marco Zero");
                String [] mz_coords = getPontoCoord("Marco Zero").split(",");
                mz.setLongitude(Double.parseDouble(mz_coords[0]));
                mz.setLatitude(Double.parseDouble(mz_coords[1]));
                mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(mz_coords[1]), Double.parseDouble(mz_coords[0]))).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_account_balance_black_36dp)).title("Marco Zero"));

                Location pmn  = new Location("Ponte Mauricio de Nassau");
                String [] pnm_coords = getPontoCoord("Ponte Mauricio de Nassau").split(",");
                pmn.setLongitude(Double.parseDouble(pnm_coords[0]));
                pmn.setLatitude(Double.parseDouble(pnm_coords[1]));
                mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(pnm_coords[1]), Double.parseDouble(pnm_coords[0]))).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_account_balance_black_36dp)).title("Ponte Mauricio de Nassau"));

                //Location aur  = new Location("Casorio da rua da aurora");
                //String [] aur_coords = getPontoCoord("Casorio da rua da aurora").split(",");
                //aur.setLongitude(Double.parseDouble(aur_coords[0]));
                //aur.setLatitude(Double.parseDouble(aur_coords[1]));
                //mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(aur_coords[1]), Double.parseDouble(aur_coords[0]))).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_account_balance_black_36dp)).title("Casorio da rua da aurora"));


                Location tsi  = new Location("Teatro Santa Izabel");
                String [] tsi_coords = getPontoCoord("Teatro Santa Izabel").split(",");
                tsi.setLongitude(Double.parseDouble(tsi_coords[0]));
                tsi.setLatitude(Double.parseDouble(tsi_coords[1]));
                mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(tsi_coords[1]), Double.parseDouble(tsi_coords[0]))).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_account_balance_black_36dp)).title("Teatro Santa Izabel"));

                Location msj  = new Location("Mercado Sao Jose");
                String [] msj_coords = {"-34.877724","-8.068495"};
                msj.setLongitude(Double.parseDouble(msj_coords[0]));
                msj.setLatitude(Double.parseDouble(msj_coords[1]));
                mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(msj_coords[1]), Double.parseDouble(msj_coords[0]))).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_account_balance_black_36dp)).title("Mercado Sao Jose"));

                Location b_mz = pontoMaisProx(mz);
                Location b_pmn = pontoMaisProx(pmn);
                //Location b_aur = pontoMaisProx(aur);
                Location b_tsi = pontoMaisProx(tsi);
                Location b_msj = pontoMaisProx(msj);

                mMap.addMarker(new MarkerOptions().position(new LatLng(b_mz.getLatitude(), b_mz.getLongitude())).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_directions_bike_black_36dp)));
                mMap.addMarker(new MarkerOptions().position(new LatLng(b_pmn.getLatitude(), b_pmn.getLongitude())).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_directions_bike_black_36dp)));
                //mMap.addMarker(new MarkerOptions().position(new LatLng(b_aur.getLatitude(), b_aur.getLongitude())).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_directions_bike_black_36dp)));
                mMap.addMarker(new MarkerOptions().position(new LatLng(b_tsi.getLatitude(), b_tsi.getLongitude())).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_directions_bike_black_36dp)));
                mMap.addMarker(new MarkerOptions().position(new LatLng(b_msj.getLatitude(), b_msj.getLongitude())).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_directions_bike_black_36dp)));


                List<LatLng> latLngs = new ArrayList<>();
                latLngs.add(new LatLng(b_pmn.getLatitude(), b_pmn.getLongitude()));
                //latLngs.add(new LatLng(b_aur.getLatitude(), b_aur.getLongitude()));
                latLngs.add(new LatLng(b_tsi.getLatitude(), b_tsi.getLongitude()));

                GoogleDirection.withServerKey("AIzaSyD9Vc8q8uM30dSAH4-ifLoIFwVqM9ouVBk")
                        .from(new LatLng(b_mz.getLatitude(), b_mz.getLongitude()))
                        .to(new LatLng(b_msj.getLatitude(), b_msj.getLongitude()))
                        .transportMode(TransportMode.BICYCLING)
                        .waypoints(latLngs)
                        .execute(this);


                break;
        }
    }

    @Override
    public void onDirectionSuccess(Direction direction, String rawBody) {

        if (direction.isOK()) {

            List<LatLng> directionPositionList = direction.getRouteList().get(0).getOverviewPolyline().getPointList();
            List<Integer> directionPositionOrder = direction.getRouteList().get(0).getWaypointOrder();
            mMap.addPolyline(DirectionConverter.createPolyline(this, directionPositionList, 4, Color.BLACK));
        }


    }

    @Override
    public void onDirectionFailure(Throwable t) {

    }
}
