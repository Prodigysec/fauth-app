package io.fusionauth.app.action.oauth2;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.service.authentication.AuthenticationType;
import io.fusionauth.api.service.oauth2.OAuthService;
import io.fusionauth.api.service.security.ThreatDetectionService;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.FrontEndThemeResolver;
import io.fusionauth.app.service.security.LoginIntentService;
import io.fusionauth.app.service.security.SSOService;
import io.fusionauth.domain.api.LoginResponse;
import io.fusionauth.domain.api.identityProvider.IdentityProviderLoginRequest;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.validation.ValidationMethod;

@Action
public class WaitAction extends BaseOAuthAuthenticationAction {
  public boolean checkStatus;
  
  public String code;
  
  @FTLVariable
  public String waitURL;
  
  @Inject
  public WaitAction(FrontEndSupport paramFrontEndSupport, FrontEndThemeResolver paramFrontEndThemeResolver, LoginIntentService paramLoginIntentService, OAuthService paramOAuthService, SSOService paramSSOService, ThreatDetectionService paramThreatDetectionService) {
    super(paramFrontEndSupport, paramFrontEndThemeResolver, paramLoginIntentService, paramOAuthService, paramSSOService, paramThreatDetectionService);
  }
  
  public String get() {
    if (this.checkStatus) {
      this.checkStatus = false;
      ClientResponse<LoginResponse, Errors> clientResponse = this.client.identityProviderLogin((new IdentityProviderLoginRequest())
          .with(paramIdentityProviderLoginRequest -> paramIdentityProviderLoginRequest.eventInfo = this.frontEndSupport.buildEventInfo(this.metaData))
          .with(paramIdentityProviderLoginRequest -> paramIdentityProviderLoginRequest.identityProviderId = this.identityProviderId)
          .with(paramIdentityProviderLoginRequest -> paramIdentityProviderLoginRequest.addData("code", this.code)));
      if (clientResponse.wasSuccessful()) {
        if (clientResponse.status == 204) {
          this.checkStatus = true;
          buildWaitURL();
          return "input";
        } 
        WaitAction waitAction = this;
        return handleInteractiveLoginResponse(clientResponse, (SSOService.NewDeviceResult)null, paramErrors -> paramWaitAction.transferErrors(paramErrors), SSOService.RememberDeviceState.NoChange, AuthenticationType.HYPR, false);
      } 
      addGeneralError("[ExternalAuthenticationExpired]", new Object[0]);
      buildRedirectToAuthorizeURI();
      return "redirect-to-authorize";
    } 
    buildWaitURL();
    return "input";
  }
  
  @ValidationMethod(httpMethods = {"GET"})
  public void validateGet() {
    String str = validateAndHandleErrors(false);
    if (str != null)
      throw new ErrorException(str, false); 
  }
  
  private void buildWaitURL() {
    this


      
      .waitURL = baseQueryBuilder("wait").with("code", this.code).with("identityProviderId", this.identityProviderId).with("checkStatus", Boolean.valueOf(true)).build();
  }
}
