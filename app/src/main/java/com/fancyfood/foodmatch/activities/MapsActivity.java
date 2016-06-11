package com.fancyfood.foodmatch.activities;

import android.Manifest;
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
    //server Key for Google Direction API
    String serverKey = "AIzaSyBd65lCjwNb-1W03t4XZTfqQu9yjUhyssY";
    //position of start and end address
    private LatLng origin;
    //private LatLng destination= new LatLng(52.523986, 13.402637);
    //focus start position
    private LatLng camera;// = new LatLng(52.455521, 13.526905);
    private LocationManager manager;
    private LocationListener listener;
    private String provider;
    public static final String TAG = MapsActivity.class.getSimpleName();
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.


    public void setOrigin(LatLng origin) {
        this.origin = origin;
    }

    public void setCamera(LatLng camera) {
        this.camera = camera;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        //trigger von onMapReady
        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
        Intent DataIntent = getIntent();
        //get Date from MainActivity
        LatLng destination = new LatLng(DataIntent.getDoubleExtra("Lat", 0), DataIntent.getDoubleExtra("Lng", 0));

        //get StartLocation
        getCurrentLocation();

        //call requestDirection for calculation and drawing of the route
        requestDirection(destination);
    }

    public void getCurrentLocation() {

        manager = (LocationManager) getSystemService(LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        provider = manager.getBestProvider(criteria, true);

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
            public void onLocationChanged(Location location) {
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
        if (manager.isProviderEnabled(provider)) {
            manager.requestLocationUpdates(provider, 0, 0, listener);
        }
    }
	
    @Override
    public void onMapReady(GoogleMap mMap) {
        this.mMap = mMap;
		//set start Position and zoomlevel
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(camera, 14));
    }

    public void requestDirection(LatLng destination){
		//use the Android-GoogleDirectionLibrary option see http://www.akexorcist.com/2015/12/google-direction-library-for-android-en.html
        if (origin==null)
        {
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
            if (manager.isProviderEnabled(provider)){
            manager.requestSingleUpdate(provider,listener, null);
            }

        }
        if (origin==null)
        {

            manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0, listener);
        }
        if (origin==null)
        {
            Location Loc=manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            setOrigin(new LatLng(Loc.getLatitude(), Loc.getLongitude()));
            setCamera(new LatLng(Loc.getLatitude(), Loc.getLongitude()));
        }
        GoogleDirection.withServerKey(serverKey)
                .from(origin)		
                .to(destination)
                .transportMode(TransportMode.TRANSIT)
                .execute(this);
    }

    @Override
    public void onDirectionSuccess(Direction direction, String rawBody) {
		//if connection was sucessful get status from Google Directions
        String status = direction.getStatus();
		//if Status is ok (if round was found)
        if(status.equals(RequestResult.OK)) {
			//get arrayList of routepoint
            ArrayList<LatLng> sectionPositionList = direction.getRouteList().get(0).getLegList().get(0).getSectionPoint();
			//draw every route point on the map
            for (LatLng position : sectionPositionList) {
                mMap.addMarker(new MarkerOptions().position(position));
            }
			//get Route Steps
            List<Step> stepList = direction.getRouteList().get(0).getLegList().get(0).getStepList();
			//create Colors according to step list
            ArrayList<PolylineOptions> polylineOptionList = DirectionConverter.createTransitPolyline(this, stepList, 5, Color.RED, 3, Color.BLUE);
			//draw route with polylines
            for (PolylineOptions polylineOption : polylineOptionList) {
                mMap.addPolyline(polylineOption);
            }
        } else if(status.equals(RequestResult.NOT_FOUND)) {
            // Do something
        }
        // Do something here
    }

    @Override
    public void onDirectionFailure(Throwable t) {
        // Do something here
        int test=1;
    }
}
