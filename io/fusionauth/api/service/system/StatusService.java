package io.fusionauth.api.service.system;

import com.inversoft.metrics.HealthCheckResult;

public interface StatusService {
  StatusResponse get();
  
  HealthCheckResult runHealthCheck(String paramString);
  
  public static class StatusResponse extends com.inversoft.metrics.StatusResponse {
    public String status;
    
    public void secure() {
      this.healthChecks = null;
      this.metrics = null;
      this.version = null;
    }
  }
}
