package io.fusionauth.app.action.account.webauthn;

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
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.WebAuthnCredential;
import io.fusionauth.domain.api.WebAuthnCredentialResponse;
import java.util.UUID;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{id}", requiresAuthentication = true, scheme = {"account-user"})
@List({@Redirect(code = "missing", uri = "/account/webauthn/?client_id=${client_id}&tenantId=${tenantId}"), @Redirect(code = "success", uri = "/account/webauthn/?client_id=${client_id}&tenantId=${tenantId}")})
public class DeleteAction extends BaseAccountAction {
  public WebAuthnCredential credential;
  
  public UUID id;
  
  @Inject
  public DeleteAction(FrontEndSupport paramFrontEndSupport, FrontEndThemeResolver paramFrontEndThemeResolver, LoginIntentService paramLoginIntentService, OAuthService paramOAuthService, SSOService paramSSOService, ThreatDetectionService paramThreatDetectionService) {
    super(paramFrontEndSupport, paramFrontEndThemeResolver, paramLoginIntentService, paramOAuthService, paramSSOService, paramThreatDetectionService);
  }
  
  public String get() {
    if (this.id == null)
      return "missing"; 
    ClientResponse<WebAuthnCredentialResponse, Errors> clientResponse = this.client.retrieveWebAuthnCredential(this.id);
    if (clientResponse.wasSuccessful()) {
      this.credential = ((WebAuthnCredentialResponse)clientResponse.successResponse).credential;
      if (!this.credential.userId.equals(this.codeUser.id))
        this.credential = null; 
    } 
    if (this.credential == null)
      return "missing"; 
    return "input";
  }
  
  public String post() {
    if (this.id == null)
      return "missing"; 
    this.credential = ((WebAuthnCredentialResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveWebAuthnCredential(this.id))).credential;
    if (this.credential != null && !this.credential.userId.equals(this.codeUser.id))
      return "missing"; 
    this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.deleteWebAuthnCredential(this.id));
    return "success";
  }
  
  @ValidationMethod(httpMethods = {"GET", "POST"})
  public void validate() {
    accountValidation();
    if (!this.webauthnAvailable)
      throw new ErrorException("redirect-to-account", false); 
  }
}
