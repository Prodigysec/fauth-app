package io.fusionauth.api.domain.ip.maxmind;

import com.maxmind.db.MaxMindDbConstructor;
import com.maxmind.db.MaxMindDbParameter;

public class Location {
  public final Integer accuracyRadius;
  
  public final Integer averageIncome;
  
  public final Double latitude;
  
  public final Double longitude;
  
  public final Integer metroCode;
  
  public final Integer populationDensity;
  
  public final String timeZone;
  
  public Location() {
    this.accuracyRadius = null;
    this.averageIncome = null;
    this.latitude = null;
    this.longitude = null;
    this.metroCode = null;
    this.populationDensity = null;
    this.timeZone = null;
  }
  
  @MaxMindDbConstructor
  public Location(@MaxMindDbParameter(name = "accuracy_radius") Integer paramInteger1, @MaxMindDbParameter(name = "average_income") Integer paramInteger2, @MaxMindDbParameter(name = "latitude") Double paramDouble1, @MaxMindDbParameter(name = "longitude") Double paramDouble2, @MaxMindDbParameter(name = "metro_code") Integer paramInteger3, @MaxMindDbParameter(name = "population_density") Integer paramInteger4, @MaxMindDbParameter(name = "time_zone") String paramString) {
    this.accuracyRadius = paramInteger1;
    this.averageIncome = paramInteger2;
    this.latitude = paramDouble1;
    this.longitude = paramDouble2;
    this.metroCode = paramInteger3;
    this.populationDensity = paramInteger4;
    this.timeZone = paramString;
  }
}
