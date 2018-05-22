package apps.gligerglg.isafe;

public class SpeedLimitPoint {
    private double latitude;
    private double longitude;
    private double speedLimit;
    private double thresholdLimit;
    private double radius;
    private String message;

    public SpeedLimitPoint() {
    }

    public SpeedLimitPoint(double latitude, double longitude, double speedLimit, double thresholdLimit, double radius, String message) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.speedLimit = speedLimit;
        this.thresholdLimit = thresholdLimit;
        this.radius = radius;
        this.message = message;
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

    public double getSpeedLimit() {
        return speedLimit;
    }

    public void setSpeedLimit(double speedLimit) {
        this.speedLimit = speedLimit;
    }

    public double getThresholdLimit() {
        return thresholdLimit;
    }

    public void setThresholdLimit(double thresholdLimit) {
        this.thresholdLimit = thresholdLimit;
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
}
