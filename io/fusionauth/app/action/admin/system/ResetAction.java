package io.fusionauth.app.action.admin.system;

import com.google.inject.Inject;
import io.fusionauth.api.domain.KickstartFile;
import io.fusionauth.api.domain.RuntimeMode;
import io.fusionauth.api.service.lock.ResetDistributedLock;
import io.fusionauth.api.service.system.ResetService;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.http.FileInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;
import org.primeframework.mvc.control.form.annotation.FormPrepareMethod;
import org.primeframework.mvc.parameter.fileupload.annotation.FileUpload;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(requiresAuthentication = true, constraints = {"admin"})
@List({@Redirect(code = "exit", uri = "/admin/"), @Redirect(code = "logout", uri = "/admin/logout"), @Redirect(code = "success", uri = "/admin/system/reset")})
public class ResetAction extends BaseAction {
  private final ResetDistributedLock resetDistributedLock;
  
  private final ResetService resetService;
  
  public String action;
  
  public String confirm;
  
  @FileUpload(contentTypes = {"application/json", "application/zip", "application/octet-stream", "application/x-zip-compressed", "multipart/x-zip", "multipart/form-data"}, maxSize = 50000000L)
  public FileInfo kickstart;
  
  public List<KickstartFile> kickstartFiles = new ArrayList<>();
  
  public UUID kickstartId;
  
  public boolean running;
  
  @Inject
  public ResetAction(FrontEndSupport paramFrontEndSupport, ResetDistributedLock paramResetDistributedLock, ResetService paramResetService) {
    super(paramFrontEndSupport);
    this.resetDistributedLock = paramResetDistributedLock;
    this.resetService = paramResetService;
  }
  
  public String get() {
    if (this.frontEndSupport.configuration.runtimeMode() == RuntimeMode.Production)
      return "exit"; 
    if (this.resetDistributedLock.isLocked()) {
      this.frontEndSupport.addGeneralInfo("reset-in-progress", new Object[0]);
      this.running = true;
    } 
    return "input";
  }
  
  public String post() throws Exception {
    if (this.frontEndSupport.configuration.runtimeMode() == RuntimeMode.Production)
      return "exit"; 
    if (this.action != null && this.action.equals("upload")) {
      this.resetService.createKickstart(this.kickstart.fileName, this.kickstart.file);
      return "success";
    } 
    if (this.resetDistributedLock.isLocked())
      return "success"; 
    this.resetService.resetToKickstart(this.kickstartId);
    return "logout";
  }
  
  @FormPrepareMethod
  public void prepare() {
    this.kickstartFiles = this.resetService.retrieveAllKickstartFiles();
  }
  
  @ValidationMethod
  public void validate() {
    if (this.action != null && this.action.equals("upload")) {
      if (this.kickstart == null || this.kickstart.fileName.isBlank())
        this.frontEndSupport.addFieldError("kickstart", "[missing]kickstart", new Object[0]); 
      return;
    } 
    if (this.kickstartId == null) {
      this.frontEndSupport.addFieldError("kickstartId", "[blank]kickstartId", new Object[0]);
      return;
    } 
    if (this.confirm == null) {
      this.frontEndSupport.addFieldError("confirm", "[missing]confirm", new Object[0]);
    } else if (!this.confirm.equals("RESET")) {
      this.frontEndSupport.addFieldError("confirm", "[invalid]confirm", new Object[0]);
    } 
  }
}
