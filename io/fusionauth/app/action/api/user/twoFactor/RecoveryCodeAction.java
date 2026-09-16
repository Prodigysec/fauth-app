package io.fusionauth.app.action.api.user.twoFactor;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.user.UserService;
import io.fusionauth.app.action.api.BaseTenantAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.TwoFactorRecoveryCodeResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{userId}", requiresAuthentication = true, scheme = {"api"})
public class RecoveryCodeAction extends BaseTenantAPIAction {
  private final UserService userService;
  
  @JSONResponse
  public TwoFactorRecoveryCodeResponse response;
  
  public UUID userId;
  
  private UserService.ValidationResult result;
  
  @Inject
  public RecoveryCodeAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, UserService paramUserService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
    this.userService = paramUserService;
  }
  
  public String get() {
    if (this.result.user == null)
      return "missing"; 
    ArrayList<String> arrayList = new ArrayList();
    this.response = new TwoFactorRecoveryCodeResponse(arrayList);
    return "render";
  }
  
  public String post() {
    if (this.result.user == null)
      return "missing"; 
    List<String> list = this.userService.generateTwoFactorRecoveryCodes(getTenant(), this.result.user);
    this.response = new TwoFactorRecoveryCodeResponse(list);
    return "render";
  }
  
  @ValidationMethod(httpMethods = {"GET", "POST"})
  public void validate() {
    if (this.userId == null)
      this.frontEndSupport.addFieldError("userId", "[missing]userId", new Object[0]); 
    this.result = this.userService.validateRecoveryCodeRequest(getOptionalTenant(), this.userId);
    conditionallyUpdateTenant(this.result.tenant);
    this.frontEndSupport.transfer(this.result.errors);
  }
}
