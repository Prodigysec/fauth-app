package io.fusionauth.app.action.account.ajax.webauthn;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.service.oauth2.OAuthService;
import io.fusionauth.api.service.security.ThreatDetectionService;
import io.fusionauth.app.action.account.BaseAccountAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.FrontEndThemeResolver;
import io.fusionauth.app.service.security.LoginIntentService;
import io.fusionauth.app.service.security.SSOService;
import io.fusionauth.domain.User;
import io.fusionauth.domain.api.WebAuthnRegisterStartRequest;
import io.fusionauth.domain.api.WebAuthnRegisterStartResponse;
import io.fusionauth.domain.webauthn.WebAuthnWorkflow;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.JSON;
import org.primeframework.mvc.action.result.annotation.Status;
import org.primeframework.mvc.action.result.annotation.Status.List;
import org.primeframework.mvc.content.json.annotation.JSONResponse;

@Action(requiresAuthentication = true, scheme = {"account-user"})
@JSON(code = "render-json", status = 200)
@List({@Status(code = "error", status = 400), @Status(code = "unauthenticated", status = 401), @Status(code = "unauthorized", status = 403)})
public class StartRegistrationAction extends BaseAccountAction {
  public String displayName;
  
  @JSONResponse
  public Object response;
  
  public String userAgent;
  
  @Inject
  public StartRegistrationAction(FrontEndSupport paramFrontEndSupport, FrontEndThemeResolver paramFrontEndThemeResolver, LoginIntentService paramLoginIntentService, OAuthService paramOAuthService, SSOService paramSSOService, ThreatDetectionService paramThreatDetectionService) {
    super(paramFrontEndSupport, paramFrontEndThemeResolver, paramLoginIntentService, paramOAuthService, paramSSOService, paramThreatDetectionService);
    mapWebAuthnFieldErrorsToGeneralErrors();
  }
  
  public String post() {
    User user = (User)this.frontEndSupport.userLoginSecurityContext.getCurrentUser();
    ClientResponse<WebAuthnRegisterStartResponse, Errors> clientResponse = this.client.startWebAuthnRegistration((new WebAuthnRegisterStartRequest()).with(paramWebAuthnRegisterStartRequest -> paramWebAuthnRegisterStartRequest.userAgent = this.userAgent)
        .with(paramWebAuthnRegisterStartRequest -> paramWebAuthnRegisterStartRequest.userId = (paramUser != null) ? paramUser.id : null)
        .with(paramWebAuthnRegisterStartRequest -> paramWebAuthnRegisterStartRequest.displayName = this.displayName)
        .with(paramWebAuthnRegisterStartRequest -> paramWebAuthnRegisterStartRequest.workflow = WebAuthnWorkflow.general));
    if (!clientResponse.wasSuccessful())
      return "error"; 
    this.response = clientResponse.successResponse;
    return "render-json";
  }
}
