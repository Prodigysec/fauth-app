package io.fusionauth.api.service.risk;

import com.google.inject.Inject;
import io.fusionauth.api.domain.ZonedDateTimeWrapper;
import io.fusionauth.api.service.ip.LocationService;
import io.fusionauth.api.service.login.LoginService;
import io.fusionauth.api.util.LatLon;
import io.fusionauth.domain.DisplayableRawLogin;
import io.fusionauth.domain.Location;
import io.fusionauth.domain.search.LoginRecordSearchCriteria;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultImpossibleTravelService implements ImpossibleTravelService {
  private static final double MAX_MILES_PER_MILLI = 2.1666666666666668E-4D;
  
  private static final Logger logger = LoggerFactory.getLogger(DefaultImpossibleTravelService.class);
  
  private final LocationService locationService;
  
  private final LoginService loginService;
  
  @Inject
  public DefaultImpossibleTravelService(LocationService paramLocationService, LoginService paramLoginService) {
    this.locationService = paramLocationService;
    this.loginService = paramLoginService;
  }
  
  public Optional<RiskLevel> computeRiskLevel(RiskSignalContext paramRiskSignalContext) {
    Optional<RiskLevel> optional = Optional.of(RiskLevel.NOT_AVAILABLE);
    if (paramRiskSignalContext == null) {
      logger.error("Unable to compute risk score for ip, risk signal context is null.");
      return optional;
    } 
    if (StringUtils.isEmpty(paramRiskSignalContext.ipAddress)) {
      logger.warn("Unable to compute risk score for ip, ip is missing.");
      return optional;
    } 
    Location location = this.locationService.ipToLocation(paramRiskSignalContext.ipAddress);
    if (location == null)
      return Optional.empty(); 
    LoginRecordSearchCriteria loginRecordSearchCriteria = new LoginRecordSearchCriteria(paramRiskSignalContext.user.id, null, null, null, 0, 1);
    List<DisplayableRawLogin> list = this.loginService.retrieveRawLoginsByCriteria(loginRecordSearchCriteria);
    DisplayableRawLogin displayableRawLogin = list.isEmpty() ? null : (DisplayableRawLogin)list.getFirst();
    if (displayableRawLogin == null || displayableRawLogin.location == null)
      return optional; 
    LatLon latLon1 = new LatLon(location.latitude, location.longitude);
    LatLon latLon2 = new LatLon(displayableRawLogin.location.latitude, displayableRawLogin.location.longitude);
    ZonedDateTime zonedDateTime = ZonedDateTimeWrapper.now(ZoneOffset.UTC);
    long l = ChronoUnit.MILLIS.between(displayableRawLogin.instant, zonedDateTime);
    if (l <= 0L) {
      logger.debug("Unable to compute impossible travel, the previous login instant [{}] is not before now [{}].", displayableRawLogin.instant, zonedDateTime);
      return optional;
    } 
    double d1 = latLon1.distanceInMiles(latLon2);
    double d2 = d1 / l;
    boolean bool = (d2 >= 2.1666666666666668E-4D) ? true : false;
    return bool ? Optional.<RiskLevel>of(RiskLevel.HIGH) : Optional.<RiskLevel>of(RiskLevel.LOW);
  }
}
