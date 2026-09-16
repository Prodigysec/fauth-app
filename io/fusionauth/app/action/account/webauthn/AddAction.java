package io.fusionauth.app.action.account.webauthn;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import com.inversoft.validator.Validator;
import io.fusionauth.api.service.oauth2.OAuthService;
import io.fusionauth.api.service.security.ThreatDetectionService;
import io.fusionauth.app.action.account.BaseAccountAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.FrontEndThemeResolver;
import io.fusionauth.app.service.security.LoginIntentService;
import io.fusionauth.app.service.security.SSOService;
import io.fusionauth.app.service.webauthn.WebAuthnFrontendService;
import io.fusionauth.domain.api.WebAuthnRegisterCompleteRequest;
import io.fusionauth.domain.api.WebAuthnRegisterCompleteResponse;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(requiresAuthentication = true, scheme = {"account-user"})
@List({@Redirect(code = "success", uri = "/account/webauthn/?client_id=${client_id}&tenantId=${tenantId}"), @Redirect(code = "redirect-to-webauthn", uri = "/account/webauthn/?client_id=${client_id}&tenantId=${tenantId}"), @Redirect(code = "redirect-to-webauthn-add", uri = "/account/webauthn/add?client_id=${client_id}&tenantId=${tenantId}")})
public class AddAction extends BaseAccountAction {
  private final WebAuthnFrontendService webAuthnFrontendService;
  
  public String displayName;
  
  public String webAuthnRegisterRequest;
  
  @Inject
  public AddAction(FrontEndSupport paramFrontEndSupport, FrontEndThemeResolver paramFrontEndThemeResolver, LoginIntentService paramLoginIntentService, OAuthService paramOAuthService, SSOService paramSSOService, ThreatDetectionService paramThreatDetectionService, WebAuthnFrontendService paramWebAuthnFrontendService) {
    super(paramFrontEndSupport, paramFrontEndThemeResolver, paramLoginIntentService, paramOAuthService, paramSSOService, paramThreatDetectionService);
    mapWebAuthnFieldErrorsToGeneralErrors();
    this.webAuthnFrontendService = paramWebAuthnFrontendService;
  }
  
  public String get() {
    if (!this.webauthnAvailable) {
      addGeneralError("[WebAuthnDisabled]", new Object[0]);
      return "redirect-to-account";
    } 
    return "input";
  }
  
  public String post() {
    WebAuthnRegisterCompleteRequest webAuthnRegisterCompleteRequest = this.webAuthnFrontendService.unmarshalWebAuthnRegistrationRequest(this.webAuthnRegisterRequest, this.userId);
    if (webAuthnRegisterCompleteRequest == null) {
      addGeneralError("[InvalidWebAuthnBrowserResponse]", new Object[0]);
      return "redirect-to-webauthn-add";
    } 
    ClientResponse<WebAuthnRegisterCompleteResponse, Errors> clientResponse = this.client.completeWebAuthnRegistration(webAuthnRegisterCompleteRequest);
    if (clientResponse.wasSuccessful())
      return "success"; 
    if (clientResponse.status == 404)
      clientResponse.errorResponse = (new Errors()).addGeneralError("[WebAuthnFailed]", null, new Object[0]); 
    transferErrors((Errors)clientResponse.errorResponse);
    return "redirect-to-webauthn-add";
  }
  
  @ValidationMethod(httpMethods = {"GET", "POST"})
  public void validate() {
    accountValidation();
    if (this.frontEndSupport.isPOST()) {
      AddAction addAction = this;
      (new Validator()).notBlank(this.displayName, "displayName", new Object[0]).done(paramErrors -> paramAddAction.transferErrors(paramErrors));
    } 
  }
}
