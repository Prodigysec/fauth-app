package io.fusionauth.app.action;

import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.api.domain.IdentityExternalIdHelper;
import io.fusionauth.api.service.oauth2.OAuthService;
import io.fusionauth.api.service.security.ThreatDetectionService;
import io.fusionauth.api.service.user.ExternalIdentifierReaderService;
import io.fusionauth.app.action.oauth2.BaseOAuthCompletionAction;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.primeframework.ThemedForward;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.FrontEndThemeResolver;
import io.fusionauth.app.service.security.LoginIntentService;
import io.fusionauth.app.service.security.SSOService;
import io.fusionauth.domain.IdentityType;
import io.fusionauth.domain.User;
import io.fusionauth.domain.VerificationStrategy;
import io.fusionauth.domain.api.UserResponse;
import io.fusionauth.domain.api.identity.verify.VerifyCompleteRequest;
import io.fusionauth.domain.api.identity.verify.VerifyCompleteResponse;
import io.fusionauth.domain.api.identity.verify.VerifySendRequest;
import io.fusionauth.domain.api.identity.verify.VerifyStartRequest;
import io.fusionauth.domain.api.identity.verify.VerifyStartResponse;
import io.fusionauth.domain.api.user.VerifyRegistrationRequest;
import io.fusionauth.domain.api.user.VerifyRegistrationResponse;
import io.fusionauth.domain.oauth2.UserState;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.primeframework.mvc.parameter.annotation.PreRenderMethod;

public abstract class BaseVerificationRequiredAction extends BaseOAuthCompletionAction {
  protected final ExternalIdentifierReaderService externalIdentifierReader;
  
  public String action;
  
  @FTLVariable
  public boolean collectVerificationCode;
  
  public String oneTimeCode;
  
  @FTLVariable
  public String verificationId;
  
  protected BaseVerificationRequiredAction(FrontEndSupport paramFrontEndSupport, FrontEndThemeResolver paramFrontEndThemeResolver, LoginIntentService paramLoginIntentService, OAuthService paramOAuthService, SSOService paramSSOService, ThreatDetectionService paramThreatDetectionService, ExternalIdentifierReaderService paramExternalIdentifierReaderService) {
    super(paramFrontEndSupport, paramFrontEndThemeResolver, paramLoginIntentService, paramOAuthService, paramSSOService, paramThreatDetectionService);
    this.externalIdentifierReader = paramExternalIdentifierReaderService;
  }
  
  @PreRenderMethod({ThemedForward.class})
  public void prePageRenderSetup() {
    this.showCaptcha = (this.showCaptcha || showCaptchaOnInitialPageRender(this.codeUserId));
  }
  
  protected String baseValidate() {
    this.action = (this.action != null) ? this.action : "";
    if (this.codeUser == null) {
      deleteLoginIntent();
      buildRedirectToAuthorizeURI();
      return "redirect-to-authorize";
    } 
    if (getPostAuthenticationStep() != this.codeLoginIntent.step)
      return handleRedirectToExpectedStep(this.codeLoginIntent); 
    if (isAlreadyVerified(this.codeUser)) {
      LoginIntentService.LoginIntent loginIntent = getNextIntent(this.codeUser, this.codeUser
          
          .getRegistrationForApplication(this.codeApplication.id), 
          getPostAuthenticationStep());
      return loginIntent.step.getResultCode();
    } 
    return null;
  }
  
  protected String handleErrorResponse(ClientResponse<?, Errors> paramClientResponse) {
    if (paramClientResponse.status == 400) {
      transferErrors((Errors)paramClientResponse.errorResponse);
    } else {
      if (((Errors)paramClientResponse.errorResponse).containsError("[MessengerError]")) {
        addGeneralError("[MessengerError]", new Object[0]);
        return "input";
      } 
      addGeneralError("[APIError]", new Object[0]);
      return "error";
    } 
    return "input";
  }
  
  protected String handleResponseAndRedirectBackHere(VerificationStrategy paramVerificationStrategy, int paramInt, String paramString) {
    this.userState = UserState.AuthenticatedNotVerified;
    if (paramInt != 429)
      this.verificationId = paramString; 
    String str = (paramVerificationStrategy == VerificationStrategy.FormField) ? this.verificationId : null;
    buildVerificationRedirect(str);
    return getPostAuthenticationStep().getResultCode();
  }
  
  protected <T> String handleUserRegistrationResponseAndRedirectBackHere(ClientResponse<T, Errors> paramClientResponse, Function<T, String> paramFunction, String paramString) {
    if (paramClientResponse.wasSuccessful() || paramClientResponse.status == 429) {
      this.userState = UserState.AuthenticatedNotVerified;
      if (paramClientResponse.status != 429)
        this.verificationId = paramFunction.apply((T)paramClientResponse.successResponse); 
      String str = (this.codeApplication.verificationStrategy == VerificationStrategy.FormField) ? this.verificationId : null;
      buildRedirectToRegistrationVerificationRequired(str);
      addGeneralInfo(paramString, new Object[0]);
      return "redirect-to-registration-verification-required";
    } 
    return handleErrorResponse(paramClientResponse);
  }
  
  protected IdentityResult resendIdentityVerification(String paramString, IdentityType paramIdentityType) {
    ClientResponse<VerifyStartResponse, Errors> clientResponse = this.client.startVerifyIdentity((new VerifyStartRequest())
        .with(paramVerifyStartRequest -> paramVerifyStartRequest.applicationId = this.codeApplication.id)
        .with(paramVerifyStartRequest -> paramVerifyStartRequest.loginId = paramString)
        .with(paramVerifyStartRequest -> paramVerifyStartRequest.loginIdType = paramIdentityType.name)
        .with(paramVerifyStartRequest -> paramVerifyStartRequest.state = captureState()));
    if (!clientResponse.wasSuccessful())
      return new IdentityResult(null, clientResponse); 
    String str = ((VerifyStartResponse)clientResponse.successResponse).verificationId;
    ClientResponse<Void, Errors> clientResponse1 = this.client.sendVerifyIdentity(new VerifySendRequest(str));
    return new IdentityResult(str, clientResponse1);
  }
  
  protected boolean resendIdentityVerificationRequired(String paramString, IdentityType paramIdentityType) {
    List<ExternalIdentifier> list = this.externalIdentifierReader.retrieveAllByUserId(this.codeUser.id, IdentityExternalIdHelper.identityExternalIdTypes);
    return list.stream()
      
      .noneMatch(paramExternalIdentifier -> IdentityExternalIdHelper.matchesIdentity(paramExternalIdentifier, paramString, paramIdentityType));
  }
  
  protected String resendUserRegistrationVerification() {
    ClientResponse<VerifyRegistrationResponse, Errors> clientResponse = this.client.resendRegistrationVerification(this.codeUserEmail, this.codeApplication.id);
    return handleUserRegistrationResponseAndRedirectBackHere(clientResponse, paramVerifyRegistrationResponse -> paramVerifyRegistrationResponse.verificationId, "[RegistrationVerificationSent]");
  }
  
  protected String verifyIdentity(IdentityType paramIdentityType) {
    if (paramIdentityType.is(IdentityType.email)) {
      if (this.codeTenant.emailConfiguration.verificationStrategy != VerificationStrategy.FormField)
        return "input"; 
    } else if (paramIdentityType.is(IdentityType.phoneNumber)) {
      if (this.codeTenant.phoneConfiguration.verificationStrategy != VerificationStrategy.FormField)
        return "input"; 
    } else {
      throw new IllegalArgumentException("Unsupported identity type: " + String.valueOf(paramIdentityType));
    } 
    ClientResponse<VerifyCompleteResponse, Errors> clientResponse = this.client.completeVerifyIdentity((new VerifyCompleteRequest())
        .with(paramVerifyCompleteRequest -> paramVerifyCompleteRequest.eventInfo = this.frontEndSupport.buildEventInfo(this.metaData))
        .with(paramVerifyCompleteRequest -> paramVerifyCompleteRequest.verificationId = this.verificationId)
        .with(paramVerifyCompleteRequest -> paramVerifyCompleteRequest.oneTimeCode = this.oneTimeCode));
    return handleVerificationResponse(clientResponse);
  }
  
  protected String verifyUserRegistration() {
    if (this.codeApplication.verificationStrategy != VerificationStrategy.FormField)
      return "input"; 
    ClientResponse<Void, Errors> clientResponse = this.client.verifyUserRegistration(new VerifyRegistrationRequest(this.frontEndSupport.buildEventInfo(this.metaData), this.oneTimeCode, this.verificationId));
    return handleVerificationResponse(clientResponse);
  }
  
  private String handleVerificationResponse(ClientResponse<?, Errors> paramClientResponse) {
    if (paramClientResponse.wasSuccessful()) {
      ClientResponse<UserResponse, Errors> clientResponse = this.client.setTenantId(this.codeTenant.id).retrieveUser(this.codeUser.id);
      if (clientResponse.wasSuccessful()) {
        setUserVariables(((UserResponse)clientResponse.getSuccessResponse()).user);
      } else {
        buildRedirectToAuthorizeURI();
        return "redirect-to-authorize";
      } 
      return (getNextIntent(this.codeUser, this.codeUser
          
          .getRegistrationForApplication(this.codeApplication.id), 
          getPostAuthenticationStep())).step
        .getResultCode();
    } 
    if (paramClientResponse.status == 400) {
      Map map = ((Errors)paramClientResponse.errorResponse).fieldErrors;
      if (map.containsKey("verificationId")) {
        addFieldError("oneTimeCode", "[invalid]code", new Object[0]);
      } else if (map.containsKey("oneTimeCode")) {
        addFieldError("oneTimeCode", "[invalid]code", new Object[0]);
      } else {
        transferErrors((Errors)paramClientResponse.errorResponse);
      } 
    } else if (paramClientResponse.status == 404) {
      addFieldError("oneTimeCode", "[invalid]code", new Object[0]);
    } else {
      addGeneralError("[APIError]", new Object[0]);
      return "error";
    } 
    return "input";
  }
  
  protected abstract void buildVerificationRedirect(String paramString);
  
  protected abstract boolean isAlreadyVerified(User paramUser);
  
  protected static class IdentityResult {
    public final ClientResponse<?, Errors> response;
    
    public final String verificationId;
    
    protected IdentityResult(String param1String, ClientResponse<?, Errors> param1ClientResponse) {
      this.verificationId = param1String;
      this.response = param1ClientResponse;
    }
  }
}
