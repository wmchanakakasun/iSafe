package apps.gligerglg.isafe;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.gson.Gson;
import com.jaredrummler.materialspinner.MaterialSpinner;

import net.ralphpina.permissionsmanager.PermissionsManager;
import net.ralphpina.permissionsmanager.PermissionsResult;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import plugins.gligerglg.locusservice.LocusService;
import rx.functions.Action1;

public class Navigation extends FragmentActivity implements OnMapReadyCallback, RoutingListener {

    private GoogleMap mMap;
    private LatLng myPosition = null, destination = null;
    private LocusService locusService;
    private MaterialDialog dialog;
    private double myLocLat, myLocLon, desLat, desLon;
    private boolean isReroute = false;
    private CoordinatorLayout layout;
    private FloatingActionButton btn_gps;
    private PlaceAutocompleteFragment autocompleteFragment;
    private List<Polyline> polylines;
    private String destination_name;
    private Route selected_path;
    private HashMap<Polyline, Route> routeHashMap = new HashMap<>();
    private static final int[] COLORS = new int[]{R.color.colorPrimary, R.color.alternativeRouteColor};

    private FusedLocationProviderClient locationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private int realtime_incidents = 0;
    private int blackspots = 0;
    private int speedpoints = 0;
    private int critical = 0;
    private int traffic = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Init();
        buildLocationRequest();
        buildLocationCallBack();

        //myPosition = new LatLng(5.9382617,80.5734473);

        btn_gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (myPosition == null) {
                    if (locusService.isGPSProviderEnabled()) {
                        //locusService.startRealtimeGPSListening(2000);
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        locationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                        setProgressDialog("Calculating GPS Location");
                    } else
                        locusService.openSettingsWindow("iSafe needs to enable GPS service to acquire precise location data\n" +
                                "Do you need to enable GPS manually?");
                }else {
                    if(myPosition!=null && destination!=null && selected_path!=null)
                        showPathInfoDialog();
                    else if(myPosition!=null && destination==null)
                        setMessage("Set a Destination");
                }
            }
        });


        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {

            @Override
            public void onPlaceSelected(Place place) {
                destination = place.getLatLng();
                destination_name = place.getName().toString();
                mMap.clear();
                if(myPosition!=null)
                    mMap.addMarker(new MarkerOptions().position(myPosition).title("My Location"));
                mMap.addMarker(new MarkerOptions().position(destination).title(destination_name));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destination,14.0f));

                if(myPosition!=null)
                    route();
            }

            @Override
            public void onError(Status status) {

            }
        });

    }

    private void Init() {

        isReroute = getIntent().getBooleanExtra("isReroute",false);
        if(isReroute){
            myLocLat = getIntent().getDoubleExtra("myLocLat",0);
            myLocLon = getIntent().getDoubleExtra("myLocLon",0);
            desLat = getIntent().getDoubleExtra("desLat",0);
            desLon = getIntent().getDoubleExtra("desLon",0);
            myPosition = new LatLng(myLocLat,myLocLon);
            destination = new LatLng(desLat,desLon);
        }

        locusService = new LocusService(this,false);
        locationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        layout = findViewById(R.id.main_coordinatorLayout);
        btn_gps = findViewById(R.id.main_fab_gps);
        autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteFragment.setHint("Destination");
        polylines = new ArrayList<>();
    }

    private void showPathInfoDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(Navigation.this,R.style.Theme_AppCompat_Dialog_Alert);
        View view_dialog = getLayoutInflater().inflate(R.layout.choose_best_route_fragment,null);

        TextView txt_totalDistance = view_dialog.findViewById(R.id.txt_total_distance);
        TextView txt_totalDuration = view_dialog.findViewById(R.id.txt_total_time);
        TextView txt_realtime_Incident = view_dialog.findViewById(R.id.txt_realtime_incidents);
        TextView txt_blackspots = view_dialog.findViewById(R.id.txt_blackspots);
        TextView txtcriticalLocation = view_dialog.findViewById(R.id.txt_criticalLocations);
        TextView txt_speedPoint = view_dialog.findViewById(R.id.txt_speedPoints);
        TextView txt_traffic = view_dialog.findViewById(R.id.txt_trafficSigns);
        TextView btn_navigate = view_dialog.findViewById(R.id.btn_route_navigate);

        txt_totalDistance.setText(selected_path.getDistanceText());
        txt_totalDuration.setText(selected_path.getDurationText());
        txt_realtime_Incident.setText(""+realtime_incidents);
        txt_blackspots.setText(""+blackspots);
        txtcriticalLocation.setText(""+critical);
        txt_speedPoint.setText(""+speedpoints);
        txt_traffic.setText(""+traffic);

        final Intent intent = new Intent(getApplicationContext(),MapsNavigate.class);
        RouteInfo routeInfo = new RouteInfo(myPosition,destination,selected_path.getPoints(),destination_name,
                selected_path.getDistanceValue(),selected_path.getDurationValue());

        //Static Data Class
        StaticIncidents staticIncidents = new StaticIncidents();
        String static_data = new Gson().toJson(staticIncidents);
        String route_data = new Gson().toJson(routeInfo);
        intent.putExtra("route",route_data);
        intent.putExtra("staticdata",static_data);

        btn_navigate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(intent);
                finish();
            }
        });

        builder.setView(view_dialog);
        builder.create().show();

    }

    private void route(){
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(myPosition).title("My Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.start_point)));
        mMap.addMarker(new MarkerOptions().position(destination).title(destination_name).icon(BitmapDescriptorFactory.fromResource(R.drawable.stop_point)));

        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(true)
                .waypoints(myPosition, destination)
                .build();
        routing.execute();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Move camera to Sri Lanka
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(7.5414423,80.6452276),7.0f));
        mMap.getUiSettings().setMapToolbarEnabled(false);

        if(isReroute){
            route();
        }

        mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(Polyline polyline) {
                selected_path = routeHashMap.get(polyline);
                mMap.clear();
                for(Polyline poly : polylines)
                    MapController.drawPolyline(getApplicationContext(),poly.getPoints(),R.color.alternativeRouteColor,mMap);
                MapController.drawPolyline(getApplicationContext(),polyline.getPoints(),R.color.colorPrimary,mMap);
            }
        });

    }


    private void setMessage(String message)
    {
        Snackbar snackbar = Snackbar.make(layout,message,Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    @Override
    public void onRoutingFailure(RouteException e) {
        route();
    }

    @Override
    public void onRoutingStart() {
        //setMessage("Searching routes for your trip");
    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> routes, int shortestRouteIndex) {
        polylines.clear();
        routeHashMap.clear();
        selected_path = routes.get(shortestRouteIndex);
        for (int i = 0; i <routes.size(); i++) {
            if(i==shortestRouteIndex)
                continue;
            else
                drawRoute(routes.get(i),1);
        }
        drawRoute(routes.get(shortestRouteIndex),0);
        MapController.setCameraBounds(myPosition,destination,mMap);
        btn_gps.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.icon_navigation));
    }

    private void drawRoute(Route route, int colorIndex)
    {
        PolylineOptions polyOptions = new PolylineOptions();
        polyOptions.color(getResources().getColor(COLORS[colorIndex]));
        polyOptions.width(10);
        polyOptions.addAll(route.getPoints());
        Polyline polyline = mMap.addPolyline(polyOptions);
        polylines.add(polyline);
        routeHashMap.put(polyline,route);
        polyline.setClickable(true);
    }

    @Override
    public void onRoutingCancelled() {

    }

    private void setProgressDialog(String message){
        dialog = new MaterialDialog.Builder(this)
                .content(message)
                .progress(true, 0)
                .progressIndeterminateStyle(true)
                .cancelable(false)
                .show();
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
                        mMap.clear();
                        dialog.dismiss();
                        myPosition = new LatLng(location.getLatitude(),location.getLongitude());
                        mMap.addMarker(new MarkerOptions().position(myPosition).title("My Location"));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myPosition,14.0f));
                        //locusService.stopRealTimeGPSListening();
                        locationProviderClient.removeLocationUpdates(locationCallback);
                        if(destination!=null)
                            route();
                    }
                }
            }
        };
    }

}
