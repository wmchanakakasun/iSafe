package apps.gligerglg.isafe;

import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DrivingSummery extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SummeryInfo summeryInfo;
    private List<SpeedMarker> speedMap;

    private static final int lowerSpeed = 10;
    private static final int midSpeed = 15;

    private double totalTime = 0;
    private double totalDistance = 0;
    private double averageSpeed = 0;

    private int addIncidentScore=0, removeIncidentScore=0, overSpeedScore=0, totalScore=0;

    private TextView txt_totalDistance, txt_totalTime, txt_averageSpeed;
    private TextView txt_scoreAddIncident, txt_scoreRemoveIncident, txt_scoreOverSpeed, txt_scoreTotal;
    private AppCompatButton btnSave;


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
        String dataset = getIntent().getStringExtra("summeryInfo");
        if(dataset!=null){
            Gson gson = new Gson();
            summeryInfo = gson.fromJson(dataset,SummeryInfo.class);
            speedMap = summeryInfo.getSpeedMarkerList();
        }

        txt_totalDistance = findViewById(R.id.txt_summery_distance);
        txt_totalTime = findViewById(R.id.txt_summery_time);
        txt_averageSpeed = findViewById(R.id.txt_summery_speed);
        txt_scoreAddIncident = findViewById(R.id.txt_summery_addIncident);
        txt_scoreRemoveIncident = findViewById(R.id.txt_summery_removeIncident);
        txt_scoreOverSpeed = findViewById(R.id.txt_summery_overSpeed);
        txt_scoreTotal = findViewById(R.id.txt_summery_totalScore);
        btnSave = findViewById(R.id.btn_summery_save);

        addIncidentScore = summeryInfo.getScore_addIncidents();
        removeIncidentScore = summeryInfo.getScore_removeIncidents();
        overSpeedScore = summeryInfo.getScore_overSpeed();
        totalScore = addIncidentScore + removeIncidentScore - overSpeedScore;
        if(totalScore<0)
            totalScore=0;
    }

    private void updateUI(){
        double time=0, speed=0;
        List<LatLng> route = new ArrayList<>();
        for(SpeedMarker marker : speedMap){

            if(marker.getSpeed()<lowerSpeed)
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(marker.getLatitude(), marker.getLongitude()))
                        .title("Lower Speed")
                        .snippet(generateSpeedString(marker.getSpeed()))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.low_speed_marker)));
            else if(marker.getSpeed()<midSpeed)
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(marker.getLatitude(), marker.getLongitude()))
                        .title("Mid Speed")
                        .snippet(generateSpeedString(marker.getSpeed()))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.mid_speed_marker)));
            else if(marker.getSpeed()>=midSpeed)
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(marker.getLatitude(), marker.getLongitude()))
                        .title("Higher Speed")
                        .snippet(generateSpeedString(marker.getSpeed()))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.high_speed_marker)));

            route.add(new LatLng(marker.getLatitude(), marker.getLongitude()));
            time++;
            speed+=marker.getSpeed();
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
        MapController.drawPolyline(getApplicationContext(),route,R.color.colorPrimaryDark,mMap);

        txt_totalDistance.setText(generateDistanceString(totalDistance));
        txt_totalTime.setText(generateTimeString(totalTime));
        txt_averageSpeed.setText(generateSpeedString(averageSpeed));
        txt_scoreAddIncident.setText("" + addIncidentScore);
        txt_scoreRemoveIncident.setText("" + removeIncidentScore);
        txt_scoreOverSpeed.setText("" + overSpeedScore);
        txt_scoreTotal.setText("" + totalScore);
    }

    private String generateDistanceString(double Distance){
        String distance = "";
        if(Distance>=1000) {
            distance += (Distance / 1000) + " km ";
            Distance%=1000;
        }
        if(Distance<1000)
            distance += String.format("%.2f",Distance) + " m ";
        return distance;
    }

    private String generateTimeString(double Time){
        String time = "";
        if(Time>=3600){
            time += (Time/3600) + " H ";
            Time%=3600;
        }

        if(Time>=60){
            time += (Time/60) + " m ";
            Time%=60;
        }

        if(Time<60)
            time += Time + " s ";

        return time;
    }

    private String generateSpeedString(double Speed){
        String speed = "";
        Speed *=(18/5.0);
        speed += String.format("%.2f",Speed) + " kmph";
        return speed;
    }

}
