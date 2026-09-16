package io.fusionauth.app.action.registration;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import com.inversoft.validator.Validator;
import io.fusionauth.api.domain.ConfirmationRequiredReason;
import io.fusionauth.api.service.security.ThreatDetectionService;
import io.fusionauth.app.action.BaseVerifyAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.FrontEndThemeResolver;
import io.fusionauth.app.service.security.LoginIntentService;
import io.fusionauth.app.service.security.SSOService;
import io.fusionauth.domain.IdentityType;
import io.fusionauth.domain.api.user.VerifyRegistrationRequest;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.validation.ValidationMethod;

@Action("{verificationId}")
public class VerifyAction extends BaseVerifyAction {
  public String email;
  
  @Inject
  public VerifyAction(FrontEndSupport paramFrontEndSupport, FrontEndThemeResolver paramFrontEndThemeResolver, LoginIntentService paramLoginIntentService, SSOService paramSSOService, ThreatDetectionService paramThreatDetectionService) {
    super(paramFrontEndThemeResolver, paramLoginIntentService, paramSSOService, paramThreatDetectionService, paramFrontEndSupport);
  }
  
  public String get() {
    if (this.verificationId == null)
      return "input"; 
    if (requireUserConfirmation())
      return redirectToConfirmationRequired(ConfirmationRequiredReason.verifyRegistration); 
    ClientResponse<Void, Errors> clientResponse = this.client.verifyUserRegistration(new VerifyRegistrationRequest(this.frontEndSupport.buildEventInfo(null), this.verificationId));
    return handleVerifyResponse((String)null, clientResponse);
  }
  
  public String post() {
    UUID uUID = (this.codeApplication != null) ? this.codeApplication.id : null;
    return handleResendResponse(this.client.resendRegistrationVerification(this.email, uUID), this.email, IdentityType.email);
  }
  
  @ValidationMethod(httpMethods = {"POST"})
  public void validatePost() {
    VerifyAction verifyAction = this;
    (new Validator()).notBlank(this.email, "email", new Object[0]).done(paramErrors -> paramVerifyAction.transferErrors(paramErrors));
    validateCaptchaAfterAttemptingToResolveUser(this.captcha_token, this.email, null, () -> this.showCaptcha = true);
  }
  
  protected String getBaseCompleteURI() {
    return "/registration/complete";
  }
  
  protected String getBaseSentURI() {
    return "/registration/sent";
  }
}
