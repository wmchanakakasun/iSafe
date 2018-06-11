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
}
