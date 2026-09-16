package io.fusionauth.app.action.api.system.log;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.cache.SystemConfigurationCache;
import io.fusionauth.api.service.system.SystemLogService;
import io.fusionauth.api.util.DownloadTools;
import io.fusionauth.app.action.api.BaseExportAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.SystemLogsExportRequest;
import java.io.IOException;
import java.io.OutputStream;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.NoOp;
import org.primeframework.mvc.content.json.annotation.JSONRequest;

@NoOp
@Action(requiresAuthentication = true, scheme = {"api-no-tenant"})
public class ExportAction extends BaseExportAPIAction {
  @JSONRequest
  public final SystemLogsExportRequest request = new SystemLogsExportRequest();
  
  private final SystemLogService fileSystemLogReader;
  
  public boolean includeArchived;
  
  public int lastNBytes;
  
  @Inject
  public ExportAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, SystemConfigurationCache paramSystemConfigurationCache, SystemLogService paramSystemLogService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache, paramSystemConfigurationCache);
    this.fileSystemLogReader = paramSystemLogService;
  }
  
  public String get() throws IOException {
    this.request.dateTimeSecondsFormat = this.dateTimeSecondsFormat;
    this.request.lastNBytes = this.lastNBytes;
    this.request.zoneId = this.zoneId;
    this.request.includeArchived = this.includeArchived;
    return post();
  }
  
  public String post() throws IOException {
    setDefaultExportOptions(this.request);
    String str1 = ZonedDateTime.now(this.request.zoneId).format(DateTimeFormatter.ofPattern(this.request.dateTimeSecondsFormat));
    String str2 = "System logs Export " + str1 + ".zip";
    String str3 = System.getProperty("fusionauth.log.directory");
    OutputStream outputStream = DownloadTools.beginDownloadResponse(this.frontEndSupport.response, "application/zip", str2);
    this.fileSystemLogReader.downloadAllLogs(str3, outputStream, this.request.lastNBytes, this.request.includeArchived);
    return "success";
  }
}
