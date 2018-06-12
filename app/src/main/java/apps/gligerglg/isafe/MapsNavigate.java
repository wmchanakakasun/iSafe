package apps.gligerglg.isafe;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.os.Handler;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.gordonwong.materialsheetfab.MaterialSheetFab;
import com.tapadoo.alerter.Alerter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;

import plugins.gligerglg.locusservice.LocusService;

public class MapsNavigate extends FragmentActivity implements OnMapReadyCallback,TextToSpeech.OnInitListener {

    private GoogleMap mMap;
    private RouteInfo route;
    private LocusService locusService;
    private LatLng myLatLanLocation, speedLocation;
    private Location myLocation;

    private CoordinatorLayout layout;
    private Location bearingLocation = new Location("Bearing");
    private ImageButton btn_slideDown;
    private LinearLayout btn_voiceAssistant, btn_newIncident, btn_dayNight, btn_emergency;
    private CardView btn_incident_accident, btn_incident_traffic, btn_incident_hazard, btn_incident_clauser, btn_incident_flood, btn_incident_landslip;

    private ConstraintLayout driveInfoLayout, incidentLayout, incident_info_layout;
    private BottomSheetBehavior driveInfoSheet, incidentSheet, incident_info;
    private TextView txt_ETA, txt_distance, txt_dayNight, txt_voiceAssistant;

    private Fab fab;
    private FloatingActionButton btn_cam_ctrl;
    private MaterialSheetFab<Fab> materialSheetFab;
    private Handler bearingHandler, realtimeIncidentHandler;
    private Handler speedPointHandler, criticalLocationHandler, trafficSignHandler, blackspotHandler;
    private Handler speedCalcHandler;
    private int total_distance, total_duration;

    private static final int bearingInterval = 2000;

    private int locusInterval = 1000;
    private static final int realtimeRadius = 50;

    private ImageView img_position;
    FirebaseDatabase database;
    DatabaseReference myRef;

    private boolean isDayStyleEnabled = true;
    private boolean isMapDraggable = false;
    private boolean isMapReady = false;

    private ImageView img_incident;

    private TextView txt_incident_type, txt_incident_time, txt_incident_description;
    private AppCompatButton btn_remove_incident;

    private TextToSpeech textToSpeech;
    private int DATA_CHECK_CODE = 0;

    //Static Incident Lists
    private HashMap<String,RealtimeIncident> realtimeIncidentHashMap;
    private List<SpeedMarker>  speedMap;
    private List<SpeedLimitPoint> speedLimitPointList;
    private List<CriticalLocation> criticalLocationList;
    private List<TrafficSign> trafficSignList;
    private List<BlackSpot> blackSpotList;

    private int realtimeIncidentInterval = 2000;
    private int speedPointInterval = 1000;
    private int criticalLocationInterval = 1000;
    private int trafficSignInterval = 1000;
    private int blackspotInterval = 1000;
    private int outOfRangeDistance = 100;

    private Circle realtimeCircle = null, trafficSignCircle = null, blackspotCircle = null;
    private Marker trafficSignMarker = null, blackspotMarker = null, realtimeMarker = null;
    private boolean isRealtimeIncidentNotified = false;
    private boolean isSpeedLimitNotified = false, isCriticalNotified = false;
    private boolean isTrafficSignNotified = false, isBlackspotNotified = false;
    private boolean isRealtimeObjectFound = false;
    private boolean isSpeedLimitObjectFound = false, isCriticalObjectFound = false;
    private boolean isTrafficObjectFound = false, isBlackspotObjectFound = false;
    private boolean isVoiceAssistantEnabled = true;

    private RealtimeIncident currentIncident = null;
    private SpeedLimitPoint currentSpeedPoint = null;
    private CriticalLocation currentCriticalPoint = null;
    private TrafficSign currentTrafficSign = null;
    private BlackSpot currentBlackspot = null;

    private int score_addIncident = 0, score_removeIncident = 0, score_OverSpeed = 0;
    private String startTime;

    ///////////////////////////////
    private TextView test;
    private Queue<LatLng> pointQueue;
    private Handler mokeLocationGenerateHandler;
    //////////////////////////////


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_navigate);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Init();

        //////////////////////////////////////
        test = findViewById(R.id.test);
        myLocation = new Location("Test");
        initializeData();
        pointQueue = new LinkedList<>();
        mokeLocationGenerateHandler = new Handler();
        for(LatLng point : route.getPoints()) {
            pointQueue.add(point);
        }

        mokeLocationGenerateHandler.postDelayed(mokeLocationGenerationThread,300);




        //////////////////////////////////////

        //Check TTS Data
        Intent TTSIntent = new Intent();
        TTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(TTSIntent,DATA_CHECK_CODE);

        locusService.setRealTimeLocationListener(new LocusService.RealtimeListenerService() {
            @Override
            public void OnRealLocationChanged(Location location) {
                myLocation = location;
                myLatLanLocation = new LatLng(myLocation.getLatitude(),myLocation.getLongitude());
                myLatLanLocation = getNearestRoutePoint(myLatLanLocation);

                if(!isMapDraggable) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(myLatLanLocation));
                    img_position.setVisibility(View.VISIBLE);
                    if (mMap.getCameraPosition().zoom != 19.0f)
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLanLocation, 19.0f));
                }
            }
        });

        btn_remove_incident.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    myRef.child(currentIncident.getIncident_id()).removeValue();
                    score_removeIncident += 5;
                }catch (Exception e){}
                incident_info.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });

        btn_slideDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                driveInfoSheet.setState(BottomSheetBehavior.STATE_HIDDEN);
                fab.setVisibility(View.VISIBLE);
            }
        });

        btn_emergency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generateDrivingSummery(false);
            }
        });

        btn_voiceAssistant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isVoiceAssistantEnabled) {
                    isVoiceAssistantEnabled = false;
                    txt_voiceAssistant.setText("Enable Voice Assistant");
                }
                else {
                    isVoiceAssistantEnabled = true;
                    txt_voiceAssistant.setText("Disable Voice Assistant");
                }

                materialSheetFab.hideSheet();
            }
        });

        btn_dayNight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MapStyleOptions styleOptions;
                if(isDayStyleEnabled) {
                    styleOptions = MapStyleOptions.loadRawResourceStyle(getApplicationContext(), R.raw.night_style);
                    mMap.setMapStyle(styleOptions);
                    txt_dayNight.setText("Set Day Style");
                    isDayStyleEnabled = false;
                }
                else {
                    styleOptions = MapStyleOptions.loadRawResourceStyle(getApplicationContext(), R.raw.day_style);
                    mMap.setMapStyle(styleOptions);
                    txt_dayNight.setText("Set Night Style");
                    isDayStyleEnabled = true;
                }

                materialSheetFab.hideSheet();
            }
        });

        btn_newIncident.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(driveInfoSheet.getState()==BottomSheetBehavior.STATE_EXPANDED)
                    driveInfoSheet.setState(BottomSheetBehavior.STATE_HIDDEN);
                img_position.setVisibility(View.GONE);
                incidentSheet.setState(BottomSheetBehavior.STATE_EXPANDED);
                materialSheetFab.hideSheet();
                fab.setVisibility(View.GONE);
            }
        });

        incidentSheet.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                img_position.setVisibility(View.VISIBLE);
            }
        });

        btn_incident_accident.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setIncident("Accident");
                incidentSheet.setState(BottomSheetBehavior.STATE_HIDDEN);
                img_position.setVisibility(View.VISIBLE);
            }
        });

        btn_incident_clauser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setIncident("Closure");
                incidentSheet.setState(BottomSheetBehavior.STATE_HIDDEN);
                img_position.setVisibility(View.VISIBLE);
            }
        });

        btn_incident_flood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setIncident("Flood");
                incidentSheet.setState(BottomSheetBehavior.STATE_HIDDEN);
                img_position.setVisibility(View.VISIBLE);
            }
        });

        btn_incident_hazard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setIncident("Hazard");
                incidentSheet.setState(BottomSheetBehavior.STATE_HIDDEN);
                img_position.setVisibility(View.VISIBLE);
            }
        });

        btn_incident_landslip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setIncident("Landslip");
                incidentSheet.setState(BottomSheetBehavior.STATE_HIDDEN);
                img_position.setVisibility(View.VISIBLE);
            }
        });

        btn_incident_traffic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setIncident("Traffic-Jam");
                incidentSheet.setState(BottomSheetBehavior.STATE_HIDDEN);
                img_position.setVisibility(View.VISIBLE);
            }
        });


        btn_cam_ctrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (isMapDraggable) {
                        isMapDraggable = false;
                        resetDataMap();
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLanLocation, 19.0f));
                        mMap.getUiSettings().setAllGesturesEnabled(false);
                        btn_cam_ctrl.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_drag_enable));
                    } else {
                        isMapDraggable = true;
                        img_position.setVisibility(View.GONE);
                        MapController.setCameraBounds(route.getStart_point(),route.getDestination(),mMap);
                        visualizeIncidentData();
                        mMap.getUiSettings().setAllGesturesEnabled(true);
                        btn_cam_ctrl.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_recenter));
                    }
                }catch (Exception e){}

            }
        });

        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                RealtimeIncident incident =  dataSnapshot.getValue(RealtimeIncident.class);
                boolean isInRange = false;
                for(LatLng point: route.getPoints()){
                        if(getDistance(point,new LatLng(incident.getLatitude(),incident.getLongitude()))<=realtimeRadius){
                            isInRange = true;
                            break;
                        }
                }

                if(isInRange)
                    realtimeIncidentHashMap.put(incident.getIncident_id(),incident);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                RealtimeIncident incident =  dataSnapshot.getValue(RealtimeIncident.class);

                    if(realtimeIncidentHashMap.containsKey(incident.getIncident_id())){
                        realtimeIncidentHashMap.remove(incident.getIncident_id());
                        if(realtimeCircle!=null)
                            realtimeCircle.remove();
                        if(realtimeMarker!=null)
                            realtimeMarker.remove();
                        if(incident_info.getState()==BottomSheetBehavior.STATE_EXPANDED)
                            incident_info.setState(BottomSheetBehavior.STATE_HIDDEN);
                    }

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void Init() {

        String dataset = getIntent().getStringExtra("route");
        if(dataset!=null){
            Gson gson = new Gson();
            route = gson.fromJson(dataset,RouteInfo.class);
        }

        total_distance = route.getDistance();
        total_duration = route.getDuration();
        locusInterval = (int) ((route.getPoints().size() * 0.02 *2) + 500);
        startTime = getCurrentDateTime();

        locusService = new LocusService(getApplicationContext());
        img_position = findViewById(R.id.img_poition);
        img_position.setVisibility(View.GONE);
        layout = findViewById(R.id.layout_navigate);

        // Initialize Firebase-database
        database = FirebaseDatabase.getInstance();
        myRef = database.getReferenceFromUrl("https://isafe-5e90f.firebaseio.com/");

        bearingHandler = new Handler();
        realtimeIncidentHandler = new Handler();
        speedPointHandler = new Handler();
        criticalLocationHandler = new Handler();
        trafficSignHandler = new Handler();
        blackspotHandler = new Handler();
        speedCalcHandler = new Handler();

        btn_slideDown = findViewById(R.id.btn_sheetDown);
        btn_voiceAssistant = findViewById(R.id.btn_voiceAssistant);
        btn_newIncident = findViewById(R.id.btn_newIncident);
        btn_dayNight = findViewById(R.id.btn_dayNight);
        btn_emergency = findViewById(R.id.btn_emergency);
        btn_newIncident = findViewById(R.id.btn_newIncident);
        btn_remove_incident = findViewById(R.id.btn_remove_incident);

        driveInfoLayout = findViewById(R.id.drive_info_layout);
        incidentLayout = findViewById(R.id.incident_layout);
        driveInfoSheet = BottomSheetBehavior.from(driveInfoLayout);
        driveInfoSheet.setSkipCollapsed(true);
        driveInfoSheet.setState(BottomSheetBehavior.STATE_HIDDEN);

        incidentSheet = BottomSheetBehavior.from(incidentLayout);
        incidentSheet.setSkipCollapsed(true);
        incidentSheet.setState(BottomSheetBehavior.STATE_HIDDEN);
        incident_info_layout = findViewById(R.id.incident_info_layout);
        incident_info = BottomSheetBehavior.from(incident_info_layout);
        incident_info.setSkipCollapsed(true);
        incident_info.setState(BottomSheetBehavior.STATE_HIDDEN);
        txt_distance = findViewById(R.id.driveInfo_txtDistance);
        txt_ETA = findViewById(R.id.driveInfo_txtETA);
        txt_incident_description = findViewById(R.id.txt_incident_description);
        txt_incident_time = findViewById(R.id.txt_incident_time);
        txt_incident_type = findViewById(R.id.txt_incident_type);
        txt_dayNight = findViewById(R.id.txt_dayNight);
        txt_voiceAssistant = findViewById(R.id.txt_voiceAssistant);

        btn_incident_accident = findViewById(R.id.btn_incident_accident);
        btn_incident_traffic = findViewById(R.id.btn_incident_traffic);
        btn_incident_hazard = findViewById(R.id.btn_incident_hazards);
        btn_incident_clauser = findViewById(R.id.btn_incident_closer);
        btn_incident_flood = findViewById(R.id.btn_incident_flood);
        btn_incident_landslip = findViewById(R.id.btn_incident_landslip);
        btn_cam_ctrl = findViewById(R.id.btn_cam_control);

        img_incident = findViewById(R.id.img_incident_icon);

        realtimeIncidentHashMap = new HashMap<>();
        speedMap = new ArrayList<>();
        speedLimitPointList = new ArrayList<>();
        criticalLocationList = new ArrayList<>();
        trafficSignList = new ArrayList<>();
        blackSpotList = new ArrayList<>();

        fab = findViewById(R.id.nav_fab);
        View sheetView = findViewById(R.id.fab_sheet);
        View overlay = findViewById(R.id.overlay);
        int sheetColor = getResources().getColor(R.color.sheetColor);
        int fabColor = getResources().getColor(R.color.fabColor);

        materialSheetFab = new MaterialSheetFab<>(fab, sheetView, overlay,
                sheetColor, fabColor);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.getUiSettings().setAllGesturesEnabled(false);

        isMapReady = true;

        /////////////////////////////////////////////////////////////////////////////////////

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(route.getStart_point(),19.0f));
        resetDataMap();
        //////////////////////////////////////////////////////////////////////////////////////
    }

    private void updateBearing(float bearing){
        CameraPosition cameraPosition = new CameraPosition.Builder(mMap.getCameraPosition())
                .bearing(bearing)
                .tilt(60)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //locusService.startRealtimeGPSListening(locusInterval);

        bearingHandler.postDelayed(bearingThread,bearingInterval);
        realtimeIncidentHandler.postDelayed(realtimeIncidentThread,realtimeIncidentInterval);

        speedPointHandler.postDelayed(speedPointThread,speedPointInterval);
        criticalLocationHandler.postDelayed(criticalLocationThread,criticalLocationInterval);
        trafficSignHandler.postDelayed(trafficSignThread,trafficSignInterval);
        blackspotHandler.postDelayed(blackspotThread,blackspotInterval);
        speedCalcHandler.postDelayed(speedCalcThread,1000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        locusService.stopRealTimeGPSListening();

        bearingHandler.removeCallbacks(bearingThread);
        realtimeIncidentHandler.removeCallbacks(realtimeIncidentThread);
        speedPointHandler.removeCallbacks(speedPointThread);
        criticalLocationHandler.removeCallbacks(criticalLocationThread);
        trafficSignHandler.removeCallbacks(trafficSignThread);
        blackspotHandler.removeCallbacks(blackspotThread);
        speedCalcHandler.removeCallbacks(speedCalcThread);
    }

    private String generateHashID(double lat, double lon, String incidentName){
        MessageDigest messageDigest = null;
        String level = "" + lat + lon + incidentName;

        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        messageDigest.update(level.getBytes(), 0, level.length());
        return new BigInteger(1, messageDigest.digest()).toString(16);
    }

    private Runnable bearingThread = new Runnable() {
        @Override
        public void run() {
            //Calculate Bearing
            if(!isMapDraggable) {
                if (myLocation != null) {

                    if (bearingLocation != null) {
                        updateBearing(bearingLocation.bearingTo(myLocation));
                    }
                    bearingLocation = myLocation;
                }
            }
            bearingHandler.postDelayed(this,bearingInterval);
        }
    };



    private Runnable realtimeIncidentThread = new Runnable() {
        @Override
        public void run() {
            realtimeIncidentReport();
            realtimeIncidentHandler.postDelayed(this,realtimeIncidentInterval);
        }
    };

    private Runnable speedPointThread = new Runnable() {
        @Override
        public void run() {
            speedPointReport();
            speedPointHandler.postDelayed(this,speedPointInterval);
        }
    };

    private Runnable criticalLocationThread = new Runnable() {
        @Override
        public void run() {
            criticalLocationReport();
            criticalLocationHandler.postDelayed(this,criticalLocationInterval);
        }
    };

    private Runnable trafficSignThread = new Runnable() {
        @Override
        public void run() {
            trafficSignReport();
            trafficSignHandler.postDelayed(this,trafficSignInterval);
        }
    };

    private Runnable blackspotThread = new Runnable() {
        @Override
        public void run() {
            blackspotReport();
            blackspotHandler.postDelayed(this,blackspotInterval);
        }
    };

    private Runnable speedCalcThread = new Runnable() {
        @Override
        public void run() {
            if(myLatLanLocation!=null){
                if(speedLocation!=null){
                    calcSpeed(myLatLanLocation,speedLocation);
                }

                speedLocation = myLatLanLocation;
            }

            speedCalcHandler.postDelayed(this,1000);
        }
    };


    @Override
    public void onBackPressed() {
        if (materialSheetFab.isSheetVisible()) {
            materialSheetFab.hideSheet();
        } else {
            super.onBackPressed();
        }
    }

    private void calcSpeed(LatLng point1, LatLng point2){
        Double speed = getDistance(point1,point2);
        test.setText(speed + " ms");
        if(speed!=0)
            speedMap.add(new SpeedMarker(myLatLanLocation.latitude,myLatLanLocation.longitude,speed));
    }

    private int getIncidentImage(String incident_name){
        switch (incident_name){
            case "Accident": return R.drawable.accident;
            case "Closure" : return R.drawable.closure;
            case "Flood": return R.drawable.flood;
            case "Hazard": return R.drawable.animal;
            case "Landslip": return R.drawable.icon_landslip;
            case "Traffic-Jam": return R.drawable.traffic;
        }
        return 0;
    }

    private void setIncident(String incidentType){
        LatLng position = myLatLanLocation;
        if(myLatLanLocation!=null){
            String hashID = generateHashID(position.latitude, position.longitude, incidentType);
            RealtimeIncident incident = new RealtimeIncident(hashID, incidentType, "ABC", getCurrentDateTime(), position.latitude, position.longitude);

            if(currentIncident!=null){
                if(!incident.getIncident_name().equals(currentIncident.getIncident_name())){
                    myRef.child(incident.getIncident_id()).setValue(incident);
                    score_addIncident += 25;
                }
                else
                    setMessage("This incident is already exists!");
            }
            else
                myRef.child(incident.getIncident_id()).setValue(incident);
        }
    }

    private void setPopupMessage(String title, String message, int icon, boolean voiceAssist){
        Alerter.create(this)
                .setTitle(title)
                .setText(message)
                .setIcon(icon)
                .setBackgroundColorRes(R.color.colorPrimaryDark)
                .setDuration(5000)
                .show();

        if(voiceAssist)
            speakNotification(message);
    }

    private int mapMarkerIcon(String type){
        switch (type){
            case "Accident": return R.drawable.accident_pin;
            case "Closure": return R.drawable.closure_pin;
            case "Flood": return R.drawable.flood_pin;
            case "Hazard": return R.drawable.hazard_pin;
            case "Landslip": return R.drawable.landslip_pin;
            case "Traffic-Jam": return R.drawable.traffic_pin;
        }
        return 0;
    }

    private LatLng getNearestRoutePoint(LatLng myLocation) {
        LatLng min_point = route.getPoints().get(0);
        for(LatLng point : route.getPoints()){
            if(getDistance(myLocation,point)<=getDistance(myLocation,min_point))
                min_point = point;
        }

        if(getDistance(myLocation,min_point)>outOfRangeDistance)
            reRoute();

        return min_point;
    }

    private void reRoute() {
        locusService.stopRealTimeGPSListening();
        ///////////////
        //mokeLocationGenerateHandler.removeCallbacks(mokeLocationGenerationThread);
        //////////////////
        bearingHandler.removeCallbacks(bearingThread);
        realtimeIncidentHandler.removeCallbacks(realtimeIncidentThread);
        speedPointHandler.removeCallbacks(speedPointThread);
        criticalLocationHandler.removeCallbacks(criticalLocationThread);
        trafficSignHandler.removeCallbacks(trafficSignThread);
        blackspotHandler.removeCallbacks(blackspotThread);

        AlertDialog.Builder builder =
                new AlertDialog.Builder(MapsNavigate.this,R.style.Theme_AppCompat_Dialog_Alert);
        builder.setTitle("Out Of Range");
        builder.setMessage("You are out of the selected path.");
        builder.setCancelable(false);
        builder.setPositiveButton("REROUTE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(getApplicationContext(),Navigation.class);
                intent.putExtra("isReroute",true);
                intent.putExtra("myLocLat",myLocation.getLatitude());
                intent.putExtra("myLocLon",myLocation.getLongitude());
                intent.putExtra("desLat",route.getDestination().latitude);
                intent.putExtra("desLon",route.getDestination().longitude);
                startActivity(intent);
                finish();
            }
        }).setNegativeButton("EXIT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });

        builder.create().show();
    }

    private void realtimeIncidentReport(){
        for(RealtimeIncident point : realtimeIncidentHashMap.values()){

            if (getDistance(myLatLanLocation,new LatLng(point.getLatitude(),point.getLongitude())) <= realtimeRadius) {
                isRealtimeObjectFound = true;
                currentIncident = point;
                break;
            }
            else {
                isRealtimeObjectFound = false;
                currentIncident=null;
                if(incident_info.getState()==BottomSheetBehavior.STATE_EXPANDED)
                    incident_info.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        }

        if(isRealtimeObjectFound && !isRealtimeIncidentNotified){
            isRealtimeIncidentNotified = true;
            realtimeCircle = mMap.addCircle(new CircleOptions().strokeWidth(2).radius(realtimeRadius).fillColor(0x22ff0000)
                    .strokeColor(Color.TRANSPARENT).center(new LatLng(currentIncident.getLatitude(), currentIncident.getLongitude())));
            setIncidentAlert(currentIncident.getIncident_name() + " Found!",
                    currentIncident.getIncident_name() + " Found!\nIt is reported " + generateIncidentTime(currentIncident.getTime()),
                    generateIncidentTime(currentIncident.getTime()),
                    getIncidentImage(currentIncident.getIncident_name()));

            realtimeMarker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(currentIncident.getLatitude(),currentIncident.getLongitude()))
                    .icon(BitmapDescriptorFactory.fromResource(mapMarkerIcon(currentIncident.getIncident_name()))));

        }else if(!isRealtimeObjectFound){
            if(realtimeCircle!=null)
                realtimeCircle.remove();
            if(realtimeMarker!=null)
                realtimeMarker.remove();
            isRealtimeIncidentNotified = false;
        }
    }

    private void speedPointReport(){
        for(SpeedLimitPoint point : speedLimitPointList){
            if(getDistance(myLatLanLocation,new LatLng(point.getLatitude(),point.getLongitude()))<=point.getRadius()){
                isSpeedLimitObjectFound = true;
                currentSpeedPoint = point;
                break;
            }
            else
                isSpeedLimitObjectFound = false;
        }

        if(isSpeedLimitObjectFound && !isSpeedLimitNotified){
            setPopupMessage("Speed Limit",currentSpeedPoint.getMessage() + "Drive under " + currentSpeedPoint.getSpeedLimit() + " kmh", R.drawable.accident,true);
            isSpeedLimitNotified = true;
        }else if(!isSpeedLimitObjectFound){
            isSpeedLimitNotified = false;
        }
    }

    private void criticalLocationReport(){
        for(CriticalLocation point : criticalLocationList){
            if(getDistance(myLatLanLocation,new LatLng(point.getLatitude(),point.getLongitude()))<=point.getRadius()){
                isCriticalObjectFound = true;
                currentCriticalPoint = point;
                break;
            }
            else
                isCriticalObjectFound = false;
        }

        if(isCriticalObjectFound && !isCriticalNotified){
            setPopupMessage("Critical Location", currentCriticalPoint.getMessage(), R.drawable.accident,true);
            isCriticalNotified = true;
        }
        else if(!isCriticalObjectFound){
            isCriticalNotified = false;
        }
    }

    private void trafficSignReport(){
        for(TrafficSign point : trafficSignList){
            if(getDistance(myLatLanLocation,new LatLng(point.getLatitude(),point.getLongitude()))<=point.getRadius()){
                isTrafficObjectFound = true;
                currentTrafficSign = point;
                break;
            }
            else {
                isTrafficObjectFound = false;
            }
        }

        if(isTrafficObjectFound && !isTrafficSignNotified){
            isTrafficSignNotified = true;
            trafficSignMarker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(currentTrafficSign.getLatitude(), currentTrafficSign.getLongitude()))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.accident_pin)));
            trafficSignCircle = mMap.addCircle(new CircleOptions().strokeWidth(2).radius(currentTrafficSign.getRadius()).fillColor(0x22255dea)
                    .strokeColor(Color.TRANSPARENT).center(new LatLng(currentTrafficSign.getLatitude(), currentTrafficSign.getLongitude())));
            //Set Alert
            setPopupMessage(currentTrafficSign.getSign(),currentTrafficSign.getMessage(),R.drawable.accident,true);
        }else if(!isTrafficObjectFound){
            isTrafficSignNotified = false;

            if(trafficSignCircle!=null) {
                trafficSignCircle.remove();
                trafficSignCircle = null;
            }
            if(trafficSignMarker!=null)
                trafficSignMarker.remove();
        }
    }

    private void blackspotReport(){
        for(BlackSpot point : blackSpotList){
            if(getDistance(myLatLanLocation,new LatLng(point.getLatitude(),point.getLongitude()))<=point.getRadius()){
                isBlackspotObjectFound = true;
                currentBlackspot = point;
                break;
            }
            else {
                isBlackspotObjectFound = false;
            }
        }

        if(isBlackspotObjectFound && !isBlackspotNotified){
            isBlackspotNotified = true;
            blackspotMarker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(currentBlackspot.getLatitude(), currentBlackspot.getLongitude()))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.accident_pin)));
            blackspotCircle = mMap.addCircle(new CircleOptions().strokeWidth(2).radius(currentBlackspot.getRadius()).fillColor(0x22e61919)
                    .strokeColor(Color.TRANSPARENT).center(new LatLng(currentBlackspot.getLatitude(), currentBlackspot.getLongitude())));
            //Set Alert
            setPopupMessage("Accident Black-spot",currentBlackspot.getMessage(), R.drawable.accident,true);
        }
        else if(!isBlackspotObjectFound){
            isBlackspotNotified = false;

            if(blackspotCircle!=null) {
                blackspotCircle.remove();
                blackspotCircle = null;
            }
            if(blackspotMarker!=null)
                blackspotMarker.remove();
        }
    }

    private void drawSpeedPath(){
        List<LatLng> speedList = new ArrayList<>();
        for(SpeedLimitPoint speedLimitPoint: speedLimitPointList){
            for(LatLng point : route.getPoints()){
                if(getDistance(point,new LatLng(speedLimitPoint.getLatitude(),speedLimitPoint.getLongitude()))<=speedLimitPoint.getRadius())
                    speedList.add(point);
            }
            MapController.drawPolyline(getApplicationContext(),speedList,R.color.speedPointColor,mMap);
            speedList.clear();
        }
    }

    private void drawCriticalPath(){
        List<LatLng> speedList = new ArrayList<>();
        for(CriticalLocation criticalLocation: criticalLocationList){
            for(LatLng point : route.getPoints()){
                if(getDistance(point,new LatLng(criticalLocation.getLatitude(),criticalLocation.getLongitude()))<=criticalLocation.getRadius())
                    speedList.add(point);
            }
            MapController.drawPolyline(getApplicationContext(),speedList,R.color.criticalLocationColor,mMap);
            speedList.clear();
        }
    }

    private double getDistance(LatLng point1, LatLng point2) {
        double p = 0.017453292519943295;
        double a = 0.5 - Math.cos((point2.latitude - point1.latitude) * p)/2 +
                Math.cos(point1.latitude * p) * Math.cos(point2.latitude * p) *
                        (1 - Math.cos((point2.longitude - point1.longitude) * p))/2;

        return 12.742 * Math.asin(Math.sqrt(a))*1000*1000;
    }

    private void setIncidentAlert(String incident_name, String message,String time, int image){

        txt_incident_type.setText(incident_name);
        txt_incident_time.setText(time);
        txt_incident_description.setText(message);
        img_incident.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),image));

        speakNotification(message);

        if(incident_info.getState()!=BottomSheetBehavior.STATE_EXPANDED) {
            incident_info.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    private String getCurrentDateTime(){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(calendar.getTime());
    }

    private String generateIncidentTime(String incidentTime){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String returnTime = "";
        try {
            Date incidentDate = simpleDateFormat.parse(incidentTime);
            Date currentDate = new Date();

            long diff = currentDate.getTime() - incidentDate.getTime();
            long seconds = diff / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;

            if(days != 0) {
                if(days==1)
                    returnTime = "1 day ago";
                else
                    returnTime = days + " days ago";
            }
            else if(hours!= 0) {
                if(hours==1)
                    returnTime = "1 hour ago";
                else
                    returnTime = hours + " hours ago";
            }
            else if(minutes!=0) {
                if(minutes==1)
                    returnTime = "1 minute ago";
                else
                    returnTime = minutes + " minutes ago";
            }
            else if(seconds!=0) {
                if(seconds==1)
                    returnTime = "1 second ago";
                else
                    returnTime = seconds + " seconds ago";
            }

        } catch (ParseException e) { }

        return returnTime;
    }

    @Override
    public void onInit(int i) {
        if(i==TextToSpeech.SUCCESS){
            if(textToSpeech.isLanguageAvailable(Locale.US)==TextToSpeech.LANG_AVAILABLE)
                textToSpeech.setLanguage(Locale.US);
        }
        else if(i==TextToSpeech.ERROR)
            Toast.makeText(getApplicationContext(),"Text to Speech Failed!",Toast.LENGTH_SHORT).show();
    }

    private void speakNotification(String message)  //Set Voice Assistant
    {
        if(isVoiceAssistantEnabled)
            textToSpeech.speak(message,TextToSpeech.QUEUE_ADD,null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==DATA_CHECK_CODE){
            if(resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS){
                textToSpeech = new TextToSpeech(this,this);
            }else {
                Intent installTTS = new Intent();
                installTTS.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTS);
            }
        }
    }

    private void setMessage(String message){
        Snackbar snackbar = Snackbar.make(layout,message,Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    private void visualizeIncidentData()    //Visualize All Incidents in the Map
    {
        if(isMapReady){
            //Real-time Data
            for(RealtimeIncident incident : realtimeIncidentHashMap.values()){
                mMap.addCircle(new CircleOptions().strokeWidth(2).radius(realtimeRadius).fillColor(0x22ff0000)
                        .strokeColor(Color.TRANSPARENT).center(new LatLng(incident.getLatitude(), incident.getLongitude())));

                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(incident.getLatitude(),incident.getLongitude()))
                        .title(incident.getIncident_name())
                        .icon(BitmapDescriptorFactory.fromResource(mapMarkerIcon(incident.getIncident_name()))));
            }

            //Blackspots
            for(BlackSpot blackSpot : blackSpotList){
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(blackSpot.getLatitude(), blackSpot.getLongitude()))
                        .title("BlackSpot")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.accident_pin)));
                mMap.addCircle(new CircleOptions().strokeWidth(2).radius(blackSpot.getRadius()).fillColor(0x22e61919)
                        .strokeColor(Color.TRANSPARENT).center(new LatLng(blackSpot.getLatitude(), blackSpot.getLongitude())));
            }

            //Traffic Sign
            for(TrafficSign trafficSign : trafficSignList){
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(trafficSign.getLatitude(), trafficSign.getLongitude()))
                        .title(trafficSign.getSign())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.accident_pin)));
                mMap.addCircle(new CircleOptions().strokeWidth(2).radius(trafficSign.getRadius()).fillColor(0x22255dea)
                        .strokeColor(Color.TRANSPARENT).center(new LatLng(trafficSign.getLatitude(), trafficSign.getLongitude())));
            }

            //Remove all Thread Callbacks
            bearingHandler.removeCallbacks(bearingThread);
            realtimeIncidentHandler.removeCallbacks(realtimeIncidentThread);
            speedPointHandler.removeCallbacks(speedPointThread);
            criticalLocationHandler.removeCallbacks(criticalLocationThread);
            trafficSignHandler.removeCallbacks(trafficSignThread);
            blackspotHandler.removeCallbacks(blackspotThread);
        }
    }

    private void resetDataMap() //Clear Incident data & Draw Paths
    {
        //Clear Map
        mMap.clear();

        if(isMapReady){
            mMap.addMarker(new MarkerOptions().position(route.getStart_point()).title("My Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.start_point)));
            mMap.addMarker(new MarkerOptions().position(route.getDestination()).title("Destination").icon(BitmapDescriptorFactory.fromResource(R.drawable.stop_point)));
            MapController.drawPolyline(getApplicationContext(),route.getPoints(),R.color.colorPrimary,mMap);
            drawCriticalPath();
            drawSpeedPath();
        }

        //Start all threads
        bearingHandler.postDelayed(bearingThread,bearingInterval);
        realtimeIncidentHandler.postDelayed(realtimeIncidentThread,realtimeIncidentInterval);

        speedPointHandler.postDelayed(speedPointThread,speedPointInterval);
        criticalLocationHandler.postDelayed(criticalLocationThread,criticalLocationInterval);
        trafficSignHandler.postDelayed(trafficSignThread,trafficSignInterval);
        blackspotHandler.postDelayed(blackspotThread,blackspotInterval);
    }

    private void generateDrivingSummery(boolean isEndJourney){
        SummeryInfo summeryInfo = new SummeryInfo();
        summeryInfo.setStart_location(route.getStart_point());
        summeryInfo.setEnd_location(route.getDestination());
        summeryInfo.setScore_addIncidents(score_addIncident);
        summeryInfo.setScore_overSpeed(score_OverSpeed);
        summeryInfo.setScore_removeIncidents(score_removeIncident);
        summeryInfo.setSpeedMarkerList(speedMap);
        summeryInfo.setRoute("To " + route.getEndLocation());
        summeryInfo.setStartTime(startTime);
        summeryInfo.setEndJourney(isEndJourney);
        String dataSet = new Gson().toJson(summeryInfo);
        Intent intent = new Intent(MapsNavigate.this,DrivingSummery.class);
        intent.putExtra("summeryInfo",dataSet);
        startActivity(intent);
    }

    //////////////////////////////////////////////////////////////////
    //Testing Methods
    private void mokeLocationGenerator(){
        LatLng point = pointQueue.poll();
        myLocation.setLatitude(point.latitude);
        myLocation.setLongitude(point.longitude);
        myLatLanLocation = point;

        myLatLanLocation = getNearestRoutePoint(myLatLanLocation);

        if(!isMapDraggable) {
            mMap.animateCamera(CameraUpdateFactory.newLatLng(myLatLanLocation));
            img_position.setVisibility(View.VISIBLE);
            if (mMap.getCameraPosition().zoom != 19.0f)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLanLocation, 19.0f));
        }
    }

    Runnable mokeLocationGenerationThread = new Runnable() {
        @Override
        public void run() {
            mokeLocationGenerator();
            mokeLocationGenerateHandler.postDelayed(this,700);
        }
    };

    private void initializeData(){
        speedLimitPointList.add(new SpeedLimitPoint(6.71009,80.52014,30,40,400,
                "You are in a Speed limit Zone.\nPlease Drive under 30 kilometers per hour"));
        criticalLocationList.add(new CriticalLocation(5.9376317,80.5730873,100,"Please Drive carefully. So many bends here","",""));
        trafficSignList.add(new TrafficSign(5.9376317,80.5730873,75,"Pedestrian Crossing","Pedestrian Crossing"));
        trafficSignList.add(new TrafficSign(5.9376317,80.5730873,75,"Nearby Silent Zone","Silent Zone"));
        trafficSignList.add(new TrafficSign(5.942877,80.5669242,75,"Three way junction here","Slippy Road"));

        blackSpotList.add(new BlackSpot(5.9408335,80.5705505,100,"Be care full! So many accidents",0));
        blackSpotList.add(new BlackSpot(5.9406734,80.5662053,200,"Dangerous Bend",0));

    }

    //////////////////////////////////////////////////////////////////
}

