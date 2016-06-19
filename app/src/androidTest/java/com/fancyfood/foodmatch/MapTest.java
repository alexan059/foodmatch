package com.fancyfood.foodmatch;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;

import com.fancyfood.foodmatch.activities.MapsActivity;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

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
        //Simulation of sendDestination (MainActivity)
        Intent i=new Intent();
        LatLng destination = new LatLng(52.523986, 13.402637);                              // passing Data to MapsActivity
        i.putExtra("Lat", destination.latitude);                                            //add data to Intent to use them in MapActivity
        i.putExtra("Lng", destination.longitude);
        setActivityIntent(i);                                                               //setIntent
        mapTest=getActivity();                                                              //start Activity

    }
    public void testPreconditions() {                                                       //check if Activity exist
        assertNotNull(mapTest);
    }
    public void testSetOrigin(){                                                            //test of get and set method
        LatLng expectedValue = new LatLng(52.523986, 13.402637);
        mapTest.setOrigin(expectedValue);
        LatLng actValue=mapTest.getOrigin();
        assertEquals(expectedValue,actValue);
    }
    public void testSetDestination(){                                                       //test  Intent and Data processing
        LatLng expectedValue = new LatLng(52.523986, 13.402637);
        LatLng actValue=mapTest.getDestination();
        assertEquals(expectedValue,actValue);
    }
    public void testCurrentLocation(){                                                      //current Location has not to be NULL
        LatLng actValue=mapTest.getOrigin();
        assertNotNull(actValue);
    }

    public void testCalculateRoute(){
        LatLng origin = new LatLng(52.526909, 13.405209);
        mapTest.setOrigin(origin);
        LatLng destination = new LatLng(52.523986, 13.402637);

        mapTest.requestDirection(destination);                                              //onDirectionSuccess seems to be not called -> directionPoints=null -> test failed so far
        ArrayList<LatLng>directionPoints= mapTest.getSectionPositionList();
        assertNotNull(directionPoints);

    }
}