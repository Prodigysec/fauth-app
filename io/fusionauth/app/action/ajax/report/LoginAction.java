package io.fusionauth.app.action.ajax.report;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.time.TimeUtils;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.api.report.Count;
import io.fusionauth.domain.api.report.LoginReportResponse;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, constraints = {"admin", "report_viewer"})
public class LoginAction extends BaseReportAction {
  @FTLVariable
  public String q;
  
  public UUID userId;
  
  @Inject
  public LoginAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  protected List<Count> getCounts(ZonedDateTime paramZonedDateTime1, ZonedDateTime paramZonedDateTime2) {
    if (this.userId != null)
      return ((LoginReportResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveUserLoginReport(this.applicationId, this.userId, paramZonedDateTime1.toInstant().toEpochMilli(), paramZonedDateTime2.toInstant().toEpochMilli()))).hourlyCounts; 
    return ((LoginReportResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveLoginReport(this.applicationId, paramZonedDateTime1.toInstant().toEpochMilli(), paramZonedDateTime2.toInstant().toEpochMilli()))).hourlyCounts;
  }
  
  BiFunction<Integer, ZoneId, ZonedDateTime> getFunctionForMappingIntervalValueToDate() {
    return TimeUtils::fromHours;
  }
}
