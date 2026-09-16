package io.fusionauth.app.action.api.user;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.user.ExternalIdentifierReaderService;
import io.fusionauth.api.service.user.UserService;
import io.fusionauth.app.action.api.BaseTenantAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.user.VerifyRegistrationRequest;
import io.fusionauth.domain.api.user.VerifyRegistrationResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.security.annotation.AnonymousAccess;
import org.primeframework.mvc.security.annotation.AuthorizeMethod;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{verificationId}", requiresAuthentication = true, scheme = {"api", "authorize-method"})
public class VerifyRegistrationAction extends BaseTenantAPIAction {
  @JSONRequest
  public final VerifyRegistrationRequest request = new VerifyRegistrationRequest();
  
  private final ExternalIdentifierReaderService externalIdentifierReader;
  
  private final UserService userService;
  
  public UUID applicationId;
  
  public String email;
  
  @JSONResponse
  public VerifyRegistrationResponse response;
  
  public boolean sendVerifyRegistrationEmail = true;
  
  @Deprecated
  public String verificationId;
  
  private boolean apiKeyAuthenticated = true;
  
  private ExternalIdentifierReaderService.ValidationResult result;
  
  @Inject
  public VerifyRegistrationAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, ExternalIdentifierReaderService paramExternalIdentifierReaderService, UserService paramUserService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
    this.externalIdentifierReader = paramExternalIdentifierReaderService;
    this.userService = paramUserService;
  }
  
  @AuthorizeMethod
  public boolean authorize() {
    this.apiKeyAuthenticated = false;
    this.sendVerifyRegistrationEmail = true;
    return true;
  }
  
  @AnonymousAccess
  public String post() {
    if (this.result.id == null)
      return "missing"; 
    this.userService.verifyRegistration(getTenant(), this.result.application, this.result.user, this.request.eventInfo, this.result.id);
    return "success";
  }
  
  public String put() {
    if (!this.result.application.verifyRegistration)
      return "disabled"; 
    UserService.VerificationId verificationId = this.userService.createNewRegistrationVerificationId(getTenant(), this.result.application, this.email, this.sendVerifyRegistrationEmail);
    if (this.apiKeyAuthenticated) {
      this
        
        .response = (verificationId != null) ? new VerifyRegistrationResponse(verificationId.otp, verificationId.id) : new VerifyRegistrationResponse();
      return "render";
    } 
    return "success";
  }
  
  @ValidationMethod(httpMethods = {"POST"})
  public void validatePost() {
    if (this.request.verificationId == null)
      this.request.verificationId = this.verificationId; 
    this.result = this.externalIdentifierReader.validateRegistrationVerification(getOptionalTenant(), this.request.oneTimeCode, this.request.verificationId);
    conditionallyUpdateTenant(this.result.tenant);
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  @ValidationMethod(httpMethods = {"PUT"})
  public void validatePut() {
    this.result = this.externalIdentifierReader.validateResendRegistrationVerification(getOptionalTenant(), this.applicationId, this.email);
    conditionallyUpdateTenant(this.result.tenant);
    this.frontEndSupport.transfer(this.result.errors);
  }
}
