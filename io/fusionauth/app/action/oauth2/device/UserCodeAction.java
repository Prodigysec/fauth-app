package io.fusionauth.app.action.oauth2.device;

import com.google.inject.Inject;
import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.api.domain.TenantRequest;
import io.fusionauth.api.service.oauth2.OAuthService;
import io.fusionauth.app.action.oauth2.BaseIntrospectAction;
import io.fusionauth.domain.oauth2.DeviceUserCodeResponse;
import io.fusionauth.http.server.HTTPRequest;
import io.fusionauth.http.server.HTTPResponse;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.security.annotation.AuthorizeMethod;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(requiresAuthentication = true, scheme = {"api", "authorize-method"})
public class UserCodeAction extends BaseIntrospectAction {
  public String user_code;
  
  private boolean apiKeyAuthenticated = true;
  
  private OAuthService.OAuthValidationResult result;
  
  @Inject
  public UserCodeAction(HTTPRequest paramHTTPRequest, HTTPResponse paramHTTPResponse, OAuthService paramOAuthService) {
    super(paramHTTPRequest, paramHTTPResponse, paramOAuthService);
  }
  
  @AuthorizeMethod
  public boolean authorize() {
    this.apiKeyAuthenticated = false;
    return true;
  }
  
  public String post() {
    ExternalIdentifier externalIdentifier = this.result.externalIdentifier;
    this




      
      .response = (new DeviceUserCodeResponse()).with(paramDeviceUserCodeResponse -> paramDeviceUserCodeResponse.client_id = this.result.client_id).with(paramDeviceUserCodeResponse -> paramDeviceUserCodeResponse.expiresIn = Integer.valueOf((int)Duration.between(ZonedDateTime.now(ZoneOffset.UTC), paramExternalIdentifier.getExpiration(this.codeTenant)).getSeconds())).with(paramDeviceUserCodeResponse -> paramDeviceUserCodeResponse.deviceInfo = paramExternalIdentifier.data.device).with(paramDeviceUserCodeResponse -> paramDeviceUserCodeResponse.pendingIdPLink = paramExternalIdentifier.buildPendingIdpLink()).with(paramDeviceUserCodeResponse -> paramDeviceUserCodeResponse.scope = String.join(" ", (Iterable)this.result.scopes)).with(paramDeviceUserCodeResponse -> paramDeviceUserCodeResponse.tenantId = this.codeTenant.id).with(paramDeviceUserCodeResponse -> paramDeviceUserCodeResponse.user_code = paramExternalIdentifier.id);
    return "render";
  }
  
  @ValidationMethod
  public void validate() {
    this
      
      .result = this.apiKeyAuthenticated ? this.oauthService.validateUserCodeInfoRequestUsingAPIKey((UUID)this.httpRequest.getAttribute(TenantRequest.KEY), this.user_code) : this.oauthService.validateUserCodeInfoRequest(this.httpRequest.getHeader("Authorization"), this.client_id, this.client_secret, this.user_code, this.tenantId);
    handleValidationResult(this.result);
  }
}
