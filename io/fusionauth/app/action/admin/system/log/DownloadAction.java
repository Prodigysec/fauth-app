package io.fusionauth.app.action.admin.system.log;

import com.google.inject.Inject;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.system.SystemLogFrontendService;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.NoOp;

@NoOp
@Action(requiresAuthentication = true, constraints = {"admin", "system_manager"})
public class DownloadAction extends BaseAction {
  private final SystemLogFrontendService systemLogFrontendService;
  
  public boolean includeArchived;
  
  @Inject
  public DownloadAction(FrontEndSupport paramFrontEndSupport, SystemLogFrontendService paramSystemLogFrontendService) {
    super(paramFrontEndSupport);
    this.systemLogFrontendService = paramSystemLogFrontendService;
  }
  
  public String get() throws IOException {
    String str1 = this.frontEndSupport.messageProvider.getMessage("date-time-seconds-format", new Object[0]);
    String str2 = ZonedDateTime.now(this.zoneId).format(DateTimeFormatter.ofPattern(str1));
    this.systemLogFrontendService.downloadLogs("FusionAuth Logs " + str2 + ".zip", this.frontEndSupport.response, str1, this.zoneId, this.includeArchived);
    writeAuditLog("System logs were downloaded.");
    return "success";
  }
}
