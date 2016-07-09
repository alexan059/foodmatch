package com.fancyfood.foodmatch.helpers;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.fancyfood.foodmatch.R;
import com.fancyfood.foodmatch.core.CoreApplication;
import com.fancyfood.foodmatch.preferences.Constants;
import com.fancyfood.foodmatch.services.StatusService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class GoogleApiLocationHelper implements OnConnectionFailedListener, ConnectionCallbacks, LocationListener, GpsStatus.Listener {

    private static final String TAG = GoogleApiLocationHelper.class.getSimpleName();

    public static final int MY_PERMISSIONS_COARSE_LOCATIONS = 1;
    public static final int MY_PERMISSIONS_FINE_LOCATIONS = 2;

    private OnLocationChangedListener listener;

    private GoogleApiClient googleApiClient;
    private Context context;

    private Location currentLocation;
    private LocationRequest locationRequest;

    public interface OnLocationChangedListener {
        void onLocationChanged(Location location);

        void onGpsDisabled();

        void showPermissionDialog();
    }

    public GoogleApiLocationHelper(Context context) {
        this.context = context;

        buildGoogleApiClient();
        connect();

        Log.d(TAG, "Location listener has been established.");
    }

    public GoogleApiClient getGoogleApiClient() {
        return googleApiClient;
    }

    private void buildGoogleApiClient() {
        // Create instance of GoogleAPIClient
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient
                    .Builder(context)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    public void connect() {
        if (googleApiClient != null && !googleApiClient.isConnected()) {
            googleApiClient.connect();
            Log.d(TAG, "Google Api Client connecting...");
        }
    }

    public void disconnect() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            stopLocationUpdates();
            googleApiClient.disconnect();
        }
    }

    public boolean isConnected() {
        return googleApiClient != null && googleApiClient.isConnected();
    }

    public void startDelayedService() {
        if (locationRequest == null && isConnected()) {
            createLocationRequests();
            startLocationUpdates();
        }
    }

    protected void createLocationRequests() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        // API 23+ Check explicitly for granted permissions or retrieve them
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            listener.showPermissionDialog();
        } else {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        }
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
    }

    public void setOnLocationChangedListener(OnLocationChangedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        listener.onLocationChanged(currentLocation);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        final LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        // API 23+ Check explicitly for granted permissions or retrieve them
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            listener.showPermissionDialog();
        } else if (!isGpsEnabled(context, false, false)) {
            listener.onGpsDisabled();
        } else {

            manager.addGpsStatusListener(this);

            // Get last location and store it
            currentLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

            // Show values of last location
            if (currentLocation != null) {
                listener.onLocationChanged(currentLocation);
            }

            createLocationRequests();
            startLocationUpdates();

            Log.d(TAG, "Google Api Client connected");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended: googleApiClient.connect()");
        googleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onGpsDisabled: connectionResult.toString() = " + connectionResult.toString());
    }

    public static boolean isGpsEnabled(Context context, boolean showMessage, boolean startService) {
        final LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean isEnabled = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!isEnabled) {
            if (startService) {
                Intent intent = new Intent(context, StatusService.class);
                intent.setData(Uri.parse(Constants.GPS_DISBALED));
                context.startService(intent);
            }

            if (showMessage) buildAlertMessageNoGps(context);
        }

        return isEnabled;
    }

    @Override
    public void onGpsStatusChanged(int event) {
        if (event == GpsStatus.GPS_EVENT_STOPPED) listener.onGpsDisabled();
    }

    /**
     * Method called when GPS is unavailable.
     */
    private static void buildAlertMessageNoGps(final Context context) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Kein Standort")
                .setMessage("Dein Standort kann nicht ermittelt werden. Bitte schalte GPS ein.")
                .setIcon(R.drawable.place)
                .setCancelable(false)
                .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        context.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("Nein", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
}
