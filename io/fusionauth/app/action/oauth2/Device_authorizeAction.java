package io.fusionauth.app.action.oauth2;

import com.google.inject.Inject;
import io.fusionauth.api.service.oauth2.OAuthService;
import io.fusionauth.app.action.NoStoreJSONBaseAction;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.jwt.RefreshToken;
import io.fusionauth.domain.oauth2.OAuthError;
import io.fusionauth.domain.oauth2.OAuthResponse;
import io.fusionauth.http.server.HTTPRequest;
import java.util.UUID;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Status;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.validation.ValidationMethod;

@Status(code = "service-unavailable", status = 503, cacheControl = "no-store")
@Action
public class Device_authorizeAction extends NoStoreJSONBaseAction {
  private final HTTPRequest httpRequest;
  
  private final OAuthService oauthService;
  
  public String client_id;
  
  public RefreshToken.MetaData metaData = new RefreshToken.MetaData();
  
  @JSONResponse
  public OAuthResponse response;
  
  public String scope;
  
  public UUID tenantId;
  
  private Application codeApplication;
  
  private Tenant codeTenant;
  
  private OAuthService.OAuthValidationResult result;
  
  @Inject
  public Device_authorizeAction(OAuthService paramOAuthService, HTTPRequest paramHTTPRequest) {
    this.oauthService = paramOAuthService;
    this.httpRequest = paramHTTPRequest;
  }
  
  public String post() {
    this.response = this.oauthService.generateDeviceResponse(this.codeTenant, this.codeApplication, this.result.scopes, this.metaData, this.result.dPoPThumbprint);
    return "render";
  }
  
  @ValidationMethod(httpMethods = {"POST"})
  public void validate() {
    this.result = this.oauthService.validateDeviceRequest(this.tenantId, this.client_id, this.scope, this.httpRequest);
    if (this.result.error != null) {
      this.response = this.result.error;
      String str = (this.result.error.error == OAuthError.OAuthErrorType.invalid_client) ? "invalid-client" : "input";
      throw new ErrorException(str, false);
    } 
    this.codeApplication = this.result.application;
    this.codeTenant = this.result.tenant;
  }
}
