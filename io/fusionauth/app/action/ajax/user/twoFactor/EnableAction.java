package io.fusionauth.app.action.ajax.user.twoFactor;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.security.TwoFactorFrontendService;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.api.TwoFactorRequest;
import io.fusionauth.domain.api.TwoFactorResponse;
import io.fusionauth.domain.api.twoFactor.SecretResponse;
import io.fusionauth.domain.message.MessageType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Forward;
import org.primeframework.mvc.control.form.annotation.FormPrepareMethod;

@Action(requiresAuthentication = true, constraints = {"admin", "user_manager", "user_support_manager", "user_support_viewer"})
@Forward(code = "show-recovery-codes", page = "recovery-codes.ftl", status = 400, cacheControl = "no-store")
public class EnableAction extends BaseAJAXAction {
  private final TwoFactorFrontendService twoFactorFrontendService;
  
  public String action;
  
  public List<String> availableMethods;
  
  public String code;
  
  public String email;
  
  public MessageType messageType;
  
  public String method;
  
  public String mobilePhone;
  
  public List<String> recoveryCodes;
  
  public String secret;
  
  public String secretBase32Encoded;
  
  public String twoFactorName;
  
  private Map<String, String> errorMapping = new HashMap<>();
  
  @Inject
  public EnableAction(FrontEndSupport paramFrontEndSupport, TwoFactorFrontendService paramTwoFactorFrontendService) {
    super(paramFrontEndSupport);
    this.twoFactorFrontendService = paramTwoFactorFrontendService;
    this.errorMapping.put("name", "twoFactorName");
  }
  
  @FormPrepareMethod
  public void formPrepare() {
    this.availableMethods = this.twoFactorFrontendService.availableMethodsToConfigureForUser(this.tenants.get(this.codeCurrentUser.tenantId), this.codeCurrentUser);
  }
  
  public String get() {
    if (this.twoFactorFrontendService.availableMethodsToConfigureForUser(this.tenants.get(this.codeCurrentUser.tenantId), this.codeCurrentUser).isEmpty())
      return "success"; 
    SecretResponse secretResponse = this.delegate.execute(FusionAuthClient::generateTwoFactorSecret);
    this.secret = secretResponse.secret;
    this.secretBase32Encoded = secretResponse.secretBase32Encoded;
    return "render";
  }
  
  public String post() {
    if ("complete".equals(this.action))
      return "success"; 
    ClientResponse<TwoFactorResponse, Errors> clientResponse = this.client.enableTwoFactor(this.codeCurrentUser.id, (new TwoFactorRequest()).with(paramTwoFactorRequest -> paramTwoFactorRequest.code = this.code)
        .with(paramTwoFactorRequest -> paramTwoFactorRequest.email = this.email)
        .with(paramTwoFactorRequest -> paramTwoFactorRequest.eventInfo = this.frontEndSupport.buildEventInfo(null))
        .with(paramTwoFactorRequest -> paramTwoFactorRequest.method = this.method)
        .with(paramTwoFactorRequest -> paramTwoFactorRequest.mobilePhone = this.mobilePhone)
        .with(paramTwoFactorRequest -> paramTwoFactorRequest.name = this.twoFactorName)
        .with(paramTwoFactorRequest -> paramTwoFactorRequest.secret = this.secret));
    if (clientResponse.wasSuccessful()) {
      writeAuditLog("A user with Id [" + String.valueOf(this.codeCurrentUser.id) + "] enabled a two-factor method from themselves. Method [" + this.method + "]" + (
          this.method.equals("authenticator") ? "" : (", value [" + (this.method.equals("email") ? this.email : this.mobilePhone) + "]")));
      this.recoveryCodes = ((TwoFactorResponse)clientResponse.successResponse).recoveryCodes;
      if (this.recoveryCodes != null && !this.recoveryCodes.isEmpty())
        return "show-recovery-codes"; 
      return "success";
    } 
    if (clientResponse.status == 421) {
      this.frontEndSupport.addFieldError("code", "[invalid]code", new Object[0]);
    } else {
      this.frontEndSupport.transfer((Errors)clientResponse.errorResponse, this.errorMapping);
    } 
    return "input";
  }
}
