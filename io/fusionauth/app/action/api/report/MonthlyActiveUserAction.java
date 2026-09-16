package io.fusionauth.app.action.api.report;

import com.google.inject.Inject;
import io.fusionauth.api.domain.IntervalCount;
import io.fusionauth.api.domain.LoginMapper;
import io.fusionauth.api.time.TimeUtils;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.report.Count;
import io.fusionauth.domain.api.report.MonthlyActiveUserReportResponse;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONResponse;

@Action(requiresAuthentication = true, scheme = {"api-no-tenant"})
public class MonthlyActiveUserAction extends BaseReportAction {
  private final LoginMapper loginMapper;
  
  @JSONResponse
  public MonthlyActiveUserReportResponse response = new MonthlyActiveUserReportResponse();
  
  @Inject
  public MonthlyActiveUserAction(FrontEndSupport paramFrontEndSupport, LoginMapper paramLoginMapper) {
    super(paramFrontEndSupport);
    this.loginMapper = paramLoginMapper;
  }
  
  public String get() {
    ZonedDateTime zonedDateTime1 = this.start.withZoneSameInstant(ZoneOffset.UTC).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
    ZonedDateTime zonedDateTime2 = this.end.withZoneSameInstant(ZoneOffset.UTC).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
    int i = TimeUtils.toMonth(zonedDateTime1);
    int j = TimeUtils.toMonth(zonedDateTime2);
    List<IntervalCount> list = (this.applicationId != null) ? this.loginMapper.retrieveMonthlyActives(this.applicationId, i, j) : this.loginMapper.retrieveGlobalMonthlyActives(i, j);
    list.sort(Comparator.comparingInt(paramIntervalCount -> paramIntervalCount.period));
    this.response

      
      .total = list.stream().peek(paramIntervalCount -> this.response.monthlyActiveUsers.add(new Count(paramIntervalCount.count, paramIntervalCount.period))).mapToLong(paramIntervalCount -> paramIntervalCount.count).sum();
    return "render";
  }
}
