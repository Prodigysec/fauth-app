package io.fusionauth.app.action.account.webauthn;

import com.google.inject.Inject;
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
import java.util.ArrayList;
import java.util.List;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(requiresAuthentication = true, scheme = {"account-user"})
public class IndexAction extends BaseAccountAction {
  public List<WebAuthnCredential> webAuthnCredentials = new ArrayList<>();
  
  @Inject
  public IndexAction(FrontEndSupport paramFrontEndSupport, FrontEndThemeResolver paramFrontEndThemeResolver, LoginIntentService paramLoginIntentService, OAuthService paramOAuthService, SSOService paramSSOService, ThreatDetectionService paramThreatDetectionService) {
    super(paramFrontEndSupport, paramFrontEndThemeResolver, paramLoginIntentService, paramOAuthService, paramSSOService, paramThreatDetectionService);
  }
  
  public String get() {
    if (!this.webauthnAvailable)
      return "redirect-to-account"; 
    this.webAuthnCredentials = ((WebAuthnCredentialResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveWebAuthnCredentialsForUser(this.codeUser.id))).credentials;
    return "input";
  }
  
  @ValidationMethod(httpMethods = {"GET"})
  public void validate() {
    accountValidation();
  }
}
