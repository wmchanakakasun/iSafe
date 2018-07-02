package apps.gligerglg.isafe;

import android.content.Context;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;

import java.util.List;

public class MapController {
    public static void drawPolyline(Context context, List<LatLng> pointList, int color, GoogleMap mMap){
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(context.getResources().getColor(color));
        polylineOptions.addAll(pointList);
        polylineOptions.width(12);
        polylineOptions.startCap(new RoundCap());
        polylineOptions.endCap(new RoundCap());
        mMap.addPolyline(polylineOptions);
    }

    public static void setCameraBounds(LatLng myPosition, LatLng destination, GoogleMap mMap) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(myPosition);
        builder.include(destination);
        LatLngBounds bounds = builder.build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 75);
        mMap.animateCamera(cameraUpdate);
    }

    public static double getDistance(LatLng point1, LatLng point2) {
        double p = 0.017453292519943295;
        double a = 0.5 - Math.cos((point2.latitude - point1.latitude) * p)/2 +
                Math.cos(point1.latitude * p) * Math.cos(point2.latitude * p) *
                        (1 - Math.cos((point2.longitude - point1.longitude) * p))/2;

        return 12.742 * Math.asin(Math.sqrt(a))*1000*1000;
    }

    public static int mapMarkerIcon(String type){
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
}
