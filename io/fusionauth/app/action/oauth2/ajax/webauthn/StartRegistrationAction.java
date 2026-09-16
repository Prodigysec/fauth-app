package io.fusionauth.app.action.oauth2.ajax.webauthn;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.service.oauth2.OAuthService;
import io.fusionauth.api.service.security.ThreatDetectionService;
import io.fusionauth.app.action.oauth2.BaseOAuthAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.FrontEndThemeResolver;
import io.fusionauth.app.service.security.LoginIntentService;
import io.fusionauth.app.service.security.SSOService;
import io.fusionauth.domain.api.WebAuthnRegisterStartRequest;
import io.fusionauth.domain.api.WebAuthnRegisterStartResponse;
import io.fusionauth.domain.webauthn.WebAuthnWorkflow;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;

@Action
public class StartRegistrationAction extends BaseOAuthAJAXAction {
  public String displayName;
  
  @JSONResponse
  public WebAuthnRegisterStartResponse response;
  
  public String userAgent;
  
  private LoginIntentService.LoginIntent loginIntent;
  
  @Inject
  public StartRegistrationAction(FrontEndSupport paramFrontEndSupport, FrontEndThemeResolver paramFrontEndThemeResolver, LoginIntentService paramLoginIntentService, OAuthService paramOAuthService, SSOService paramSSOService, ThreatDetectionService paramThreatDetectionService) {
    super(paramFrontEndSupport, paramFrontEndThemeResolver, paramLoginIntentService, paramOAuthService, paramSSOService, paramThreatDetectionService);
    mapWebAuthnFieldErrorsToGeneralErrors();
  }
  
  public String post() {
    ClientResponse<WebAuthnRegisterStartResponse, Errors> clientResponse = this.client.startWebAuthnRegistration((new WebAuthnRegisterStartRequest())
        .with(paramWebAuthnRegisterStartRequest -> paramWebAuthnRegisterStartRequest.userAgent = this.userAgent)
        .with(paramWebAuthnRegisterStartRequest -> paramWebAuthnRegisterStartRequest.userId = this.codeUserId)
        .with(paramWebAuthnRegisterStartRequest -> paramWebAuthnRegisterStartRequest.displayName = this.displayName)
        .with(paramWebAuthnRegisterStartRequest -> paramWebAuthnRegisterStartRequest.workflow = WebAuthnWorkflow.reauthentication));
    if (clientResponse.wasSuccessful()) {
      this.response = (WebAuthnRegisterStartResponse)clientResponse.successResponse;
    } else if (clientResponse.errorResponse != null) {
      if (((Errors)clientResponse.errorResponse).fieldErrors.containsKey("workflow"))
        return "disabled"; 
      transferErrors((Errors)clientResponse.errorResponse);
      throw new ErrorException("input", false);
    } 
    return "render-json";
  }
  
  public void resolveUserAndZoneId() {
    setUserVariables((this.loginIntent != null) ? this.loginIntent.user : null);
  }
  
  @PostParameterMethod
  public void retrieveLoginIntent() {
    this.loginIntent = this.loginIntentService.getLoginIntent(this.codeTenant, this.codeApplication, this.loginIntentCookie);
    resolveUserAndZoneId();
  }
}
