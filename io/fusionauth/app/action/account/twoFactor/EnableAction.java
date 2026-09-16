package io.fusionauth.app.action.account.twoFactor;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.service.oauth2.OAuthService;
import io.fusionauth.api.service.security.ThreatDetectionService;
import io.fusionauth.api.util.TwoFactorTools;
import io.fusionauth.app.action.account.BaseAccountAction;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.FrontEndThemeResolver;
import io.fusionauth.app.service.security.LoginIntentService;
import io.fusionauth.app.service.security.SSOService;
import io.fusionauth.app.service.security.TwoFactorFrontendService;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.TwoFactorMethod;
import io.fusionauth.domain.api.TwoFactorRequest;
import io.fusionauth.domain.api.TwoFactorResponse;
import io.fusionauth.domain.api.twoFactor.SecretResponse;
import io.fusionauth.domain.message.MessageType;
import java.util.ArrayList;
import java.util.List;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.validation.ValidationMethod;
import org.primeframework.mvc.validation.annotation.PostValidationMethod;

@Action(requiresAuthentication = true, scheme = {"account-user"})
@Redirect(code = "success", uri = "/account/two-factor/?client_id=${client_id}&tenantId=${tenantId}")
public class EnableAction extends BaseAccountAction {
  private final TwoFactorFrontendService twoFactorFrontendService;
  
  public String action;
  
  public List<String> availableMethods;
  
  @FTLVariable
  public String code;
  
  public String email;
  
  public MessageType messageType;
  
  public String method;
  
  public String mobilePhone;
  
  public List<String> phoneMessageTypes;
  
  public List<String> recoveryCodes = new ArrayList<>();
  
  @FTLVariable
  public String secret;
  
  @FTLVariable
  public String secretBase32Encoded;
  
  public String twoFactorName;
  
  @Inject
  public EnableAction(FrontEndSupport paramFrontEndSupport, FrontEndThemeResolver paramFrontEndThemeResolver, LoginIntentService paramLoginIntentService, OAuthService paramOAuthService, TwoFactorFrontendService paramTwoFactorFrontendService, SSOService paramSSOService, ThreatDetectionService paramThreatDetectionService) {
    super(paramFrontEndSupport, paramFrontEndThemeResolver, paramLoginIntentService, paramOAuthService, paramSSOService, paramThreatDetectionService);
    this.twoFactorFrontendService = paramTwoFactorFrontendService;
    this.errorMapping.put("name", "twoFactorName");
  }
  
  public String get() {
    SecretResponse secretResponse = this.delegate.execute(FusionAuthClient::generateTwoFactorSecret);
    this.secret = secretResponse.secret;
    this.secretBase32Encoded = secretResponse.secretBase32Encoded;
    if (this.email == null && this.user.email != null && this.user.twoFactor.methods.stream().noneMatch(paramTwoFactorMethod -> this.user.email.equals(paramTwoFactorMethod.email)))
      this.email = this.user.email; 
    if (this.mobilePhone == null && this.user.mobilePhone != null && this.user.twoFactor.methods.stream().noneMatch(paramTwoFactorMethod -> this.user.mobilePhone.equals(paramTwoFactorMethod.mobilePhone)))
      this.mobilePhone = this.user.mobilePhone; 
    return "input";
  }
  
  public String post() {
    if (this.action != null && this.action.equals("send")) {
      handleEnableSend(this.email, this.method, this.mobilePhone, this.messageType, this.userId);
      return "input";
    } 
    ClientResponse<TwoFactorResponse, Errors> clientResponse = this.client.enableTwoFactor(this.userId, (new TwoFactorRequest(this.frontEndSupport
          
          .buildEventInfo(this.metaData), (this.codeApplication != null) ? this.codeApplication.id : null, this.code, this.method, this.secret))
        .with(paramTwoFactorRequest -> paramTwoFactorRequest.email = this.email)
        .with(paramTwoFactorRequest -> paramTwoFactorRequest.mobilePhone = this.mobilePhone)
        .with(paramTwoFactorRequest -> paramTwoFactorRequest.name = this.twoFactorName));
    if (clientResponse.wasSuccessful()) {
      addGeneralInfo("[TwoFactorAuthenticationMethodEnabled]", new Object[0]);
      this.recoveryCodes = ((TwoFactorResponse)clientResponse.successResponse).recoveryCodes;
      if (this.recoveryCodes != null && !this.recoveryCodes.isEmpty())
        return "input"; 
      return "success";
    } 
    if (clientResponse.status == 421) {
      addFieldError("code", "[invalid]code", new Object[0]);
    } else {
      transferErrors((Errors)clientResponse.errorResponse);
    } 
    return "input";
  }
  
  @PostValidationMethod
  public void setupAvailableMethods() {
    this.availableMethods = this.twoFactorFrontendService.availableMethodsToConfigureForUser(this.codeTenant, this.codeUser);
    this.phoneMessageTypes = TwoFactorTools.getPhoneMessageTypesForTenant(this.codeTenant);
  }
  
  @ValidationMethod(httpMethods = {"GET", "POST"})
  public void validate() {
    accountValidation();
  }
}
