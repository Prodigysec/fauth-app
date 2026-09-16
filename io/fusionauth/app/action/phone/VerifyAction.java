package io.fusionauth.app.action.phone;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.validator.Validator;
import io.fusionauth.api.domain.ConfirmationRequiredReason;
import io.fusionauth.api.service.security.ThreatDetectionService;
import io.fusionauth.app.action.BaseVerifyAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.FrontEndThemeResolver;
import io.fusionauth.app.service.security.LoginIntentService;
import io.fusionauth.app.service.security.SSOService;
import io.fusionauth.domain.IdentityType;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.validation.ValidationMethod;

@Action("{verificationId}")
public class VerifyAction extends BaseVerifyAction {
  public String phoneNumber;
  
  @Inject
  public VerifyAction(FrontEndSupport paramFrontEndSupport, FrontEndThemeResolver paramFrontEndThemeResolver, LoginIntentService paramLoginIntentService, SSOService paramSSOService, ThreatDetectionService paramThreatDetectionService) {
    super(paramFrontEndThemeResolver, paramLoginIntentService, paramSSOService, paramThreatDetectionService, paramFrontEndSupport);
  }
  
  public String get() {
    if (requireUserConfirmation())
      return redirectToConfirmationRequired(ConfirmationRequiredReason.verifyPhone); 
    return verifyIdentity();
  }
  
  public String post() {
    return resendIdentityVerificationAndRedirect(this.phoneNumber, IdentityType.phoneNumber);
  }
  
  @ValidationMethod(httpMethods = {"POST"})
  public void validatePost() {
    VerifyAction verifyAction = this;
    (new Validator()).notBlank(this.phoneNumber, "phoneNumber", new Object[0]).done(paramErrors -> paramVerifyAction.transferErrors(paramErrors));
    validateCaptchaAfterAttemptingToResolveUser(this.captcha_token, this.phoneNumber, null, () -> this.showCaptcha = true);
  }
  
  protected String getBaseCompleteURI() {
    return "/phone/complete";
  }
  
  protected String getBaseSentURI() {
    return "/phone/sent";
  }
}
