package io.fusionauth.api.service.ip;

import com.google.inject.Inject;
import com.maxmind.db.Metadata;
import com.maxmind.db.Reader;
import io.fusionauth.api.domain.ip.ipinfo.Response;
import io.fusionauth.api.domain.ip.maxmind.CityResponse;
import io.fusionauth.api.domain.ip.maxmind.Subdivision;
import io.fusionauth.api.service.cache.MaxMindDatabaseCache;
import io.fusionauth.domain.Location;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MaxMindDBLocationService implements LocationService {
  public static final String IPInfoType = "ipinfo standard_location.mmdb";
  
  public static final String MaxMindType = "GeoIP2-City";
  
  private static final Map<String, ResponseToLocation> Functions = Map.of("ipinfo standard_location.mmdb", MaxMindDBLocationService::ipInfoFormat, "GeoIP2-City", MaxMindDBLocationService::maxMindFormat);
  
  private static final Logger logger = LoggerFactory.getLogger(MaxMindDBLocationService.class);
  
  private final MaxMindDatabaseCache cache;
  
  @Inject
  public MaxMindDBLocationService(MaxMindDatabaseCache paramMaxMindDatabaseCache) {
    this.cache = paramMaxMindDatabaseCache;
  }
  
  public static Location ipInfoFormat(Reader paramReader, InetAddress paramInetAddress) throws IOException {
    Response response = (Response)paramReader.get(paramInetAddress, Response.class);
    if (response == null)
      return null; 
    return (new Location()).with(paramLocation -> paramLocation.city = paramResponse.city)
      .with(paramLocation -> paramLocation.country = paramResponse.country)
      .with(paramLocation -> paramLocation.latitude = (paramResponse.latitude != null) ? paramResponse.latitude.doubleValue() : 0.0D)
      .with(paramLocation -> paramLocation.longitude = (paramResponse.longitude != null) ? paramResponse.longitude.doubleValue() : 0.0D)
      .with(paramLocation -> paramLocation.region = paramResponse.regionCode)
      .with(paramLocation -> paramLocation.zipcode = paramResponse.postalCode);
  }
  
  public static Location maxMindFormat(Reader paramReader, InetAddress paramInetAddress) throws IOException {
    CityResponse cityResponse = (CityResponse)paramReader.get(paramInetAddress, CityResponse.class);
    if (cityResponse == null)
      return null; 
    return (new Location()).with(paramLocation -> paramLocation.city = paramCityResponse.city.getName())
      .with(paramLocation -> paramLocation.country = paramCityResponse.country.getName())
      .with(paramLocation -> paramLocation.latitude = (paramCityResponse.location.latitude != null) ? paramCityResponse.location.latitude.doubleValue() : 0.0D)
      .with(paramLocation -> paramLocation.longitude = (paramCityResponse.location.longitude != null) ? paramCityResponse.location.longitude.doubleValue() : 0.0D)
      .with(paramLocation -> paramLocation.region = (paramCityResponse.subdivisions.size() > 0) ? ((Subdivision)paramCityResponse.subdivisions.get(0)).getName() : null)
      .with(paramLocation -> paramLocation.zipcode = paramCityResponse.postal.code);
  }
  
  public Location ipToLocation(String paramString) {
    Reader reader = (Reader)this.cache.get();
    if (reader == null)
      return null; 
    try {
      InetAddress inetAddress = InetAddress.getByName(paramString);
      Metadata metadata = reader.getMetadata();
      ResponseToLocation responseToLocation = Functions.get(metadata.getDatabaseType());
      if (responseToLocation == null)
        responseToLocation = Functions.get("ipinfo standard_location.mmdb"); 
      return responseToLocation.convert(reader, inetAddress);
    } catch (Exception exception) {
      logger.error("Unable to read from ip-location database. This will not fail a request, a null location will be returned.", exception);
      return null;
    } 
  }
  
  private static interface ResponseToLocation {
    Location convert(Reader param1Reader, InetAddress param1InetAddress) throws IOException;
  }
}
