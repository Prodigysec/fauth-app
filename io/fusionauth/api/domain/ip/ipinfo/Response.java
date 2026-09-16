package io.fusionauth.api.domain.ip.ipinfo;

import com.maxmind.db.MaxMindDbConstructor;
import com.maxmind.db.MaxMindDbParameter;

public final class Response {
  public final String city;
  
  public final String country;
  
  public final String id;
  
  public final Double latitude;
  
  public final Double longitude;
  
  public final String postalCode;
  
  public final String region;
  
  public final String regionCode;
  
  public final String timezone;
  
  @MaxMindDbConstructor
  public Response(@MaxMindDbParameter(name = "city") String paramString1, @MaxMindDbParameter(name = "country") String paramString2, @MaxMindDbParameter(name = "geoname_id") String paramString3, @MaxMindDbParameter(name = "lat") String paramString4, @MaxMindDbParameter(name = "lng") String paramString5, @MaxMindDbParameter(name = "postal_code") String paramString6, @MaxMindDbParameter(name = "region") String paramString7, @MaxMindDbParameter(name = "region_code") String paramString8, @MaxMindDbParameter(name = "timezone") String paramString9) {
    this.city = paramString1;
    this.country = paramString2;
    this.id = paramString3;
    this.latitude = (paramString4 != null) ? Double.valueOf(Double.parseDouble(paramString4)) : null;
    this.longitude = (paramString5 != null) ? Double.valueOf(Double.parseDouble(paramString5)) : null;
    this.postalCode = paramString6;
    this.region = paramString7;
    this.regionCode = paramString8;
    this.timezone = paramString9;
  }
}
