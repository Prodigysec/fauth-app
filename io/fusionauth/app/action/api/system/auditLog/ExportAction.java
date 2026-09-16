package io.fusionauth.app.action.api.system.auditLog;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.cache.SystemConfigurationCache;
import io.fusionauth.api.service.system.AuditService;
import io.fusionauth.api.util.DownloadTools;
import io.fusionauth.app.action.api.BaseExportAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.AuditLogExportRequest;
import io.fusionauth.domain.search.AuditLogSearchCriteria;
import java.io.IOException;
import java.io.OutputStream;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.NoOp;
import org.primeframework.mvc.content.json.annotation.JSONRequest;

@NoOp
@Action(requiresAuthentication = true, scheme = {"api"})
public class ExportAction extends BaseExportAPIAction {
  @JSONRequest
  public final AuditLogExportRequest request = new AuditLogExportRequest();
  
  private final AuditService auditService;
  
  public String message;
  
  public String orderBy;
  
  public UUID tenantId;
  
  public String user;
  
  @Inject
  public ExportAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, SystemConfigurationCache paramSystemConfigurationCache, AuditService paramAuditService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache, paramSystemConfigurationCache);
    this.auditService = paramAuditService;
  }
  
  public String get() throws IOException {
    this.request.criteria = new AuditLogSearchCriteria(this.tenantId, this.message, this.user, this.start, this.end, this.orderBy);
    this.request.dateTimeSecondsFormat = this.dateTimeSecondsFormat;
    this.request.zoneId = this.zoneId;
    return post();
  }
  
  public String post() throws IOException {
    if (this.request.criteria == null)
      this.request.criteria = new AuditLogSearchCriteria(); 
    if (tenantIdWasSpecified())
      this.request.criteria.tenantId = getOptionalTenantId(); 
    this.request.criteria
      .secure()
      .prepare();
    setDefaultExportOptions(this.request);
    String str1 = ZonedDateTime.now(this.request.zoneId).format(DateTimeFormatter.ofPattern(this.request.dateTimeSecondsFormat));
    String str2 = "Audit Log Export " + str1 + ".zip";
    OutputStream outputStream = DownloadTools.beginDownloadResponse(this.frontEndSupport.response, "application/zip", str2);
    this.auditService.exportSearchResults(outputStream, this.request.criteria, this.request.dateTimeSecondsFormat, this.request.zoneId);
    return "success";
  }
}
