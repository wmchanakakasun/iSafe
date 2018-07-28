package apps.gligerglg.isafe;

import java.util.List;

public class StaticIncidents {

    private List<TrafficSign> trafficSignList;
    private List<SpeedLimitPoint> speedLimitPointList;
    private List<CriticalLocation> criticalLocationList;
    private List<BlackSpot> blackSpotList;

    public StaticIncidents() {
    }

    public StaticIncidents(List<TrafficSign> trafficSignList, List<SpeedLimitPoint> speedLimitPointList, List<CriticalLocation> criticalLocationList, List<BlackSpot> blackSpotList) {
        this.trafficSignList = trafficSignList;
        this.speedLimitPointList = speedLimitPointList;
        this.criticalLocationList = criticalLocationList;
        this.blackSpotList = blackSpotList;
    }

    public List<TrafficSign> getTrafficSignList() {
        return trafficSignList;
    }

    public void setTrafficSignList(List<TrafficSign> trafficSignList) {
        this.trafficSignList = trafficSignList;
    }

    public List<SpeedLimitPoint> getSpeedLimitPointList() {
        return speedLimitPointList;
    }

    public void setSpeedLimitPointList(List<SpeedLimitPoint> speedLimitPointList) {
        this.speedLimitPointList = speedLimitPointList;
    }

    public List<CriticalLocation> getCriticalLocationList() {
        return criticalLocationList;
    }

    public void setCriticalLocationList(List<CriticalLocation> criticalLocationList) {
        this.criticalLocationList = criticalLocationList;
    }

    public List<BlackSpot> getBlackSpotList() {
        return blackSpotList;
    }

    public void setBlackSpotList(List<BlackSpot> blackSpotList) {
        this.blackSpotList = blackSpotList;
    }

    public static class TrafficSign {
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

    public static class SpeedLimitPoint {
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

    public static class CriticalLocation {
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

    public static class BlackSpot {
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
}
