package apps.gligerglg.isafe;

public class BlackSpot {
    private double latitude;
    private double longitude;
    private double radius;
    private String message;
    private int condition;

    public BlackSpot() {
    }

    public BlackSpot(double latitude, double longitude, double radius, String message, int condition) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        this.message = message;
        this.condition = condition;
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

    public int getCondition() {
        return condition;
    }

    public void setCondition(int condition) {
        this.condition = condition;
    }
}
