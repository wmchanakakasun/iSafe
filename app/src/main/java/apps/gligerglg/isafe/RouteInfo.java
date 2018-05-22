package apps.gligerglg.isafe;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by Gayan Lakshitha on 4/16/2018.
 */

public class RouteInfo implements Parcelable {
    private LatLng start_point;
    private LatLng destination;
    private List<LatLng> points;

    private int distance;
    private int duration;

    public RouteInfo() {
    }

    public RouteInfo(LatLng start_point, LatLng destination, List<LatLng> points, int distance, int duration) {
        this.start_point = start_point;
        this.destination = destination;
        this.points = points;
        this.distance = distance;
        this.duration = duration;
    }

    public LatLng getStart_point() {
        return start_point;
    }

    public void setStart_point(LatLng start_point) {
        this.start_point = start_point;
    }

    public LatLng getDestination() {
        return destination;
    }

    public void setDestination(LatLng destination) {
        this.destination = destination;
    }

    public List<LatLng> getPoints() {
        return points;
    }

    public void setPoints(List<LatLng> points) {
        this.points = points;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.start_point, flags);
        dest.writeParcelable(this.destination, flags);
        dest.writeTypedList(this.points);
        dest.writeInt(this.distance);
        dest.writeInt(this.duration);
    }

    protected RouteInfo(Parcel in) {
        this.start_point = in.readParcelable(LatLng.class.getClassLoader());
        this.destination = in.readParcelable(LatLng.class.getClassLoader());
        this.points = in.createTypedArrayList(LatLng.CREATOR);
        this.distance = in.readInt();
        this.duration = in.readInt();
    }

    public static final Creator<RouteInfo> CREATOR = new Creator<RouteInfo>() {
        @Override
        public RouteInfo createFromParcel(Parcel source) {
            return new RouteInfo(source);
        }

        @Override
        public RouteInfo[] newArray(int size) {
            return new RouteInfo[size];
        }
    };
}
