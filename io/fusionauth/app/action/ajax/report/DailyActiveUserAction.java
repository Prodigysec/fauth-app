package io.fusionauth.app.action.ajax.report;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.time.TimeUtils;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.api.report.Count;
import io.fusionauth.domain.api.report.DailyActiveUserReportResponse;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.function.BiFunction;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, constraints = {"admin", "report_viewer"})
public class DailyActiveUserAction extends BaseReportAction {
  @Inject
  public DailyActiveUserAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
    this.intervals.remove(BaseReportAction.Interval.Hourly);
  }
  
  protected List<Count> getCounts(ZonedDateTime paramZonedDateTime1, ZonedDateTime paramZonedDateTime2) {
    return ((DailyActiveUserReportResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveDailyActiveReport(this.applicationId, paramZonedDateTime1.toInstant().toEpochMilli(), paramZonedDateTime2.toInstant().toEpochMilli()))).dailyActiveUsers;
  }
  
  BiFunction<Integer, ZoneId, ZonedDateTime> getFunctionForMappingIntervalValueToDate() {
    return TimeUtils::fromDays;
  }
}
