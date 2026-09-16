package io.fusionauth.api.service.jwt;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.inversoft.error.Errors;
import com.inversoft.validator.Validator;
import io.fusionauth.api.domain.ApplicationMapper;
import io.fusionauth.api.domain.RefreshTokenMapper;
import io.fusionauth.api.domain.api.service.ImmutableLambdaArgument;
import io.fusionauth.api.domain.api.service.LambdaArgument;
import io.fusionauth.api.domain.api.service.MutableLambdaArgument;
import io.fusionauth.api.domain.mybatis._RefreshToken;
import io.fusionauth.api.service.authentication.AuthenticationType;
import io.fusionauth.api.service.cache.ApplicationCache;
import io.fusionauth.api.service.cache.TenantCache;
import io.fusionauth.api.service.jwt.claims.JWTType;
import io.fusionauth.api.service.lambda.LambdaInvocationService;
import io.fusionauth.api.service.oauth2.DPoPService;
import io.fusionauth.api.service.system.ApplicationReaderService;
import io.fusionauth.api.service.system.EventHelper;
import io.fusionauth.api.service.system.KeyReaderService;
import io.fusionauth.api.service.system.KeyService;
import io.fusionauth.api.service.system.LambdaInvocationException;
import io.fusionauth.api.service.system.TenantReaderService;
import io.fusionauth.api.service.user.UserMetricsService;
import io.fusionauth.api.service.user.UserReaderService;
import io.fusionauth.api.util.ClaimTools;
import io.fusionauth.api.util.HashTools;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.Entity;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.IdentityType;
import io.fusionauth.domain.JWTConfiguration;
import io.fusionauth.domain.Key;
import io.fusionauth.domain.RefreshTokenExpirationPolicy;
import io.fusionauth.domain.RefreshTokenUsagePolicy;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.Tenantable;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserIdentity;
import io.fusionauth.domain.UserRegistration;
import io.fusionauth.domain.event.JWTRefreshEvent;
import io.fusionauth.domain.jwt.RefreshToken;
import io.fusionauth.domain.oauth2.AccessToken;
import io.fusionauth.domain.oauth2.GrantType;
import io.fusionauth.domain.oauth2.OAuthScopeHandlingPolicy;
import io.fusionauth.domain.oauth2.TokenType;
import io.fusionauth.http.server.HTTPRequest;
import io.fusionauth.jwt.OpenIDConnect;
import io.fusionauth.jwt.Signer;
import io.fusionauth.jwt.Verifier;
import io.fusionauth.jwt.domain.Header;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.jwt.hmac.HMACVerifier;
import java.net.URI;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultJWTService implements JWTService {
  private static final Set<String> ClientCredentialsReservedClaims = Set.of((Object[])new String[] { 
        "amr", "aud", "cnf", "exp", "gty", "iat", "permissions", "sub", "tid", "tty", 
        "use" });
  
  private static final Set<String> ReservedClaims = Set.of("amr", "auth_time", "cnf", "fa_uid", "gty", "iat", "sub", "tid", "tty");
  
  private static final Set<String> VendingReservedClaims = Set.of("amr", "cnf", "exp", "iat");
  
  private static final Logger logger = LoggerFactory.getLogger(DefaultJWTService.class);
  
  private final ApplicationCache applicationCache;
  
  private final ApplicationMapper applicationMapper;
  
  private final ApplicationReaderService applicationReader;
  
  private final DPoPService dpopService;
  
  private final JWTClaimValidator jwtClaimValidator;
  
  private final KeyReaderService keyReader;
  
  private final LambdaInvocationService lambdaInvocationService;
  
  private final UserMetricsService loginService;
  
  private final RefreshTokenMapper refreshTokenMapper;
  
  private final RefreshTokenService refreshTokenService;
  
  private final TenantCache tenantCache;
  
  private final TenantReaderService tenantReaderService;
  
  private final UserReaderService userReader;
  
  private final Provider<Map<String, Verifier>> verifierProvider;
  
  @Inject
  public DefaultJWTService(ApplicationCache paramApplicationCache, ApplicationMapper paramApplicationMapper, ApplicationReaderService paramApplicationReaderService, DPoPService paramDPoPService, KeyReaderService paramKeyReaderService, LambdaInvocationService paramLambdaInvocationService, RefreshTokenMapper paramRefreshTokenMapper, TenantCache paramTenantCache, TenantReaderService paramTenantReaderService, UserReaderService paramUserReaderService, UserMetricsService paramUserMetricsService, RefreshTokenService paramRefreshTokenService, Provider<Map<String, Verifier>> paramProvider, JWTClaimValidator paramJWTClaimValidator) {
    this.applicationCache = paramApplicationCache;
    this.tenantCache = paramTenantCache;
    this.applicationMapper = paramApplicationMapper;
    this.applicationReader = paramApplicationReaderService;
    this.dpopService = paramDPoPService;
    this.keyReader = paramKeyReaderService;
    this.lambdaInvocationService = paramLambdaInvocationService;
    this.refreshTokenMapper = paramRefreshTokenMapper;
    this.tenantReaderService = paramTenantReaderService;
    this.userReader = paramUserReaderService;
    this.loginService = paramUserMetricsService;
    this.refreshTokenService = paramRefreshTokenService;
    this.verifierProvider = paramProvider;
    this.jwtClaimValidator = paramJWTClaimValidator;
  }
  
  public JWTService.JWTResult createJWT(Tenant paramTenant, User paramUser, AuthenticationType paramAuthenticationType, Application paramApplication, Map<String, Object> paramMap, UUID paramUUID, GrantType paramGrantType, JWTType paramJWTType, Set<String> paramSet, @Nullable String paramString) {
    return generateJWTWithExpiration(paramTenant, paramUser, paramAuthenticationType, paramApplication, paramMap, null, paramUUID, paramGrantType, paramJWTType, paramSet, paramString);
  }
  
  public JWTService.JWTResult createJWT(Tenant paramTenant, Entity paramEntity, Map<String, Entity> paramMap, Map<String, Set<String>> paramMap1, GrantType paramGrantType, Map<String, Object> paramMap2, String paramString) {
    ZonedDateTime zonedDateTime1 = ZonedDateTime.now(ZoneOffset.UTC);
    JWTConfiguration jWTConfiguration = paramTenant.lookupJWTConfiguration(null);
    int i = jWTConfiguration.timeToLiveInSeconds;
    UUID uUID = jWTConfiguration.accessTokenKeyId;
    if (paramEntity.type.jwtConfiguration.enabled) {
      uUID = (paramEntity.type.jwtConfiguration.accessTokenKeyId != null) ? paramEntity.type.jwtConfiguration.accessTokenKeyId : uUID;
      i = paramEntity.type.jwtConfiguration.timeToLiveInSeconds;
    } 
    ZonedDateTime zonedDateTime2 = zonedDateTime1.plusSeconds(i);
    String str = paramTenant.issuer;
    JWT jWT = (new JWT()).setIssuer(str).setIssuedAt(zonedDateTime1).setExpiration(zonedDateTime2).setSubject(paramEntity.id.toString()).setUniqueId(UUID.randomUUID().toString()).addClaim("tty", JWTType.AccessToken.value());
    Objects.requireNonNull(jWT);
    paramMap2.forEach(jWT::addClaim);
    jWT.addClaim("tid", paramTenant.id);
    if (paramMap1 != null && !paramMap1.isEmpty()) {
      jWT.addClaim("aud", paramMap1.keySet());
      jWT.addClaim("permissions", paramMap1);
    } 
    if (paramEntity.type.id.equals(paramTenant.scimServerConfiguration.clientEntityTypeId))
      jWT.addClaim("use", "scim_server"); 
    jWT.addClaim("gty", List.of(paramGrantType.grantName()));
    if (paramString != null)
      jWT.addClaim("cnf", Map.of("jkt", paramString)); 
    applyLambda(paramTenant.oauthConfiguration.clientCredentialsAccessTokenPopulateLambdaId, jWT, paramEntity, paramMap, paramMap1);
    Signer signer = getSigner(null, uUID, JWTType.AccessToken);
    return new JWTService.JWTResult(jWT, JWT.getEncoder()
        .encode(jWT, signer, paramHeader -> paramHeader.set("kid", paramSigner.getKid())), signer
        .getAlgorithm());
  }
  
  public JWTService.JWTResult createJWTFromAnotherJWT(Tenant paramTenant, User paramUser, Application paramApplication, Map<String, Object> paramMap, JWT paramJWT, UUID paramUUID, GrantType paramGrantType, JWTType paramJWTType, @Nullable String paramString) {
    return generateJWTWithExpiration(paramTenant, paramUser, AuthenticationType.JWT_SSO, paramApplication, paramMap, (paramJWT == null) ? null : paramJWT.expiration, paramUUID, paramGrantType, paramJWTType, Collections.emptySet(), paramString);
  }
  
  public JWTService.JWTResult createJWTWithExpiration(Tenant paramTenant, User paramUser, AuthenticationType paramAuthenticationType, Application paramApplication, ZonedDateTime paramZonedDateTime, Map<String, Object> paramMap, UUID paramUUID, GrantType paramGrantType, JWTType paramJWTType, Set<String> paramSet) {
    return generateJWTWithExpiration(paramTenant, paramUser, paramAuthenticationType, paramApplication, paramMap, paramZonedDateTime, paramUUID, paramGrantType, paramJWTType, paramSet, null);
  }
  
  public JWTService.JWTResult createVendedJWT(Key paramKey, int paramInt, Map<String, Object> paramMap) {
    ZonedDateTime zonedDateTime1 = ZonedDateTime.now(ZoneOffset.UTC);
    ZonedDateTime zonedDateTime2 = zonedDateTime1.plusSeconds(paramInt);
    JWT jWT = (new JWT()).setIssuedAt(zonedDateTime1).setExpiration(zonedDateTime2).setUniqueId(UUID.randomUUID().toString());
    if (paramMap != null)
      for (String str : paramMap.keySet()) {
        if (!VendingReservedClaims.contains(str))
          jWT.addClaim(str, paramMap.get(str)); 
      }  
    jWT.addClaim("amr", List.of("none"));
    Signer signer = JWTHelper.buildSigner(paramKey);
    return new JWTService.JWTResult(jWT, JWT.getEncoder().encode(jWT, signer), signer.getAlgorithm());
  }
  
  @Transactional
  public JWTService.RefreshResult refreshAccessToken(Tenant paramTenant, User paramUser, Application paramApplication, RefreshToken paramRefreshToken, String paramString1, Set<String> paramSet, Integer paramInteger, Supplier<Map<String, Object>> paramSupplier, EventInfo paramEventInfo, @Nullable String paramString2) {
    Map<String, Object> map = (paramSupplier != null) ? paramSupplier.get() : new HashMap<>();
    ZonedDateTime zonedDateTime1 = ZonedDateTime.now(ZoneOffset.UTC);
    JWTConfiguration jWTConfiguration = paramTenant.lookupJWTConfiguration(paramApplication);
    boolean bool = false;
    if (jWTConfiguration.refreshTokenUsagePolicy == RefreshTokenUsagePolicy.OneTimeUse) {
      String str1 = ((_RefreshToken)paramRefreshToken).requestedToken;
      _RefreshToken _RefreshToken1 = (_RefreshToken)this.refreshTokenService.retrieveForUpdateUsingSeed(str1);
      if (_RefreshToken1 == null)
        return null; 
      paramRefreshToken = _RefreshToken1;
      _RefreshToken _RefreshToken2 = (_RefreshToken)paramRefreshToken;
      if (_RefreshToken2.seedOnly) {
        if (paramTenant.jwtConfiguration.refreshTokenRevocationPolicy.onOneTimeTokenReuse)
          this.refreshTokenMapper.deleteById(paramRefreshToken.id); 
        return null;
      } 
      if (_RefreshToken2.hashN1) {
        int i = jWTConfiguration.refreshTokenOneTimeUseConfiguration.gracePeriodInSeconds;
        ZonedDateTime zonedDateTime = getLastRotated(paramRefreshToken);
        if (zonedDateTime == null || zonedDateTime.plusSeconds(i).isBefore(zonedDateTime1)) {
          if (paramTenant.jwtConfiguration.refreshTokenRevocationPolicy.onOneTimeTokenReuse)
            this.refreshTokenMapper.deleteById(paramRefreshToken.id); 
          return null;
        } 
      } else {
        paramRefreshToken.token = RefreshTokenService.HashedRefreshToken.v2Rotate(paramRefreshToken.token, _RefreshToken2.isVersion(2));
        paramRefreshToken.data.put("lastRotated", Long.valueOf(zonedDateTime1.toEpochSecond()));
        _RefreshToken2.setVersion(2);
        bool = true;
      } 
    } 
    paramRefreshToken.metaData.device.lastAccessedInstant = zonedDateTime1;
    if (jWTConfiguration.refreshTokenExpirationPolicy == RefreshTokenExpirationPolicy.SlidingWindow || jWTConfiguration.refreshTokenExpirationPolicy == RefreshTokenExpirationPolicy.SlidingWindowWithMaximumLifetime)
      paramRefreshToken.startInstant = zonedDateTime1; 
    this.refreshTokenMapper.update(new RefreshTokenService.HashedRefreshToken(paramRefreshToken, HashTools.sha256(paramRefreshToken.token)), bool);
    ZonedDateTime zonedDateTime2 = zonedDateTime1.plusSeconds(jWTConfiguration.timeToLiveInSeconds);
    String str = paramTenant.issuer;
    JWT jWT = createBaseJWT(str, zonedDateTime1, zonedDateTime2, AuthenticationType.REFRESH_TOKEN, paramUser, JWTType.AccessToken, paramSet, paramApplication.oauthConfiguration.scopeHandlingPolicy);
    Object object1 = paramRefreshToken.data.get("amr");
    if (object1 != null)
      map.put("amr", object1); 
    map.put("auth_time", paramRefreshToken.data.get("auth_time"));
    map.put("tid", paramTenant.id);
    map.put("sid", paramRefreshToken.id);
    if (!paramSet.isEmpty())
      map.put("scope", String.join(" ", (Iterable)paramSet)); 
    if (paramString2 != null)
      jWT.addClaim("cnf", Map.of("jkt", paramString2)); 
    Objects.requireNonNull(jWT);
    map.forEach(jWT::addClaim);
    UserRegistration userRegistration = paramUser.getRegistrationForApplication(paramRefreshToken.applicationId);
    applyLambda(paramApplication.lambdaConfiguration.accessTokenPopulateId, jWT, paramUser, userRegistration, paramSet);
    if (jWT.expiration.isAfter(zonedDateTime2)) {
      jWT.expiration = zonedDateTime2;
    } else if (jWT.expiration.truncatedTo(ChronoUnit.SECONDS).equals(zonedDateTime2.truncatedTo(ChronoUnit.SECONDS)) && paramInteger != null) {
      jWT.expiration = zonedDateTime1.plusSeconds(paramInteger.intValue()).truncatedTo(ChronoUnit.SECONDS);
    } 
    Signer signer = getSigner(paramApplication, jWTConfiguration.accessTokenKeyId, JWTType.AccessToken);
    AccessToken accessToken = new AccessToken();
    Object object2 = paramRefreshToken.data.get("grants");
    if (object2 instanceof List) {
      List<String> list1 = (List)object2;
      list1.add(GrantType.refresh_token.grantName());
    } 
    List list = (List)object2;
    jWT.addClaim("gty", (object2 instanceof List) ? list : null);
    accessToken.refreshTokenId = paramRefreshToken.id;
    accessToken.token = JWT.getEncoder().encode(jWT, signer, paramHeader -> paramHeader.set("kid", paramSigner.getKid()));
    if (!paramSet.isEmpty()) {
      if (paramSet.contains("openid")) {
        JWT jWT1 = createBaseJWT(str, zonedDateTime1, zonedDateTime2, AuthenticationType.REFRESH_TOKEN, paramUser, JWTType.IdToken, paramSet, paramApplication.oauthConfiguration.scopeHandlingPolicy);
        map.remove("applicationId");
        map.remove("roles");
        map.remove("sid");
        Object object = paramRefreshToken.data.get("ssoSessionId");
        if (object != null)
          map.put("sid", object); 
        Objects.requireNonNull(jWT1);
        map.forEach(jWT1::addClaim);
        jWT1.addClaim("at_hash", OpenIDConnect.at_hash(accessToken.token, signer.getAlgorithm()));
        applyLambda(paramApplication.lambdaConfiguration.idTokenPopulateId, jWT1, paramUser, userRegistration, paramSet);
        if (jWT1.expiration.isAfter(zonedDateTime2)) {
          jWT1.expiration = zonedDateTime2;
        } else if (jWT1.expiration.truncatedTo(ChronoUnit.SECONDS).equals(zonedDateTime2.truncatedTo(ChronoUnit.SECONDS)) && paramInteger != null) {
          jWT1.expiration = zonedDateTime1.plusSeconds(paramInteger.intValue()).truncatedTo(ChronoUnit.SECONDS);
        } 
        jWT1.addClaim("gty", object2);
        Signer signer1 = getSigner(paramApplication, jWTConfiguration.idTokenKeyId, JWTType.IdToken);
        accessToken.idToken = JWT.getEncoder().encode(jWT1, signer1, paramHeader -> paramHeader.set("kid", paramSigner.getKid()));
      } 
      accessToken.scope = String.join(" ", (Iterable)paramSet);
    } 
    accessToken.expiresIn = Integer.valueOf((int)zonedDateTime1.until(jWT.expiration, ChronoUnit.SECONDS));
    accessToken.tokenType = (paramString2 == null) ? TokenType.Bearer : TokenType.DPoP;
    accessToken.userId = paramRefreshToken.userId;
    this.loginService.updateActiveUserMetrics(paramUser.id, paramApplication.id);
    JWTRefreshEvent jWTRefreshEvent = new JWTRefreshEvent(paramEventInfo, paramApplication.id, accessToken.token, paramString1, paramRefreshToken.token, accessToken.userId);
    EventHelper.send(paramTenant, paramApplication, jWTRefreshEvent);
    return new JWTService.RefreshResult(accessToken, paramRefreshToken);
  }
  
  public boolean validateAccessTokenOwnershipForRefresh(RefreshToken paramRefreshToken, String paramString, Tenant paramTenant, Application paramApplication) {
    JWT jWT;
    if (paramTenant == null || paramApplication == null)
      return false; 
    try {
      ValidatedJWTResult validatedJWTResult1 = FusionAuthJWTDecoder.validateConstraints(paramString, FusionAuthJWTDecoder.JWTConstraints.UserAccessToken);
      if (!validatedJWTResult1.valid)
        return false; 
      jWT = validatedJWTResult1.jwt;
      UUID uUID = paramRefreshToken.userId;
      if (!uUID.toString().equals(jWT.subject))
        return false; 
      String str = paramRefreshToken.id.toString();
      if (!str.equals(jWT.getString("sid")))
        return false; 
    } catch (Exception exception1) {
      return false;
    } 
    JWTValidationContext.SuppliedTenantAndApplication suppliedTenantAndApplication = new JWTValidationContext.SuppliedTenantAndApplication(paramTenant, paramApplication);
    ValidatedJWTResult validatedJWTResult = validateJWT(paramString, FusionAuthJWTDecoder.JWTConstraints.UserAccessToken, suppliedTenantAndApplication);
    if (validatedJWTResult.valid)
      return true; 
    Exception exception = validatedJWTResult.exception;
    if (exception instanceof io.fusionauth.jwt.JWTExpiredException || exception instanceof io.fusionauth.jwt.JWTUnavailableForProcessingException) {
      ValidatedJWTResult validatedJWTResult1 = new ValidatedJWTResult();
      validatedJWTResult1.jwt = jWT;
      validateJWTContext(validatedJWTResult1, suppliedTenantAndApplication, false);
      return validatedJWTResult1.valid;
    } 
    return false;
  }
  
  public ValidatedJWTResult validateEntityJWT(String paramString, FusionAuthJWTDecoder.JWTConstraints paramJWTConstraints, Tenant paramTenant, @Nonnull Entity paramEntity) {
    ValidatedJWTResult validatedJWTResult = FusionAuthJWTDecoder.decode(paramString, (Map<String, Verifier>)this.verifierProvider.get(), paramJWTConstraints);
    if (!validatedJWTResult.valid || validatedJWTResult.jwt == null)
      return validatedJWTResult; 
    Optional.<Tenant>ofNullable(paramTenant)
      .or(() -> {
          Objects.requireNonNull(this.tenantReaderService);
          return ClaimTools.getTenantIdFromTidClaim(paramValidatedJWTResult.jwt).map(this.tenantReaderService::retrieveById);
        }).ifPresentOrElse(paramTenant -> applyClaimResult(paramValidatedJWTResult, this.jwtClaimValidator.validateForEntity(paramValidatedJWTResult.jwt, paramTenant, paramEntity)), () -> {
          paramValidatedJWTResult.valid = false;
          paramValidatedJWTResult.reason = "Unable to resolve a tenant for this token";
          paramValidatedJWTResult.reasonCode = ValidatedJWTResult.ReasonCode.claimMismatch;
        });
    return validatedJWTResult;
  }
  
  public ValidatedJWTResult validateIdTokenHint(String paramString) {
    JWT jWT;
    ValidatedJWTResult validatedJWTResult = new ValidatedJWTResult();
    boolean bool = false;
    try {
      validatedJWTResult = FusionAuthJWTDecoder.validateConstraints(paramString, FusionAuthJWTDecoder.JWTConstraints.OAuthUserIdToken);
      if (!validatedJWTResult.valid) {
        validatedJWTResult = attemptLegacyIdTokenValidation(paramString, validatedJWTResult);
        if (!validatedJWTResult.valid)
          return validatedJWTResult; 
        bool = true;
      } 
      jWT = validatedJWTResult.jwt;
    } catch (Exception exception1) {
      validatedJWTResult.exception = exception1;
      return validatedJWTResult;
    } 
    if (bool) {
      validatedJWTResult = FusionAuthJWTDecoder.decode(paramString, (Map<String, Verifier>)this.verifierProvider.get(), FusionAuthJWTDecoder.JWTConstraints.LegacyOAuthUserIdTokenForLogout);
      if (validatedJWTResult.valid)
        validatedJWTResult.legacyIdTokenHint = true; 
    } else {
      validatedJWTResult = validateJWTWithShadowKeyFallback(paramString, FusionAuthJWTDecoder.JWTConstraints.OAuthUserIdToken, new JWTValidationContext.AudienceApplicationRequired(TenantSource.SignedTid.INSTANCE));
    } 
    if (validatedJWTResult.valid)
      return validatedJWTResult; 
    Exception exception = validatedJWTResult.exception;
    if (exception instanceof io.fusionauth.jwt.JWTExpiredException) {
      ValidatedJWTResult validatedJWTResult1 = new ValidatedJWTResult();
      validatedJWTResult1.jwt = jWT;
      validatedJWTResult1.verifiedUsingClientSecretShadow = validatedJWTResult.verifiedUsingClientSecretShadow;
      if (!bool) {
        validateJWTContext(validatedJWTResult1, new JWTValidationContext.AudienceApplicationRequired(TenantSource.SignedTid.INSTANCE), validatedJWTResult1.verifiedUsingClientSecretShadow);
      } else {
        validatedJWTResult1.valid = true;
        validatedJWTResult1.legacyIdTokenHint = true;
      } 
      return validatedJWTResult1;
    } 
    return validatedJWTResult;
  }
  
  public JWTService.ValidationResult validateIssue(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2, String paramString, JWT paramJWT, HTTPRequest paramHTTPRequest) {
    Objects.requireNonNull(paramUUID2);
    JWTService.ValidationResult validationResult = this.refreshTokenService.validateRefreshToken(paramTenant, paramUUID2, paramUUID1, paramString, null, paramHTTPRequest);
    if (validationResult.errors != null && !validationResult.errors.empty())
      return validationResult; 
    if (validationResult.dPoPThumbprint == null) {
      DPoPService.DPoPResult dPoPResult = this.dpopService.parseDPoP(paramHTTPRequest);
      if (dPoPResult.error() != null) {
        validationResult.errors = new Errors();
        validationResult.errors.addGeneralError("[" + (dPoPResult.error()).error.toString() + "]", (dPoPResult.error()).description, new Object[0]);
        return validationResult;
      } 
      validationResult.dPoPThumbprint = dPoPResult.dPoPThumbprint();
    } 
    validationResult
      
      .errors = (new Validator()).notMissing(paramUUID1, "applicationId", new Object[0]).ifLastCheckHadNoError(paramValidator -> paramValidator.validObject(paramValidationResult.application, "applicationId", new Object[] { paramUUID })).done();
    if (validationResult.refreshToken != null) {
      String str = paramJWT.getString("sid");
      if (!validationResult.refreshToken.id.toString().equals(str))
        validationResult.refreshToken = null; 
    } 
    validationResult.jwtApplicationId = ClaimTools.resolveApplicationId(paramJWT);
    if (validationResult.jwtApplicationId != null) {
      Objects.requireNonNull(this.applicationReader);
      validationResult.jwtApplication = this.applicationCache.get((paramTenant == null) ? null : paramTenant.id, validationResult.jwtApplicationId, this.applicationReader::retrieveById);
    } 
    return validationResult;
  }
  
  public ValidatedJWTResult validateJWT(String paramString, FusionAuthJWTDecoder.JWTConstraints paramJWTConstraints, @Nonnull JWTValidationContext paramJWTValidationContext) {
    return validateJWTWithShadowKeyFallback(paramString, paramJWTConstraints, paramJWTValidationContext);
  }
  
  public JWTService.ValidationResult validateRefresh(Tenant paramTenant, String paramString1, HTTPRequest paramHTTPRequest, String paramString2, Integer paramInteger) {
    JWTService.ValidationResult validationResult = new JWTService.ValidationResult();
    validationResult.refreshToken = (paramString1 == null) ? null : this.refreshTokenService.retrieveRefreshTokenUsingSeed(paramString1);
    if (validationResult.refreshToken != null) {
      DPoPService.DPoPResult dPoPResult = this.dpopService.parseDPoPRefreshToken(paramHTTPRequest, validationResult.refreshToken);
      if (dPoPResult.error() != null) {
        validationResult.errors = new Errors();
        validationResult.errors.addGeneralError("[" + (dPoPResult.error()).error.toString() + "]", (dPoPResult.error()).description, new Object[0]);
        return validationResult;
      } 
      validationResult.dPoPThumbprint = dPoPResult.dPoPThumbprint();
      if (validationResult.refreshToken.applicationId == null)
        validationResult.refreshToken = null; 
    } 
    if (validationResult.refreshToken != null) {
      validationResult.application = this.applicationMapper.retrieveById((paramTenant != null) ? paramTenant.id : null, validationResult.refreshToken.applicationId);
      validationResult.tenant = this.tenantReaderService.resolve(paramTenant, new Tenantable[] { validationResult.application });
      if (validationResult.tenant != null)
        if (validationResult.refreshToken.isExpired(validationResult.tenant, validationResult.application)) {
          validationResult.refreshToken = null;
        } else {
          validationResult.user = this.userReader.retrieveById(validationResult.tenant.id, validationResult.refreshToken.userId);
        }  
    } 
    validationResult



      
      .errors = (new Validator()).notMissing(paramString1, "refreshToken", new Object[0]).ifLastCheckHadNoError(paramValidator -> paramValidator.validObject(paramValidationResult.refreshToken, "refreshToken", new Object[0])).ifTrue((validationResult.tenant != null && paramInteger != null), paramValidator -> paramValidator.ensure((paramInteger.intValue() <= (paramValidationResult.tenant.lookupJWTConfiguration(paramValidationResult.application)).timeToLiveInSeconds), "timeToLiveInSeconds", "[invalid]", new Object[] { Integer.valueOf((paramValidationResult.tenant.lookupJWTConfiguration(paramValidationResult.application)).timeToLiveInSeconds) })).ifTrue((validationResult.application != null), paramValidator -> paramValidator.ensure(paramValidationResult.application.loginConfiguration.allowTokenRefresh, "refreshToken", "[disabled]", new Object[0])).done();
    if (validationResult.errors.empty() && validationResult.application != null && validationResult.refreshToken != null && 
      !validationResult.application.oauthConfiguration.authorizedResourceUris.isEmpty())
      validationResult
        .resolvedResources = Optional.<List<URI>>ofNullable(validationResult.refreshToken.metaData.resources).orElse(List.of()); 
    if (validationResult.errors.empty() && paramString2 != null && validationResult.user != null)
      if (validateAccessTokenOwnershipForRefresh(validationResult.refreshToken, paramString2, validationResult.tenant, validationResult.application))
        validationResult.accessToken = paramString2;  
    return validationResult;
  }
  
  public JWTService.ValidationResult validateRetrieve(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2) {
    JWTService.ValidationResult validationResult = new JWTService.ValidationResult();
    validationResult.user = (paramUUID2 != null) ? this.userReader.retrieveById((paramTenant != null) ? paramTenant.id : null, paramUUID2) : null;
    validationResult.tenant = this.tenantReaderService.resolve(paramTenant, new Tenantable[] { validationResult.user });
    validationResult.refreshToken = (paramUUID1 != null) ? this.refreshTokenService.retrieveRefreshTokenById(paramUUID1) : null;
    if (validationResult.refreshToken != null)
      validationResult.application = this.applicationMapper.retrieveById((validationResult.tenant != null) ? validationResult.tenant.id : null, validationResult.refreshToken.applicationId); 
    if (validationResult.tenant == null && validationResult.refreshToken != null) {
      UUID uUID = this.tenantReaderService.retrieveApplicationTenantId(validationResult.refreshToken.applicationId);
      validationResult.tenant = this.tenantReaderService.retrieveById(uUID);
    } 
    validationResult






      
      .errors = (new Validator()).ifTrue((paramUUID1 == null), paramValidator -> paramValidator.notMissing(paramUUID, "userId", new Object[0])).ifTrue((paramUUID2 != null), paramValidator -> paramValidator.validObject(paramValidationResult.user, "userId", new Object[] { paramUUID })).done();
    return validationResult;
  }
  
  public JWTService.VendValidationResult validateVend(Tenant paramTenant, UUID paramUUID, int paramInt) {
    JWTService.VendValidationResult vendValidationResult = new JWTService.VendValidationResult();
    vendValidationResult.key = (paramUUID != null) ? this.keyReader.retrieveById(paramUUID) : this.keyReader.retrieveById(paramTenant.jwtConfiguration.accessTokenKeyId);
    vendValidationResult.timeToLiveInSeconds = (paramInt != 0) ? paramInt : paramTenant.jwtConfiguration.timeToLiveInSeconds;
    vendValidationResult




      
      .errors = (new Validator()).ifTrue((paramUUID != null), paramValidator -> paramValidator.validObject(paramVendValidationResult.key, "keyId", new Object[] { paramUUID }).ifNoFieldErrors("keyId", ())).done();
    return vendValidationResult;
  }
  
  private void applyClaimResult(ValidatedJWTResult paramValidatedJWTResult, JWTClaimValidator.ClaimValidationResult paramClaimValidationResult) {
    // Byte code:
    //   0: aload_2
    //   1: dup
    //   2: invokestatic requireNonNull : (Ljava/lang/Object;)Ljava/lang/Object;
    //   5: pop
    //   6: astore_3
    //   7: iconst_0
    //   8: istore #4
    //   10: aload_3
    //   11: iload #4
    //   13: <illegal opcode> typeSwitch : (Lio/fusionauth/api/service/jwt/JWTClaimValidator$ClaimValidationResult;I)I
    //   18: tableswitch default -> 44, 0 -> 54, 1 -> 76, 2 -> 114
    //   44: new java/lang/MatchException
    //   47: dup
    //   48: aconst_null
    //   49: aconst_null
    //   50: invokespecial <init> : (Ljava/lang/String;Ljava/lang/Throwable;)V
    //   53: athrow
    //   54: aload_1
    //   55: getstatic io/fusionauth/api/service/jwt/ValidatedJWTResult$ReasonCode.invalidAudience : Lio/fusionauth/api/service/jwt/ValidatedJWTResult$ReasonCode;
    //   58: putfield reasonCode : Lio/fusionauth/api/service/jwt/ValidatedJWTResult$ReasonCode;
    //   61: aload_1
    //   62: ldc_w 'The [aud] claim is invalid.'
    //   65: putfield reason : Ljava/lang/String;
    //   68: aload_1
    //   69: iconst_0
    //   70: putfield valid : Z
    //   73: goto -> 119
    //   76: aload_3
    //   77: checkcast io/fusionauth/api/service/jwt/JWTClaimValidator$ClaimValidationResult$Mismatch
    //   80: astore #5
    //   82: aload #5
    //   84: invokevirtual reason : ()Ljava/lang/String;
    //   87: astore #7
    //   89: aload #7
    //   91: astore #6
    //   93: aload_1
    //   94: getstatic io/fusionauth/api/service/jwt/ValidatedJWTResult$ReasonCode.claimMismatch : Lio/fusionauth/api/service/jwt/ValidatedJWTResult$ReasonCode;
    //   97: putfield reasonCode : Lio/fusionauth/api/service/jwt/ValidatedJWTResult$ReasonCode;
    //   100: aload_1
    //   101: aload #6
    //   103: putfield reason : Ljava/lang/String;
    //   106: aload_1
    //   107: iconst_0
    //   108: putfield valid : Z
    //   111: goto -> 119
    //   114: aload_1
    //   115: iconst_1
    //   116: putfield valid : Z
    //   119: goto -> 136
    //   122: astore_3
    //   123: new java/lang/MatchException
    //   126: dup
    //   127: aload_3
    //   128: invokevirtual toString : ()Ljava/lang/String;
    //   131: aload_3
    //   132: invokespecial <init> : (Ljava/lang/String;Ljava/lang/Throwable;)V
    //   135: athrow
    //   136: return
    // Line number table:
    //   Java source line number -> byte code offset
    //   #798	-> 0
    //   #800	-> 54
    //   #801	-> 61
    //   #802	-> 68
    //   #803	-> 73
    //   #804	-> 76
    //   #805	-> 93
    //   #806	-> 100
    //   #807	-> 106
    //   #808	-> 111
    //   #809	-> 114
    //   #811	-> 136
    // Exception table:
    //   from	to	target	type
    //   84	87	122	java/lang/Throwable
  }
  
  private void applyLambda(UUID paramUUID, JWT paramJWT, Entity paramEntity, Map<String, Entity> paramMap, Map<String, Set<String>> paramMap1) {
    if (paramUUID == null)
      return; 
    Map<String, Object> map = paramJWT.getRawClaims();
    try {
      this.lambdaInvocationService.invoke(paramUUID, new LambdaArgument[] { new MutableLambdaArgument(map), new ImmutableLambdaArgument(paramEntity), new ImmutableLambdaArgument(paramMap), new ImmutableLambdaArgument(paramMap1) });
    } catch (LambdaInvocationException lambdaInvocationException) {
      if (logger.isDebugEnabled()) {
        logger.debug("Failed to apply lambda during JWT creation for client credentials grant.", (Throwable)lambdaInvocationException);
      } else {
        logger.error("Failed to apply lambda during JWT creation for client credentials grant. See Event Log for additional details.");
      } 
      return;
    } 
    mergeClaims(paramJWT, map, ClientCredentialsReservedClaims);
  }
  
  private void applyLambda(UUID paramUUID, JWT paramJWT, User paramUser, UserRegistration paramUserRegistration, Set<String> paramSet) {
    if (paramUUID == null)
      return; 
    Map map = Map.of("scopes", paramSet);
    Map<String, Object> map1 = paramJWT.getRawClaims();
    try {
      this.lambdaInvocationService.invoke(paramUUID, new LambdaArgument[] { new MutableLambdaArgument(map1), new ImmutableLambdaArgument((new User(paramUser))
              
              .secure()), new ImmutableLambdaArgument(paramUserRegistration), new ImmutableLambdaArgument(map, true) });
    } catch (LambdaInvocationException lambdaInvocationException) {
      if (logger.isDebugEnabled()) {
        logger.debug("Failed to apply lambda during JWT creation.", (Throwable)lambdaInvocationException);
      } else {
        logger.error("Failed to apply lambda during JWT creation. See Event Log for additional details");
      } 
      return;
    } 
    mergeClaims(paramJWT, map1, ReservedClaims);
  }
  
  private void applyUnresolvedContext(ValidatedJWTResult paramValidatedJWTResult) {
    paramValidatedJWTResult.valid = false;
    paramValidatedJWTResult.reason = "Unable to resolve the JWT validation context.";
    paramValidatedJWTResult.reasonCode = ValidatedJWTResult.ReasonCode.unresolvedContext;
  }
  
  private ValidatedJWTResult attemptLegacyIdTokenValidation(String paramString, ValidatedJWTResult paramValidatedJWTResult) {
    if (paramValidatedJWTResult == null || paramValidatedJWTResult.jwt == null)
      return paramValidatedJWTResult; 
    if (paramValidatedJWTResult.jwt.getObject("tty") != null)
      return paramValidatedJWTResult; 
    if (paramValidatedJWTResult.jwt.getObject("gty") != null)
      return paramValidatedJWTResult; 
    ValidatedJWTResult validatedJWTResult = FusionAuthJWTDecoder.validateConstraints(paramString, FusionAuthJWTDecoder.JWTConstraints.LegacyOAuthUserIdTokenForLogout);
    if (!validatedJWTResult.valid)
      return validatedJWTResult; 
    return validatedJWTResult;
  }
  
  private ValidatedJWTResult attemptShadowKeyIdTokenValidation(String paramString, FusionAuthJWTDecoder.JWTConstraints paramJWTConstraints, Tenant paramTenant, Application paramApplication, ValidatedJWTResult paramValidatedJWTResult) {
    JWT jWT;
    try {
      jWT = FusionAuthJWTDecoder.unsafeDecode(paramString);
    } catch (Exception exception) {
      return paramValidatedJWTResult;
    } 
    if (!JWTType.is(jWT, JWTType.IdToken))
      return paramValidatedJWTResult; 
    Tenant tenant = paramTenant;
    if (tenant == null) {
      Objects.requireNonNull(this.tenantReaderService);
      tenant = ClaimTools.getTenantIdFromTidClaim(jWT).<Tenant>map(this.tenantReaderService::retrieveById).orElse((Tenant)null);
    } 
    if (tenant == null)
      return paramValidatedJWTResult; 
    Optional<UUID> optional = ClaimTools.getAppIdFromKidClaim(jWT);
    if (optional.isEmpty())
      return paramValidatedJWTResult; 
    Application application = paramApplication;
    if (application == null) {
      Objects.requireNonNull(this.applicationReader);
      application = this.applicationCache.get(tenant.id, optional.get(), this.applicationReader::retrieveById);
    } else if (!application.id.equals(optional.get())) {
      return paramValidatedJWTResult;
    } 
    if (application == null || application.oauthConfiguration.clientSecret == null)
      return paramValidatedJWTResult; 
    Map<String, Verifier> map = Map.of(application.id.toString(), HMACVerifier.newVerifier(application.oauthConfiguration.clientSecret));
    return validateJWT(paramString, paramJWTConstraints, tenant, application, map, true);
  }
  
  private ValidatedJWTResult attemptShadowKeyIdTokenValidation(String paramString, FusionAuthJWTDecoder.JWTConstraints paramJWTConstraints, JWTValidationContext paramJWTValidationContext, ValidatedJWTResult paramValidatedJWTResult) {
    // Byte code:
    //   0: aload_1
    //   1: invokestatic unsafeDecode : (Ljava/lang/String;)Lio/fusionauth/jwt/domain/JWT;
    //   4: astore #5
    //   6: goto -> 14
    //   9: astore #6
    //   11: aload #4
    //   13: areturn
    //   14: aload #5
    //   16: getstatic io/fusionauth/api/service/jwt/claims/JWTType.IdToken : Lio/fusionauth/api/service/jwt/claims/JWTType;
    //   19: invokestatic is : (Lio/fusionauth/jwt/domain/JWT;Lio/fusionauth/api/service/jwt/claims/JWTType;)Z
    //   22: ifne -> 28
    //   25: aload #4
    //   27: areturn
    //   28: aload_3
    //   29: dup
    //   30: invokestatic requireNonNull : (Ljava/lang/Object;)Ljava/lang/Object;
    //   33: pop
    //   34: astore #7
    //   36: iconst_0
    //   37: istore #8
    //   39: aload #7
    //   41: iload #8
    //   43: <illegal opcode> typeSwitch : (Lio/fusionauth/api/service/jwt/JWTValidationContext;I)I
    //   48: tableswitch default -> 76, 0 -> 86, 1 -> 109, 2 -> 138
    //   76: new java/lang/MatchException
    //   79: dup
    //   80: aconst_null
    //   81: aconst_null
    //   82: invokespecial <init> : (Ljava/lang/String;Ljava/lang/Throwable;)V
    //   85: athrow
    //   86: aload #7
    //   88: checkcast io/fusionauth/api/service/jwt/JWTValidationContext$SuppliedTenantAudienceApplicationIfResolvable
    //   91: astore #9
    //   93: aload #9
    //   95: invokevirtual tenant : ()Lio/fusionauth/domain/Tenant;
    //   98: astore #11
    //   100: aload #11
    //   102: astore #10
    //   104: aload #10
    //   106: goto -> 165
    //   109: aload #7
    //   111: checkcast io/fusionauth/api/service/jwt/JWTValidationContext$AudienceApplicationRequired
    //   114: astore #11
    //   116: aload #11
    //   118: invokevirtual tenantSource : ()Lio/fusionauth/api/service/jwt/TenantSource;
    //   121: astore #13
    //   123: aload #13
    //   125: astore #12
    //   127: aload_0
    //   128: aload #5
    //   130: aload #12
    //   132: invokevirtual resolveTenant : (Lio/fusionauth/jwt/domain/JWT;Lio/fusionauth/api/service/jwt/TenantSource;)Lio/fusionauth/domain/Tenant;
    //   135: goto -> 165
    //   138: aload #7
    //   140: checkcast io/fusionauth/api/service/jwt/JWTValidationContext$SuppliedTenantAndApplication
    //   143: astore #13
    //   145: aload #13
    //   147: invokevirtual tenant : ()Lio/fusionauth/domain/Tenant;
    //   150: astore #15
    //   152: aload #15
    //   154: astore #14
    //   156: aload #13
    //   158: invokevirtual application : ()Lio/fusionauth/domain/Application;
    //   161: astore #15
    //   163: aload #14
    //   165: astore #6
    //   167: aload #6
    //   169: ifnonnull -> 175
    //   172: aload #4
    //   174: areturn
    //   175: aload #5
    //   177: invokestatic getAppIdFromKidClaim : (Lio/fusionauth/jwt/domain/JWT;)Ljava/util/Optional;
    //   180: astore #7
    //   182: aload #7
    //   184: invokevirtual isEmpty : ()Z
    //   187: ifeq -> 193
    //   190: aload #4
    //   192: areturn
    //   193: aload_3
    //   194: dup
    //   195: invokestatic requireNonNull : (Ljava/lang/Object;)Ljava/lang/Object;
    //   198: pop
    //   199: astore #9
    //   201: iconst_0
    //   202: istore #10
    //   204: aload #9
    //   206: iload #10
    //   208: <illegal opcode> typeSwitch : (Lio/fusionauth/api/service/jwt/JWTValidationContext;I)I
    //   213: lookupswitch default -> 282, 0 -> 232
    //   232: aload #9
    //   234: checkcast io/fusionauth/api/service/jwt/JWTValidationContext$SuppliedTenantAndApplication
    //   237: astore #11
    //   239: aload #11
    //   241: invokevirtual tenant : ()Lio/fusionauth/domain/Tenant;
    //   244: astore #13
    //   246: aload #11
    //   248: invokevirtual application : ()Lio/fusionauth/domain/Application;
    //   251: astore #13
    //   253: aload #13
    //   255: astore #12
    //   257: aload #12
    //   259: getfield id : Ljava/util/UUID;
    //   262: aload #7
    //   264: invokevirtual get : ()Ljava/lang/Object;
    //   267: invokevirtual equals : (Ljava/lang/Object;)Z
    //   270: ifeq -> 278
    //   273: aload #12
    //   275: goto -> 316
    //   278: aconst_null
    //   279: goto -> 316
    //   282: aload_0
    //   283: getfield applicationCache : Lio/fusionauth/api/service/cache/ApplicationCache;
    //   286: aload #6
    //   288: getfield id : Ljava/util/UUID;
    //   291: aload #7
    //   293: invokevirtual get : ()Ljava/lang/Object;
    //   296: checkcast java/util/UUID
    //   299: aload_0
    //   300: getfield applicationReader : Lio/fusionauth/api/service/system/ApplicationReaderService;
    //   303: dup
    //   304: invokestatic requireNonNull : (Ljava/lang/Object;)Ljava/lang/Object;
    //   307: pop
    //   308: <illegal opcode> apply : (Lio/fusionauth/api/service/system/ApplicationReaderService;)Ljava/util/function/BiFunction;
    //   313: invokevirtual get : (Ljava/util/UUID;Ljava/util/UUID;Ljava/util/function/BiFunction;)Lio/fusionauth/domain/Application;
    //   316: astore #8
    //   318: aload #8
    //   320: ifnull -> 334
    //   323: aload #8
    //   325: getfield oauthConfiguration : Lio/fusionauth/domain/oauth2/OAuth2Configuration;
    //   328: getfield clientSecret : Ljava/lang/String;
    //   331: ifnonnull -> 337
    //   334: aload #4
    //   336: areturn
    //   337: aload #8
    //   339: getfield id : Ljava/util/UUID;
    //   342: invokevirtual toString : ()Ljava/lang/String;
    //   345: aload #8
    //   347: getfield oauthConfiguration : Lio/fusionauth/domain/oauth2/OAuth2Configuration;
    //   350: getfield clientSecret : Ljava/lang/String;
    //   353: invokestatic newVerifier : (Ljava/lang/String;)Lio/fusionauth/jwt/hmac/HMACVerifier;
    //   356: invokestatic of : (Ljava/lang/Object;Ljava/lang/Object;)Ljava/util/Map;
    //   359: astore #9
    //   361: aload_0
    //   362: aload_1
    //   363: aload_2
    //   364: aload_3
    //   365: aload #9
    //   367: iconst_1
    //   368: invokevirtual validateJWT : (Ljava/lang/String;Lio/fusionauth/api/service/jwt/FusionAuthJWTDecoder$JWTConstraints;Lio/fusionauth/api/service/jwt/JWTValidationContext;Ljava/util/Map;Z)Lio/fusionauth/api/service/jwt/ValidatedJWTResult;
    //   371: areturn
    //   372: astore #10
    //   374: new java/lang/MatchException
    //   377: dup
    //   378: aload #10
    //   380: invokevirtual toString : ()Ljava/lang/String;
    //   383: aload #10
    //   385: invokespecial <init> : (Ljava/lang/String;Ljava/lang/Throwable;)V
    //   388: athrow
    // Line number table:
    //   Java source line number -> byte code offset
    //   #959	-> 0
    //   #962	-> 6
    //   #960	-> 9
    //   #961	-> 11
    //   #964	-> 14
    //   #965	-> 25
    //   #968	-> 28
    //   #969	-> 86
    //   #970	-> 109
    //   #971	-> 138
    //   #972	-> 165
    //   #973	-> 167
    //   #974	-> 172
    //   #977	-> 175
    //   #978	-> 182
    //   #979	-> 190
    //   #983	-> 193
    //   #984	-> 232
    //   #985	-> 282
    //   #986	-> 316
    //   #987	-> 318
    //   #988	-> 334
    //   #991	-> 337
    //   #992	-> 361
    //   #984	-> 372
    // Exception table:
    //   from	to	target	type
    //   0	6	9	java/lang/Exception
    //   95	98	372	java/lang/Throwable
    //   118	121	372	java/lang/Throwable
    //   147	150	372	java/lang/Throwable
    //   158	161	372	java/lang/Throwable
    //   241	244	372	java/lang/Throwable
    //   248	251	372	java/lang/Throwable
  }
  
  private JWT createBaseJWT(String paramString, ZonedDateTime paramZonedDateTime1, ZonedDateTime paramZonedDateTime2, AuthenticationType paramAuthenticationType, User paramUser, JWTType paramJWTType, Set<String> paramSet, OAuthScopeHandlingPolicy paramOAuthScopeHandlingPolicy) {
    JWT jWT = (new JWT()).setIssuer(paramString).setIssuedAt(paramZonedDateTime1).setExpiration(paramZonedDateTime2).setSubject(paramUser.id.toString()).setUniqueId(UUID.randomUUID().toString()).addClaim("authenticationType", paramAuthenticationType).addClaim("tty", paramJWTType.value());
    UserIdentity userIdentity = paramUser.resolvePrimaryIdentity(IdentityType.email);
    String str = (userIdentity != null) ? userIdentity.value : null;
    Boolean bool = (userIdentity != null) ? Boolean.valueOf(!userIdentity.verificationRequired()) : null;
    if (paramOAuthScopeHandlingPolicy.equals(OAuthScopeHandlingPolicy.Compatibility)) {
      jWT.addClaim("email", str)
        .addClaim("email_verified", bool)
        .addClaim("preferred_username", paramUser.username);
    } else if (paramOAuthScopeHandlingPolicy.equals(OAuthScopeHandlingPolicy.Strict) && paramJWTType == JWTType.IdToken) {
      if (paramSet.contains("email"))
        jWT.addClaim("email", str)
          .addClaim("email_verified", bool); 
      if (paramSet.contains("phone")) {
        UserIdentity userIdentity1 = paramUser.resolvePrimaryIdentity(IdentityType.phoneNumber);
        jWT.addClaim("phone_number", Optional.<UserIdentity>ofNullable(userIdentity1)
            .map(paramUserIdentity -> paramUserIdentity.value).orElse(paramUser.mobilePhone));
        if (userIdentity1 != null)
          jWT.addClaim("phone_number_verified", Boolean.valueOf(!userIdentity1.verificationRequired())); 
      } 
      if (paramSet.contains("profile")) {
        jWT.addClaim("given_name", paramUser.firstName)
          .addClaim("middle_name", paramUser.middleName)
          .addClaim("family_name", paramUser.lastName)
          .addClaim("name", paramUser.fullName)
          .addClaim("preferred_username", paramUser.username);
        if (paramUser.birthDate != null)
          jWT.addClaim("birthdate", paramUser.birthDate.format(DateTimeFormatter.ISO_LOCAL_DATE)); 
        if (paramUser.imageUrl != null)
          jWT.addClaim("picture", paramUser.imageUrl.toString()); 
        if (!paramUser.preferredLanguages.isEmpty())
          jWT.addClaim("locale", ((Locale)paramUser.preferredLanguages.get(0)).toLanguageTag()); 
        if (paramUser.timezone != null)
          jWT.addClaim("zoneinfo", paramUser.timezone.getId()); 
      } 
    } 
    return jWT;
  }
  
  private JWTService.JWTResult generateJWTWithExpiration(Tenant paramTenant, User paramUser, AuthenticationType paramAuthenticationType, Application paramApplication, Map<String, Object> paramMap, ZonedDateTime paramZonedDateTime, UUID paramUUID, GrantType paramGrantType, JWTType paramJWTType, Set<String> paramSet, @Nullable String paramString) {
    Objects.requireNonNull(paramUser);
    Objects.requireNonNull(paramAuthenticationType);
    Objects.requireNonNull(paramJWTType);
    ZonedDateTime zonedDateTime1 = ZonedDateTime.now(ZoneOffset.UTC);
    JWTConfiguration jWTConfiguration = paramTenant.lookupJWTConfiguration(paramApplication);
    int i = jWTConfiguration.timeToLiveInSeconds;
    ZonedDateTime zonedDateTime2 = zonedDateTime1.plusSeconds(i);
    if (paramZonedDateTime != null && paramZonedDateTime.isBefore(zonedDateTime2))
      zonedDateTime2 = paramZonedDateTime; 
    String str = paramTenant.issuer;
    JWT jWT = createBaseJWT(str, zonedDateTime1, zonedDateTime2, paramAuthenticationType, paramUser, paramJWTType, paramSet, (paramApplication != null) ? paramApplication.oauthConfiguration.scopeHandlingPolicy : OAuthScopeHandlingPolicy.Compatibility);
    if (paramMap != null) {
      Objects.requireNonNull(jWT);
      paramMap.forEach(jWT::addClaim);
    } 
    if (paramMap == null || !paramMap.containsKey("auth_time"))
      jWT.addClaim("auth_time", Long.valueOf(zonedDateTime1.toEpochSecond())); 
    jWT.addClaim("tid", paramTenant.id);
    jWT.addClaim("gty", (paramGrantType != null) ? List.of(paramGrantType.grantName()) : null);
    if (paramString != null)
      jWT.addClaim("cnf", Map.of("jkt", paramString)); 
    UUID uUID1 = (paramApplication == null) ? null : paramApplication.id;
    UserRegistration userRegistration = paramUser.getRegistrationForApplication(uUID1);
    applyLambda(paramUUID, jWT, paramUser, userRegistration, paramSet);
    if (jWT.expiration.isAfter(zonedDateTime2))
      jWT.expiration = zonedDateTime2; 
    UUID uUID2 = (paramJWTType == JWTType.AccessToken) ? jWTConfiguration.accessTokenKeyId : jWTConfiguration.idTokenKeyId;
    Signer signer = getSigner(paramApplication, uUID2, paramJWTType);
    return new JWTService.JWTResult(jWT, JWT.getEncoder().encode(jWT, signer, paramHeader -> paramHeader.set("kid", paramSigner.getKid())), signer
        .getAlgorithm());
  }
  
  private ZonedDateTime getLastRotated(RefreshToken paramRefreshToken) {
    Number number = (Number)paramRefreshToken.data.get("lastRotated");
    return (number != null) ? 
      ZonedDateTime.ofInstant(Instant.ofEpochSecond(number.longValue()), ZoneOffset.UTC) : 
      null;
  }
  
  private Signer getSigner(Application paramApplication, UUID paramUUID, JWTType paramJWTType) {
    Key key = this.keyReader.retrieveById(paramUUID);
    if (paramJWTType == JWTType.IdToken && KeyService.ClientSecretShadowKeys.contains(key.id)) {
      if (paramApplication == null)
        throw new IllegalStateException("The application cannot be null when requesting an Id Token."); 
      key.secret = paramApplication.oauthConfiguration.clientSecret;
      key.kid = paramApplication.id.toString();
    } 
    return JWTHelper.buildSigner(key);
  }
  
  private void mergeClaims(JWT paramJWT, Map<String, Object> paramMap, Set<String> paramSet) {
    paramMap.keySet().forEach(paramString -> {
          if (!paramSet.contains(paramString)) {
            Object object = paramMap.get(paramString);
            if (object == null) {
              switch (paramString) {
                case "aud":
                  paramJWT.audience = null;
                  return;
                case "iss":
                  paramJWT.issuer = null;
                  return;
                case "jti":
                  paramJWT.uniqueId = null;
                  return;
              } 
              paramJWT.otherClaims.remove(paramString);
            } else {
              paramJWT.addClaim(paramString, object);
            } 
          } 
        });
  }
  
  private Application resolveAudienceApplication(JWT paramJWT, Tenant paramTenant) {
    UUID uUID = ClaimTools.resolveApplicationId(paramJWT);
    Objects.requireNonNull(this.applicationReader);
    return (uUID == null) ? null : this.applicationCache.get(paramTenant.id, uUID, this.applicationReader::retrieveById);
  }
  
  private Tenant resolveTenant(JWT paramJWT, TenantSource paramTenantSource) {
    // Byte code:
    //   0: aload_2
    //   1: dup
    //   2: invokestatic requireNonNull : (Ljava/lang/Object;)Ljava/lang/Object;
    //   5: pop
    //   6: astore_3
    //   7: iconst_0
    //   8: istore #4
    //   10: aload_3
    //   11: iload #4
    //   13: <illegal opcode> typeSwitch : (Lio/fusionauth/api/service/jwt/TenantSource;I)I
    //   18: lookupswitch default -> 44, 0 -> 54, 1 -> 76
    //   44: new java/lang/MatchException
    //   47: dup
    //   48: aconst_null
    //   49: aconst_null
    //   50: invokespecial <init> : (Ljava/lang/String;Ljava/lang/Throwable;)V
    //   53: athrow
    //   54: aload_3
    //   55: checkcast io/fusionauth/api/service/jwt/TenantSource$Supplied
    //   58: astore #5
    //   60: aload #5
    //   62: invokevirtual tenant : ()Lio/fusionauth/domain/Tenant;
    //   65: astore #7
    //   67: aload #7
    //   69: astore #6
    //   71: aload #6
    //   73: goto -> 96
    //   76: aload_1
    //   77: invokestatic getTenantIdFromTidClaim : (Lio/fusionauth/jwt/domain/JWT;)Ljava/util/Optional;
    //   80: aload_0
    //   81: <illegal opcode> apply : (Lio/fusionauth/api/service/jwt/DefaultJWTService;)Ljava/util/function/Function;
    //   86: invokevirtual map : (Ljava/util/function/Function;)Ljava/util/Optional;
    //   89: aconst_null
    //   90: invokevirtual orElse : (Ljava/lang/Object;)Ljava/lang/Object;
    //   93: checkcast io/fusionauth/domain/Tenant
    //   96: areturn
    //   97: astore_3
    //   98: new java/lang/MatchException
    //   101: dup
    //   102: aload_3
    //   103: invokevirtual toString : ()Ljava/lang/String;
    //   106: aload_3
    //   107: invokespecial <init> : (Ljava/lang/String;Ljava/lang/Throwable;)V
    //   110: athrow
    // Line number table:
    //   Java source line number -> byte code offset
    //   #1192	-> 0
    //   #1193	-> 54
    //   #1194	-> 76
    //   #1195	-> 86
    //   #1196	-> 90
    //   #1192	-> 96
    //   #1194	-> 97
    // Exception table:
    //   from	to	target	type
    //   62	65	97	java/lang/Throwable
  }
  
  private ValidatedJWTResult selectShadowKeyValidationResult(ValidatedJWTResult paramValidatedJWTResult1, ValidatedJWTResult paramValidatedJWTResult2) {
    if (paramValidatedJWTResult2.jwt != null || paramValidatedJWTResult2.exception instanceof io.fusionauth.jwt.JWTExpiredException || paramValidatedJWTResult2.exception instanceof io.fusionauth.jwt.JWTUnavailableForProcessingException) {
      paramValidatedJWTResult2.verifiedUsingClientSecretShadow = true;
      return paramValidatedJWTResult2;
    } 
    return paramValidatedJWTResult1;
  }
  
  private boolean shouldAttemptShadowKeyIdTokenValidation(String paramString, ValidatedJWTResult paramValidatedJWTResult, Map<String, Verifier> paramMap) {
    if (paramValidatedJWTResult.exception instanceof io.fusionauth.jwt.MissingVerifierException)
      return true; 
    if (paramValidatedJWTResult.exception == null)
      return false; 
    try {
      JWT jWT = FusionAuthJWTDecoder.unsafeDecode(paramString);
      String str = (jWT.header == null) ? null : jWT.header.getString("kid");
      return (JWTType.is(jWT, JWTType.IdToken) && ClaimTools.getAppIdFromKidClaim(jWT).isPresent() && paramMap.containsKey(str));
    } catch (Exception exception) {
      return false;
    } 
  }
  
  private ValidatedJWTResult validateJWT(String paramString, FusionAuthJWTDecoder.JWTConstraints paramJWTConstraints, Tenant paramTenant, Application paramApplication, Map<String, Verifier> paramMap, boolean paramBoolean) {
    ValidatedJWTResult validatedJWTResult = FusionAuthJWTDecoder.decode(paramString, paramMap, paramJWTConstraints);
    if (!validatedJWTResult.valid || validatedJWTResult.jwt == null)
      return validatedJWTResult; 
    validateJWTContext(validatedJWTResult, paramTenant, paramApplication, paramBoolean);
    return validatedJWTResult;
  }
  
  private ValidatedJWTResult validateJWT(String paramString, FusionAuthJWTDecoder.JWTConstraints paramJWTConstraints, JWTValidationContext paramJWTValidationContext, Map<String, Verifier> paramMap, boolean paramBoolean) {
    ValidatedJWTResult validatedJWTResult = FusionAuthJWTDecoder.decode(paramString, paramMap, paramJWTConstraints);
    if (!validatedJWTResult.valid || validatedJWTResult.jwt == null)
      return validatedJWTResult; 
    validateJWTContext(validatedJWTResult, paramJWTValidationContext, paramBoolean);
    return validatedJWTResult;
  }
  
  private void validateJWTContext(ValidatedJWTResult paramValidatedJWTResult, JWTValidationContext paramJWTValidationContext, boolean paramBoolean) {
    // Byte code:
    //   0: aload_2
    //   1: dup
    //   2: invokestatic requireNonNull : (Ljava/lang/Object;)Ljava/lang/Object;
    //   5: pop
    //   6: astore #5
    //   8: iconst_0
    //   9: istore #6
    //   11: aload #5
    //   13: iload #6
    //   15: <illegal opcode> typeSwitch : (Lio/fusionauth/api/service/jwt/JWTValidationContext;I)I
    //   20: tableswitch default -> 48, 0 -> 58, 1 -> 110, 2 -> 195
    //   48: new java/lang/MatchException
    //   51: dup
    //   52: aconst_null
    //   53: aconst_null
    //   54: invokespecial <init> : (Ljava/lang/String;Ljava/lang/Throwable;)V
    //   57: athrow
    //   58: aload #5
    //   60: checkcast io/fusionauth/api/service/jwt/JWTValidationContext$SuppliedTenantAndApplication
    //   63: astore #7
    //   65: aload #7
    //   67: invokevirtual tenant : ()Lio/fusionauth/domain/Tenant;
    //   70: astore #10
    //   72: aload #10
    //   74: astore #8
    //   76: aload #7
    //   78: invokevirtual application : ()Lio/fusionauth/domain/Application;
    //   81: astore #10
    //   83: aload #10
    //   85: astore #9
    //   87: aload_0
    //   88: getfield jwtClaimValidator : Lio/fusionauth/api/service/jwt/JWTClaimValidator;
    //   91: aload_1
    //   92: getfield jwt : Lio/fusionauth/jwt/domain/JWT;
    //   95: aload #8
    //   97: aload #9
    //   99: iload_3
    //   100: invokeinterface validateForApplication : (Lio/fusionauth/jwt/domain/JWT;Lio/fusionauth/domain/Tenant;Lio/fusionauth/domain/Application;Z)Lio/fusionauth/api/service/jwt/JWTClaimValidator$ClaimValidationResult;
    //   105: astore #4
    //   107: goto -> 268
    //   110: aload #5
    //   112: checkcast io/fusionauth/api/service/jwt/JWTValidationContext$AudienceApplicationRequired
    //   115: astore #10
    //   117: aload #10
    //   119: invokevirtual tenantSource : ()Lio/fusionauth/api/service/jwt/TenantSource;
    //   122: astore #12
    //   124: aload #12
    //   126: astore #11
    //   128: aload_0
    //   129: aload_1
    //   130: getfield jwt : Lio/fusionauth/jwt/domain/JWT;
    //   133: aload #11
    //   135: invokevirtual resolveTenant : (Lio/fusionauth/jwt/domain/JWT;Lio/fusionauth/api/service/jwt/TenantSource;)Lio/fusionauth/domain/Tenant;
    //   138: astore #12
    //   140: aload #12
    //   142: ifnonnull -> 149
    //   145: aconst_null
    //   146: goto -> 159
    //   149: aload_0
    //   150: aload_1
    //   151: getfield jwt : Lio/fusionauth/jwt/domain/JWT;
    //   154: aload #12
    //   156: invokevirtual resolveAudienceApplication : (Lio/fusionauth/jwt/domain/JWT;Lio/fusionauth/domain/Tenant;)Lio/fusionauth/domain/Application;
    //   159: astore #13
    //   161: aload #13
    //   163: ifnonnull -> 172
    //   166: aload_0
    //   167: aload_1
    //   168: invokevirtual applyUnresolvedContext : (Lio/fusionauth/api/service/jwt/ValidatedJWTResult;)V
    //   171: return
    //   172: aload_0
    //   173: getfield jwtClaimValidator : Lio/fusionauth/api/service/jwt/JWTClaimValidator;
    //   176: aload_1
    //   177: getfield jwt : Lio/fusionauth/jwt/domain/JWT;
    //   180: aload #12
    //   182: aload #13
    //   184: iload_3
    //   185: invokeinterface validateForApplication : (Lio/fusionauth/jwt/domain/JWT;Lio/fusionauth/domain/Tenant;Lio/fusionauth/domain/Application;Z)Lio/fusionauth/api/service/jwt/JWTClaimValidator$ClaimValidationResult;
    //   190: astore #4
    //   192: goto -> 268
    //   195: aload #5
    //   197: checkcast io/fusionauth/api/service/jwt/JWTValidationContext$SuppliedTenantAudienceApplicationIfResolvable
    //   200: astore #12
    //   202: aload #12
    //   204: invokevirtual tenant : ()Lio/fusionauth/domain/Tenant;
    //   207: astore #14
    //   209: aload #14
    //   211: astore #13
    //   213: aload_0
    //   214: aload_1
    //   215: getfield jwt : Lio/fusionauth/jwt/domain/JWT;
    //   218: aload #13
    //   220: invokevirtual resolveAudienceApplication : (Lio/fusionauth/jwt/domain/JWT;Lio/fusionauth/domain/Tenant;)Lio/fusionauth/domain/Application;
    //   223: astore #14
    //   225: aload #14
    //   227: ifnonnull -> 248
    //   230: aload_0
    //   231: getfield jwtClaimValidator : Lio/fusionauth/api/service/jwt/JWTClaimValidator;
    //   234: aload_1
    //   235: getfield jwt : Lio/fusionauth/jwt/domain/JWT;
    //   238: aload #13
    //   240: invokeinterface validateForTenant : (Lio/fusionauth/jwt/domain/JWT;Lio/fusionauth/domain/Tenant;)Lio/fusionauth/api/service/jwt/JWTClaimValidator$ClaimValidationResult;
    //   245: goto -> 266
    //   248: aload_0
    //   249: getfield jwtClaimValidator : Lio/fusionauth/api/service/jwt/JWTClaimValidator;
    //   252: aload_1
    //   253: getfield jwt : Lio/fusionauth/jwt/domain/JWT;
    //   256: aload #13
    //   258: aload #14
    //   260: iload_3
    //   261: invokeinterface validateForApplication : (Lio/fusionauth/jwt/domain/JWT;Lio/fusionauth/domain/Tenant;Lio/fusionauth/domain/Application;Z)Lio/fusionauth/api/service/jwt/JWTClaimValidator$ClaimValidationResult;
    //   266: astore #4
    //   268: aload_0
    //   269: aload_1
    //   270: aload #4
    //   272: invokevirtual applyClaimResult : (Lio/fusionauth/api/service/jwt/ValidatedJWTResult;Lio/fusionauth/api/service/jwt/JWTClaimValidator$ClaimValidationResult;)V
    //   275: goto -> 295
    //   278: astore #5
    //   280: new java/lang/MatchException
    //   283: dup
    //   284: aload #5
    //   286: invokevirtual toString : ()Ljava/lang/String;
    //   289: aload #5
    //   291: invokespecial <init> : (Ljava/lang/String;Ljava/lang/Throwable;)V
    //   294: athrow
    //   295: return
    // Line number table:
    //   Java source line number -> byte code offset
    //   #1271	-> 0
    //   #1272	-> 58
    //   #1273	-> 87
    //   #1274	-> 110
    //   #1275	-> 128
    //   #1276	-> 140
    //   #1277	-> 161
    //   #1278	-> 166
    //   #1279	-> 171
    //   #1282	-> 172
    //   #1283	-> 192
    //   #1284	-> 195
    //   #1285	-> 213
    //   #1286	-> 225
    //   #1287	-> 230
    //   #1288	-> 248
    //   #1292	-> 268
    //   #1284	-> 278
    //   #1293	-> 295
    // Exception table:
    //   from	to	target	type
    //   67	70	278	java/lang/Throwable
    //   78	81	278	java/lang/Throwable
    //   119	122	278	java/lang/Throwable
    //   204	207	278	java/lang/Throwable
  }
  
  private void validateJWTContext(ValidatedJWTResult paramValidatedJWTResult, Tenant paramTenant, Application paramApplication, boolean paramBoolean) {
    JWTClaimValidator.ClaimValidationResult claimValidationResult;
    Objects.requireNonNull(this.tenantReaderService);
    Tenant tenant = (paramTenant != null) ? paramTenant : ClaimTools.getTenantIdFromTidClaim(paramValidatedJWTResult.jwt).<Tenant>map(this.tenantReaderService::retrieveById).orElse((Tenant)null);
    if (tenant != null) {
      if (paramApplication == null) {
        UUID uUID = ClaimTools.getAppIdFromAudClaim(paramValidatedJWTResult.jwt).orElse((UUID)null);
        if (uUID != null) {
          Objects.requireNonNull(this.applicationReader);
          Application application = this.applicationCache.get(tenant.id, uUID, this.applicationReader::retrieveById);
          if (application != null) {
            claimValidationResult = this.jwtClaimValidator.validateForApplication(paramValidatedJWTResult.jwt, tenant, application, paramBoolean);
          } else {
            claimValidationResult = new JWTClaimValidator.ClaimValidationResult.InvalidAudience();
          } 
        } else {
          claimValidationResult = this.jwtClaimValidator.validateForTenant(paramValidatedJWTResult.jwt, tenant);
        } 
      } else {
        claimValidationResult = this.jwtClaimValidator.validateForApplication(paramValidatedJWTResult.jwt, tenant, paramApplication, paramBoolean);
      } 
    } else {
      claimValidationResult = new JWTClaimValidator.ClaimValidationResult.Mismatch("Unable to resolve a tenant for this token");
    } 
    applyClaimResult(paramValidatedJWTResult, claimValidationResult);
  }
  
  private ValidatedJWTResult validateJWTWithShadowKeyFallback(String paramString, FusionAuthJWTDecoder.JWTConstraints paramJWTConstraints, JWTValidationContext paramJWTValidationContext) {
    Map<String, Verifier> map = (Map)this.verifierProvider.get();
    ValidatedJWTResult validatedJWTResult = validateJWT(paramString, paramJWTConstraints, paramJWTValidationContext, map, false);
    if (shouldAttemptShadowKeyIdTokenValidation(paramString, validatedJWTResult, map)) {
      ValidatedJWTResult validatedJWTResult1 = attemptShadowKeyIdTokenValidation(paramString, paramJWTConstraints, paramJWTValidationContext, validatedJWTResult);
      return selectShadowKeyValidationResult(validatedJWTResult, validatedJWTResult1);
    } 
    return validatedJWTResult;
  }
}
