package io.fusionauth.app.action.admin;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import com.inversoft.util.SecurityTools;
import io.fusionauth.api.domain.InstallationType;
import io.fusionauth.api.domain.ZonedDateTimeWrapper;
import io.fusionauth.api.service.system.SetupService;
import io.fusionauth.api.time.TimeUtils;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.ReportUtil;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.DisplayableRawLogin;
import io.fusionauth.domain.api.report.Count;
import io.fusionauth.domain.api.report.DailyActiveUserReportResponse;
import io.fusionauth.domain.api.report.LoginReportResponse;
import io.fusionauth.domain.api.report.RegistrationReportResponse;
import io.fusionauth.domain.api.report.TotalsReportResponse;
import io.fusionauth.domain.api.user.RecentLoginResponse;
import java.text.DecimalFormat;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Forward;

@Action(requiresAuthentication = true)
@Forward(code = "poc", page = "/admin/poc-index.ftl")
public class IndexAction extends BaseAction {
  @FTLVariable
  public final UUID fusionAuthTenantId;
  
  private final SetupService setupService;
  
  public IndexReport dailyActiveUserReport;
  
  @FTLVariable
  public SetupService.FirstTimeSetup firstTimeSetup;
  
  public ReportUtil.ReportData loginData;
  
  public IndexReport loginReport;
  
  public List<DisplayableRawLogin> logins;
  
  public IndexReport registrationReport;
  
  public IndexReport totalsReport;
  
  @Inject
  public IndexAction(FrontEndSupport paramFrontEndSupport, SetupService paramSetupService) {
    super(paramFrontEndSupport);
    this.fusionAuthTenantId = paramFrontEndSupport.fusionAuthTenantId;
    this.setupService = paramSetupService;
    paramFrontEndSupport.context.setAttribute("ProxyTestNonce", SecurityTools.secureRandom());
  }
  
  public String get() {
    if (this.frontEndSupport.configuration.installationType() == InstallationType.poc)
      return "poc"; 
    ZonedDateTime zonedDateTime1 = ZonedDateTimeWrapper.now(this.systemConfiguration.reportTimezone);
    ZonedDateTime zonedDateTime2 = zonedDateTime1.minusDays(1L).truncatedTo(ChronoUnit.DAYS);
    ZonedDateTime zonedDateTime3 = zonedDateTime1.truncatedTo(ChronoUnit.DAYS);
    long l1 = zonedDateTime2.toInstant().toEpochMilli();
    long l2 = zonedDateTime1.toInstant().toEpochMilli();
    long l3 = zonedDateTime1.plusDays(1L).toInstant().toEpochMilli();
    TotalsReportResponse totalsReportResponse = superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveTotalReportWithExcludes(List.of("applicationTotals")));
    this.totalsReport = new IndexReport(totalsReportResponse.globalRegistrations, 0L);
    LoginReportResponse loginReportResponse = this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveLoginReport(null, paramLong1, paramLong2));
    List<Count> list = loginReportResponse.hourlyCounts;
    this.loginReport = countsToReport(list, TimeUtils.toHour(zonedDateTime3));
    RegistrationReportResponse registrationReportResponse = superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveRegistrationReport(null, paramLong1, paramLong2));
    list = registrationReportResponse.hourlyCounts;
    this.registrationReport = countsToReport(list, TimeUtils.toHour(zonedDateTime3));
    DailyActiveUserReportResponse dailyActiveUserReportResponse = superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveDailyActiveReport(null, paramLong1, paramLong2));
    list = dailyActiveUserReportResponse.dailyActiveUsers;
    this.dailyActiveUserReport = countsToReport(list, TimeUtils.toDay(zonedDateTime3));
    this.firstTimeSetup = this.setupService.retrieveFirstTimeSetupState(this.codeCurrentTenant);
    this.logins = hasRole(new String[] { "admin", "system_manager" }) ? ((RecentLoginResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveRecentLogins(0, Integer.valueOf(10)))).logins : List.of();
    ZonedDateTime zonedDateTime4 = zonedDateTime1.truncatedTo(ChronoUnit.HOURS).plusHours(1L);
    ZonedDateTime zonedDateTime5 = zonedDateTime4.minusHours(12L);
    this.loginData = ReportUtil.calculate(loginReportResponse.hourlyCounts, zonedDateTime5, zonedDateTime4, this.zoneId, paramZonedDateTime -> paramZonedDateTime.plusHours(1L), "ha", TimeUtils::fromHours);
    return "success";
  }
  
  private IndexReport countsToReport(List<Count> paramList, long paramLong) {
    long l1 = paramList.stream().filter(paramCount -> (paramCount.interval >= paramLong)).mapToLong(paramCount -> paramCount.count).sum();
    long l2 = paramList.stream().filter(paramCount -> (paramCount.interval < paramLong)).mapToLong(paramCount -> paramCount.count).sum();
    return new IndexReport(l1, l2);
  }
  
  public static class IndexReport {
    public String change;
    
    public String count;
    
    public boolean increase;
    
    public IndexReport(long param1Long1, long param1Long2) {
      if (param1Long1 != 0L && param1Long2 != 0L) {
        long l = Math.round((param1Long1 / param1Long2 - 1.0D) * 100.0D);
        this.change = String.valueOf(l);
        this.increase = (l >= 0L);
      } 
      if (param1Long1 < 1000L) {
        this.count = Long.toString(param1Long1);
      } else if (param1Long1 < 1000000L) {
        double d = param1Long1 / 1000.0D;
        this.count = (new DecimalFormat("##.#")).format(d) + "K";
      } else {
        double d = param1Long1 / 1000000.0D;
        this.count = (new DecimalFormat("##.#")).format(d) + "M";
      } 
    }
  }
}
