package io.fusionauth.app.action.api;

import com.google.inject.Inject;
import com.inversoft.metrics.HealthCheckResult;
import io.fusionauth.api.service.system.StatusService;
import io.fusionauth.app.service.FrontEndSupport;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Status;
import org.primeframework.mvc.action.result.annotation.Status.List;

@Action
@List({@Status(code = "healthy", status = 200, cacheControl = "no-store"), @Status(code = "unhealthy", status = 500, cacheControl = "no-store")})
public class HealthAction extends BaseAPIAction {
  private final StatusService statusService;
  
  @Inject
  public HealthAction(FrontEndSupport paramFrontEndSupport, StatusService paramStatusService) {
    super(paramFrontEndSupport);
    this.statusService = paramStatusService;
  }
  
  public String get() {
    HealthCheckResult healthCheckResult = this.statusService.runHealthCheck("Database-primary");
    return healthCheckResult.healthy ? 
      "healthy" : 
      "unhealthy";
  }
}
