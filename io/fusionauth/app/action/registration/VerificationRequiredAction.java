package io.fusionauth.app.action.registration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import com.inversoft.validator.Validator;
import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.api.service.oauth2.OAuthService;
import io.fusionauth.api.service.security.ThreatDetectionService;
import io.fusionauth.api.service.user.ExternalIdentifierReaderService;
import io.fusionauth.app.action.BaseVerificationRequiredAction;
import io.fusionauth.app.primeframework.PostAuthenticationStep;
import io.fusionauth.app.primeframework.ThemedForward;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.FrontEndThemeResolver;
import io.fusionauth.app.service.security.LoginIntentService;
import io.fusionauth.app.service.security.SSOService;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserRegistration;
import io.fusionauth.domain.VerificationStrategy;
import io.fusionauth.domain.api.user.VerifyRegistrationResponse;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.parameter.annotation.PreRenderMethod;
import org.primeframework.mvc.validation.ValidationMethod;

@Action("{verificationId}")
public class VerificationRequiredAction extends BaseVerificationRequiredAction {
  @Inject
  public VerificationRequiredAction(ExternalIdentifierReaderService paramExternalIdentifierReaderService, FrontEndSupport paramFrontEndSupport, FrontEndThemeResolver paramFrontEndThemeResolver, LoginIntentService paramLoginIntentService, OAuthService paramOAuthService, SSOService paramSSOService, ThreatDetectionService paramThreatDetectionService) {
    super(paramFrontEndSupport, paramFrontEndThemeResolver, paramLoginIntentService, paramOAuthService, paramSSOService, paramThreatDetectionService, paramExternalIdentifierReaderService);
  }
  
  public String get() {
    if (this.externalIdentifierReader.retrieveByUserId(this.codeUser.id, ExternalIdentifier.ExternalIdType.RegistrationVerification) == null) {
      ClientResponse<VerifyRegistrationResponse, Errors> clientResponse = this.client.resendRegistrationVerification(this.codeUserEmail, this.codeApplication.id);
      if (clientResponse.wasSuccessful() && this.codeApplication.verificationStrategy == VerificationStrategy.FormField)
        this.verificationId = ((VerifyRegistrationResponse)clientResponse.successResponse).verificationId; 
    } 
    return "input";
  }
  
  public String post() {
    switch (this.action) {
      case "resend":
      
      case "verify":
      
    } 
    return 

      
      "input";
  }
  
  @PreRenderMethod({ThemedForward.class})
  public void prePageRenderSetup() {
    this.showCaptcha = (this.showCaptcha || showCaptchaOnInitialPageRender(this.codeUserId));
  }
  
  @ValidationMethod(httpMethods = {"GET", "POST"})
  public void validate() {
    String str = baseValidate();
    if (str != null)
      throw new ErrorException(str); 
    this.collectVerificationCode = (this.codeApplication.verificationStrategy == VerificationStrategy.FormField);
    if (this.frontEndSupport.isPOST()) {
      Validator validator = new Validator();
      if ("verify".equals(this.action))
        validator.notBlank(this.oneTimeCode, "oneTimeCode", new Object[0]); 
      if (!validator.hasErrors())
        if ("resend".equals(this.action) || "verify".equals(this.action))
          validateCaptchaAfterAttemptingToResolveUser(this.captcha_token, null, this.codeUserId, () -> this.showCaptcha = true);  
      transferErrors(validator.done());
    } 
  }
  
  protected void buildVerificationRedirect(String paramString) {
    buildRedirectToRegistrationVerificationRequired(paramString);
  }
  
  @JsonIgnore
  protected PostAuthenticationStep getPostAuthenticationStep() {
    return PostAuthenticationStep.RegistrationVerification;
  }
  
  protected boolean isAlreadyVerified(User paramUser) {
    UserRegistration userRegistration = paramUser.getRegistrationForApplication(this.codeApplication.id);
    return userRegistration.verified;
  }
}
