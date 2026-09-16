package io.fusionauth.app.action.oauth2;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.service.oauth2.OAuthService;
import io.fusionauth.api.service.security.ThreatDetectionService;
import io.fusionauth.api.service.user.ExternalIdentifierReaderService;
import io.fusionauth.api.util.TwoFactorTools;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.FrontEndThemeResolver;
import io.fusionauth.app.service.security.LoginIntentService;
import io.fusionauth.app.service.security.SSOService;
import io.fusionauth.app.service.security.TwoFactorFrontendService;
import io.fusionauth.domain.TwoFactorMethod;
import io.fusionauth.domain.User;
import io.fusionauth.domain.api.twoFactor.TwoFactorSendRequest;
import io.fusionauth.domain.message.MessageType;
import io.fusionauth.domain.oauth2.OAuthError;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.validation.ValidationMethod;
import org.primeframework.mvc.validation.annotation.PostValidationMethod;

@Action
public class TwoFactorMethodsAction extends BaseOAuthAuthenticationAction {
  private final TwoFactorFrontendService twoFactorFrontendService;
  
  @FTLVariable
  public Map<String, TwoFactorMethod> availableMethodsMap = new LinkedHashMap<>();
  
  public String methodId;
  
  public List<String> phoneMessageTypes;
  
  public int recoverCodesAvailable;
  
  @FTLVariable
  public boolean selectMethod;
  
  private String messageType;
  
  private User user;
  
  @Inject
  public TwoFactorMethodsAction(FrontEndSupport paramFrontEndSupport, FrontEndThemeResolver paramFrontEndThemeResolver, LoginIntentService paramLoginIntentService, OAuthService paramOAuthService, SSOService paramSSOService, TwoFactorFrontendService paramTwoFactorFrontendService, ThreatDetectionService paramThreatDetectionService) {
    super(paramFrontEndSupport, paramFrontEndThemeResolver, paramLoginIntentService, paramOAuthService, paramSSOService, paramThreatDetectionService);
    this.twoFactorFrontendService = paramTwoFactorFrontendService;
  }
  
  public String get() {
    if (this.selectMethod)
      return "input"; 
    TwoFactorMethod twoFactorMethod = this.user.twoFactor.getLastUsedMethod();
    this.methodId = (twoFactorMethod != null) ? twoFactorMethod.id : null;
    if (this.methodId == null && this.user.twoFactor.methods.stream().allMatch(paramTwoFactorMethod -> paramTwoFactorMethod.method.equals("authenticator")))
      this.methodId = ((TwoFactorMethod)this.user.twoFactor.methods.get(0)).id; 
    if (this.methodId != null && !messageTypeSelectionRequired(this.methodId))
      return buildRedirectToNextStep(true, this.methodId); 
    return "input";
  }
  
  public String post() {
    return buildRedirectToNextStep(false, this.methodId);
  }
  
  @PostValidationMethod
  public void setup() {
    List<String> list = this.twoFactorFrontendService.availableMethodsToConfigureForUser(this.codeTenant, this.user);
    for (TwoFactorMethod twoFactorMethod : this.user.twoFactor.methods) {
      if (list.contains(twoFactorMethod.method))
        this.availableMethodsMap.put(twoFactorMethod.id, twoFactorMethod); 
    } 
    this.phoneMessageTypes = TwoFactorTools.getPhoneMessageTypesForTenant(this.codeTenant);
    if (this.methodId != null && this.methodId.contains("-")) {
      this.messageType = this.methodId.substring(this.methodId.indexOf("-") + 1);
      this.methodId = this.methodId.substring(0, this.methodId.indexOf("-"));
    } 
  }
  
  @ValidationMethod(httpMethods = {"GET", "POST"})
  public void validate() {
    if (this.client_id != null) {
      String str = validateAndHandleErrors(false, paramOAuthError -> !paramOAuthError.reason.name().startsWith("missing_"));
      if (str != null)
        throw new ErrorException(str, false); 
    } 
    if (this.twoFactorId == null) {
      buildRedirectToAuthorizeURI();
      throw new ErrorException("redirect-to-authorize");
    } 
    ExternalIdentifierReaderService.ValidationResult validationResult = this.twoFactorFrontendService.verifyTwoFactorId(this.codeTenant, this.twoFactorId);
    if (validationResult.id == null) {
      addGeneralInfo("[TwoFactorTimeout]", new Object[0]);
      buildRedirectToAuthorizeURI();
      throw new ErrorException("redirect-to-authorize");
    } 
    this.recoverCodesAvailable = validationResult.user.twoFactor.recoveryCodes.size();
    this.user = (new User(validationResult.user)).secure();
    if (this.frontEndSupport.isPOST() && 
      this.methodId == null)
      addFieldError("methodId", "[blank]methodId", new Object[0]); 
  }
  
  private String buildRedirectToNextStep(boolean paramBoolean, String paramString) {
    if (paramString.equals("recoveryCode") || "authenticator".equals((this.user.twoFactor.getMethodById(paramString)).method)) {
      buildRedirectToTwoFactorURI(paramString);
      return "redirect-to-two-factor";
    } 
    TwoFactorSendRequest twoFactorSendRequest = new TwoFactorSendRequest(paramString);
    twoFactorSendRequest.messageType = MessageType.safeValueOf(this.messageType);
    TwoFactorMethod twoFactorMethod = this.user.twoFactor.getMethodById(paramString);
    if (!paramBoolean && twoFactorMethod != null && "sms".equals(twoFactorMethod.method) && twoFactorSendRequest.messageType == null)
      twoFactorSendRequest.messageType = MessageType.SMS; 
    ClientResponse<Void, Errors> clientResponse = this.client.sendTwoFactorCodeForLoginUsingMethod(this.twoFactorId, twoFactorSendRequest);
    if (clientResponse.wasSuccessful()) {
      addGeneralInfo("sent-code", new Object[0]);
      buildRedirectToTwoFactorURI(paramString);
      return "redirect-to-two-factor";
    } 
    if (clientResponse.status == 404) {
      addGeneralInfo("[TwoFactorTimeout]", new Object[0]);
      buildRedirectToAuthorizeURI();
      return "redirect-to-authorize";
    } 
    if (clientResponse.status == 429) {
      transferErrors((Errors)clientResponse.errorResponse);
      return "input";
    } 
    addGeneralError("[PushTwoFactorFailed]", new Object[0]);
    return "input";
  }
  
  private boolean messageTypeSelectionRequired(String paramString) {
    TwoFactorMethod twoFactorMethod = this.user.twoFactor.getMethodById(paramString);
    return (twoFactorMethod != null && "sms"
      .equals(twoFactorMethod.method) && this.codeTenant.multiFactorConfiguration.sms.enabled && this.codeTenant.multiFactorConfiguration.voice.enabled);
  }
}
