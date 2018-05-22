package apps.gligerglg.isafe;

public class CriticalLocation {
    private double latitude;
    private double longitude;
    private double radius;
    private String message;
    private String startTime;
    private String endTime;

    public CriticalLocation() {
    }

    public CriticalLocation(double latitude, double longitude, double radius, String message, String startTime, String endTime) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        this.message = message;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
