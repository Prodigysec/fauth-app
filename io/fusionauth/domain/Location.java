package io.fusionauth.domain;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Location implements Buildable<Location> {
  public String city;
  
  public String country;
  
  public double latitude;
  
  public double longitude;
  
  public String region;
  
  public String zipcode;
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof Location))
      return false; 
    Location location = (Location)paramObject;
    return (Objects.equals(this.city, location.city) && 
      Objects.equals(this.country, location.country) && 
      Objects.equals(Double.valueOf(this.latitude), Double.valueOf(location.latitude)) && 
      Objects.equals(Double.valueOf(this.longitude), Double.valueOf(location.longitude)) && 
      Objects.equals(this.region, location.region) && 
      Objects.equals(this.zipcode, location.zipcode));
  }
  
  public String getDisplayString() {
    List<? extends CharSequence> list = (List)Stream.<String>of(new String[] { this.city, this.region, this.country }).filter(Objects::nonNull).collect(Collectors.toList());
    return String.join(", ", list);
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.city, this.country, Double.valueOf(this.latitude), Double.valueOf(this.longitude), this.region, this.zipcode });
  }
}
