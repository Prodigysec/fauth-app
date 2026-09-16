package io.fusionauth.app.action.legacy.oauth2;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.configuration.FusionAuthConfiguration;
import io.fusionauth.api.domain.guice.FusionAuthClientProvider;
import io.fusionauth.api.service.jwt.FusionAuthJWTDecoder;
import io.fusionauth.api.service.jwt.JWTService;
import io.fusionauth.api.service.jwt.JWTValidationContext;
import io.fusionauth.api.service.jwt.TenantSource;
import io.fusionauth.api.service.jwt.ValidatedJWTResult;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.api.util.ClaimTools;
import io.fusionauth.app.service.legacy.LegacyTokenSigner;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.api.ApplicationResponse;
import io.fusionauth.domain.oauth2.OAuthError;
import io.fusionauth.domain.oauth2.UserinfoResponse;
import io.fusionauth.http.server.HTTPRequest;
import io.fusionauth.jwt.domain.JWT;
import java.util.Map;
import java.util.UUID;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;
import org.primeframework.mvc.validation.ValidationMethod;

@Action
public class UserinfoAction extends BaseLegacyAdapterAction {
  private final JWTService jwtService;
  
  private final LegacyTokenSigner legacyTokenSigner;
  
  @JSONResponse
  public Object response;
  
  private String token;
  
  private ValidatedJWTResult validatedResult;
  
  @Inject
  public UserinfoAction(FusionAuthConfiguration paramFusionAuthConfiguration, FusionAuthClientProvider paramFusionAuthClientProvider, HTTPRequest paramHTTPRequest, ReactorStatusService paramReactorStatusService, LegacyTokenSigner paramLegacyTokenSigner, JWTService paramJWTService) {
    super(paramFusionAuthConfiguration, paramFusionAuthClientProvider, paramHTTPRequest, paramReactorStatusService);
    this.legacyTokenSigner = paramLegacyTokenSigner;
    this.jwtService = paramJWTService;
  }
  
  @PostParameterMethod
  public void extractToken() {
    this.token = extractBearerToken();
  }
  
  public String get() {
    String str1 = this.validatedResult.jwt.getString("fa_uid");
    String str2 = this.validatedResult.jwt.subject;
    String str3 = this.token;
    if (str1 != null) {
      str3 = buildUpstreamToken(str1);
      if (str3 == null)
        str3 = this.token; 
    } 
    FusionAuthClient fusionAuthClient = this.fusionAuthClientProvider.get(this.tenantId);
    ClientResponse<UserinfoResponse, OAuthError> clientResponse = fusionAuthClient.retrieveUserInfoFromAccessToken(str3);
    if (clientResponse.wasSuccessful()) {
      if (str1 != null && str2 != null) {
        UserinfoResponse userinfoResponse = new UserinfoResponse();
        userinfoResponse.putAll((Map<? extends String, ?>)clientResponse.successResponse);
        userinfoResponse.put("sub", str2);
        userinfoResponse.put("fa_uid", str1);
        this.response = userinfoResponse;
      } else {
        this.response = clientResponse.successResponse;
      } 
      return "render";
    } 
    if (clientResponse.status == 401) {
      this.response = clientResponse.errorResponse;
      return "unauthorized";
    } 
    this.response = (clientResponse.errorResponse != null) ? clientResponse.errorResponse : new OAuthError();
    return "input";
  }
  
  public String post() {
    return get();
  }
  
  @ValidationMethod(httpMethods = {"GET", "POST"})
  public void validateToken() {
    if (isDisabled())
      throw new ErrorException("not-found", false); 
    if (isNotLicensed()) {
      this.response = buildNotLicensedError();
      throw new ErrorException("input", false);
    } 
    if (this.token == null)
      throw new ErrorException("unauthorized", false); 
    this.validatedResult = this.jwtService.validateJWT(this.token, FusionAuthJWTDecoder.JWTConstraints.OAuthUserAccessToken, new JWTValidationContext.AudienceApplicationRequired(TenantSource.SignedTid.INSTANCE));
    if (!this.validatedResult.valid)
      throw new ErrorException("unauthorized", false); 
  }
  
  private String buildUpstreamToken(String paramString) {
    UUID uUID = ClaimTools.resolveApplicationId(this.validatedResult.jwt);
    if (uUID == null)
      return null; 
    ClientResponse<ApplicationResponse, Void> clientResponse = this.fusionAuthClientProvider.get(this.tenantId).retrieveApplication(uUID);
    if (!clientResponse.wasSuccessful() || clientResponse.successResponse == null || ((ApplicationResponse)clientResponse.successResponse).application == null)
      return null; 
    Application application = ((ApplicationResponse)clientResponse.successResponse).application;
    JWT jWT = FusionAuthJWTDecoder.unsafeDecode(this.token);
    jWT.setSubject(paramString);
    jWT.otherClaims.remove("fa_uid");
    String str = (jWT.header != null) ? (String)jWT.header.properties.get("kid") : null;
    return this.legacyTokenSigner.sign(jWT, str, uUID, application.oauthConfiguration.clientSecret, false);
  }
}
