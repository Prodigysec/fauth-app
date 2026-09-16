package io.fusionauth.api.service.login;

import com.inversoft.jdbc.JDBCTools;
import com.inversoft.jdbc.Select;
import io.fusionauth.api.domain.mybatis.IdentityTypeTypeHandler;
import io.fusionauth.api.service.ip.LocationService;
import io.fusionauth.api.util.NetworkTools;
import io.fusionauth.domain.Location;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import org.apache.commons.csv.CSVPrinter;

public class LoginRecordTypeHandler implements Select.ThrowingSelectHandler {
  private final DateTimeFormatter dateTimeFormatter;
  
  private final boolean licensedForIPLocation;
  
  private final LocationService locationService;
  
  private final CSVPrinter printer;
  
  private final ZoneId zoneId;
  
  public LoginRecordTypeHandler(CSVPrinter paramCSVPrinter, boolean paramBoolean, DateTimeFormatter paramDateTimeFormatter, LocationService paramLocationService, ZoneId paramZoneId) {
    this.printer = paramCSVPrinter;
    this.dateTimeFormatter = paramDateTimeFormatter;
    this.licensedForIPLocation = paramBoolean;
    this.locationService = paramLocationService;
    this.zoneId = paramZoneId;
  }
  
  public void row(ResultSet paramResultSet) throws IOException, SQLException {
    String str1 = Objects.toString(JDBCTools.getUUID(paramResultSet, "users_id"));
    String str2 = Objects.toString(JDBCTools.get(paramResultSet, "identities_value"), null);
    String str3 = Objects.toString(JDBCTools.getInteger(paramResultSet, "identities_type"), null);
    String str4 = null;
    if (str3 != null)
      str4 = IdentityTypeTypeHandler.OrdinalToIdentityType.get(Short.valueOf(Short.parseShort(str3))); 
    String str5 = ZonedDateTime.ofInstant(Instant.ofEpochMilli(((Long)JDBCTools.get(paramResultSet, "instant")).longValue()), ZoneOffset.UTC).withZoneSameInstant(this.zoneId).format(this.dateTimeFormatter);
    String str6 = Objects.toString(JDBCTools.getUUID(paramResultSet, "applications_id"), null);
    String str7 = NetworkTools.sanitizeIPAddress(Objects.toString(JDBCTools.get(paramResultSet, "ip_address"), null));
    Location location = this.licensedForIPLocation ? this.locationService.ipToLocation(str7) : new Location();
    if (location == null)
      location = new Location(); 
    this.printer.printRecord(new Object[] { 
          str1, str2, str4, str5, str6, str7, location.city, location.country, location.zipcode, location.region, 
          (location.latitude == 0.0D) ? null : Double.valueOf(location.latitude), 
          (location.longitude == 0.0D) ? null : Double.valueOf(location.longitude) });
  }
}
