package io.fusionauth.app.action.phone;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import com.inversoft.validator.Validator;
import io.fusionauth.api.service.oauth2.OAuthService;
import io.fusionauth.api.service.security.ThreatDetectionService;
import io.fusionauth.api.service.user.ExternalIdentifierReaderService;
import io.fusionauth.app.action.BaseVerificationRequiredAction;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.primeframework.PostAuthenticationStep;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.FrontEndThemeResolver;
import io.fusionauth.app.service.security.LoginIntentService;
import io.fusionauth.app.service.security.SSOService;
import io.fusionauth.domain.IdentityType;
import io.fusionauth.domain.User;
import io.fusionauth.domain.VerificationStrategy;
import io.fusionauth.domain.api.UserResponse;
import java.util.Map;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.validation.ValidationMethod;

@Action("{verificationId}")
public class VerificationRequiredAction extends BaseVerificationRequiredAction {
  @FTLVariable
  public boolean allowPhoneNumberChange;
  
  public String phoneNumber;
  
  @Inject
  public VerificationRequiredAction(FrontEndSupport paramFrontEndSupport, FrontEndThemeResolver paramFrontEndThemeResolver, LoginIntentService paramLoginIntentService, OAuthService paramOAuthService, SSOService paramSSOService, ThreatDetectionService paramThreatDetectionService, ExternalIdentifierReaderService paramExternalIdentifierReaderService) {
    super(paramFrontEndSupport, paramFrontEndThemeResolver, paramLoginIntentService, paramOAuthService, paramSSOService, paramThreatDetectionService, paramExternalIdentifierReaderService);
    this.errorMapping.put("user.phoneNumber", "phoneNumber");
  }
  
  public String get() {
    this.phoneNumber = this.codeUser.phoneNumber;
    if (resendIdentityVerificationRequired(this.phoneNumber, IdentityType.phoneNumber)) {
      BaseVerificationRequiredAction.IdentityResult identityResult = resendIdentityVerification(this.phoneNumber, IdentityType.phoneNumber);
      if (identityResult.response.wasSuccessful()) {
        if (this.codeTenant.phoneConfiguration.verificationStrategy == VerificationStrategy.FormField)
          this.verificationId = identityResult.verificationId; 
      } else if (((Errors)identityResult.response.errorResponse).containsError("[MessengerError]")) {
        addGeneralError("[MessengerError]", new Object[0]);
      } 
    } 
    return "input";
  }
  
  public String post() {
    switch (this.action) {
      case "changePhoneNumber":
      
      case "resend":
      
      case "verify":
      
    } 
    return 


      
      "input";
  }
  
  @ValidationMethod(httpMethods = {"GET", "POST"})
  public void validate() {
    String str = baseValidate();
    if (str != null)
      throw new ErrorException(str); 
    this.allowPhoneNumberChange = this.codeTenant.phoneConfiguration.unverified.allowPhoneNumberChangeWhenGated;
    this.collectVerificationCode = (this.codeTenant.phoneConfiguration.verificationStrategy == VerificationStrategy.FormField);
    if (this.frontEndSupport.isPOST()) {
      Validator validator = new Validator();
      if ("changePhoneNumber".equals(this.action)) {
        validator.notBlank(this.phoneNumber, "phoneNumber", new Object[0]);
      } else if ("verify".equals(this.action)) {
        validator.notBlank(this.oneTimeCode, "oneTimeCode", new Object[0]);
      } 
      if (!validator.hasErrors() && (
        "resend".equals(this.action) || "verify".equals(this.action)))
        validateCaptchaAfterAttemptingToResolveUser(this.captcha_token, null, this.codeUserId, () -> this.showCaptcha = true); 
      transferErrors(validator.done());
    } 
  }
  
  protected void buildVerificationRedirect(String paramString) {
    buildRedirectToPhoneVerificationRequired(paramString);
  }
  
  protected PostAuthenticationStep getPostAuthenticationStep() {
    return PostAuthenticationStep.PhoneVerification;
  }
  
  protected boolean isAlreadyVerified(User paramUser) {
    return !paramUser.resolvePrimaryIdentity(IdentityType.phoneNumber).verificationRequired();
  }
  
  private String changePhoneNumber(String paramString) {
    if (!this.codeTenant.phoneConfiguration.unverified.allowPhoneNumberChangeWhenGated)
      return "input"; 
    ClientResponse<UserResponse, Errors> clientResponse = this.client.patchUser(this.codeUser.id, Map.of("user", Map.of("phoneNumber", paramString)));
    if (clientResponse.wasSuccessful() || clientResponse.status == 429) {
      addGeneralInfo("[PhoneVerificationPhoneNumberUpdated]", new Object[0]);
      return handleResponseAndRedirectBackHere(this.codeTenant.phoneConfiguration.verificationStrategy, clientResponse.status, (String)null);
    } 
    return handleErrorResponse(clientResponse);
  }
  
  private String resendIdentityVerificationAndRedirect() {
    BaseVerificationRequiredAction.IdentityResult identityResult = resendIdentityVerification(this.phoneNumber, IdentityType.phoneNumber);
    if (identityResult.response.wasSuccessful() || identityResult.response.status == 429) {
      addGeneralInfo("[PhoneVerificationSent]", new Object[0]);
      return handleResponseAndRedirectBackHere(this.codeTenant.phoneConfiguration.verificationStrategy, identityResult.response.status, identityResult.verificationId);
    } 
    return handleErrorResponse(identityResult.response);
  }
}
