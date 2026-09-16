package io.fusionauth.app.action.email;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import java.util.Optional;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.validation.ValidationMethod;

@Action("{verificationId}")
public class VerificationRequiredAction extends BaseVerificationRequiredAction {
  @FTLVariable
  public boolean allowEmailChange;
  
  public String email;
  
  @Inject
  public VerificationRequiredAction(ExternalIdentifierReaderService paramExternalIdentifierReaderService, FrontEndSupport paramFrontEndSupport, FrontEndThemeResolver paramFrontEndThemeResolver, LoginIntentService paramLoginIntentService, OAuthService paramOAuthService, SSOService paramSSOService, ThreatDetectionService paramThreatDetectionService) {
    super(paramFrontEndSupport, paramFrontEndThemeResolver, paramLoginIntentService, paramOAuthService, paramSSOService, paramThreatDetectionService, paramExternalIdentifierReaderService);
    this.errorMapping.put("user.email", "email");
  }
  
  public String get() {
    this.email = this.codeUserEmail;
    if (resendIdentityVerificationRequired(this.codeUserEmail, IdentityType.email)) {
      BaseVerificationRequiredAction.IdentityResult identityResult = resendIdentityVerification(this.codeUserEmail, IdentityType.email);
      if (identityResult.response.wasSuccessful() && 
        this.codeTenant.emailConfiguration.verificationStrategy == VerificationStrategy.FormField)
        this.verificationId = identityResult.verificationId; 
    } 
    return "input";
  }
  
  public String post() {
    switch (this.action) {
      case "changeEmail":
      
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
    this.allowEmailChange = this.codeTenant.emailConfiguration.unverified.allowEmailChangeWhenGated;
    this.collectVerificationCode = (this.codeTenant.emailConfiguration.verificationStrategy == VerificationStrategy.FormField);
    if (this.frontEndSupport.isPOST()) {
      Validator validator = new Validator();
      if ("changeEmail".equals(this.action)) {
        validator.notBlank(this.email, "email", new Object[0]);
      } else if ("verify".equals(this.action)) {
        validator.notBlank(this.oneTimeCode, "oneTimeCode", new Object[0]);
      } 
      if (!validator.hasErrors())
        if ("resend".equals(this.action) || "verify".equals(this.action))
          validateCaptchaAfterAttemptingToResolveUser(this.captcha_token, null, this.codeUserId, () -> this.showCaptcha = true);  
      transferErrors(validator.done());
    } 
  }
  
  protected void buildVerificationRedirect(String paramString) {
    buildRedirectToEmailVerificationRequired(paramString);
  }
  
  @JsonIgnore
  protected PostAuthenticationStep getPostAuthenticationStep() {
    return PostAuthenticationStep.EmailVerification;
  }
  
  protected boolean isAlreadyVerified(User paramUser) {
    return paramUser.verified;
  }
  
  private String changeEmail(String paramString) {
    if (!this.codeTenant.emailConfiguration.unverified.allowEmailChangeWhenGated)
      return "input"; 
    ClientResponse<UserResponse, Errors> clientResponse = this.client.patchUser(this.codeUser.id, Map.of("user", Map.of("email", paramString)));
    if (clientResponse.wasSuccessful() || clientResponse.status == 429) {
      addGeneralInfo("[EmailVerificationEmailUpdated]", new Object[0]);
      return handleResponseAndRedirectBackHere(this.codeTenant.emailConfiguration.verificationStrategy, clientResponse.status, 
          Optional.<UserResponse>ofNullable((UserResponse)clientResponse.successResponse)
          .map(paramUserResponse -> paramUserResponse.emailVerificationId)
          .orElse(null));
    } 
    return handleErrorResponse(clientResponse);
  }
  
  private String resendIdentityVerificationAndRedirect() {
    BaseVerificationRequiredAction.IdentityResult identityResult = resendIdentityVerification(this.codeUserEmail, IdentityType.email);
    if (identityResult.response.wasSuccessful() || identityResult.response.status == 429) {
      addGeneralInfo("[EmailVerificationSent]", new Object[0]);
      return handleResponseAndRedirectBackHere(this.codeTenant.emailConfiguration.verificationStrategy, identityResult.response.status, identityResult.verificationId);
    } 
    return handleErrorResponse(identityResult.response);
  }
}
