package com.iluminati.reciclovia.util;

import android.app.Activity;
import android.graphics.Bitmap;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.iluminati.reciclovia.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapUtil {
    
//    public static final LatLng CENTER_POINT = new LatLng(-8.0524376, -34.9511914);
//    public static final LatLng DESTINATION_POINT = new LatLng(-8.052276, -34.946933);
//
//    public static final LatLng[] ENTRY_POINTS = { new LatLng(-8.051995, -34.946845),
//                                                new LatLng(-8.055428, -34.956152),
//                                                new LatLng(-8.046545, -34.950556)};
//
//    public static final LatLng[] EXIT_POINTS = { new LatLng(-8.052300, -34.946882),
//                                                new LatLng(-8.055428, -34.956152),
//                                                new LatLng(-8.046545, -34.950556)};

    //for Riomar demo
    public static final LatLng CENTER_POINT = new LatLng(-8.086040, -34.894164);
    public static final LatLng DESTINATION_POINT = new LatLng(-8.052276, -34.946933);

    public static final LatLng[] ENTRY_POINTS = { new LatLng(-8.051995, -34.946845),
            new LatLng(-8.055428, -34.956152),
            new LatLng(-8.046545, -34.950556)};

    public static final LatLng[] EXIT_POINTS = { new LatLng(-8.052300, -34.946882),
            new LatLng(-8.055428, -34.956152),
            new LatLng(-8.046545, -34.950556)};

    public static final float ZOOM = 19;




    public static void removeMapMarkers(List<Marker> markers) {
        if(markers==null) return;
        for (Marker marker: markers) {
            removeMarker(marker);
        }
    }

    public static void removeMarker(Marker marker){
        if(marker!=null) marker.remove();
    }

    public static void setMapMarkersVisible(List<Marker> markers, boolean visible) {
        if(markers==null) return;
        for (Marker marker: markers) {
            marker.setVisible(visible);
        }
    }

    public static String getNativeGoogleMapsURL(LatLng origin, LatLng destination, List<LatLng> orderedWaypoints){
        String URL = "http://maps.google.com/maps?" +
                    "saddr=" + origin.latitude + "," + origin.longitude +
                    "&daddr=" + waypointsToString(orderedWaypoints) +
                    "+to:" + destination.latitude + "," + destination.longitude;
        return URL;
    }

    public static String getNativeGoogleMapsURL(String origin, String destination, List<String> waypoints){
        String URL = "http://maps.google.com/maps?" +
                "saddr=" + origin +
                "&daddr=" + waypointsToUrl(waypoints) +
                "+to:" + destination +
                "&dirflg=w"+
                "&zoom=18";
        return URL;
    }

    private static String waypointsToString(List<LatLng> waypoints){
        if (waypoints != null && !waypoints.isEmpty()) {
            String string = waypoints.get(0).latitude + "," + waypoints.get(0).longitude;
            for (int i = 1; i < waypoints.size(); i++) {
                string += "+to:" + waypoints.get(i).latitude + "," + waypoints.get(i).longitude;
            }
            return string;
        }
        return null;
    }

    private static String waypointsToUrl(List<String> waypoints){
        if (waypoints != null && !waypoints.isEmpty()) {
            String string = waypoints.get(0);
            for (int i = 1; i < waypoints.size(); i++) {
                string += "+to:" + waypoints.get(i);
            }
            return string;
        }
        return null;
    }

    public static List<LatLng> getOrderedPoints(List<LatLng> points, List<Integer> pointOrder){
        List<LatLng> orderedWaypoints = new ArrayList<>();
        for (int i = 0; i < pointOrder.size(); i++) {
            int order = pointOrder.get(i);
            orderedWaypoints.add(points.get(order));
        }
        return orderedWaypoints;
    }

    public static void removePolyline(Polyline directionPolyline) {
        if (directionPolyline != null) directionPolyline.remove();
    }
    public static void showMarkerInfoWindow(Marker marker){
        if(marker!=null) marker.showInfoWindow();
    }
    public static void setMakerVisible(Marker marker, boolean visible){
        if(marker!=null) marker.setVisible(visible);
    }

}