package com.fancyfood.foodmatch;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;

import com.fancyfood.foodmatch.activities.MapsActivity;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Carolina on 18.06.2016.
 */
public class MapTest extends ActivityInstrumentationTestCase2<MapsActivity> {

    private MapsActivity mapTest;

    public MapTest() {
        super(MapsActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Intent i=new Intent();
        LatLng destination = new LatLng(52.523986, 13.402637);                              // Dummy for passing Data to MapsActivity
        i.putExtra("Lat", destination.latitude);                                            //add data to Intent to use them in MapActivity
        i.putExtra("Lng", destination.longitude);
        setActivityIntent(i);
        mapTest=getActivity();

    }
    public void testPreconditions() {
        assertNotNull("mapTest is null", mapTest);
    }
}