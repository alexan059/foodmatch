package com.fancyfood.foodmatch.helpers;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;

public class GeoHelper {

    static LatLng getOrigin(Context context) {
        // Get location manager from system service
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        // Check if GPS is enabled
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return null;
        }



        // 1. GPS prüfen
        // 2. Standort bekommen
        // 3. Konvertieren
        // 4. LatLng Object zurückgeben

        return null;
    }

    static void startDirectionAcitivity(LatLng origin, LatLng destination) {
        //
    }
}
