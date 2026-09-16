package io.fusionauth.app.action.oauth2;

import com.google.inject.Inject;
import io.fusionauth.api.service.jwt.FusionAuthJWTDecoder;
import io.fusionauth.api.service.jwt.JWTService;
import io.fusionauth.api.service.jwt.JWTValidationContext;
import io.fusionauth.api.service.jwt.ValidatedJWTResult;
import io.fusionauth.api.service.oauth2.OAuthService;
import io.fusionauth.api.util.ClaimTools;
import io.fusionauth.api.util.UUIDTools;
import io.fusionauth.domain.oauth2.GrantType;
import io.fusionauth.domain.oauth2.IntrospectResponse;
import io.fusionauth.http.server.HTTPRequest;
import io.fusionauth.http.server.HTTPResponse;
import io.fusionauth.jwt.domain.JWT;
import java.time.ZonedDateTime;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.validation.ValidationMethod;

@Action
public class IntrospectAction extends BaseIntrospectAction {
  private final JWTService jwtService;
  
  public String token;
  
  public String token_type_hint;
  
  private boolean unsafeIntrospectDecodeFailed;
  
  @Inject
  public IntrospectAction(HTTPRequest paramHTTPRequest, HTTPResponse paramHTTPResponse, OAuthService paramOAuthService, JWTService paramJWTService) {
    super(paramHTTPRequest, paramHTTPResponse, paramOAuthService);
    this.jwtService = paramJWTService;
  }
  
  public String post() {
    this.response = new IntrospectResponse(false);
    if (this.token_type_hint != null && this.token_type_hint.equals("refresh_token"))
      return handleRefreshToken(); 
    if (this.unsafeIntrospectDecodeFailed)
      return "render"; 
    JWT jWT = null;
    try {
      ValidatedJWTResult validatedJWTResult = (this.grantType == GrantType.client_credentials) ? this.jwtService.validateEntityJWT(this.token, FusionAuthJWTDecoder.JWTConstraints.OAuthIntrospectEntityToken, this.codeTenant, this.codeEntity) : this.jwtService.validateJWT(this.token, FusionAuthJWTDecoder.JWTConstraints.OAuthIntrospectApplicationToken, new JWTValidationContext.SuppliedTenantAndApplication(this.codeTenant, this.codeApplication));
      if (validatedJWTResult.valid)
        jWT = validatedJWTResult.jwt; 
    } catch (Exception exception) {}
    if (jWT == null)
      return "render"; 
    if (this.grantType != GrantType.client_credentials && ClaimTools.getPrimaryGrantType(jWT) == GrantType.client_credentials)
      return "render"; 
    if (this.grantType == GrantType.client_credentials) {
      if (ClaimTools.getPrimaryGrantType(jWT) != GrantType.client_credentials)
        return "render"; 
      UUID uUID = UUIDTools.fromString(jWT.subject);
      if (this.codeEntity == null || this.codeEntity.id == null || !this.codeEntity.id.equals(uUID))
        return "render"; 
      if (this.codeEntity.clientId == null || !this.codeEntity.clientId.equals(this.client_id))
        return "render"; 
    } 
    IntrospectResponse introspectResponse = new IntrospectResponse(true);
    introspectResponse.putAll(jWT.getRawClaims());
    this.response = introspectResponse;
    return "render";
  }
  
  @ValidationMethod
  public void validate() {
    OAuthService.OAuthValidationResult oAuthValidationResult = this.oauthService.validateIntrospectRequest(this.httpRequest.getHeader("Authorization"), this.client_id, this.client_secret, this.tenantId, this.token, this.token_type_hint);
    this.unsafeIntrospectDecodeFailed = oAuthValidationResult.unsafeIntrospectDecodeFailed;
    handleValidationResult(oAuthValidationResult);
  }
  
  private String handleRefreshToken() {
    if (this.refreshToken == null)
      return "render"; 
    int i = (this.codeTenant.lookupJWTConfiguration(this.codeApplication)).refreshTokenTimeToLiveInMinutes;
    ZonedDateTime zonedDateTime = this.refreshToken.startInstant.plusMinutes(i);
    IntrospectResponse introspectResponse = new IntrospectResponse(true);
    introspectResponse.put("exp", Long.valueOf(zonedDateTime.toEpochSecond()));
    this.response = introspectResponse;
    return "render";
  }
}
