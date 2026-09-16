package io.fusionauth.app.action.account.twoFactor;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import com.inversoft.util.StringTools;
import io.fusionauth.api.service.oauth2.OAuthService;
import io.fusionauth.api.service.security.ThreatDetectionService;
import io.fusionauth.app.action.account.BaseAccountAction;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.FrontEndThemeResolver;
import io.fusionauth.app.service.security.LoginIntentService;
import io.fusionauth.app.service.security.SSOService;
import io.fusionauth.domain.TwoFactorMethod;
import io.fusionauth.domain.api.TwoFactorUpdateRequest;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(requiresAuthentication = true, scheme = {"account-user"})
@Redirect(code = "success", uri = "/account/two-factor/?client_id=${client_id}&tenantId=${tenantId}")
public class EditAction extends BaseAccountAction {
  @FTLVariable
  public String methodId;
  
  public String twoFactorName;
  
  @Inject
  public EditAction(FrontEndSupport paramFrontEndSupport, FrontEndThemeResolver paramFrontEndThemeResolver, LoginIntentService paramLoginIntentService, OAuthService paramOAuthService, SSOService paramSSOService, ThreatDetectionService paramThreatDetectionService) {
    super(paramFrontEndSupport, paramFrontEndThemeResolver, paramLoginIntentService, paramOAuthService, paramSSOService, paramThreatDetectionService);
    this.errorMapping.put("name", "twoFactorName");
  }
  
  public String get() {
    TwoFactorMethod twoFactorMethod = (this.methodId == null) ? null : this.user.twoFactor.getMethodById(this.methodId);
    if (twoFactorMethod == null)
      return "success"; 
    this.twoFactorName = twoFactorMethod.name;
    return "input";
  }
  
  public String post() {
    if (this.methodId == null || this.user.twoFactor.getMethodById(this.methodId) == null)
      return "success"; 
    String str = StringTools.isBlank(this.twoFactorName) ? null : this.twoFactorName;
    ClientResponse<Void, Errors> clientResponse = this.client.updateTwoFactor(this.userId, new TwoFactorUpdateRequest(this.frontEndSupport
          
          .buildEventInfo(this.metaData), this.methodId, str));
    if (clientResponse.wasSuccessful())
      return "success"; 
    transferErrors((Errors)clientResponse.errorResponse);
    return "input";
  }
  
  @ValidationMethod(httpMethods = {"GET", "POST"})
  public void validate() {
    accountValidation();
  }
}
