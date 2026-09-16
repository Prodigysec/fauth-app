package io.fusionauth.app.action.oauth2;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import com.inversoft.util.StringTools;
import com.inversoft.validator.Validator;
import io.fusionauth.api.service.oauth2.OAuthService;
import io.fusionauth.api.service.security.ThreatDetectionService;
import io.fusionauth.app.primeframework.ThemedForward;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.FrontEndThemeResolver;
import io.fusionauth.app.service.security.LoginIntentService;
import io.fusionauth.app.service.security.SSOService;
import io.fusionauth.app.service.webauthn.WebAuthnFrontendService;
import io.fusionauth.domain.api.UserResponse;
import io.fusionauth.domain.api.WebAuthnLoginRequest;
import java.util.UUID;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;
import org.primeframework.mvc.parameter.annotation.PreRenderMethod;
import org.primeframework.mvc.validation.ValidationMethod;

@Action
@Redirect(code = "webauthn-error", uri = "${redirectToWebAuthnURI}")
public class WebauthnAction extends BaseOAuthAuthenticationAction {
  private final WebAuthnFrontendService webAuthnFrontendService;
  
  public boolean rememberDevice = true;
  
  public String webAuthnRequest;
  
  @Inject
  public WebauthnAction(FrontEndSupport paramFrontEndSupport, FrontEndThemeResolver paramFrontEndThemeResolver, LoginIntentService paramLoginIntentService, OAuthService paramOAuthService, SSOService paramSSOService, ThreatDetectionService paramThreatDetectionService, WebAuthnFrontendService paramWebAuthnFrontendService) {
    super(paramFrontEndSupport, paramFrontEndThemeResolver, paramLoginIntentService, paramOAuthService, paramSSOService, paramThreatDetectionService);
    this.webAuthnFrontendService = paramWebAuthnFrontendService;
    mapWebAuthnFieldErrorsToGeneralErrors();
  }
  
  public String get() {
    return "input";
  }
  
  @PostParameterMethod
  public void initialize() {
    persistRememberDeviceChoice(this.rememberDevice, this.frontEndSupport.isPOST());
  }
  
  public String post() {
    if (StringTools.isTrimmedEmpty(this.webAuthnRequest)) {
      addGeneralInfo("[WebAuthnCredentialSelectionCanceled]", new Object[0]);
      buildRedirectToWebAuthnURI();
      return "redirect-to-webauthn";
    } 
    UUID uUID = null;
    ClientResponse<UserResponse, Errors> clientResponse = this.client.retrieveUserByLoginIdWithLoginIdTypes(this.loginId, StandardLoginIdTypes);
    if (clientResponse.wasSuccessful())
      uUID = ((UserResponse)clientResponse.successResponse).user.id; 
    WebAuthnLoginRequest webAuthnLoginRequest = this.webAuthnFrontendService.unmarshalWebAuthnLoginRequest(this.webAuthnRequest);
    if (webAuthnLoginRequest == null) {
      addGeneralError("[InvalidWebAuthnBrowserResponse]", new Object[0]);
      buildRedirectToWebAuthnURI();
      return "redirect-to-webauthn";
    } 
    return handleLoginForWebAuthn(webAuthnLoginRequest, uUID, this.rememberDevice);
  }
  
  @PreRenderMethod({ThemedForward.class})
  public void prePageRenderSetup() {
    this.rememberDevice = getRememberDeviceCookieValue(this.rememberDevice, this.frontEndSupport.isGET());
    this.showCaptcha = (this.showCaptcha || showCaptchaOnInitialPageRender(this.codeUserId));
  }
  
  @ValidationMethod(httpMethods = {"GET", "POST"})
  public void validate() {
    String str = validateAndHandleErrors(true);
    if (str != null)
      throw new ErrorException(str, false); 
    if (this.frontEndSupport.isPOST()) {
      Errors errors = (new Validator()).notBlank(this.loginId, "loginId", new Object[0]).done();
      transferErrors(errors);
      if (errors.empty())
        validateCaptchaAfterAttemptingToResolveUser(this.captcha_token, this.loginId, null, () -> this.showCaptcha = true); 
    } 
  }
}
