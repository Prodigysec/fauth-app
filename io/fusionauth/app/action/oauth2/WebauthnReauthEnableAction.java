package io.fusionauth.app.action.oauth2;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import com.inversoft.util.StringTools;
import com.inversoft.validator.Validator;
import io.fusionauth.api.service.oauth2.OAuthService;
import io.fusionauth.api.service.security.ThreatDetectionService;
import io.fusionauth.api.service.user.IdentityHelper;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.primeframework.PostAuthenticationStep;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.FrontEndThemeResolver;
import io.fusionauth.app.service.security.LoginIntentService;
import io.fusionauth.app.service.security.SSOService;
import io.fusionauth.app.service.webauthn.WebAuthnFrontendService;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.UserRegistration;
import io.fusionauth.domain.WebAuthnCredential;
import io.fusionauth.domain.api.WebAuthnAssertResponse;
import io.fusionauth.domain.api.WebAuthnCredentialResponse;
import io.fusionauth.domain.api.WebAuthnLoginRequest;
import io.fusionauth.domain.api.WebAuthnRegisterCompleteRequest;
import io.fusionauth.domain.api.WebAuthnRegisterCompleteResponse;
import io.fusionauth.domain.webauthn.AuthenticatorAttachmentPreference;
import io.fusionauth.domain.webauthn.UserVerificationRequirement;
import java.util.ArrayList;
import java.util.List;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.validation.ValidationMethod;

@Action
public class WebauthnReauthEnableAction extends BaseOAuthCompletionAction {
  private final WebAuthnFrontendService webAuthnFrontendService;
  
  public String action;
  
  public String displayName;
  
  public boolean doNotAskAgain;
  
  @FTLVariable
  public List<WebAuthnCredential> webAuthnCredentials = new ArrayList<>();
  
  public String webAuthnLoginRequest;
  
  public String webAuthnRegisterRequest;
  
  @Inject
  public WebauthnReauthEnableAction(FrontEndSupport paramFrontEndSupport, FrontEndThemeResolver paramFrontEndThemeResolver, LoginIntentService paramLoginIntentService, OAuthService paramOAuthService, SSOService paramSSOService, ThreatDetectionService paramThreatDetectionService, WebAuthnFrontendService paramWebAuthnFrontendService) {
    super(paramFrontEndSupport, paramFrontEndThemeResolver, paramLoginIntentService, paramOAuthService, paramSSOService, paramThreatDetectionService);
    this.webAuthnFrontendService = paramWebAuthnFrontendService;
    mapWebAuthnFieldErrorsToGeneralErrors();
  }
  
  public String get() {
    return "input";
  }
  
  public String post() {
    WebAuthnCredential webAuthnCredential;
    ClientResponse<WebAuthnRegisterCompleteResponse, Errors> clientResponse;
    if ("skip".equals(this.action)) {
      if (this.doNotAskAgain) {
        SSOService.CookieValue cookieValue = this.ssoService.buildWebAuthnReAuthenticationCookie(this.tenant, this.codeUser.id, null);
        this.frontEndSupport.addHttpOnlyPersistentCookie(cookieValue.name, cookieValue.value);
      } 
      UserRegistration userRegistration = this.codeUser.getRegistrationForApplication(this.codeApplication.id);
      this.userState = IdentityHelper.getUserStateFromUserAndRegistration(this.codeUser, userRegistration);
      return (getNextIntent(this.codeUser, userRegistration, PostAuthenticationStep.WebAuthnReauthEnable)).step.getResultCode();
    } 
    if (StringTools.isTrimmedEmpty(this.webAuthnLoginRequest) && StringTools.isTrimmedEmpty(this.webAuthnRegisterRequest)) {
      addGeneralInfo("[WebAuthnCredentialSelectionCanceled]", new Object[0]);
      buildRedirectToWebAuthnReauthEnableURI();
      return "redirect-to-webauthn-reauth-enable";
    } 
    if (this.webAuthnLoginRequest != null) {
      WebAuthnLoginRequest webAuthnLoginRequest = this.webAuthnFrontendService.unmarshalWebAuthnLoginRequest(this.webAuthnLoginRequest);
      if (webAuthnLoginRequest == null) {
        addGeneralError("[InvalidWebAuthnBrowserResponse]", new Object[0]);
        buildRedirectToWebAuthnReauthEnableURI();
        return "redirect-to-webauthn-reauth-enable";
      } 
      ClientResponse<WebAuthnAssertResponse, Errors> clientResponse1 = this.client.completeWebAuthnAssertion(webAuthnLoginRequest);
      webAuthnCredential = clientResponse1.wasSuccessful() ? ((WebAuthnAssertResponse)clientResponse1.successResponse).credential : null;
    } else {
      WebAuthnRegisterCompleteRequest webAuthnRegisterCompleteRequest = this.webAuthnFrontendService.unmarshalWebAuthnRegistrationRequest(this.webAuthnRegisterRequest, this.codeUser.id);
      if (webAuthnRegisterCompleteRequest == null) {
        addGeneralError("[InvalidWebAuthnBrowserResponse]", new Object[0]);
        buildRedirectToWebAuthnReauthEnableURI();
        return "redirect-to-webauthn-reauth-enable";
      } 
      clientResponse = this.client.completeWebAuthnRegistration(webAuthnRegisterCompleteRequest);
      webAuthnCredential = clientResponse.wasSuccessful() ? ((WebAuthnRegisterCompleteResponse)clientResponse.successResponse).credential : null;
    } 
    if (clientResponse.wasSuccessful()) {
      SSOService.CookieValue cookieValue = this.ssoService.buildWebAuthnReAuthenticationCookie(this.tenant, this.codeUser.id, webAuthnCredential.id);
      this.frontEndSupport.addHttpOnlyPersistentCookie(cookieValue.name, cookieValue.value);
      UserRegistration userRegistration = this.codeUser.getRegistrationForApplication(this.codeApplication.id);
      this.userState = IdentityHelper.getUserStateFromUserAndRegistration(this.codeUser, userRegistration);
      return (getNextIntent(this.codeUser, userRegistration, PostAuthenticationStep.WebAuthnReauthEnable)).step.getResultCode();
    } 
    if (clientResponse.status == 404)
      clientResponse.errorResponse = (new Errors()).addGeneralError("[WebAuthnFailed]", null, new Object[0]); 
    transferErrors((Errors)clientResponse.errorResponse);
    buildRedirectToWebAuthnReauthEnableURI();
    return "redirect-to-webauthn-reauth-enable";
  }
  
  @ValidationMethod(httpMethods = {"GET", "POST"})
  public void validateGet() {
    String str = validateAndHandleErrors(false);
    if (str != null)
      throw new ErrorException(str, false); 
    if (this.codeUser == null) {
      deleteLoginIntent();
      buildRedirectToAuthorizeURI();
      throw new ErrorException("redirect-to-authorize");
    } 
    if (getPostAuthenticationStep() != this.codeLoginIntent.step)
      throw new ErrorException(handleRedirectToExpectedStep(this.codeLoginIntent)); 
    retrieveCredentials();
    if (this.frontEndSupport.isPOST()) {
      if ("skip".equals(this.action))
        return; 
      WebauthnReauthEnableAction webauthnReauthEnableAction = this;
      (new Validator()).ifTrue("register".equals(this.action), paramValidator -> paramValidator.notBlank(this.displayName, "displayName", new Object[0])).done(paramErrors -> paramWebauthnReauthEnableAction.transferErrors(paramErrors));
    } 
  }
  
  @JsonIgnore
  protected PostAuthenticationStep getPostAuthenticationStep() {
    return PostAuthenticationStep.WebAuthnReauthEnable;
  }
  
  private void retrieveCredentials() {
    this










      
      .webAuthnCredentials = ((WebAuthnCredentialResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveWebAuthnCredentialsForUser(this.codeUser.id))).credentials.stream().filter(paramWebAuthnCredential -> {
          switch (this.tenant.webAuthnConfiguration.reauthenticationWorkflow.authenticatorAttachmentPreference) {
            default:
              throw new MatchException(null, null);
            case any:
            
            case platform:
              return (paramWebAuthnCredential.transports.contains("internal") || paramWebAuthnCredential.transports.isEmpty());
            case crossPlatform:
              break;
          } 
          return (paramWebAuthnCredential.transports.stream().anyMatch(()) || paramWebAuthnCredential.transports.isEmpty());
        }).filter(paramWebAuthnCredential -> (this.tenant.webAuthnConfiguration.reauthenticationWorkflow.userVerificationRequirement != UserVerificationRequirement.required || paramWebAuthnCredential.authenticatorSupportsUserVerification)).toList();
  }
}
