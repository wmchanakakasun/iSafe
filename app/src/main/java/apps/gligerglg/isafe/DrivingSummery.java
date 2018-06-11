package apps.gligerglg.isafe;

import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DrivingSummery extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SummeryInfo summeryInfo;
    private HashMap<LatLng,Double> speedMap;

    private static final int lowerSpeed = 10;
    private static final int midSpeed = 15;

    private double totalTime = 0;
    private double totalDistance = 0;
    private double averageSpeed = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driving_summery);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapSpeed);
        mapFragment.getMapAsync(this);

        Init();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        updateUI();
    }

    private void Init(){
        Bundle data = getIntent().getExtras();
        summeryInfo = data.getParcelable("summeryInfo");
        speedMap = new HashMap<>();

        for(int i=0;i<summeryInfo.getLatlngMap().size();i++)
            speedMap.put(summeryInfo.getLatlngMap().get(i),summeryInfo.getSpeedMap().get(i));

    }

    private void updateUI(){
        double time=0, speed=0;
        for(Map.Entry<LatLng,Double> entry : speedMap.entrySet()){

            if(entry.getValue()<lowerSpeed)
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(entry.getKey().latitude, entry.getKey().longitude))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.low_speed_marker)));
            else if(entry.getValue()<midSpeed)
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(entry.getKey().latitude, entry.getKey().longitude))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.mid_speed_marker)));
            else if(entry.getValue()>=midSpeed)
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(entry.getKey().latitude, entry.getKey().longitude))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.high_speed_marker)));

            time++;
            speed+=entry.getValue();
        }

        totalDistance = speed;
        averageSpeed = speed/time;
        totalTime = time;

        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(summeryInfo.getStart_location().latitude, summeryInfo.getStart_location().longitude))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.home_pin)));

        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(summeryInfo.getEnd_location().latitude, summeryInfo.getEnd_location().longitude))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.home_pin)));

        MapController.setCameraBounds(summeryInfo.getStart_location(),summeryInfo.getEnd_location(),mMap);
        MapController.drawPolyline(getApplicationContext(),summeryInfo.getLatlngMap(),R.color.colorPrimaryDark,mMap);

    }



}
