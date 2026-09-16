package io.fusionauth.api.service.authentication;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.inversoft.json.ToString;
import com.inversoft.rest.ProxyInfoSupplier;
import com.inversoft.validator.Validator;
import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.api.domain.IdentityProviderLinkMapper;
import io.fusionauth.api.domain.api.service.ImmutableLambdaArgument;
import io.fusionauth.api.domain.api.service.LambdaArgument;
import io.fusionauth.api.domain.api.service.MutableLambdaArgument;
import io.fusionauth.api.security.KeyCache;
import io.fusionauth.api.service.cache.ApplicationCache;
import io.fusionauth.api.service.cache.IdentityProviderCache;
import io.fusionauth.api.service.cache.TenantCache;
import io.fusionauth.api.service.identity.IdentityProviderUserService;
import io.fusionauth.api.service.identity.OpenIdConnectIdentityProviderHelper;
import io.fusionauth.api.service.lambda.LambdaInvocationService;
import io.fusionauth.api.service.system.EventLogService;
import io.fusionauth.api.service.system.eventLog.Debugger;
import io.fusionauth.api.service.user.ExternalIdentifierReaderService;
import io.fusionauth.api.service.user.ExternalIdentifierService;
import io.fusionauth.api.service.user.UserMetricsService;
import io.fusionauth.api.service.user.UserReaderService;
import io.fusionauth.api.service.user.UserService;
import io.fusionauth.api.util.EmailTools;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserRegistration;
import io.fusionauth.domain.api.identityProvider.IdentityProviderLoginRequest;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.domain.provider.ExternalJWTIdentityProvider;
import io.fusionauth.jwt.JWTException;
import io.fusionauth.jwt.Verifier;
import io.fusionauth.jwt.domain.Header;
import io.fusionauth.jwt.domain.JWT;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.primeframework.mvc.security.UnauthenticatedException;

public class ExternalJWTIdentityProviderAuthenticationService extends BaseIdentityProviderAuthenticationService {
  private final KeyCache keyCache;
  
  @Inject
  public ExternalJWTIdentityProviderAuthenticationService(ApplicationCache paramApplicationCache, AuthenticationService paramAuthenticationService, EventLogService paramEventLogService, ExpressionEvaluator paramExpressionEvaluator, ExternalIdentifierReaderService paramExternalIdentifierReaderService, ExternalIdentifierService paramExternalIdentifierService, FailedLoginService paramFailedLoginService, IdentityProviderCache paramIdentityProviderCache, IdentityProviderLinkMapper paramIdentityProviderLinkMapper, IdentityProviderUserService paramIdentityProviderUserService, KeyCache paramKeyCache, LambdaInvocationService paramLambdaInvocationService, ProxyInfoSupplier paramProxyInfoSupplier, TenantCache paramTenantCache, UserMetricsService paramUserMetricsService, UserReaderService paramUserReaderService, UserService paramUserService) {
    super(paramApplicationCache, paramAuthenticationService, paramEventLogService, paramExternalIdentifierReaderService, paramExternalIdentifierService, paramExpressionEvaluator, paramFailedLoginService, paramIdentityProviderCache, paramIdentityProviderLinkMapper, paramIdentityProviderUserService, paramLambdaInvocationService, paramProxyInfoSupplier, paramUserReaderService, paramUserService, paramUserMetricsService, paramTenantCache);
    this.keyCache = paramKeyCache;
  }
  
  public AuthenticationService.AuthenticationResult _login(Tenant paramTenant, Application paramApplication, @Nonnull BaseIdentityProvider<?> paramBaseIdentityProvider, IdentityProviderLoginRequest paramIdentityProviderLoginRequest, @Nullable ExternalIdentifier paramExternalIdentifier) {
    JWT jWT;
    ExternalJWTIdentityProvider externalJWTIdentityProvider = (ExternalJWTIdentityProvider)paramBaseIdentityProvider;
    BaseIdentityProviderAuthenticationService.LoginContext loginContext = new BaseIdentityProviderAuthenticationService.LoginContext(paramTenant, paramApplication, paramIdentityProviderLoginRequest, new Debugger(externalJWTIdentityProvider.debug, "External JWT IdP Response Debug Log for [" + externalJWTIdentityProvider.name + "] with Id [" + String.valueOf(externalJWTIdentityProvider.id) + "]"), externalJWTIdentityProvider, paramExternalIdentifier);
    Debugger debugger = loginContext.debugger;
    try {
      Map<String, Verifier> map = this.keyCache.getVerifiersByKeyIds(externalJWTIdentityProvider.verificationKeyIds);
      if (!map.isEmpty())
        map.put("", this.keyCache.getVerifierByKeyId((UUID)externalJWTIdentityProvider.verificationKeyIds.getFirst())); 
      Function function = paramHeader -> (String)Optional.<String>ofNullable((String)paramHeader.get(paramExternalJWTIdentityProvider.headerKeyParameter)).orElse("");
      jWT = JWT.getDecoder().decode(paramIdentityProviderLoginRequest.data.get("token"), map, function);
    } catch (JWTException jWTException) {
      debugger.log("Reconcile request failed. The JWT is invalid, a 401 response code will be returned.")
        .log("Raw encoded JWT: " + (String)paramIdentityProviderLoginRequest.data.get("token"))
        .log((Exception)jWTException)
        .done();
      throw new UnauthenticatedException();
    } 
    debugger.log("Decoded JWT:\n" + ToString.toString(jWT));
    if (externalJWTIdentityProvider.uniqueIdentityClaim == null) {
      JsonNode jsonNode = OpenIdConnectIdentityProviderHelper.jwtToJsonNode(jWT);
      loginContext.identityProviderUserId = OpenIdConnectIdentityProviderHelper.getClaimWithPointer(jsonNode, externalJWTIdentityProvider.oauth2.uniqueIdClaim);
      loginContext.email = OpenIdConnectIdentityProviderHelper.getClaimWithPointer(jsonNode, externalJWTIdentityProvider.oauth2.emailClaim);
      loginContext.email_verified = OpenIdConnectIdentityProviderHelper.getClaimWithPointer(jsonNode, externalJWTIdentityProvider.oauth2.emailVerifiedClaim);
      loginContext.username = OpenIdConnectIdentityProviderHelper.getClaimWithPointer(jsonNode, externalJWTIdentityProvider.oauth2.usernameClaim);
      loginContext.identityProviderDisplayName = (loginContext.email != null) ? loginContext.email : loginContext.username;
    } else {
      loginContext.identityProviderUserId = jWT.subject;
      loginContext.email = jWT.getString(externalJWTIdentityProvider.uniqueIdentityClaim);
      loginContext.identityProviderDisplayName = loginContext.email;
      if (loginContext.email == null) {
        debugger.log("Reconcile request failed. The JWT did not contain the required unique identity claim [" + externalJWTIdentityProvider.uniqueIdentityClaim + "].\nA 401 response code will be returned.")
          
          .done();
        throw new UnauthenticatedException();
      } 
    } 
    if (loginContext.email != null && 
      !externalJWTIdentityProvider.domains.isEmpty() && !externalJWTIdentityProvider.domains.contains(EmailTools.getEmailDomain(loginContext.email))) {
      debugger.log("Reconcile request failed. A unique identity claim of [" + loginContext.email + "] was found but this email address or domain is not being managed by this configuration.\nA 401 response code will be returned.")
        
        .done();
      throw new UnauthenticatedException();
    } 
    loginContext.identityProviderToken = paramIdentityProviderLoginRequest.data.get("refresh_token");
    return completeLogin(loginContext, paramUserResult -> handleClaimMappings(paramUserResult.user(), paramUserResult.registration(), paramExternalJWTIdentityProvider, paramJWT), paramUserResult -> lambdaArgs(new LambdaArgument[] { new MutableLambdaArgument(paramUserResult.user()), new MutableLambdaArgument(paramUserResult.registration()), new ImmutableLambdaArgument(paramJWT) }, ), paramUserResult -> OpenIdConnectIdentityProviderHelper.jwtToJsonNode(paramJWT));
  }
  
  public AuthenticationType authenticationType() {
    return AuthenticationType.FEDERATED_JWT;
  }
  
  public IdentityProviderAuthenticationService.ValidationResult validate(Tenant paramTenant, @Nonnull BaseIdentityProvider<?> paramBaseIdentityProvider, IdentityProviderLoginRequest paramIdentityProviderLoginRequest, @Nullable ExternalIdentifier paramExternalIdentifier) {
    IdentityProviderAuthenticationService.ValidationResult validationResult = commonValidate(paramTenant, paramIdentityProviderLoginRequest.applicationId, paramBaseIdentityProvider, paramExternalIdentifier);
    validationResult.errors.add((new Validator())
        .notMissing(paramIdentityProviderLoginRequest.data.get("token"), "encodedJWT", new Object[0])
        .notMissing(paramIdentityProviderLoginRequest.identityProviderId, "identityProviderId", new Object[0])
        .done());
    return validationResult;
  }
  
  private void handleClaimMappings(User paramUser, UserRegistration paramUserRegistration, ExternalJWTIdentityProvider paramExternalJWTIdentityProvider, JWT paramJWT) {
    paramExternalJWTIdentityProvider.claimMap.forEach((paramString1, paramString2) -> {
          switch (paramString2) {
            case "birthDate":
              paramUser.birthDate = synchronizeClaim(paramString1, paramJWT, paramUser.birthDate);
              break;
            case "firstName":
              paramUser.firstName = synchronizeClaim(paramString1, paramJWT, paramUser.firstName);
              break;
            case "middleName":
              paramUser.middleName = synchronizeClaim(paramString1, paramJWT, paramUser.middleName);
              break;
            case "lastName":
              paramUser.lastName = synchronizeClaim(paramString1, paramJWT, paramUser.lastName);
              break;
            case "fullName":
              paramUser.fullName = synchronizeClaim(paramString1, paramJWT, paramUser.fullName);
              break;
            case "mobilePhone":
              paramUser.mobilePhone = synchronizeClaim(paramString1, paramJWT, paramUser.mobilePhone);
              break;
            case "imageUrl":
              paramUser.imageUrl = synchronizeClaim(paramString1, paramJWT, paramUser.imageUrl);
              break;
            case "timezone":
              paramUserRegistration.timezone = synchronizeClaim(paramString1, paramJWT, paramUserRegistration.timezone);
              break;
            case "UserData":
              setUserDataValue(paramString1, paramJWT, paramUser);
              break;
            case "RegistrationData":
              setRegistrationDataValue(paramUserRegistration, paramString1, paramJWT);
              break;
          } 
        });
  }
  
  private void setRegistrationDataValue(UserRegistration paramUserRegistration, String paramString, JWT paramJWT) {
    Object object = paramJWT.getObject(paramString);
    if (object == null)
      return; 
    paramUserRegistration.data.put(paramString, object);
  }
  
  private void setUserDataValue(String paramString, JWT paramJWT, User paramUser) {
    Object object = paramJWT.getObject(paramString);
    if (object == null)
      return; 
    paramUser.data.put(paramString, object);
  }
  
  private ZoneId synchronizeClaim(String paramString, JWT paramJWT, ZoneId paramZoneId) {
    ZoneId zoneId;
    String str = paramJWT.getString(paramString);
    if (str == null)
      return paramZoneId; 
    try {
      zoneId = ZoneId.of(str);
    } catch (DateTimeException dateTimeException) {
      return paramZoneId;
    } 
    return zoneId;
  }
  
  private LocalDate synchronizeClaim(String paramString, JWT paramJWT, LocalDate paramLocalDate) {
    LocalDate localDate;
    String str = paramJWT.getString(paramString);
    if (str == null)
      return paramLocalDate; 
    try {
      localDate = LocalDate.parse(str);
    } catch (DateTimeParseException dateTimeParseException) {
      return paramLocalDate;
    } 
    return localDate;
  }
  
  private URI synchronizeClaim(String paramString, JWT paramJWT, URI paramURI) {
    URI uRI;
    String str = paramJWT.getString(paramString);
    if (str == null)
      return paramURI; 
    try {
      uRI = new URI(str);
    } catch (URISyntaxException uRISyntaxException) {
      return paramURI;
    } 
    return uRI;
  }
  
  private String synchronizeClaim(String paramString1, JWT paramJWT, String paramString2) {
    String str = paramJWT.getString(paramString1);
    if (str == null)
      return paramString2; 
    return str;
  }
}
