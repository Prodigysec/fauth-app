package io.fusionauth.app.primeframework;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.admin.JWTManager;
import io.fusionauth.api.domain.guice.FusionAuthLocalClientURL;
import io.fusionauth.api.domain.guice.FusionAuthTenantId;
import io.fusionauth.api.service.cache.ApplicationCache;
import io.fusionauth.api.service.cache.TenantCache;
import io.fusionauth.api.service.jwt.FusionAuthJWTDecoder;
import io.fusionauth.api.service.jwt.JWTClaimValidator;
import io.fusionauth.api.service.jwt.RefreshTokenService;
import io.fusionauth.api.service.jwt.ValidatedJWTResult;
import io.fusionauth.api.service.system.ApplicationReaderService;
import io.fusionauth.api.service.system.TenantReaderService;
import io.fusionauth.api.util.UUIDTools;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.User;
import io.fusionauth.domain.api.UserResponse;
import io.fusionauth.http.Cookie;
import io.fusionauth.http.server.HTTPRequest;
import io.fusionauth.http.server.HTTPResponse;
import io.fusionauth.jwt.domain.JWT;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import org.primeframework.mvc.security.BaseJWTRefreshTokenCookiesUserLoginSecurityContext;
import org.primeframework.mvc.security.VerifierProvider;
import org.primeframework.mvc.security.oauth.OAuthConfiguration;
import org.primeframework.mvc.security.oauth.TokenAuthenticationMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FusionAuthUserLoginSecurityContext extends BaseJWTRefreshTokenCookiesUserLoginSecurityContext {
  public static final String ApplicationId = "securityContext.applicationId";
  
  public static final String ClientId = "securityContext.clientId";
  
  public static final String TenantId = "securityContext.tenantId";
  
  public static final String UniversalApplication = "securityContext.universalApplication";
  
  private static final Logger logger = LoggerFactory.getLogger(FusionAuthUserLoginSecurityContext.class);
  
  private final ApplicationCache applicationCache;
  
  private final ApplicationReaderService applicationReader;
  
  private final FusionAuthClient client;
  
  private final UUID fusionauthTenantId;
  
  private final String fusionauthURL;
  
  private final JWTClaimValidator jwtClaimValidator;
  
  private final RefreshTokenService refreshTokenService;
  
  private final TenantCache tenantCache;
  
  private final TenantReaderService tenantReader;
  
  @Inject
  public FusionAuthUserLoginSecurityContext(HTTPRequest paramHTTPRequest, HTTPResponse paramHTTPResponse, VerifierProvider paramVerifierProvider, ApplicationCache paramApplicationCache, ApplicationReaderService paramApplicationReaderService, FusionAuthClient paramFusionAuthClient, @FusionAuthLocalClientURL String paramString, RefreshTokenService paramRefreshTokenService, TenantCache paramTenantCache, TenantReaderService paramTenantReaderService, @FusionAuthTenantId UUID paramUUID, JWTClaimValidator paramJWTClaimValidator) {
    super(paramHTTPRequest, paramHTTPResponse, paramVerifierProvider);
    this.applicationCache = paramApplicationCache;
    this.applicationReader = paramApplicationReaderService;
    this.client = paramFusionAuthClient;
    this.fusionauthURL = paramString;
    this.refreshTokenService = paramRefreshTokenService;
    this.tenantCache = paramTenantCache;
    this.tenantReader = paramTenantReaderService;
    this.fusionauthTenantId = paramUUID;
    this.jwtClaimValidator = paramJWTClaimValidator;
  }
  
  public Set<String> getCurrentUsersRoles() {
    User user = (User)getCurrentUser();
    if (user == null)
      return Collections.emptySet(); 
    Set<String> set = user.getRoleNamesForApplication(getApplicationId());
    return (set != null) ? set : Collections.<String>emptySet();
  }
  
  public void logout(Object paramObject) {
    String str = getSessionId();
    if (str != null)
      try {
        this.refreshTokenService.revokeFusionAuthSessionToken(UUIDTools.fromString(str), (EventInfo)paramObject);
      } catch (Exception exception) {} 
    logout();
  }
  
  protected Cookie.SameSite cookieSameSite() {
    return isAccount() ? Cookie.SameSite.Lax : Cookie.SameSite.Strict;
  }
  
  protected UUID getApplicationId() {
    if (isAccount())
      return (UUID)this.request.getAttribute("securityContext.applicationId"); 
    return Application.FUSIONAUTH_APP_ID;
  }
  
  protected FusionAuthClient getClient() {
    return this.client;
  }
  
  protected UUID getTenantId() {
    if (isAccount())
      return (UUID)this.request.getAttribute("securityContext.tenantId"); 
    return this.fusionauthTenantId;
  }
  
  protected boolean isRevoked(JWT paramJWT) {
    return !JWTManager.isValid(paramJWT);
  }
  
  protected boolean isUniversalApplication() {
    Boolean bool = (Boolean)this.request.getAttribute("securityContext.universalApplication");
    return (bool != null && bool.booleanValue());
  }
  
  protected String jwtCookieName() {
    return isAccount() ? "account.at" : "fusionauth.at";
  }
  
  protected OAuthConfiguration oauthConfiguration() {
    Objects.requireNonNull(this.applicationReader);
    Application application = this.applicationCache.get(null, getApplicationId(), this.applicationReader::retrieveById);
    UUID uUID = getTenantId();
    return (OAuthConfiguration)((OAuthConfiguration)((OAuthConfiguration)((OAuthConfiguration)((OAuthConfiguration)(new OAuthConfiguration()).with(paramOAuthConfiguration -> paramOAuthConfiguration.authenticationMethod = TokenAuthenticationMethod.client_secret_post))
      .with(paramOAuthConfiguration -> paramOAuthConfiguration.clientId = paramApplication.oauthConfiguration.clientId))
      .with(paramOAuthConfiguration -> paramOAuthConfiguration.clientSecret = paramApplication.oauthConfiguration.clientSecret))
      .with(paramOAuthConfiguration -> paramOAuthConfiguration.tokenEndpoint = this.fusionauthURL + "/oauth2/token"))
      
      .with(paramOAuthConfiguration -> paramOAuthConfiguration.additionalParameters.put("tenantId", (paramUUID != null) ? List.of(paramUUID.toString()) : null));
  }
  
  protected String refreshTokenCookieName() {
    return isAccount() ? "account.rt" : "fusionauth.rt";
  }
  
  protected Application retrieveApplication(@Nonnull Tenant paramTenant, @Nonnull UUID paramUUID) {
    Objects.requireNonNull(this.applicationReader);
    return this.applicationCache.get(paramTenant.id, paramUUID, this.applicationReader::retrieveById);
  }
  
  protected Object retrieveUserForJWT(JWT paramJWT, String paramString) {
    UUID uUID = UUIDTools.fromString(paramJWT.subject);
    ClientResponse<UserResponse, Errors> clientResponse = getClient().retrieveUser(uUID);
    if (clientResponse.wasSuccessful())
      return ((UserResponse)clientResponse.successResponse).user; 
    return null;
  }
  
  protected boolean validateJWTClaims(JWT paramJWT) {
    FusionAuthJWTDecoder.JWTConstraints jWTConstraints = isAccount() ? FusionAuthJWTDecoder.JWTConstraints.UserAccessTokenWithAudience : FusionAuthJWTDecoder.JWTConstraints.FusionAuthOAuthUserAccessToken;
    ValidatedJWTResult validatedJWTResult = FusionAuthJWTDecoder.validateConstraints(paramJWT, jWTConstraints);
    if (!validatedJWTResult.valid) {
      logger.debug(validatedJWTResult.getMessage());
      return false;
    } 
    UUID uUID = getApplicationId();
    if (uUID == null) {
      logger.debug("Invalid JWT. An applicationId is required.");
      return false;
    } 
    Objects.requireNonNull(this.tenantReader);
    Tenant tenant = isUniversalApplication() ? this.tenantCache.get(getTenantId(), this.tenantReader::retrieveById) : this.tenantReader.retrieveByApplicationId(uUID);
    if (tenant == null) {
      if (isUniversalApplication()) {
        logger.debug("JWT validation failed. Requested tenantId [{}] is invalid", getTenantId());
      } else {
        logger.debug("Invalid JWT. The aud [{}] claim is invalid.", paramJWT.audience);
      } 
      return false;
    } 
    String str1 = paramJWT.getString("applicationId");
    if (!isAccount() && 
      str1 == null) {
      logger.debug("Invalid JWT. The [applicationId] claim is required.");
      return false;
    } 
    if (str1 != null && !uUID.toString().equals(str1)) {
      logger.debug("Invalid JWT. Expected applicationId [{}] but found [{}]", uUID, str1);
      return false;
    } 
    Application application = retrieveApplication(tenant, uUID);
    if (application == null) {
      logger.debug("Invalid JWT. Unable to resolve application [{}] for tenant [{}].", uUID, tenant.id);
      return false;
    } 
    if (!(this.jwtClaimValidator.validateForApplication(paramJWT, tenant, application) instanceof JWTClaimValidator.ClaimValidationResult.Valid))
      return false; 
    String str2 = paramJWT.issuer;
    if (!tenant.issuer.equals(str2)) {
      logger.debug("Invalid JWT. Expected iss [{}] but found [{}]", tenant.issuer, str2);
      return false;
    } 
    return true;
  }
  
  private boolean isAccount() {
    return this.request.getPath().startsWith("/account");
  }
}
