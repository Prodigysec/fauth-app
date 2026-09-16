package io.fusionauth.api.util;

public class LatLon {
  public static final double RADIUS_IN_KILOMETERS = 3958.8D;
  
  public static final double RADIUS_IN_MILES = 6371.0D;
  
  public double latitude;
  
  public double longitude;
  
  public LatLon(double paramDouble1, double paramDouble2) {
    this.latitude = paramDouble1;
    this.longitude = paramDouble2;
  }
  
  public double distanceInKilometers(LatLon paramLatLon) {
    return 6371.0D * unitDistance(paramLatLon);
  }
  
  public double distanceInMiles(LatLon paramLatLon) {
    return 3958.8D * unitDistance(paramLatLon);
  }
  
  public double unitDistance(LatLon paramLatLon) {
    double d1 = Math.toRadians(paramLatLon.latitude - this.latitude) / 2.0D;
    double d2 = Math.toRadians(paramLatLon.longitude - this.longitude) / 2.0D;
    double d3 = Math.sin(d1) * Math.sin(d1) + Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(paramLatLon.latitude)) * Math.sin(d2) * Math.sin(d2);
    return 2.0D * Math.atan2(Math.sqrt(d3), Math.sqrt(1.0D - d3));
  }
}
