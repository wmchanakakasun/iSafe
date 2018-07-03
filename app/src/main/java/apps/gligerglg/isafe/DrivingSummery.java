package apps.gligerglg.isafe;

import android.arch.persistence.room.Room;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class DrivingSummery extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SummeryInfo summeryInfo;
    private List<SpeedMarker> speedMap;

    private static final int lowerSpeed = 12;
    private static final int midSpeed = 17;

    private double totalTime = 0;
    private double totalDistance = 0;
    private double averageSpeed = 0;

    private TripDB tripDB;
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

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(summeryInfo.isEndJourney())
                    saveSummery();
                finish();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        updateUI();
    }

    private void Init() {
        String dataset = getIntent().getStringExtra("summeryInfo");
        if(dataset!=null){
            Gson gson = new Gson();
            summeryInfo = gson.fromJson(dataset,SummeryInfo.class);
            speedMap = summeryInfo.getSpeedMarkerList();
        }

        tripDB = Room.databaseBuilder(getApplicationContext(),TripDB.class,"TripDB").fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build();

        txt_totalDistance = findViewById(R.id.txt_summery_distance);
        txt_totalTime = findViewById(R.id.txt_summery_time);
        txt_averageSpeed = findViewById(R.id.txt_summery_speed);
        txt_scoreAddIncident = findViewById(R.id.txt_summery_addIncident);
        txt_scoreRemoveIncident = findViewById(R.id.txt_summery_removeIncident);
        txt_scoreOverSpeed = findViewById(R.id.txt_summery_overSpeed);
        txt_scoreTotal = findViewById(R.id.txt_summery_totalScore);
        btnSave = findViewById(R.id.btn_summery_save);

        if(summeryInfo.isEndJourney())
            btnSave.setText("SAVE AND END JOURNEY");
        else
            btnSave.setText("BACK TO NAVIGATION");

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
                        .snippet(MapController.generateSpeedString(marker.getSpeed()))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.low_speed_marker)));
            else if(marker.getSpeed()<midSpeed)
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(marker.getLatitude(), marker.getLongitude()))
                        .title("Mid Speed")
                        .snippet(MapController.generateSpeedString(marker.getSpeed()))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.mid_speed_marker)));
            else if(marker.getSpeed()>=midSpeed)
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(marker.getLatitude(), marker.getLongitude()))
                        .title("Higher Speed")
                        .snippet(MapController.generateSpeedString(marker.getSpeed()))
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

        txt_totalDistance.setText(MapController.generateDistanceString(totalDistance));
        txt_totalTime.setText(MapController.generateTimeString(totalTime));
        txt_averageSpeed.setText(MapController.generateSpeedString(averageSpeed));
        txt_scoreAddIncident.setText("" + addIncidentScore);
        txt_scoreRemoveIncident.setText("" + removeIncidentScore);
        txt_scoreOverSpeed.setText("" + overSpeedScore);
        txt_scoreTotal.setText("" + totalScore);
    }

    private void saveSummery(){
        String speedMarkerList = new Gson().toJson(speedMap);
        Trip trip = new Trip();
        trip.setDateTime(summeryInfo.getStartTime());
        trip.setEarned_score(addIncidentScore + removeIncidentScore);
        trip.setReduced_score(overSpeedScore);
        trip.setTotal_score(addIncidentScore + removeIncidentScore - overSpeedScore);
        trip.setRoute(summeryInfo.getRoute());
        trip.setSpeedMarkerList(speedMarkerList);
        trip.setAverageSpeed(averageSpeed);
        trip.setTotalDistance(totalDistance);
        trip.setTotalDuration(totalTime);
        tripDB.tripDao().insertTrip(trip);
    }

}
