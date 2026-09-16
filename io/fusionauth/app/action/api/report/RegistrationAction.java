package io.fusionauth.app.action.api.report;

import com.google.inject.Inject;
import io.fusionauth.api.domain.IntervalCount;
import io.fusionauth.api.service.count.RegistrationCountService;
import io.fusionauth.api.time.TimeUtils;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.report.Count;
import io.fusionauth.domain.api.report.RegistrationReportResponse;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONResponse;

@Action(requiresAuthentication = true, scheme = {"api-no-tenant"})
public class RegistrationAction extends BaseReportAction {
  private final RegistrationCountService registrationCountService;
  
  @JSONResponse
  public RegistrationReportResponse response = new RegistrationReportResponse();
  
  @Inject
  public RegistrationAction(FrontEndSupport paramFrontEndSupport, RegistrationCountService paramRegistrationCountService) {
    super(paramFrontEndSupport);
    this.registrationCountService = paramRegistrationCountService;
  }
  
  public String get() {
    List<IntervalCount> list;
    ZonedDateTime zonedDateTime1 = this.start.withZoneSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.HOURS);
    ZonedDateTime zonedDateTime2 = this.end.withZoneSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.HOURS);
    int i = TimeUtils.toHour(zonedDateTime1);
    int j = TimeUtils.toHour(zonedDateTime2);
    if (this.applicationId == null) {
      list = this.registrationCountService.retrieveGlobalBetween(i, j);
    } else {
      list = this.registrationCountService.retrieveApplicationBetween(this.applicationId, i, j);
    } 
    list.sort(Comparator.comparingInt(paramIntervalCount -> paramIntervalCount.period));
    this.response

      
      .total = list.stream().peek(paramIntervalCount -> this.response.hourlyCounts.add(new Count(paramIntervalCount.count, paramIntervalCount.period))).mapToLong(paramIntervalCount -> paramIntervalCount.count).sum();
    return "render";
  }
}
