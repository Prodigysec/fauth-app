package io.fusionauth.app.action.api.system.loginRecord;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.cache.SystemConfigurationCache;
import io.fusionauth.api.service.login.LoginService;
import io.fusionauth.api.util.DownloadTools;
import io.fusionauth.app.action.api.BaseExportAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.LoginRecordExportRequest;
import io.fusionauth.domain.search.LoginRecordSearchCriteria;
import java.io.IOException;
import java.io.OutputStream;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.NoOp;
import org.primeframework.mvc.content.json.annotation.JSONRequest;

@NoOp
@Action(requiresAuthentication = true, scheme = {"api-no-tenant"})
public class ExportAction extends BaseExportAPIAction {
  @JSONRequest
  public final LoginRecordExportRequest request = new LoginRecordExportRequest();
  
  private final LoginService loginService;
  
  public UUID applicationId;
  
  public UUID userId;
  
  @Inject
  public ExportAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, LoginService paramLoginService, SystemConfigurationCache paramSystemConfigurationCache) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache, paramSystemConfigurationCache);
    this.loginService = paramLoginService;
  }
  
  public String get() throws IOException {
    this.request.criteria = new LoginRecordSearchCriteria(this.userId, this.applicationId, this.start, this.end);
    this.request.dateTimeSecondsFormat = this.dateTimeSecondsFormat;
    this.request.zoneId = this.zoneId;
    return post();
  }
  
  public String post() throws IOException {
    if (this.request.criteria == null)
      this.request.criteria = new LoginRecordSearchCriteria(); 
    setDefaultExportOptions(this.request);
    String str1 = ZonedDateTime.now(this.request.zoneId).format(DateTimeFormatter.ofPattern(this.request.dateTimeSecondsFormat));
    String str2 = "Login Records Export " + str1 + ".zip";
    OutputStream outputStream = DownloadTools.beginDownloadResponse(this.frontEndSupport.response, "application/zip", str2);
    this.loginService.exportLogins(outputStream, this.request.criteria, this.request.dateTimeSecondsFormat, this.request.zoneId);
    return "success";
  }
}
