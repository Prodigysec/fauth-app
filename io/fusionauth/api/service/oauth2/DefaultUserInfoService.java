package io.fusionauth.api.service.oauth2;

import com.google.inject.Inject;
import io.fusionauth.api.domain.api.service.ImmutableLambdaArgument;
import io.fusionauth.api.domain.api.service.LambdaArgument;
import io.fusionauth.api.domain.api.service.MutableLambdaArgument;
import io.fusionauth.api.service.cache.ApplicationCache;
import io.fusionauth.api.service.jwt.FusionAuthJWTDecoder;
import io.fusionauth.api.service.jwt.JWTService;
import io.fusionauth.api.service.jwt.JWTValidationContext;
import io.fusionauth.api.service.jwt.TenantSource;
import io.fusionauth.api.service.jwt.ValidatedJWTResult;
import io.fusionauth.api.service.jwt.claims.JWTType;
import io.fusionauth.api.service.lambda.LambdaInvocationService;
import io.fusionauth.api.service.system.ApplicationReaderService;
import io.fusionauth.api.service.system.LambdaInvocationException;
import io.fusionauth.api.service.user.UserReaderService;
import io.fusionauth.api.util.ClaimTools;
import io.fusionauth.api.util.UUIDTools;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.IdentityType;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserIdentity;
import io.fusionauth.domain.oauth2.OAuthError;
import io.fusionauth.domain.oauth2.OAuthScopeHandlingPolicy;
import io.fusionauth.jwt.MissingVerifierException;
import io.fusionauth.jwt.domain.JWT;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultUserInfoService implements UserInfoService {
  private static final Set<String> ReservedClaims = new HashSet<>(Arrays.asList(new String[] { "sub", "tid", "email", "email_verified", "phone_number_verified" }));
  
  private static final Logger logger = LoggerFactory.getLogger(DefaultUserInfoService.class);
  
  private final ApplicationCache applicationCache;
  
  private final ApplicationReaderService applicationReader;
  
  private final JWTService jwtService;
  
  private final LambdaInvocationService lambdaInvocationService;
  
  private final UserReaderService userReader;
  
  @Inject
  public DefaultUserInfoService(ApplicationCache paramApplicationCache, ApplicationReaderService paramApplicationReaderService, JWTService paramJWTService, LambdaInvocationService paramLambdaInvocationService, UserReaderService paramUserReaderService) {
    this.applicationCache = paramApplicationCache;
    this.applicationReader = paramApplicationReaderService;
    this.jwtService = paramJWTService;
    this.lambdaInvocationService = paramLambdaInvocationService;
    this.userReader = paramUserReaderService;
  }
  
  public Map<String, Object> retrieveUserInfo(JWT paramJWT, User paramUser, Application paramApplication) {
    Map<?, ?> map = Collections.emptyMap();
    OAuthScopeHandlingPolicy oAuthScopeHandlingPolicy = paramApplication.oauthConfiguration.scopeHandlingPolicy;
    if (oAuthScopeHandlingPolicy == OAuthScopeHandlingPolicy.Compatibility) {
      map = populateClaimsCompatibility(paramJWT, paramUser);
    } else if (oAuthScopeHandlingPolicy == OAuthScopeHandlingPolicy.Strict) {
      map = populateClaimsStrict(paramJWT, paramUser);
    } 
    applyUserInfoLambda(paramJWT, paramUser, paramApplication, (Map)map);
    return (Map)map;
  }
  
  public UserInfoService.UserInfoValidationResult validateRetrieveUserInfo(String paramString) {
    UserInfoService.UserInfoValidationResult userInfoValidationResult = new UserInfoService.UserInfoValidationResult();
    resolveUserInfoContext(paramString, userInfoValidationResult);
    if (userInfoValidationResult.error != null)
      return userInfoValidationResult; 
    Application application = userInfoValidationResult.application;
    UUID uUID1 = ClaimTools.resolveTenantId(userInfoValidationResult.jwt);
    if (!application.universalConfiguration.universal)
      if (!application.tenantId.equals(uUID1)) {
        userInfoValidationResult.error = new OAuthError(OAuthError.OAuthErrorType.invalid_token, OAuthError.OAuthErrorReason.access_token_invalid, "The token is not suitable for the requested use.");
        return userInfoValidationResult;
      }  
    UUID uUID2 = UUIDTools.fromString(userInfoValidationResult.jwt.subject);
    validateUserById(uUID1, uUID2, userInfoValidationResult);
    if (userInfoValidationResult.error != null)
      return userInfoValidationResult; 
    OAuthScopeHandlingPolicy oAuthScopeHandlingPolicy = application.oauthConfiguration.scopeHandlingPolicy;
    validateScopePolicyConstraints(oAuthScopeHandlingPolicy, userInfoValidationResult);
    if (userInfoValidationResult.error != null)
      return userInfoValidationResult; 
    return userInfoValidationResult;
  }
  
  private void applyUserInfoLambda(JWT paramJWT, User paramUser, Application paramApplication, Map<String, Object> paramMap) {
    UUID uUID = paramApplication.lambdaConfiguration.userinfoPopulateId;
    if (uUID != null)
      try {
        HashMap<String, Object> hashMap = new HashMap<>(paramMap);
        this.lambdaInvocationService.invoke(uUID, new LambdaArgument[] { new MutableLambdaArgument(hashMap), new ImmutableLambdaArgument(paramUser), new ImmutableLambdaArgument(paramUser

                
                .getRegistrationForApplication(paramApplication.id)), new ImmutableLambdaArgument(paramJWT
                .getRawClaims()) });
        hashMap.keySet().forEach(paramString -> {
              if (!ReservedClaims.contains(paramString)) {
                Object object = paramHashMap.get(paramString);
                if (object == null) {
                  paramMap.remove(paramString);
                } else {
                  paramMap.put(paramString, object);
                } 
              } 
            });
      } catch (LambdaInvocationException lambdaInvocationException) {
        if (logger.isDebugEnabled()) {
          logger.debug("Failed to apply lambda during UserInfo request.", (Throwable)lambdaInvocationException);
        } else {
          logger.error("Failed to apply lambda during UserInfo request. See Event Log for additional details.");
        } 
      }  
  }
  
  private void handleInvalidTokenResult(ValidatedJWTResult paramValidatedJWTResult, UserInfoService.UserInfoValidationResult paramUserInfoValidationResult) throws Exception {
    if (paramValidatedJWTResult.exception != null)
      throw paramValidatedJWTResult.exception; 
    String str = ("aud".equals(paramValidatedJWTResult.requiredClaim) || paramValidatedJWTResult.reasonCode == ValidatedJWTResult.ReasonCode.invalidAudience) ? paramValidatedJWTResult.reason : "The token is not suitable for the requested use.";
    paramUserInfoValidationResult.error = new OAuthError(OAuthError.OAuthErrorType.invalid_token, OAuthError.OAuthErrorReason.access_token_invalid, str);
  }
  
  private Map<String, Object> populateClaimsCompatibility(JWT paramJWT, User paramUser) {
    Map<String, String> map = paramJWT.getRawClaims();
    map.remove("authenticationType");
    map.remove("aud");
    map.remove("auth_time");
    map.remove("exp");
    map.remove("iat");
    map.remove("iss");
    map.remove("jti");
    UserIdentity userIdentity = paramUser.resolvePrimaryIdentity(IdentityType.phoneNumber);
    map.put("phone_number", Optional.<UserIdentity>ofNullable(userIdentity)
        .map(paramUserIdentity -> paramUserIdentity.value).orElse(paramUser.mobilePhone));
    if (paramUser.birthDate != null)
      map.put("birthdate", paramUser.birthDate.format(DateTimeFormatter.ISO_LOCAL_DATE)); 
    if (paramUser.imageUrl != null)
      map.put("picture", paramUser.imageUrl.toString()); 
    if (paramUser.fullName != null)
      map.put("name", paramUser.fullName); 
    if (paramUser.firstName != null)
      map.put("given_name", paramUser.firstName); 
    if (paramUser.lastName != null)
      map.put("family_name", paramUser.lastName); 
    if (paramUser.middleName != null)
      map.put("middle_name", paramUser.middleName); 
    return (Map)map;
  }
  
  private Map<String, Object> populateClaimsStrict(JWT paramJWT, User paramUser) {
    HashMap<Object, Object> hashMap = new HashMap<>();
    hashMap.put("sub", paramUser.id);
    hashMap.put("tid", paramUser.tenantId);
    HashSet hashSet = new HashSet();
    String str = paramJWT.getString("scope");
    if (str != null)
      hashSet.addAll(Arrays.asList(str.split(" "))); 
    if (hashSet.contains("email")) {
      UserIdentity userIdentity = paramUser.resolvePrimaryIdentity(IdentityType.email);
      hashMap.put("email", Optional.<UserIdentity>ofNullable(userIdentity)
          .map(paramUserIdentity -> paramUserIdentity.value).orElse(null));
      hashMap.put("email_verified", Optional.<UserIdentity>ofNullable(userIdentity)

          
          .map(paramUserIdentity -> Boolean.valueOf(!paramUserIdentity.verificationRequired())).orElse(null));
    } 
    if (hashSet.contains("phone")) {
      UserIdentity userIdentity = paramUser.resolvePrimaryIdentity(IdentityType.phoneNumber);
      hashMap.put("phone_number", Optional.<UserIdentity>ofNullable(userIdentity)
          .map(paramUserIdentity -> paramUserIdentity.value).orElse(paramUser.mobilePhone));
      if (userIdentity != null)
        hashMap.put("phone_number_verified", Boolean.valueOf(!userIdentity.verificationRequired())); 
    } 
    if (hashSet.contains("profile")) {
      hashMap.put("given_name", paramUser.firstName);
      hashMap.put("middle_name", paramUser.middleName);
      hashMap.put("family_name", paramUser.lastName);
      hashMap.put("name", paramUser.fullName);
      hashMap.put("preferred_username", paramUser.username);
      if (paramUser.birthDate != null)
        hashMap.put("birthdate", paramUser.birthDate.format(DateTimeFormatter.ISO_LOCAL_DATE)); 
      if (paramUser.imageUrl != null)
        hashMap.put("picture", paramUser.imageUrl.toString()); 
      if (!paramUser.preferredLanguages.isEmpty())
        hashMap.put("locale", ((Locale)paramUser.preferredLanguages.get(0)).toLanguageTag()); 
      if (paramUser.timezone != null)
        hashMap.put("zoneinfo", paramUser.timezone.getId()); 
    } 
    return (Map)hashMap;
  }
  
  private void resolveUserInfoContext(String paramString, UserInfoService.UserInfoValidationResult paramUserInfoValidationResult) {
    try {
      ValidatedJWTResult validatedJWTResult = this.jwtService.validateJWT(paramString, FusionAuthJWTDecoder.JWTConstraints.OAuthUserAccessToken, new JWTValidationContext.AudienceApplicationRequired(TenantSource.SignedTid.INSTANCE));
      paramUserInfoValidationResult.jwt = validatedJWTResult.jwt;
      if (!validatedJWTResult.valid || paramUserInfoValidationResult.jwt == null) {
        handleInvalidTokenResult(validatedJWTResult, paramUserInfoValidationResult);
        return;
      } 
      UUID uUID = ClaimTools.resolveApplicationId(paramUserInfoValidationResult.jwt);
      Objects.requireNonNull(this.applicationReader);
      Application application = this.applicationCache.get(null, uUID, this.applicationReader::retrieveById);
      paramUserInfoValidationResult.application = application;
      if (application == null)
        paramUserInfoValidationResult.error = new OAuthError(OAuthError.OAuthErrorType.invalid_token, OAuthError.OAuthErrorReason.access_token_invalid, "The [aud] claim is invalid."); 
    } catch (MissingVerifierException missingVerifierException) {
      if (paramUserInfoValidationResult.jwt == null)
        paramUserInfoValidationResult.jwt = FusionAuthJWTDecoder.unsafeDecode(paramString); 
      Objects.requireNonNull(this.applicationReader);
      paramUserInfoValidationResult.application = this.applicationCache.get(null, ClaimTools.resolveApplicationId(paramUserInfoValidationResult.jwt), this.applicationReader::retrieveById);
      if (paramUserInfoValidationResult.jwt.audience == null) {
        paramUserInfoValidationResult.error = new OAuthError(OAuthError.OAuthErrorType.invalid_token, OAuthError.OAuthErrorReason.access_token_invalid, "Missing required claim [aud].");
      } else if (paramUserInfoValidationResult.application == null) {
        paramUserInfoValidationResult.error = new OAuthError(OAuthError.OAuthErrorType.invalid_token, OAuthError.OAuthErrorReason.access_token_invalid, "The [aud] claim is invalid.");
      } else if (JWTType.is(paramUserInfoValidationResult.jwt, JWTType.IdToken)) {
        paramUserInfoValidationResult.error = new OAuthError(OAuthError.OAuthErrorType.invalid_token, OAuthError.OAuthErrorReason.access_token_invalid, "The token is not suitable for the requested use.");
      } else {
        paramUserInfoValidationResult.error = OAuthService.handleJWTDecodingExceptions((Exception)missingVerifierException);
      } 
    } catch (Exception exception) {
      paramUserInfoValidationResult.error = OAuthService.handleJWTDecodingExceptions(exception);
    } 
  }
  
  private void validateScopePolicyConstraints(OAuthScopeHandlingPolicy paramOAuthScopeHandlingPolicy, UserInfoService.UserInfoValidationResult paramUserInfoValidationResult) {
    if (paramOAuthScopeHandlingPolicy == OAuthScopeHandlingPolicy.Strict) {
      String str = paramUserInfoValidationResult.jwt.getString("scope");
      if (str == null || !Arrays.<String>asList(str.split(" ")).contains("openid"))
        paramUserInfoValidationResult.error = new OAuthError(OAuthError.OAuthErrorType.invalid_request, OAuthError.OAuthErrorReason.missing_required_scope, "Missing the required [openid] scope."); 
    } 
  }
  
  private void validateUserById(UUID paramUUID1, UUID paramUUID2, UserInfoService.UserInfoValidationResult paramUserInfoValidationResult) {
    User user = this.userReader.retrieveById(paramUUID1, paramUUID2);
    if (user != null) {
      paramUserInfoValidationResult.user = user;
    } else {
      paramUserInfoValidationResult.error = new OAuthError(OAuthError.OAuthErrorType.invalid_token, OAuthError.OAuthErrorReason.user_not_found, "The [sub] claim is invalid.");
    } 
  }
}
