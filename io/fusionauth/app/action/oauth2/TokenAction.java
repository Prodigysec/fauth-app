package io.fusionauth.app.action.oauth2;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.configuration.FusionAuthConfiguration;
import io.fusionauth.api.domain.api.RequestContext;
import io.fusionauth.api.domain.guice.FusionAuthClientProvider;
import io.fusionauth.api.service.authentication.AuthenticationType;
import io.fusionauth.api.service.cache.SystemConfigurationCache;
import io.fusionauth.api.service.oauth2.OAuthService;
import io.fusionauth.api.util.ActionTools;
import io.fusionauth.api.util.NetworkTools;
import io.fusionauth.app.Cookies;
import io.fusionauth.app.action.NoStoreJSONBaseAction;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.ChangePasswordReason;
import io.fusionauth.domain.Entity;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.User;
import io.fusionauth.domain.api.LoginRequest;
import io.fusionauth.domain.api.LoginResponse;
import io.fusionauth.domain.jwt.RefreshToken;
import io.fusionauth.domain.oauth2.GrantType;
import io.fusionauth.domain.oauth2.OAuthError;
import io.fusionauth.domain.oauth2.OAuthResponse;
import io.fusionauth.http.Cookie;
import io.fusionauth.http.server.HTTPRequest;
import io.fusionauth.http.server.HTTPResponse;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;

@Action
public class TokenAction extends NoStoreJSONBaseAction {
  private final FusionAuthConfiguration configuration;
  
  private final FusionAuthClientProvider fusionAuthClientProvider;
  
  private final HTTPRequest httpRequest;
  
  private final HTTPResponse httpResponse;
  
  private final OAuthService oauthService;
  
  private final SystemConfigurationCache systemConfigurationCache;
  
  public String access_token;
  
  public String client_id;
  
  public String client_secret;
  
  public String code;
  
  public String code_verifier;
  
  public String device_code;
  
  public EventInfo eventInfo = new EventInfo();
  
  public Integer expires_in;
  
  public String grant_type;
  
  public String loginId;
  
  public RefreshToken.MetaData metaData = new RefreshToken.MetaData();
  
  public String password;
  
  public URI redirect_uri;
  
  public String refresh_token;
  
  public List<URI> resource = new ArrayList<>();
  
  @JSONResponse
  public OAuthResponse response;
  
  public String scope;
  
  public UUID tenantId;
  
  public String user_code;
  
  @Inject
  public TokenAction(FusionAuthConfiguration paramFusionAuthConfiguration, FusionAuthClientProvider paramFusionAuthClientProvider, OAuthService paramOAuthService, HTTPRequest paramHTTPRequest, HTTPResponse paramHTTPResponse, SystemConfigurationCache paramSystemConfigurationCache) {
    this.configuration = paramFusionAuthConfiguration;
    this.fusionAuthClientProvider = paramFusionAuthClientProvider;
    this.oauthService = paramOAuthService;
    this.httpRequest = paramHTTPRequest;
    this.httpResponse = paramHTTPResponse;
    this.systemConfigurationCache = paramSystemConfigurationCache;
    RequestContext.set(new RequestContext(paramHTTPRequest.getPath()));
  }
  
  public String post() {
    OAuthService.OAuthValidationResult oAuthValidationResult = this.oauthService.validateTokenRequest(this.tenantId, this.client_id, this.client_secret, this.redirect_uri, this.code, this.refresh_token, this.grant_type, this.scope, this.httpRequest
        .getHeader("Authorization"), this.code_verifier, this.device_code, this.access_token, this.expires_in, this.httpRequest);
    this.client_id = oAuthValidationResult.client_id;
    this.client_secret = oAuthValidationResult.client_secret;
    if (oAuthValidationResult.error != null) {
      this.response = oAuthValidationResult.error;
      if (oAuthValidationResult.error.error == OAuthError.OAuthErrorType.invalid_client) {
        if (oAuthValidationResult.authHeaderUsed)
          this.httpResponse.setHeader("WWW-Authenticate", "Basic"); 
        return "invalid-client";
      } 
      return "input";
    } 
    if (oAuthValidationResult.grantType == GrantType.password)
      return authenticatePasswordGrant(oAuthValidationResult.tenant, oAuthValidationResult.application, oAuthValidationResult.scopes, this.eventInfo, oAuthValidationResult.dPoPThumbprint); 
    if (oAuthValidationResult.grantType == GrantType.refresh_token)
      return refreshAccessToken(oAuthValidationResult.tenant, oAuthValidationResult.application, oAuthValidationResult.user, oAuthValidationResult.refreshToken, oAuthValidationResult.accessToken, oAuthValidationResult.expiresInSeconds, oAuthValidationResult.scopes, this.eventInfo, oAuthValidationResult.dPoPThumbprint); 
    if (oAuthValidationResult.grantType == GrantType.device_code)
      return authenticateDeviceGrant(oAuthValidationResult.tenant, oAuthValidationResult.application, oAuthValidationResult.user, oAuthValidationResult.grantType, oAuthValidationResult.scopes, oAuthValidationResult.dPoPThumbprint); 
    if (oAuthValidationResult.grantType == GrantType.client_credentials)
      return authenticateClientCredentialsGrant(oAuthValidationResult.tenant, oAuthValidationResult.recipientEntity, oAuthValidationResult.targetEntities, oAuthValidationResult.entityGrantPermissions, oAuthValidationResult.scopes, oAuthValidationResult.dPoPThumbprint); 
    return authenticateAuthorizationCodeGrant(oAuthValidationResult.tenant, oAuthValidationResult.application, oAuthValidationResult.dPoPThumbprint);
  }
  
  public void setUsername(String paramString) {
    this.loginId = paramString;
  }
  
  @PostParameterMethod
  public void setupRequestContext() {
    Cookie cookie = Cookies.get(this.httpRequest, "access_token");
    if (cookie != null)
      this.access_token = cookie.value; 
    ActionTools.resolveTenantIdFromHeader(this.httpRequest).ifPresent(paramUUID -> this.tenantId = paramUUID);
    if (this.eventInfo.ipAddress == null)
      this.eventInfo.ipAddress = NetworkTools.getTrustedClientIPAddress(this.httpRequest, this.configuration, this.systemConfigurationCache.get()); 
    if (this.eventInfo.userAgent == null)
      this.eventInfo.userAgent = this.httpRequest.getHeader("User-Agent"); 
  }
  
  private String authenticateAuthorizationCodeGrant(Tenant paramTenant, Application paramApplication, @Nullable String paramString) {
    OAuthService.OAuthTokenResult oAuthTokenResult = this.oauthService.exchangeAuthorizationCode(this.eventInfo, paramTenant, paramApplication, paramApplication.oauthConfiguration.clientId, this.redirect_uri, this.code, this.code_verifier, this.resource, paramString);
    if (oAuthTokenResult.error != null) {
      this.response = oAuthTokenResult.error;
      return "input";
    } 
    this.response = oAuthTokenResult.accessToken;
    return "render";
  }
  
  private String authenticateClientCredentialsGrant(Tenant paramTenant, Entity paramEntity, Map<String, Entity> paramMap, Map<String, Set<String>> paramMap1, Set<String> paramSet, @Nullable String paramString) {
    this.response = this.oauthService.createClientCredentialsAccessToken(paramTenant, paramEntity, paramMap, paramMap1, paramSet, paramString);
    return "render";
  }
  
  private String authenticateDeviceGrant(Tenant paramTenant, Application paramApplication, User paramUser, GrantType paramGrantType, Set<String> paramSet, @Nullable String paramString) {
    this.response = this.oauthService.createAccessToken(this.eventInfo, paramTenant, paramApplication, paramUser, paramApplication.oauthConfiguration.clientId, null, paramGrantType, "token", paramSet, null, NetworkTools.getTrustedClientIPAddress(this.httpRequest, this.configuration, this.systemConfigurationCache.get()), null, null, this.device_code, null, this.metaData, null, null, paramString);
    return "render";
  }
  
  private String authenticatePasswordGrant(Tenant paramTenant, Application paramApplication, Set<String> paramSet, EventInfo paramEventInfo, @Nullable String paramString) {
    LoginRequest loginRequest = new LoginRequest(paramEventInfo, paramApplication.id, this.loginId, this.password);
    loginRequest.noJWT = true;
    ClientResponse<LoginResponse, Errors> clientResponse = this.fusionAuthClientProvider.get(paramApplication.tenantId).login(loginRequest);
    if (clientResponse.status == 200 || clientResponse.status == 202 || clientResponse.status == 212 || clientResponse.status == 213) {
      User user = ((LoginResponse)clientResponse.successResponse).user;
      this.response = this.oauthService.createAccessToken(paramEventInfo, paramTenant, paramApplication, user, this.client_id, this.redirect_uri, GrantType.password, null, paramSet, null, 
          NetworkTools.getTrustedClientIPAddress(this.httpRequest, this.configuration, this.systemConfigurationCache.get()), null, this.user_code, null, null, this.metaData, AuthenticationType.PASSWORD, null, paramString);
      return "render";
    } 
    if (clientResponse.status == 203) {
      this.response = buildChangePasswordError(((LoginResponse)clientResponse.successResponse).changePasswordId, ((LoginResponse)clientResponse.successResponse).changePasswordReason);
      return "input";
    } 
    if (clientResponse.status == 242) {
      OAuthError oAuthError = new OAuthError(OAuthError.OAuthErrorType.two_factor_required, "The user has enabled two factor authentication.");
      oAuthError.twoFactorId = ((LoginResponse)clientResponse.successResponse).twoFactorId;
      oAuthError.methods = ((LoginResponse)clientResponse.successResponse).methods;
      this.response = oAuthError;
      return "input";
    } 
    if (clientResponse.status == 404) {
      this.response = new OAuthError(OAuthError.OAuthErrorType.invalid_grant, OAuthError.OAuthErrorReason.invalid_user_credentials, "The user credentials are invalid.");
    } else if (clientResponse.status == 409) {
      this.response = new OAuthError(OAuthError.OAuthErrorType.invalid_grant, OAuthError.OAuthErrorReason.login_prevented, "The user is currently prevented from login.");
    } else if (clientResponse.status == 410) {
      this.response = new OAuthError(OAuthError.OAuthErrorType.invalid_grant, OAuthError.OAuthErrorReason.user_expired, "The user is expired.");
    } else if (clientResponse.status == 423) {
      this.response = new OAuthError(OAuthError.OAuthErrorType.invalid_grant, OAuthError.OAuthErrorReason.user_locked, "The user is locked and cannot login.");
    } else if (clientResponse.exception != null) {
      this.response = new OAuthError(OAuthError.OAuthErrorType.server_error, OAuthError.OAuthErrorReason.unknown, "An unexpected error occurred. The request may not be completed.");
    } else {
      this.response = new OAuthError(OAuthError.OAuthErrorType.invalid_grant, OAuthError.OAuthErrorReason.unknown, "The user is unable to login. Authentication server responded with " + clientResponse.status + " status code.");
    } 
    return "input";
  }
  
  private OAuthError buildChangePasswordError(String paramString, ChangePasswordReason paramChangePasswordReason) {
    OAuthError.OAuthErrorReason oAuthErrorReason = null;
    switch (paramChangePasswordReason) {
      case Administrative:
        oAuthErrorReason = OAuthError.OAuthErrorReason.change_password_administrative;
        break;
      case Breached:
        oAuthErrorReason = OAuthError.OAuthErrorReason.change_password_breached;
        break;
      case Expired:
        oAuthErrorReason = OAuthError.OAuthErrorReason.change_password_expired;
        break;
      case Validation:
        oAuthErrorReason = OAuthError.OAuthErrorReason.change_password_validation;
        break;
    } 
    OAuthError oAuthError = new OAuthError(OAuthError.OAuthErrorType.change_password_required, oAuthErrorReason, "The user is required to change their password.");
    oAuthError.changePasswordId = paramString;
    return oAuthError;
  }
  
  private String refreshAccessToken(Tenant paramTenant, Application paramApplication, User paramUser, RefreshToken paramRefreshToken, String paramString1, Integer paramInteger, Set<String> paramSet, EventInfo paramEventInfo, @Nullable String paramString2) {
    OAuthService.OAuthTokenResult oAuthTokenResult = this.oauthService.refreshAccessToken(paramTenant, paramApplication, paramUser, paramRefreshToken, paramSet, this.user_code, paramInteger, paramString1, paramEventInfo, paramString2, this.resource);
    if (oAuthTokenResult.error != null) {
      this.response = oAuthTokenResult.error;
      return "input";
    } 
    this.response = oAuthTokenResult.accessToken;
    return "render";
  }
}
