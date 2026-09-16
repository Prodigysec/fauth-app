package io.fusionauth.app.action.account.twoFactor;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.service.oauth2.OAuthService;
import io.fusionauth.api.service.security.ThreatDetectionService;
import io.fusionauth.app.action.account.BaseAccountAction;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.FrontEndThemeResolver;
import io.fusionauth.app.service.security.LoginIntentService;
import io.fusionauth.app.service.security.SSOService;
import io.fusionauth.domain.TwoFactorMethod;
import io.fusionauth.domain.api.TwoFactorDisableRequest;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.validation.ValidationMethod;
import org.primeframework.mvc.validation.annotation.PostValidationMethod;

@Action(requiresAuthentication = true, scheme = {"account-user"})
@Redirect(code = "success", uri = "/account/two-factor/?client_id=${client_id}&tenantId=${tenantId}")
public class DisableAction extends BaseAccountAction {
  public String action;
  
  @FTLVariable
  public String code;
  
  @FTLVariable
  public String email;
  
  @FTLVariable
  public String method;
  
  @FTLVariable
  public String methodId;
  
  @FTLVariable
  public String mobilePhone;
  
  @Inject
  public DisableAction(FrontEndSupport paramFrontEndSupport, FrontEndThemeResolver paramFrontEndThemeResolver, LoginIntentService paramLoginIntentService, OAuthService paramOAuthService, SSOService paramSSOService, ThreatDetectionService paramThreatDetectionService) {
    super(paramFrontEndSupport, paramFrontEndThemeResolver, paramLoginIntentService, paramOAuthService, paramSSOService, paramThreatDetectionService);
  }
  
  public String get() {
    if (this.methodId == null || this.user.twoFactor.getMethodById(this.methodId) == null)
      return "success"; 
    return "input";
  }
  
  public String post() {
    if (this.action != null && this.action.equals("send")) {
      handleDisableSend(this.methodId, this.userId);
      return "input";
    } 
    ClientResponse<Void, Errors> clientResponse = this.client.disableTwoFactorWithRequest(this.userId, new TwoFactorDisableRequest(this.frontEndSupport
          
          .buildEventInfo(this.metaData), (this.codeApplication != null) ? this.codeApplication.id : null, this.code, this.methodId));
    if (clientResponse.wasSuccessful()) {
      addGeneralInfo("[TwoFactorAuthenticationMethodDisabled]", new Object[0]);
      return "success";
    } 
    if (clientResponse.status == 421) {
      addFieldError("code", "[invalid]code", new Object[0]);
    } else {
      transferErrors((Errors)clientResponse.errorResponse);
    } 
    return "input";
  }
  
  @PostValidationMethod
  public void setupMethod() {
    if (this.methodId != null) {
      TwoFactorMethod twoFactorMethod = this.user.twoFactor.getMethodById(this.methodId);
      if (twoFactorMethod != null) {
        this.method = twoFactorMethod.method;
        this.email = twoFactorMethod.email;
        this.mobilePhone = twoFactorMethod.mobilePhone;
      } 
    } 
  }
  
  @ValidationMethod(httpMethods = {"GET", "POST"})
  public void validate() {
    accountValidation();
  }
}
