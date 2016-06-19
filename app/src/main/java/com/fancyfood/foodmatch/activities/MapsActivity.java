package com.fancyfood.foodmatch.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.RequestResult;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Step;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.fancyfood.foodmatch.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, DirectionCallback {

    String serverKey = "AIzaSyBd65lCjwNb-1W03t4XZTfqQu9yjUhyssY";                                   //server Key for Google Direction API
    private LatLng origin;                                                                          //position of start address
    private LatLng camera;                                                                          //focus position
    private LocationManager manager;                                                                //for getting geo location
    private LocationListener listener;                                                              //for listening changes of geo location
    private Location Loc;
    private String provider;
    private GoogleMap mMap;                                                                         // Might be null if Google Play services APK is not available.
    private LatLng destination;
    private ArrayList<LatLng> sectionPositionList;


    public void setOrigin(LatLng origin) {
        this.origin = origin;
    }
    public LatLng getOrigin() {
        return this.origin;
    }
    public LatLng getDestination() {
        return this.destination;
    }
    public ArrayList<LatLng> getSectionPositionList() {return this.sectionPositionList;}
    public void setCamera(LatLng camera) {
        this.camera = camera;
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);            //trigger von onMapReady

        Intent DataIntent = getIntent();
        destination = new LatLng(DataIntent.getDoubleExtra("Lat", 0), DataIntent.getDoubleExtra("Lng", 0));        //get Date from MainActivity

        getCurrentLocation();                                                                                       //get StartLocation
        requestDirection(destination);                                                                              //call requestDirection for calculation and drawing the route
    }

    @Override
    public void onMapReady(GoogleMap mMap) {                                                                        //when map ist ready, show camera position
        this.mMap = mMap;
    }

    public void getCurrentLocation() {

        manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);                                 //get available Location_Service

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);                                                               //best accuracy
        criteria.setPowerRequirement(Criteria.POWER_HIGH);                                                          //allow high power consumption
        provider = manager.getBestProvider(criteria, true);                                                         //find the best provider problem even when GPS is unavailable GPS becomes best provider

        listener = new LocationListener() {
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }

            @Override
            public void onLocationChanged(Location location) {                                                          //called when Location checked
                LatLng origin;
                if (location != null) {
                    origin = new LatLng(location.getLatitude(), location.getLongitude());
                } else {
                    origin = new LatLng(52.455521, 13.526905);
                }
                setOrigin(origin);
                setCamera(origin);
            }
        };

    }


    public void requestDirection(LatLng destination) {
        boolean ErrFlg = false;
        if (manager.isProviderEnabled(provider)) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            //inform me, if the location has changed, if yes
            //go to onLocationChanged and set the "new" origin
            manager.requestLocationUpdates(provider, 0, 0, listener);                               //listener: a LocationListener whose onLocationChanged(Location) method will be called for each location update

        }
        if (origin==null)
        {
            Loc = manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);                   //second option: get last know location through Internet/WIFI or mobile-data
            if (Loc!=null) {
                origin=new LatLng(Loc.getLatitude(), Loc.getLongitude());
                camera=new LatLng(Loc.getLatitude(), Loc.getLongitude());
            }else {
                ErrFlg=true;
            }
        }
        if(origin!=null || !ErrFlg){
            GoogleDirection.withServerKey(serverKey)                                                //Send origin and destination to  GoogleDirection, for ROUTE CALCULATION
                    .from(origin)
                    .to(destination)
                    .transportMode(TransportMode.TRANSIT)
                    .execute(this);
        }else{
            Toast.makeText(getApplicationContext(), "unknown last location ", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDirectionSuccess(Direction direction, String rawBody) {                           //Method to DRAW THE ROUTE

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(camera, 14));                          //set start Position and zoomlevel
        String status = direction.getStatus();                                                      //if connection was successful get status from Google Directions

        if(status.equals(RequestResult.OK)) {                                                       //if Status is ok (if route was found)
            sectionPositionList = direction.getRouteList().get(0).getLegList().get(0).getSectionPoint(); //get arrayList of route points

            for (LatLng position : sectionPositionList) {                                           //draw every route point on the map
                mMap.addMarker(new MarkerOptions().position(position));
            }
            List<Step> stepList = direction.getRouteList().get(0).getLegList().get(0).getStepList(); //get Route Steps
                                                                                                     //create Colors according to step list
            ArrayList<PolylineOptions> polylineOptionList = DirectionConverter.createTransitPolyline(this, stepList, 5, Color.RED, 3, Color.BLUE);

            for (PolylineOptions polylineOption : polylineOptionList) {                             //draw route with polylines
                mMap.addPolyline(polylineOption);
            }
        } else if(status.equals(RequestResult.NOT_FOUND)) {
            Toast.makeText(getApplicationContext(), "no route found ", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDirectionFailure(Throwable t) {

        Toast.makeText(getApplicationContext(), "error while calculating route", Toast.LENGTH_SHORT).show();
    }
}
