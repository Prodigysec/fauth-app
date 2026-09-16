package io.fusionauth.app.action.api.report;

import com.google.inject.Inject;
import io.fusionauth.api.domain.IntervalCount;
import io.fusionauth.api.domain.LoginMapper;
import io.fusionauth.api.time.TimeUtils;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.report.Count;
import io.fusionauth.domain.api.report.DailyActiveUserReportResponse;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONResponse;

@Action(requiresAuthentication = true, scheme = {"api-no-tenant"})
public class DailyActiveUserAction extends BaseReportAction {
  private final LoginMapper loginMapper;
  
  @JSONResponse
  public DailyActiveUserReportResponse response = new DailyActiveUserReportResponse();
  
  @Inject
  public DailyActiveUserAction(FrontEndSupport paramFrontEndSupport, LoginMapper paramLoginMapper) {
    super(paramFrontEndSupport);
    this.loginMapper = paramLoginMapper;
  }
  
  public String get() {
    ZonedDateTime zonedDateTime1 = this.start.withZoneSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.DAYS);
    ZonedDateTime zonedDateTime2 = this.end.withZoneSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.DAYS);
    int i = TimeUtils.toDay(zonedDateTime1);
    int j = TimeUtils.toDay(zonedDateTime2);
    List<IntervalCount> list = (this.applicationId != null) ? this.loginMapper.retrieveDailyActives(this.applicationId, i, j) : this.loginMapper.retrieveGlobalDailyActives(i, j);
    list.sort(Comparator.comparingInt(paramIntervalCount -> paramIntervalCount.period));
    this.response

      
      .total = list.stream().peek(paramIntervalCount -> this.response.dailyActiveUsers.add(new Count(paramIntervalCount.count, paramIntervalCount.period))).mapToLong(paramIntervalCount -> paramIntervalCount.count).sum();
    return "render";
  }
}
