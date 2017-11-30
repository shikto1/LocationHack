package com.example.shishir.locationhack.Activity;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.shishir.locationhack.Model_Class.MyLocation;
import com.example.shishir.locationhack.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String dateStr;
    private ArrayList<MyLocation> locationList;
    private TextView dateTv;
    private MyLocation singleLoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent intent = getIntent();
        dateStr = intent.getStringExtra("date");
        locationList = (ArrayList<MyLocation>) intent.getSerializableExtra("locList");
        singleLoc = locationList.get(0);
        findViewById();
    }

    private void findViewById() {
        dateTv = (TextView) findViewById(R.id.dateTv_at_maps);
        dateTv.setText(dateStr);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        Double lat = Double.valueOf(singleLoc.getLat());
        Double lng = Double.valueOf(singleLoc.getLng());
        LatLng loc = new LatLng(lat, lng);


        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(loc);
        markerOptions.title(singleLoc.getTime());
        mMap.addMarker(markerOptions).showInfoWindow();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 10));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_from_left,R.anim.slide_to_right);
    }
}
