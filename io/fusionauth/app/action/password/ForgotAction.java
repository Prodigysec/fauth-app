package io.fusionauth.app.action.password;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import com.inversoft.validator.Validator;
import io.fusionauth.api.service.oauth2.OAuthService;
import io.fusionauth.api.service.security.ThreatDetectionService;
import io.fusionauth.app.action.oauth2.BaseOAuthAction;
import io.fusionauth.app.primeframework.ThemedForward;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.FrontEndThemeResolver;
import io.fusionauth.app.service.security.LoginIntentService;
import io.fusionauth.app.service.security.SSOService;
import io.fusionauth.domain.api.user.ForgotPasswordRequest;
import io.fusionauth.domain.api.user.ForgotPasswordResponse;
import io.fusionauth.domain.oauth2.OAuthError;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.parameter.annotation.PreRenderMethod;
import org.primeframework.mvc.validation.ValidationMethod;
import org.primeframework.mvc.validation.annotation.PreValidationMethod;

@Action
@Redirect(uri = "${redirectToSentURI}")
public class ForgotAction extends BaseOAuthAction {
  @Deprecated(since = "1.59.0")
  public String email;
  
  public String redirectToSentURI;
  
  @Inject
  public ForgotAction(FrontEndSupport paramFrontEndSupport, FrontEndThemeResolver paramFrontEndThemeResolver, LoginIntentService paramLoginIntentService, OAuthService paramOAuthService, SSOService paramSSOService, ThreatDetectionService paramThreatDetectionService) {
    super(paramFrontEndSupport, paramFrontEndThemeResolver, paramLoginIntentService, paramOAuthService, paramSSOService, paramThreatDetectionService);
  }
  
  public String get() {
    return "input";
  }
  
  public String post() {
    ForgotPasswordRequest forgotPasswordRequest = (new ForgotPasswordRequest(this.frontEndSupport.buildEventInfo(this.metaData), this.loginId, captureState())).with(paramForgotPasswordRequest -> paramForgotPasswordRequest.loginIdTypes = StandardLoginIdTypes).with(paramForgotPasswordRequest -> paramForgotPasswordRequest.sendForgotPasswordMessage = Boolean.valueOf(true));
    forgotPasswordRequest.applicationId = (this.codeApplication != null) ? this.codeApplication.id : null;
    ClientResponse<ForgotPasswordResponse, Errors> clientResponse = this.client.forgotPassword(forgotPasswordRequest);
    if (clientResponse.wasSuccessful() || clientResponse.status == 404 || clientResponse.status == 422) {
      this
        
        .redirectToSentURI = baseQueryBuilder("/password/sent").with("loginId", this.loginId).build();
      allowConfirmationBypass();
      return "success";
    } 
    if (clientResponse.status == 403)
      addGeneralError("[ForgotPasswordDisabled]", new Object[0]); 
    transferErrors((Errors)clientResponse.errorResponse);
    return "input";
  }
  
  @PreRenderMethod({ThemedForward.class})
  public void prePageRenderSetup() {
    this.showCaptcha = (this.showCaptcha || showCaptchaOnInitialPageRender(null));
  }
  
  @PreValidationMethod
  public void preValidate() {
    if (this.frontEndSupport.isPOST())
      this.loginId = (this.loginId != null) ? this.loginId : this.email; 
  }
  
  @ValidationMethod(httpMethods = {"GET", "POST"})
  public void validate() {
    if (this.client_id != null) {
      String str = validateAndHandleErrors(true, paramOAuthError -> !paramOAuthError.reason.name().startsWith("missing_"));
      if (str != null)
        throw new ErrorException(str, false); 
    } 
    if (this.frontEndSupport.isPOST()) {
      Errors errors = (new Validator()).notBlank(this.loginId, "loginId", new Object[0]).done();
      if (errors.containsError("[blank]loginId"))
        errors.addFieldError("email", "[blank]email", null, new Object[0]); 
      transferErrors(errors);
      validateCaptchaAfterAttemptingToResolveUser(this.captcha_token, this.loginId, null, () -> this.showCaptcha = true);
    } 
  }
}
