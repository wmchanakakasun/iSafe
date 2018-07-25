package apps.gligerglg.isafe;

import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Looper;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import plugins.gligerglg.locusservice.LocusService;

public class NearbyMap extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocusService locusService;
    private MaterialDialog dialog;
    private Location myLocation;
    private ConstraintLayout layout;
    private FirebaseDatabase database;
    private DatabaseReference myRef;

    private static final int coverage_radius = 10000;
    private double distance;
    private LatLng myLocationLatLng, incidentLatLng;
    private FusedLocationProviderClient locationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Init();
        buildLocationRequest();
        buildLocationCallBack();
    }

    private void Init() {
        layout = findViewById(R.id.nearby_Layout);
        database = FirebaseDatabase.getInstance();
        myRef = database.getReferenceFromUrl("https://isafe-5e90f.firebaseio.com/");
        locusService = new LocusService(getApplicationContext(),false);
        locationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false);
        //Move camera to Sri Lanka
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(7.5414423,80.6452276),7.0f));
    }

    public void getRealtimeIncidents(View view){
        if(myLocation!=null){
            mMap.clear();
            myRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    RealtimeIncident incident =  dataSnapshot.getValue(RealtimeIncident.class);
                    incidentLatLng = new LatLng(incident.getLatitude(),incident.getLongitude());
                    distance = MapController.getDistance(myLocationLatLng,incidentLatLng);
                    if(distance<=coverage_radius){
                        mMap.addCircle(new CircleOptions().strokeWidth(2).radius(50).fillColor(0x22ff0000)
                                .strokeColor(Color.TRANSPARENT).center(new LatLng(incident.getLatitude(), incident.getLongitude())));

                        mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(incident.getLatitude(),incident.getLongitude()))
                                .title(incident.getIncident_name())
                                .icon(BitmapDescriptorFactory.fromResource(MapController.mapMarkerIcon(incident.getIncident_name()))));
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myLocation.getLatitude(),myLocation.getLongitude()),13.0f));
        }
        else
            getGPSLocation();
    }

    public void getBlackspotLocations(View view){
        mMap.clear();
    }

    public void getTrafficIncidents(View view){
        mMap.clear();
    }

    public void getSpeedLocations(View view){
        mMap.clear();
    }

    public void getCriticalLocations(View view){
        mMap.clear();
    }

    private void setProgressDialog(String message){
        dialog = new MaterialDialog.Builder(this)
                .content(message)
                .progress(true, 0)
                .progressIndeterminateStyle(true)
                .cancelable(false)
                .show();
    }

    private void getGPSLocation(){
        if(myLocation==null){
            if (locusService.isGPSProviderEnabled()) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                locationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                setProgressDialog("Calculating GPS Location");
            } else
                setPopupMessage("Please enable GPS connectivity");
        }
    }

    private void setPopupMessage(String message){
        Snackbar snackbar = Snackbar.make(layout,message,Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        getGPSLocation();
    }

    private void buildLocationRequest(){
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setFastestInterval(1000);
        locationRequest.setInterval(5000);
        locationRequest.setSmallestDisplacement(10);
    }

    private void buildLocationCallBack() {
        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for(Location location : locationResult.getLocations()){
                    if(location!=null){
                        myLocation = location;
                        myLocationLatLng = new LatLng(location.getLatitude(),location.getLongitude());
                        dialog.dismiss();
                        mMap.addMarker(new MarkerOptions().position(new LatLng(myLocation.getLatitude(),myLocation.getLongitude())).title("My Location"));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myLocation.getLatitude(),myLocation.getLongitude()),10.0f));
                    }
                }
            }
        };
    }
}
