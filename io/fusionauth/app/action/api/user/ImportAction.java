package io.fusionauth.app.action.api.user;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import com.inversoft.error.Errors;
import io.fusionauth.api.service.system.EventLogHelper;
import io.fusionauth.api.service.user.UserService;
import io.fusionauth.app.action.api.BaseTenantAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.EventLogType;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.User;
import io.fusionauth.domain.api.user.ImportRequest;
import java.util.HashMap;
import java.util.Map;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(requiresAuthentication = true, scheme = {"api"})
public class ImportAction extends BaseTenantAPIAction {
  @JSONRequest
  public final ImportRequest request = new ImportRequest();
  
  private final UserService userService;
  
  @Inject
  public ImportAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, UserService paramUserService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
    this.userService = paramUserService;
  }
  
  public String post() {
    try {
      this.userService.createBulk(getTenant(), this.request.users, this.request.encryptionScheme, this.request.factor, this.request.eventInfo);
    } catch (Exception exception) {
      this.frontEndSupport.addGeneralError("[ImportRequestFailed]", new Object[0]);
      EventLogHelper.create(new EventLog(EventLogType.Error, "Import request failed. This is likely a database FK violation.", exception));
      return "input";
    } 
    return "success";
  }
  
  @ValidationMethod
  public void validate() {
    if (this.request.users == null || this.request.users.isEmpty()) {
      this.frontEndSupport.addFieldError("users", "[missing]users", new Object[0]);
      return;
    } 
    this.request.users.forEach(User::normalize);
    Tenant tenant = getTenant();
    HashMap<Object, Object> hashMap = new HashMap<>();
    Errors errors = this.userService.validateBulk(tenant, this.request.users, this.request.validateDbConstraints, this.request.encryptionScheme, this.request.factor, (Map)hashMap);
    this.frontEndSupport.transfer(errors, (Map)hashMap);
  }
}
