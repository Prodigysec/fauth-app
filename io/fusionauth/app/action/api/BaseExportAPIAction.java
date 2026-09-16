package io.fusionauth.app.action.api;

import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.cache.SystemConfigurationCache;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.BaseExportRequest;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.primeframework.mvc.action.result.annotation.Status;

@Status(code = "error", status = 500)
public abstract class BaseExportAPIAction extends BaseTenantAPIAction {
  private final SystemConfigurationCache systemConfigurationCache;
  
  public String dateTimeSecondsFormat;
  
  public ZonedDateTime end;
  
  public ZonedDateTime start;
  
  public ZoneId zoneId;
  
  protected BaseExportAPIAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, SystemConfigurationCache paramSystemConfigurationCache) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
    this.systemConfigurationCache = paramSystemConfigurationCache;
  }
  
  protected void setDefaultExportOptions(BaseExportRequest paramBaseExportRequest) {
    if (paramBaseExportRequest.zoneId == null)
      paramBaseExportRequest.zoneId = (this.systemConfigurationCache.get()).reportTimezone; 
    if (paramBaseExportRequest.dateTimeSecondsFormat == null)
      paramBaseExportRequest.dateTimeSecondsFormat = "M/d/yyyy hh:mm:ss a z"; 
  }
}
