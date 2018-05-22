package apps.gligerglg.isafe;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.transition.Visibility;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import net.ralphpina.permissionsmanager.PermissionsManager;
import net.ralphpina.permissionsmanager.PermissionsResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import plugins.gligerglg.locusservice.LocusService;
import rx.functions.Action1;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback,RoutingListener {

    private GoogleMap mMap;
    private LatLng myPosition = null, destination = null;
    private LocusService locusService;

    private CoordinatorLayout layout;
    private FloatingActionButton btn_gps;
    private PlaceAutocompleteFragment autocompleteFragment;
    private List<Polyline> polylines;
    private String destination_name;
    private Route selected_path;
    private HashMap<Polyline,Route> routeHashMap = new HashMap<>();
    private static final int[] COLORS = new int[]{R.color.colorPrimary,R.color.primary_dark_material_light};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PermissionsManager.init(this);
        PermissionsManager.get()
                .requestLocationPermission()
                .subscribe(new Action1<PermissionsResult>() {
                    @Override
                    public void call(PermissionsResult permissionsResult) {
                        if (permissionsResult.isGranted()) { // always true pre-M
                            // do whatever
                        }
                        if (permissionsResult.hasAskedForPermissions()) { // false if pre-M
                            // do whatever
                        }
                    }
                });
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Init();
        myPosition = new LatLng(6.706412,80.5446107);


        btn_gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(myPosition==null) {
                    if (locusService.isGPSProviderEnabled()) {
                        locusService.startRealtimeGPSListening(2000);
                        setMessage("Wait a moment until calculate your position");
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


        locusService.setRealTimeLocationListener(new LocusService.RealtimeListenerService() {
            @Override
            public void OnRealLocationChanged(Location location) {
                if(location!=null){
                    mMap.clear();
                    myPosition = new LatLng(location.getLatitude(),location.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(myPosition).title("My Location"));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myPosition,14.0f));
                    locusService.stopRealTimeGPSListening();
                    btn_gps.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.icon_navigation));

                    if(destination!=null)
                        route();
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
        locusService = new LocusService(this,false);
        layout = findViewById(R.id.main_coordinatorLayout);
        btn_gps = findViewById(R.id.main_fab_gps);
        autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteFragment.setHint("Destination");
        polylines = new ArrayList<>();
    }

    private void showPathInfoDialog()
    {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(MainActivity.this,R.style.Theme_AppCompat_Dialog_Alert);
        builder.setTitle("Selected Path Navigation");
        builder.setMessage("Distance\t\t" + selected_path.getDistanceText() + "\nDuration\t\t" + selected_path.getDurationText());
        builder.setCancelable(false);
        builder.setPositiveButton("START NAVIGATION", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(getApplicationContext(),MapsNavigate.class);
                intent.putExtra("route",new RouteInfo(myPosition,destination,selected_path.getPoints(),
                        selected_path.getDistanceValue(),selected_path.getDurationValue()));
                startActivity(intent);
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
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

        mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(Polyline polyline) {
                selected_path = routeHashMap.get(polyline);
                mMap.clear();
                for(Polyline poly : polylines)
                    drawPolyLine(poly,1);
                drawPolyLine(polyline,0);
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
        setCameraBounds();
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

    private void drawPolyLine(Polyline polyline, int colorIndex){

        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(getResources().getColor(COLORS[colorIndex]));
        polylineOptions.addAll(polyline.getPoints());
        mMap.addPolyline(polylineOptions);
    }

    @Override
    public void onRoutingCancelled() {

    }

    private void setCameraBounds() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(myPosition);
        builder.include(destination);
        LatLngBounds bounds = builder.build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 75);
        mMap.animateCamera(cameraUpdate);
    }
}
