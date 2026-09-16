package io.fusionauth.app.action.oauth2;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import com.inversoft.util.StringTools;
import io.fusionauth.api.service.oauth2.OAuthService;
import io.fusionauth.api.service.security.ThreatDetectionService;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.primeframework.ThemedForward;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.FrontEndThemeResolver;
import io.fusionauth.app.service.security.LoginIntentService;
import io.fusionauth.app.service.security.SSOService;
import io.fusionauth.app.service.webauthn.WebAuthnFrontendService;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.User;
import io.fusionauth.domain.WebAuthnCredential;
import io.fusionauth.domain.api.UserResponse;
import io.fusionauth.domain.api.WebAuthnCredentialResponse;
import io.fusionauth.domain.api.WebAuthnLoginRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;
import org.primeframework.mvc.parameter.annotation.PreRenderMethod;
import org.primeframework.mvc.validation.ValidationMethod;

@Action
@Redirect(code = "webauthn-error", uri = "${redirectToWebAuthnURI}")
public class WebauthnReauthAction extends BaseOAuthAuthenticationAction {
  private final WebAuthnFrontendService webAuthnFrontendService;
  
  public UUID credentialId;
  
  public boolean rememberDevice = true;
  
  @FTLVariable
  public Map<UUID, User> users = new HashMap<>();
  
  @FTLVariable
  public List<WebAuthnCredential> webAuthnCredentials = new ArrayList<>();
  
  public String webAuthnRequest;
  
  @Inject
  public WebauthnReauthAction(FrontEndSupport paramFrontEndSupport, FrontEndThemeResolver paramFrontEndThemeResolver, LoginIntentService paramLoginIntentService, OAuthService paramOAuthService, SSOService paramSSOService, ThreatDetectionService paramThreatDetectionService, WebAuthnFrontendService paramWebAuthnFrontendService) {
    super(paramFrontEndSupport, paramFrontEndThemeResolver, paramLoginIntentService, paramOAuthService, paramSSOService, paramThreatDetectionService);
    this.webAuthnFrontendService = paramWebAuthnFrontendService;
    mapWebAuthnFieldErrorsToGeneralErrors();
  }
  
  public String get() {
    if (this.webAuthnCredentials.isEmpty()) {
      buildRedirectToAuthorizeURI();
      return "redirect-to-authorize";
    } 
    return "input";
  }
  
  @PostParameterMethod
  public void initialize() {
    persistRememberDeviceChoice(this.rememberDevice, this.frontEndSupport.isPOST());
    List<SSOService.WebAuthnReAuthenticationCredential> list = this.ssoService.getWebAuthnReAuthenticationCredentials(this.frontEndSupport.request.getCookies(), this.codeTenant);
    for (SSOService.WebAuthnReAuthenticationCredential webAuthnReAuthenticationCredential : list) {
      if (webAuthnReAuthenticationCredential.credentialId == null)
        continue; 
      ClientResponse<WebAuthnCredentialResponse, Errors> clientResponse = this.client.retrieveWebAuthnCredential(webAuthnReAuthenticationCredential.credentialId);
      if (clientResponse.wasSuccessful()) {
        WebAuthnCredential webAuthnCredential = ((WebAuthnCredentialResponse)clientResponse.successResponse).credential;
        this.webAuthnCredentials.add(webAuthnCredential);
        if (!this.users.containsKey(webAuthnCredential.userId)) {
          User user = ((UserResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveUser(paramWebAuthnCredential.userId))).user;
          this.users.put(user.id, user);
        } 
        continue;
      } 
      this.frontEndSupport.deleteCookies(new String[] { webAuthnReAuthenticationCredential.cookieName });
    } 
  }
  
  public String post() {
    if (StringTools.isTrimmedEmpty(this.webAuthnRequest)) {
      addGeneralInfo("[WebAuthnCredentialSelectionCanceled]", new Object[0]);
      this
        .redirectToWebAuthnReauthURI = baseQueryBuilder("/oauth2/webauthn-reauth").build();
      return "redirect-to-webauthn-reauth";
    } 
    UUID uUID = null;
    WebAuthnCredential webAuthnCredential = this.webAuthnCredentials.stream().filter(paramWebAuthnCredential -> paramWebAuthnCredential.id.equals(this.credentialId)).findFirst().orElse(null);
    if (webAuthnCredential != null) {
      ClientResponse<UserResponse, Errors> clientResponse = this.client.retrieveUser(webAuthnCredential.userId);
      if (clientResponse.wasSuccessful())
        uUID = ((UserResponse)clientResponse.successResponse).user.id; 
    } 
    WebAuthnLoginRequest webAuthnLoginRequest = this.webAuthnFrontendService.unmarshalWebAuthnLoginRequest(this.webAuthnRequest);
    if (webAuthnLoginRequest == null) {
      addGeneralError("[InvalidWebAuthnBrowserResponse]", new Object[0]);
      this
        .redirectToWebAuthnReauthURI = baseQueryBuilder("/oauth2/webauthn-reauth").build();
      return "redirect-to-webauthn-reauth";
    } 
    if (webAuthnCredential == null || !webAuthnCredential.credentialId.equals(webAuthnLoginRequest.credential.id)) {
      addGeneralError("[WebAuthnFailed]", new Object[0]);
      this
        .redirectToWebAuthnReauthURI = baseQueryBuilder("/oauth2/webauthn-reauth").build();
      return "redirect-to-webauthn-reauth";
    } 
    return handleLoginForWebAuthn(webAuthnLoginRequest, uUID, this.rememberDevice);
  }
  
  @PreRenderMethod({ThemedForward.class})
  public void prePageRenderSetup() {
    this.rememberDevice = getRememberDeviceCookieValue(this.rememberDevice, this.frontEndSupport.isGET());
  }
  
  @ValidationMethod(httpMethods = {"GET", "POST"})
  public void validate() {
    String str = validateAndHandleErrors(true);
    if (str != null)
      throw new ErrorException(str, false); 
  }
}
