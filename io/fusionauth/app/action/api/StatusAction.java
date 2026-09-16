package io.fusionauth.app.action.api;

import com.google.inject.Inject;
import com.inversoft.metrics.HealthCheckResult;
import io.fusionauth.api.service.system.EventLogHelper;
import io.fusionauth.api.service.system.StatusService;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.EventLogType;
import java.util.function.Supplier;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.JSON;
import org.primeframework.mvc.action.result.annotation.JSON.List;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.security.annotation.AuthorizeMethod;

@Action(requiresAuthentication = true, scheme = {"api-no-tenant", "api-basic-auth", "user", "local-metrics", "authorize-method"})
@List({@JSON(code = "ok", status = 200, cacheControl = "no-store"), @JSON(code = "unhealthy-database", status = 452, cacheControl = "no-store"), @JSON(code = "unhealthy-hikari-pool-connection", status = 453, cacheControl = "no-store"), @JSON(code = "error", status = 500, cacheControl = "no-store")})
public class StatusAction extends BaseAPIAction {
  private final StatusService statusService;
  
  public boolean debug;
  
  @JSONResponse
  public StatusService.StatusResponse response;
  
  private boolean anonymousRequest;
  
  @Inject
  public StatusAction(FrontEndSupport paramFrontEndSupport, StatusService paramStatusService) {
    super(paramFrontEndSupport);
    this.statusService = paramStatusService;
  }
  
  @AuthorizeMethod
  public boolean authorize() {
    this.anonymousRequest = true;
    return true;
  }
  
  public String get() {
    try {
      this.response = this.statusService.get();
      return getResult((Supplier<String>[])new Supplier[] { () -> health("Database-primary", "database"), () -> health("Database-primary.pool.Connection99Percent", "hikari-pool-connection") });
    } catch (Exception exception) {
      if (this.debug)
        EventLogHelper.create(new EventLog(EventLogType.Debug, "Failed to get status result.", exception)); 
      return handleResultCode("error");
    } 
  }
  
  @SafeVarargs
  private String getResult(Supplier<String>... paramVarArgs) {
    for (Supplier<String> supplier : paramVarArgs) {
      String str = supplier.get();
      if (str != null)
        return handleResultCode(str); 
    } 
    return handleResultCode("ok");
  }
  
  private String getShortStatus(String paramString) {
    String[] arrayOfString = paramString.replace("-", " ").split(" ");
    StringBuilder stringBuilder = new StringBuilder();
    for (String str : arrayOfString)
      stringBuilder.append(str.substring(0, 1).toUpperCase()).append(str.substring(1)).append(" "); 
    return stringBuilder.toString().trim();
  }
  
  private String handleResultCode(String paramString) {
    this.response.status = getShortStatus(paramString);
    if (this.anonymousRequest)
      this.response.secure(); 
    return paramString;
  }
  
  private String health(String paramString1, String paramString2) {
    HealthCheckResult healthCheckResult = (HealthCheckResult)this.response.healthChecks.get(paramString1);
    if (healthCheckResult == null || healthCheckResult.healthy)
      return null; 
    return "unhealthy-" + paramString2;
  }
}
