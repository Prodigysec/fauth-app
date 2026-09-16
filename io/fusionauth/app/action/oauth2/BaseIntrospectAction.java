package io.fusionauth.app.action.oauth2;

import io.fusionauth.api.service.oauth2.OAuthService;
import io.fusionauth.api.util.ActionTools;
import io.fusionauth.app.action.NoStoreJSONBaseAction;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.Entity;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.jwt.RefreshToken;
import io.fusionauth.domain.oauth2.GrantType;
import io.fusionauth.domain.oauth2.OAuthError;
import io.fusionauth.http.server.HTTPRequest;
import io.fusionauth.http.server.HTTPResponse;
import java.util.UUID;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;

public abstract class BaseIntrospectAction extends NoStoreJSONBaseAction {
  protected final HTTPRequest httpRequest;
  
  protected final HTTPResponse httpResponse;
  
  protected final OAuthService oauthService;
  
  public String client_id;
  
  public String client_secret;
  
  @JSONResponse
  public Object response;
  
  public UUID tenantId;
  
  protected Application codeApplication;
  
  protected Entity codeEntity;
  
  protected Tenant codeTenant;
  
  protected GrantType grantType;
  
  protected RefreshToken refreshToken;
  
  protected BaseIntrospectAction(HTTPRequest paramHTTPRequest, HTTPResponse paramHTTPResponse, OAuthService paramOAuthService) {
    this.httpRequest = paramHTTPRequest;
    this.httpResponse = paramHTTPResponse;
    this.oauthService = paramOAuthService;
  }
  
  @PostParameterMethod
  public void postParameter() {
    ActionTools.resolveTenantIdFromHeader(this.httpRequest).ifPresent(paramUUID -> this.tenantId = paramUUID);
  }
  
  protected void handleValidationResult(OAuthService.OAuthValidationResult paramOAuthValidationResult) {
    this.client_id = paramOAuthValidationResult.client_id;
    this.client_secret = paramOAuthValidationResult.client_secret;
    if (paramOAuthValidationResult.error != null) {
      this.response = paramOAuthValidationResult.error;
      if (paramOAuthValidationResult.error.error == OAuthError.OAuthErrorType.invalid_client) {
        if (paramOAuthValidationResult.authHeaderUsed)
          this.httpResponse.setHeader("WWW-Authenticate", "Basic"); 
        throw new ErrorException("invalid-client");
      } 
      throw new ErrorException("input", false);
    } 
    this.codeApplication = paramOAuthValidationResult.application;
    this.codeTenant = paramOAuthValidationResult.tenant;
    this.codeEntity = paramOAuthValidationResult.recipientEntity;
    this.grantType = paramOAuthValidationResult.grantType;
    this.refreshToken = paramOAuthValidationResult.refreshToken;
  }
}
