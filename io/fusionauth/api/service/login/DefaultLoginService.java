package io.fusionauth.api.service.login;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.inversoft.jdbc.Select;
import com.inversoft.sql.WhereBuilder;
import io.fusionauth.api.domain.LoginMapper;
import io.fusionauth.api.service.ip.LocationService;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.api.service.reactor.ReactorStatusValidator;
import io.fusionauth.api.service.system.EventLogHelper;
import io.fusionauth.api.util.DownloadTools;
import io.fusionauth.api.util.NetworkTools;
import io.fusionauth.domain.DisplayableRawLogin;
import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.EventLogType;
import io.fusionauth.domain.reactor.ReactorFeatureStatus;
import io.fusionauth.domain.reactor.ReactorStatus;
import io.fusionauth.domain.search.LoginRecordSearchCriteria;
import java.io.OutputStream;
import java.sql.Connection;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import javax.sql.DataSource;
import org.apache.commons.csv.CSVPrinter;

public class DefaultLoginService implements LoginService {
  private final DataSource dataSource;
  
  private final LocationService locationService;
  
  private final LoginMapper loginMapper;
  
  private final ReactorStatusService reactorStatusService;
  
  @Inject
  public DefaultLoginService(@Named("primary") DataSource paramDataSource, LocationService paramLocationService, LoginMapper paramLoginMapper, ReactorStatusService paramReactorStatusService) {
    this.dataSource = paramDataSource;
    this.locationService = paramLocationService;
    this.loginMapper = paramLoginMapper;
    this.reactorStatusService = paramReactorStatusService;
  }
  
  public void exportLogins(OutputStream paramOutputStream, LoginRecordSearchCriteria paramLoginRecordSearchCriteria, String paramString, ZoneId paramZoneId) {
    Objects.requireNonNull(paramString);
    Objects.requireNonNull(paramZoneId);
    boolean bool = ReactorStatusValidator.isLicensedFor(this.reactorStatusService.retrieveStatus(), paramReactorStatus -> paramReactorStatus.ipGeoLocation);
    (new DownloadTools.CSVOutputStreamWriter(paramOutputStream, "login_records.csv"))
      .withExceptionObserver(paramException -> EventLogHelper.create(new EventLog(EventLogType.Error, "Failed to export [login_records.csv]", paramException)))
      .withHeaders(new String[] { 
          "User Id", "Identity value", "Identity type", "Time", "Application Id", "IP Address", "City", "Country", "Zipcode", "Region", 
          "Latitude", "Longitude" }).write(paramCSVPrinter -> {
          WhereBuilder whereBuilder = new WhereBuilder();
          whereBuilder.append("users_id = ?", paramLoginRecordSearchCriteria.userId).append("instant >= ?", paramLoginRecordSearchCriteria.start).append("instant <= ?", paramLoginRecordSearchCriteria.end).append("applications_id = ?", paramLoginRecordSearchCriteria.applicationId);
          LoginRecordTypeHandler loginRecordTypeHandler = new LoginRecordTypeHandler(paramCSVPrinter, paramBoolean, DateTimeFormatter.ofPattern(paramString), this.locationService, paramZoneId);
          Connection connection = this.dataSource.getConnection();
          try {
            connection.setAutoCommit(false);
            ((Select)(new Select(connection)).in(String.format("SELECT applications_id, instant, ip_address, users_id, identities_value, identities_type FROM raw_logins %s ORDER BY instant DESC", new Object[] { whereBuilder })).with(whereBuilder.getArgs())).stream().go(loginRecordTypeHandler);
            connection.commit();
            if (connection != null)
              connection.close(); 
          } catch (Throwable throwable) {
            if (connection != null)
              try {
                connection.close();
              } catch (Throwable throwable1) {
                throwable.addSuppressed(throwable1);
              }  
            throw throwable;
          } 
        });
  }
  
  public List<DisplayableRawLogin> retrieveRawLoginsByCriteria(LoginRecordSearchCriteria paramLoginRecordSearchCriteria) {
    List<DisplayableRawLogin> list = this.loginMapper.retrieveRawLoginsByCriteria(paramLoginRecordSearchCriteria);
    list.forEach(paramDisplayableRawLogin -> {
          paramDisplayableRawLogin.ipAddress = NetworkTools.sanitizeIPAddress(paramDisplayableRawLogin.ipAddress);
          paramDisplayableRawLogin.location = this.locationService.ipToLocation(paramDisplayableRawLogin.ipAddress);
        });
    return list;
  }
  
  public int retrieveRawLoginsByCriteriaCount(LoginRecordSearchCriteria paramLoginRecordSearchCriteria) {
    return this.loginMapper.retrieveRawLoginsByCriteriaCount(paramLoginRecordSearchCriteria);
  }
}
