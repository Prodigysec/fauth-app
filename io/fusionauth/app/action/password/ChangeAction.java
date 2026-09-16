package io.fusionauth.app.action.password;

import com.google.inject.Inject;
import com.inversoft.error.Error;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import com.inversoft.util.SecurityTools;
import com.inversoft.validator.Validator;
import io.fusionauth.api.domain.ConfirmationRequiredReason;
import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.api.service.authentication.AuthenticationType;
import io.fusionauth.api.service.oauth2.OAuthService;
import io.fusionauth.api.service.security.ThreatDetectionService;
import io.fusionauth.api.util.UUIDTools;
import io.fusionauth.app.action.oauth2.BaseOAuthAuthenticationAction;
import io.fusionauth.app.primeframework.ThemedForward;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.FrontEndThemeResolver;
import io.fusionauth.app.service.security.LoginIntentService;
import io.fusionauth.app.service.security.SSOService;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.PasswordValidationRules;
import io.fusionauth.domain.api.LoginRequest;
import io.fusionauth.domain.api.twoFactor.TwoFactorStartRequest;
import io.fusionauth.domain.api.twoFactor.TwoFactorStartResponse;
import io.fusionauth.domain.api.user.ChangePasswordRequest;
import io.fusionauth.domain.api.user.ChangePasswordResponse;
import io.fusionauth.domain.oauth2.OAuthError;
import io.fusionauth.http.Cookie;
import java.util.Map;
import java.util.UUID;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;
import org.primeframework.mvc.parameter.annotation.PreRenderMethod;
import org.primeframework.mvc.scope.annotation.ManagedCookie;
import org.primeframework.mvc.util.QueryStringBuilder;
import org.primeframework.mvc.validation.ValidationMethod;

@Action("{changePasswordId}")
@List({@Redirect(code = "retry", uri = "${redirectToForgotPasswordURI}"), @Redirect(code = "success", uri = "${completeURI}")})
public class ChangeAction extends BaseOAuthAuthenticationAction {
  public String changePasswordId;
  
  public String completeURI;
  
  public String passwordConfirm;
  
  public PasswordValidationRules passwordValidationRules;
  
  public boolean rememberDevice = true;
  
  @ManagedCookie(name = "fusionauth.trust_c")
  public Cookie trustChallenge;
  
  @ManagedCookie(name = "fusionauth.trust_t")
  public Cookie trustToken;
  
  @Inject
  public ChangeAction(FrontEndSupport paramFrontEndSupport, FrontEndThemeResolver paramFrontEndThemeResolver, LoginIntentService paramLoginIntentService, OAuthService paramOAuthService, SSOService paramSSOService, ThreatDetectionService paramThreatDetectionService) {
    super(paramFrontEndSupport, paramFrontEndThemeResolver, paramLoginIntentService, paramOAuthService, paramSSOService, paramThreatDetectionService);
    paramFrontEndSupport.customPreTransferErrorConsumer = (paramErrors -> paramFrontEndSupport.moveFieldErrorToGeneral(paramErrors, "changePasswordId", "[missing]changePasswordId", "[InvalidChangePasswordId]"));
  }
  
  public String get() {
    if (this.client_id != null && this.redirect_uri != null && this.response_type != null)
      addGeneralInfo("[UserWillBeLoggedIn]", new Object[0]); 
    return "input";
  }
  
  @PostParameterMethod
  public void initialize() {
    persistRememberDeviceChoice(this.rememberDevice, this.frontEndSupport.isPOST());
  }
  
  public String post() {
    ClientResponse<ChangePasswordResponse, Errors> clientResponse = this.client.changePassword(this.changePasswordId, (new ChangePasswordRequest())
        .with(paramChangePasswordRequest -> paramChangePasswordRequest.applicationId = (this.codeApplication != null) ? this.codeApplication.id : null)
        .with(paramChangePasswordRequest -> paramChangePasswordRequest.eventInfo = this.frontEndSupport.buildEventInfo(null))
        .with(paramChangePasswordRequest -> paramChangePasswordRequest.password = this.password)
        .with(paramChangePasswordRequest -> paramChangePasswordRequest.trustChallenge = this.trustChallenge.getValue())
        .with(paramChangePasswordRequest -> paramChangePasswordRequest.trustToken = this.trustToken.getValue()));
    if (!clientResponse.wasSuccessful()) {
      if (clientResponse.status == 404) {
        addGeneralError("[InvalidChangePasswordId]", new Object[0]);
      } else if (isTrustTokenRequired(clientResponse)) {
        addGeneralInfo("[TrustTokenExpired]", new Object[0]);
        ExternalIdentifier externalIdentifier = this.frontEndSupport.getExternalId(this.codeTenant.id, this.changePasswordId, new ExternalIdentifier.ExternalIdType[] { ExternalIdentifier.ExternalIdType.ChangePassword, ExternalIdentifier.ExternalIdType.SetupPassword });
        startTwoFactor(externalIdentifier.userId);
      } else {
        transferErrors((Errors)clientResponse.errorResponse);
      } 
      return "input";
    } 
    removeConfirmationBypass();
    this.ssoSession = this.ssoService.getSession(this.codeTenant, this.ssoCookie);
    if (((ChangePasswordResponse)clientResponse.successResponse).state != null)
      unpackSavedState(((ChangePasswordResponse)clientResponse.successResponse).state); 
    this.trustChallenge = null;
    this.trustToken = null;
    String str = validateAndHandleErrors(true);
    if (str != null) {
      this

        
        .completeURI = QueryStringBuilder.builder("/password/complete").with("client_id", this.client_id).with("tenantId", this.tenantId).build();
      return "success";
    } 
    LoginRequest loginRequest = (new LoginRequest()).with(paramLoginRequest -> paramLoginRequest.applicationId = this.codeApplication.id).with(paramLoginRequest -> paramLoginRequest.eventInfo = this.frontEndSupport.buildEventInfo(this.metaData)).with(paramLoginRequest -> paramLoginRequest.oneTimePassword = ((ChangePasswordResponse)paramClientResponse.successResponse).oneTimePassword);
    return callLogin(loginRequest, this.rememberDevice ? SSOService.RememberDeviceState.Remember : SSOService.RememberDeviceState.Forget, AuthenticationType.ONE_TIME_PASSWORD, true);
  }
  
  @PreRenderMethod({ThemedForward.class})
  public void prePageRenderSetup() {
    this.rememberDevice = getRememberDeviceCookieValue(this.rememberDevice, this.frontEndSupport.isGET());
    this.showCaptcha = (this.showCaptcha || showCaptchaOnInitialPageRender(this.codeUserId));
  }
  
  @ValidationMethod(httpMethods = {"GET"})
  public void validateGet() {
    this.passwordValidationRules = this.codeTenant.passwordValidationRules;
    if (this.changePasswordId == null) {
      addGeneralError("[MissingChangePasswordId]", new Object[0]);
      buildForgotPasswordRedirectURI();
      throw new ErrorException("retry");
    } 
    if (this.client_id != null) {
      String str = validateAndHandleErrors(true, paramOAuthError -> !paramOAuthError.reason.name().startsWith("missing_"));
      if (str != null)
        throw new ErrorException(str, false); 
    } 
    validateChangeRequest();
  }
  
  @ValidationMethod(httpMethods = {"POST"})
  public void validatePost() {
    this.passwordValidationRules = this.codeTenant.passwordValidationRules;
    ChangeAction changeAction = this;
    (new Validator()).notBlank(this.password, "password", new Object[0]).notBlank(this.passwordConfirm, "passwordConfirm", new Object[0]).ifNoErrors(paramValidator -> paramValidator.ensure(this.password.equals(this.passwordConfirm), "password", "[notEqual]", new Object[0])).done(paramErrors -> paramChangeAction.transferErrors(paramErrors));
    ExternalIdentifier externalIdentifier = this.frontEndSupport.getExternalId(this.codeTenant.id, this.changePasswordId, new ExternalIdentifier.ExternalIdType[] { ExternalIdentifier.ExternalIdType.ChangePassword, ExternalIdentifier.ExternalIdType.SetupPassword });
    UUID uUID = (externalIdentifier != null) ? externalIdentifier.userId : null;
    validateCaptchaAfterAttemptingToResolveUser(this.captcha_token, null, uUID, () -> this.showCaptcha = true);
  }
  
  private boolean isTrustTokenRequired(ClientResponse<?, Errors> paramClientResponse) {
    return (paramClientResponse.status == 400 && ((Errors)paramClientResponse.errorResponse).generalErrors.stream().anyMatch(paramError -> paramError.code.equals("[TrustTokenRequired]")));
  }
  
  private void startTwoFactor(UUID paramUUID) {
    this.twoFactorTrustCookie = null;
    this.trustChallenge.setValue(SecurityTools.secureRandom());
    UUID uUID = UUIDTools.fromString(this.client_id);
    TwoFactorStartResponse twoFactorStartResponse = this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.startTwoFactorLogin((new TwoFactorStartRequest()).with(()).with(()).with(()).with(())));
    this.twoFactorId = twoFactorStartResponse.twoFactorId;
    buildRedirectToTwoFactorMethodsURI();
    throw new ErrorException("redirect-to-two-factor-methods");
  }
  
  private void validateChangeRequest() {
    ExternalIdentifier externalIdentifier = this.frontEndSupport.getExternalId(this.codeTenant.id, this.changePasswordId, new ExternalIdentifier.ExternalIdType[] { ExternalIdentifier.ExternalIdType.ChangePassword, ExternalIdentifier.ExternalIdType.SetupPassword });
    if (externalIdentifier == null) {
      addGeneralError("[InvalidChangePasswordId]", new Object[0]);
      buildForgotPasswordRedirectURI();
      throw new ErrorException("retry");
    } 
    if (this.trustToken.getValue() != null) {
      ExternalIdentifier externalIdentifier1 = this.frontEndSupport.getExternalId(this.codeTenant.id, this.trustToken.getValue(), new ExternalIdentifier.ExternalIdType[] { ExternalIdentifier.ExternalIdType.TrustToken });
      if (externalIdentifier1 == null || !externalIdentifier.userId.equals(externalIdentifier1.userId))
        this.trustToken.setValue(null); 
    } 
    if (this.trustToken.getValue() == null) {
      ClientResponse<Void, Errors> clientResponse = this.client.checkChangePasswordUsingIdAndIPAddress(this.changePasswordId, this.frontEndSupport
          .getTrustedClientIPAddress());
      if (isTrustTokenRequired(clientResponse)) {
        if (requireUserConfirmation()) {
          String str = redirectToConfirmationRequired(ConfirmationRequiredReason.changePasswordMultiFactor);
          throw new ErrorException(str);
        } 
        unpackSavedState(externalIdentifier.getStateHelper());
        addGeneralInfo("[TrustTokenRequiredToChangePassword]", new Object[0]);
        startTwoFactor(externalIdentifier.userId);
      } 
    } 
    this.codeUserId = externalIdentifier.userId;
  }
}
