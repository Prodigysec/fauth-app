package io.fusionauth.api.service.email;

import io.fusionauth.domain.Tenant;

public interface SMTPTestService {
  SMTPTestResult test(Tenant paramTenant, String paramString);
  
  public static class SMTPTestResult {
    public Exception exception;
    
    public String message;
    
    public boolean success;
  }
}
