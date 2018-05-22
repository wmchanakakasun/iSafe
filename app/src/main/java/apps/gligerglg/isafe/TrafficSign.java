package apps.gligerglg.isafe;

public class TrafficSign {
    private double latitude;
    private double longitude;
    private double radius;
    private String message;
    private String sign;

    public TrafficSign() {
    }

    public TrafficSign(double latitude, double longitude, double radius, String message, String sign) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        this.message = message;
        this.sign = sign;
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

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }
}
