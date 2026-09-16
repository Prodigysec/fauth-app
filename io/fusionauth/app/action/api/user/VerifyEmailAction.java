package io.fusionauth.app.action.api.user;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.user.ExternalIdentifierReaderService;
import io.fusionauth.api.service.user.UserService;
import io.fusionauth.app.action.api.BaseTenantAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.user.VerifyEmailRequest;
import io.fusionauth.domain.api.user.VerifyEmailResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.security.annotation.AuthorizeMethod;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{verificationId}", requiresAuthentication = true, scheme = {"api", "authorize-method"})
public class VerifyEmailAction extends BaseTenantAPIAction {
  @JSONRequest
  public final VerifyEmailRequest request = new VerifyEmailRequest();
  
  private final ExternalIdentifierReaderService externalIdentifierReader;
  
  private final UserService userService;
  
  public UUID applicationId;
  
  public String email;
  
  @JSONResponse
  public VerifyEmailResponse response;
  
  public boolean sendVerifyEmail = true;
  
  @Deprecated
  public String verificationId;
  
  private boolean apiKeyAuthenticated = true;
  
  private ExternalIdentifierReaderService.ValidationResult result;
  
  @Inject
  public VerifyEmailAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, ExternalIdentifierReaderService paramExternalIdentifierReaderService, UserService paramUserService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
    this.externalIdentifierReader = paramExternalIdentifierReaderService;
    this.userService = paramUserService;
  }
  
  @AuthorizeMethod
  public boolean authorize() {
    this.apiKeyAuthenticated = false;
    this.sendVerifyEmail = true;
    this.request.userId = null;
    return true;
  }
  
  public String post() {
    if (this.request.verificationId != null && this.result.id == null)
      return "missing"; 
    this.userService.completeVerify(getTenant(), this.result.application, this.result.user, this.result.id, this.request.eventInfo);
    return "success";
  }
  
  public String put() {
    if (!(getTenant()).emailConfiguration.verifyEmail)
      return this.apiKeyAuthenticated ? "disabled" : "success"; 
    UserService.VerificationId verificationId = this.userService.createNewEmailVerificationId(getTenant(), this.result.application, this.email, this.sendVerifyEmail);
    if (this.apiKeyAuthenticated) {
      if (verificationId == null)
        return "missing"; 
      this.response = new VerifyEmailResponse(verificationId.otp, verificationId.id);
      return "render";
    } 
    return "success";
  }
  
  @ValidationMethod(httpMethods = {"POST"})
  public void validatePost() {
    if (this.request.verificationId == null)
      this.request.verificationId = this.verificationId; 
    this.result = this.externalIdentifierReader.validateEmailVerification(getOptionalTenant(), this.request.oneTimeCode, this.request.userId, this.request.verificationId);
    conditionallyUpdateTenant(this.result.tenant);
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  @ValidationMethod(httpMethods = {"PUT"})
  public void validatePut() {
    this.result = this.externalIdentifierReader.validateResendEmailVerification((this.applicationId != null) ? getOptionalTenant() : getTenant(), this.applicationId, this.email);
    conditionallyUpdateTenant(this.result.tenant);
    this.frontEndSupport.transfer(this.result.errors);
  }
}
