package io.fusionauth.api.service.oauth2;

import com.google.inject.Inject;
import com.inversoft.util.StringTools;
import io.fusionauth.api.configuration.FusionAuthConfiguration;
import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.api.domain.UserMapper;
import io.fusionauth.api.service.authentication.AuthenticationType;
import io.fusionauth.api.service.authentication.IdentityProviderAuthenticationService;
import io.fusionauth.api.service.cache.ApplicationCache;
import io.fusionauth.api.service.cache.IdentityProviderCache;
import io.fusionauth.api.service.cache.TenantCache;
import io.fusionauth.api.service.entity.EntityService;
import io.fusionauth.api.service.identity.IdentityProviderUserService;
import io.fusionauth.api.service.jwt.FusionAuthJWTDecoder;
import io.fusionauth.api.service.jwt.JWTService;
import io.fusionauth.api.service.jwt.JWTValidationContext;
import io.fusionauth.api.service.jwt.RefreshTokenService;
import io.fusionauth.api.service.jwt.ValidatedJWTResult;
import io.fusionauth.api.service.jwt.claims.JWTType;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.api.service.reactor.ReactorStatusValidator;
import io.fusionauth.api.service.system.ApplicationReaderService;
import io.fusionauth.api.service.system.CipherService;
import io.fusionauth.api.service.system.EventLogHelper;
import io.fusionauth.api.service.system.TenantReaderService;
import io.fusionauth.api.service.system.eventLog.Debugger;
import io.fusionauth.api.service.user.ExternalIdentifierReaderService;
import io.fusionauth.api.service.user.ExternalIdentifierService;
import io.fusionauth.api.service.user.UserReaderService;
import io.fusionauth.api.util.ClaimTools;
import io.fusionauth.api.util.IdentityProviderTools;
import io.fusionauth.api.util.PKCETools;
import io.fusionauth.api.util.ParameterTools;
import io.fusionauth.api.util.URITools;
import io.fusionauth.api.util.UUIDTools;
import io.fusionauth.app.guice.TenantManagerApplicationId;
import io.fusionauth.app.service.security.SSOService;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.ApplicationOAuthScope;
import io.fusionauth.domain.Entity;
import io.fusionauth.domain.EntityGrant;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.EventLogType;
import io.fusionauth.domain.IdentityProviderLink;
import io.fusionauth.domain.Requirable;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserRegistration;
import io.fusionauth.domain.jwt.DeviceInfo;
import io.fusionauth.domain.jwt.RefreshToken;
import io.fusionauth.domain.oauth2.AccessToken;
import io.fusionauth.domain.oauth2.ClientAuthenticationPolicy;
import io.fusionauth.domain.oauth2.DeviceResponse;
import io.fusionauth.domain.oauth2.GrantType;
import io.fusionauth.domain.oauth2.LogoutBehavior;
import io.fusionauth.domain.oauth2.OAuth2Configuration;
import io.fusionauth.domain.oauth2.OAuthApplicationRelationship;
import io.fusionauth.domain.oauth2.OAuthError;
import io.fusionauth.domain.oauth2.Oauth2AuthorizedURLValidationPolicy;
import io.fusionauth.domain.oauth2.ProofKeyForCodeExchangePolicy;
import io.fusionauth.domain.oauth2.ProvidedScopePolicy;
import io.fusionauth.domain.oauth2.TokenType;
import io.fusionauth.domain.oauth2.UnknownScopePolicy;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.domain.provider.IdentityProviderTenantConfiguration;
import io.fusionauth.domain.reactor.ReactorFeatureStatus;
import io.fusionauth.domain.reactor.ReactorStatus;
import io.fusionauth.http.server.HTTPRequest;
import io.fusionauth.http.util.HTTPTools;
import io.fusionauth.jwt.JWTUtils;
import io.fusionauth.jwt.OpenIDConnect;
import io.fusionauth.jwt.domain.JWT;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.mybatis.guice.transactional.Transactional;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.primeframework.mvc.util.QueryStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultOAuthService implements OAuthService {
  public static final String ADDRESS_SCOPE = "address";
  
  public static final Set<Integer> AllowedScopeChars = new HashSet<>(Stream.<Character>of(new Character[] { 
          Character.valueOf('A'), Character.valueOf('B'), Character.valueOf('C'), Character.valueOf('D'), Character.valueOf('E'), Character.valueOf('F'), Character.valueOf('G'), Character.valueOf('H'), Character.valueOf('I'), Character.valueOf('J'), 
          Character.valueOf('K'), Character.valueOf('L'), Character.valueOf('M'), Character.valueOf('N'), Character.valueOf('O'), Character.valueOf('P'), Character.valueOf('Q'), Character.valueOf('R'), Character.valueOf('S'), Character.valueOf('T'), 
          Character.valueOf('U'), Character.valueOf('V'), Character.valueOf('W'), Character.valueOf('X'), Character.valueOf('Y'), Character.valueOf('Z'), 
          Character.valueOf('a'), Character.valueOf('b'), Character.valueOf('c'), Character.valueOf('d'), 
          Character.valueOf('e'), Character.valueOf('f'), Character.valueOf('g'), Character.valueOf('h'), Character.valueOf('i'), Character.valueOf('j'), Character.valueOf('k'), Character.valueOf('l'), Character.valueOf('m'), Character.valueOf('n'), 
          Character.valueOf('o'), Character.valueOf('p'), Character.valueOf('q'), Character.valueOf('r'), Character.valueOf('s'), Character.valueOf('t'), Character.valueOf('u'), Character.valueOf('v'), Character.valueOf('w'), Character.valueOf('x'), 
          Character.valueOf('y'), Character.valueOf('z'), 
          Character.valueOf('0'), Character.valueOf('1'), Character.valueOf('2'), Character.valueOf('3'), Character.valueOf('4'), Character.valueOf('5'), Character.valueOf('6'), Character.valueOf('7'), 
          Character.valueOf('8'), Character.valueOf('9'), 
          Character.valueOf('!'), Character.valueOf('#'), Character.valueOf('$'), Character.valueOf('%'), Character.valueOf('&'), Character.valueOf('\''), Character.valueOf('('), Character.valueOf(')'), 
          Character.valueOf('*'), Character.valueOf('+'), Character.valueOf(','), Character.valueOf('-'), Character.valueOf('.'), Character.valueOf('/'), Character.valueOf(':'), Character.valueOf(';'), Character.valueOf('<'), Character.valueOf('='), 
          Character.valueOf('>'), Character.valueOf('?'), Character.valueOf('@'), Character.valueOf('['), Character.valueOf(']'), Character.valueOf('^'), Character.valueOf('_'), Character.valueOf('`'), Character.valueOf('{'), Character.valueOf('|'), 
          Character.valueOf('}'), Character.valueOf('~') }).map(Integer::valueOf).toList());
  
  public static final String EMAIL_SCOPE = "email";
  
  public static final String IDP_LINK_SCOPE_PREFIX = "idp-link:";
  
  public static final String OFFLINE_ACCESS_SCOPE = "offline_access";
  
  public static final String OPENID_SCOPE = "openid";
  
  public static final String PHONE_SCOPE = "phone";
  
  public static final String PROFILE_SCOPE = "profile";
  
  public static final Set<String> ReservedScopeNames = new LinkedHashSet<>(Arrays.asList(new String[] { "openid", "offline_access", "address", "email", "phone", "profile" }));
  
  public static final Set<GrantType> SupportedGrants = new LinkedHashSet<>(Arrays.asList(new GrantType[] { GrantType.authorization_code, GrantType.implicit, GrantType.password, GrantType.refresh_token, GrantType.device_code }));
  
  public static final String TARGET_ENTITY_SCOPE_PREFIX = "target-entity:";
  
  public static final Set<String> ReservedScopePrefixes = new LinkedHashSet<>(Arrays.asList(new String[] { "idp-link:", "source-entity:", "target-entity:" }));
  
  public static final Set<String> TokenTypeHints = Set.of("access_token", "id_token", "refresh_token");
  
  private static final Set<String> AllowedLogoutRelativeRedirectRootPaths = Set.of("/account/", "/admin/", "/oauth2/", "/tenant-manager");
  
  private static final Set<String> AllowedRelativeRedirects = Set.of("/account/login", "/admin/login", "/app/callback", "/tenant-manager/login", "/tenant-manager/sso/test/complete", "/oauth2/device");
  
  private static final URI SAMLRedirect = URI.create("/samlv2/callback");
  
  private static final Set<GrantType> VALID_TOKEN_ENDPOINT_GRANT_TYPES = Set.of(GrantType.authorization_code, GrantType.client_credentials, GrantType.device_code, GrantType.password, GrantType.refresh_token);
  
  private static final Set<String> ValidImplicitResponseTypes = new HashSet<>(Arrays.asList(new String[] { "token", "id_token", "token id_token", "id_token token" }));
  
  private static final Set<String> ValidOAuth2ResponseTypes = new HashSet<>(Arrays.asList(new String[] { "code", "token" }));
  
  private static final Set<String> ValidOIDCResponseTypes = new HashSet<>(Arrays.asList(new String[] { "code", "id_token", "token id_token", "id_token token" }));
  
  private static final Set<String> ValidResponseModes = new HashSet<>(Arrays.asList(new String[] { "form_post", "fragment", "query" }));
  
  private static final Logger logger = LoggerFactory.getLogger(DefaultOAuthService.class);
  
  private final ApplicationCache applicationCache;
  
  private final ApplicationReaderService applicationReader;
  
  private final CipherService cipherService;
  
  private final DPoPService dPoPService;
  
  private final EntityService entityService;
  
  private final ExpressionEvaluator expressionEvaluator;
  
  private final ExternalIdentifierReaderService externalIdentifierReader;
  
  private final ExternalIdentifierService externalIdentifierService;
  
  private final FusionAuthConfiguration fusionAuthConfiguration;
  
  private final IdentityProviderCache identityProviderCache;
  
  private final IdentityProviderUserService identityProviderUserService;
  
  private final JWTService jwtService;
  
  private final ReactorStatusService reactorStatusService;
  
  private final RefreshTokenService refreshTokenService;
  
  private final HTTPRequest request;
  
  private final TenantCache tenantCache;
  
  private final UUID tenantManagerApplicationId;
  
  private final TenantReaderService tenantReader;
  
  private final UserMapper userMapper;
  
  private final UserReaderService userReader;
  
  @Inject
  public DefaultOAuthService(ApplicationCache paramApplicationCache, ApplicationReaderService paramApplicationReaderService, CipherService paramCipherService, DPoPService paramDPoPService, EntityService paramEntityService, ExpressionEvaluator paramExpressionEvaluator, ExternalIdentifierReaderService paramExternalIdentifierReaderService, ExternalIdentifierService paramExternalIdentifierService, IdentityProviderCache paramIdentityProviderCache, IdentityProviderUserService paramIdentityProviderUserService, JWTService paramJWTService, ReactorStatusService paramReactorStatusService, RefreshTokenService paramRefreshTokenService, HTTPRequest paramHTTPRequest, TenantCache paramTenantCache, TenantReaderService paramTenantReaderService, UserReaderService paramUserReaderService, @TenantManagerApplicationId UUID paramUUID, UserMapper paramUserMapper, FusionAuthConfiguration paramFusionAuthConfiguration) {
    this.applicationCache = paramApplicationCache;
    this.applicationReader = paramApplicationReaderService;
    this.cipherService = paramCipherService;
    this.dPoPService = paramDPoPService;
    this.entityService = paramEntityService;
    this.expressionEvaluator = paramExpressionEvaluator;
    this.externalIdentifierReader = paramExternalIdentifierReaderService;
    this.externalIdentifierService = paramExternalIdentifierService;
    this.identityProviderCache = paramIdentityProviderCache;
    this.identityProviderUserService = paramIdentityProviderUserService;
    this.jwtService = paramJWTService;
    this.reactorStatusService = paramReactorStatusService;
    this.refreshTokenService = paramRefreshTokenService;
    this.request = paramHTTPRequest;
    this.tenantCache = paramTenantCache;
    this.tenantReader = paramTenantReaderService;
    this.userReader = paramUserReaderService;
    this.tenantManagerApplicationId = paramUUID;
    this.userMapper = paramUserMapper;
    this.fusionAuthConfiguration = paramFusionAuthConfiguration;
  }
  
  @Transactional
  public OAuthService.DeviceApproveResult approveDevice(EventInfo paramEventInfo, Tenant paramTenant, Application paramApplication, User paramUser, ExternalIdentifier paramExternalIdentifier, Set<String> paramSet) {
    Debugger debugger = new Debugger(paramApplication.oauthConfiguration.debug, "OAuth2 approve device grant debug log for [" + paramApplication.name + "] with clientId [" + paramApplication.oauthConfiguration.clientId + "].");
    OAuthService.DeviceApproveResult deviceApproveResult = approveDeviceCode(debugger, paramTenant, paramApplication, paramUser, paramExternalIdentifier, paramSet);
    deviceApproveResult.identityProviderLink = validateAndCompleteDeviceLink(debugger, paramEventInfo, paramExternalIdentifier, paramTenant, paramUser);
    if (deviceApproveResult.identityProviderLink != null) {
      deviceApproveResult.identityProviderLink.tenantId = null;
      deviceApproveResult.identityProviderLink.userId = null;
    } 
    return deviceApproveResult;
  }
  
  public boolean checkPersistedUserConsentChoices(Tenant paramTenant, Application paramApplication, UUID paramUUID, Set<String> paramSet) {
    ExternalIdentifier externalIdentifier = this.externalIdentifierReader.retrieveByUserIdTypeAndApplicationId(paramUUID, ExternalIdentifier.ExternalIdType.RememberOAuthScopeConsentChoice, paramApplication.id);
    if (externalIdentifier == null || externalIdentifier.isExpired(paramTenant))
      return true; 
    Set<String> set1 = ParameterTools.splitSpaceSeparated(externalIdentifier.getAttribute("approvedScopes"));
    Set<String> set2 = ParameterTools.splitSpaceSeparated(externalIdentifier.getAttribute("declinedScopes"));
    Set<String> set3 = getRequiredOAuthScopeNames(paramApplication, paramSet);
    Set set = (Set)paramSet.stream().collect(Collectors.filtering(paramString -> !paramSet.contains(paramString), Collectors.toSet()));
    Set<String> set4 = getOAuthScopeNamesForPromptFromApplication(paramApplication);
    set3.retainAll(set4);
    set.retainAll(set4);
    if ((set1.retainAll(set4) | set2.retainAll(set4)) != 0) {
      externalIdentifier.data.setAttribute("approvedScopes", String.join(" ", (Iterable)set1))
        .setAttribute("declinedScopes", String.join(" ", (Iterable)set2));
      this.externalIdentifierService.update(externalIdentifier);
    } 
    boolean bool = (!set1.containsAll(set3) || set.stream().anyMatch(paramString -> (!paramSet1.contains(paramString) && !paramSet2.contains(paramString)))) ? true : false;
    if (!bool) {
      Objects.requireNonNull(set2);
      paramSet.removeIf(set2::contains);
    } 
    return bool;
  }
  
  public AccessToken createAccessToken(EventInfo paramEventInfo, Tenant paramTenant, Application paramApplication, User paramUser, String paramString1, URI paramURI, GrantType paramGrantType, String paramString2, Set<String> paramSet, String paramString3, String paramString4, UUID paramUUID1, String paramString5, String paramString6, UUID paramUUID2, RefreshToken.MetaData paramMetaData, AuthenticationType paramAuthenticationType, String paramString7, @Nullable String paramString8) {
    HashMap<Object, Object> hashMap = new HashMap<>(2);
    hashMap.put("aud", paramString1);
    UserRegistration userRegistration = paramUser.getRegistrationForApplication(paramApplication.id);
    if (userRegistration != null) {
      hashMap.put("applicationId", paramApplication.id);
      hashMap.put("roles", userRegistration.roles);
    } 
    if (paramGrantType == GrantType.implicit)
      paramSet.remove("offline_access"); 
    if (paramSet.size() > 0)
      hashMap.put("scope", String.join(" ", (Iterable)paramSet)); 
    Set<String> set = parseResponseType(paramString2);
    AccessToken accessToken = (new AccessToken()).with(paramAccessToken -> paramAccessToken.clientId = paramString).with(paramAccessToken -> paramAccessToken.redirectURI = paramURI).with(paramAccessToken -> paramAccessToken.scope = paramSet.isEmpty() ? null : String.join(" ", paramSet)).with(paramAccessToken -> paramAccessToken.userId = paramUser.id);
    JWTService.JWTResult jWTResult = null;
    boolean bool = (paramSet.contains("offline_access") && paramApplication.oauthConfiguration.generateRefreshTokens) ? true : false;
    UUID uUID = bool ? UUID.randomUUID() : null;
    paramAuthenticationType = (paramAuthenticationType != null) ? paramAuthenticationType : getAuthenticationType(paramUUID1);
    if (set.isEmpty() || set.contains("token")) {
      UUID uUID1 = paramApplication.lambdaConfiguration.accessTokenPopulateId;
      hashMap.put("sid", uUID);
      jWTResult = this.jwtService.createJWT(paramTenant, paramUser, paramAuthenticationType, paramApplication, (Map)hashMap, uUID1, paramGrantType, JWTType.AccessToken, paramSet, paramString8);
      accessToken.token = jWTResult.encodedJWT;
      accessToken.expiresIn = Integer.valueOf((int)ZonedDateTime.now(ZoneOffset.UTC).until(jWTResult.jwt.expiration, ChronoUnit.SECONDS));
      accessToken.tokenType = (paramString8 == null) ? TokenType.Bearer : TokenType.DPoP;
    } 
    if (paramSet.contains("openid") && (paramGrantType != GrantType.implicit || (paramString2 != null && paramString2.contains("id_token")))) {
      if (jWTResult != null)
        hashMap.put("at_hash", OpenIDConnect.at_hash(accessToken.token, jWTResult.algorithm)); 
      if (paramString3 != null)
        hashMap.put("nonce", paramString3); 
      hashMap.remove("sid");
      if (paramUUID2 != null)
        hashMap.put("sid", paramUUID2); 
      hashMap.remove("applicationId");
      hashMap.remove("roles");
      UUID uUID1 = paramApplication.lambdaConfiguration.idTokenPopulateId;
      accessToken.idToken = (this.jwtService.createJWT(paramTenant, paramUser, paramAuthenticationType, paramApplication, (Map)hashMap, uUID1, paramGrantType, JWTType.IdToken, paramSet, null)).encodedJWT;
    } 
    if (bool) {
      if (paramMetaData == null)
        paramMetaData = new RefreshToken.MetaData(); 
      paramMetaData.scopes = paramSet;
      if (paramString6 != null) {
        paramMetaData.device = safelyRetrieveDeviceFromMetaData(paramTenant, paramString6, ExternalIdentifier.ExternalIdType.DeviceCode);
      } else if (paramString5 != null && paramGrantType == GrantType.password) {
        paramMetaData.device = safelyRetrieveDeviceFromMetaData(paramTenant, paramString5, ExternalIdentifier.ExternalIdType.DeviceUserCode);
      } 
      paramMetaData.device.lastAccessedAddress = paramString4;
      HashMap<Object, Object> hashMap1 = new HashMap<>();
      hashMap1.put("grants", List.of(paramGrantType.grantName()));
      hashMap1.put("source", "oauth");
      if (jWTResult != null) {
        Long long_ = null;
        try {
          long_ = jWTResult.jwt.getLong("auth_time");
        } catch (Exception exception) {}
        if (long_ == null)
          long_ = Long.valueOf(jWTResult.jwt.issuedAt.toEpochSecond()); 
        hashMap1.put("auth_time", long_);
        Object object = jWTResult.jwt.getObject("amr");
        if (object != null)
          hashMap1.put("amr", object); 
      } 
      if (paramUUID2 != null)
        hashMap1.put("ssoSessionId", paramUUID2); 
      if (paramString8 != null)
        hashMap1.put("DPoPThumbprint", paramString8); 
      RefreshToken refreshToken = this.refreshTokenService.createRefreshToken(uUID, paramTenant, paramUser, paramApplication, (Map)hashMap1, paramMetaData);
      accessToken.refreshToken = refreshToken.token;
      accessToken.refreshTokenId = refreshToken.id;
    } 
    if (paramString5 != null) {
      ExternalIdentifierReaderService.ValidationResult validationResult = this.externalIdentifierReader.validate(paramTenant, paramString5, new ExternalIdentifier.ExternalIdType[] { ExternalIdentifier.ExternalIdType.DeviceUserCode });
      ExternalIdentifier externalIdentifier = validationResult.id;
      if (externalIdentifier != null) {
        approveDeviceCode(null, paramTenant, paramApplication, paramUser, externalIdentifier, paramSet);
        validateAndCompleteDeviceLink(null, paramEventInfo, externalIdentifier, paramTenant, paramUser);
      } 
    } 
    if (paramString7 != null) {
      ExternalIdentifierReaderService.ValidationResult validationResult = this.externalIdentifierReader.validate(paramTenant, paramString7, new ExternalIdentifier.ExternalIdType[] { ExternalIdentifier.ExternalIdType.PendingIdPLinkId });
      if (validationResult.id != null)
        validateAndCompleteLink(null, paramEventInfo, validationResult.id, paramTenant, paramUser, "Complete the link request started in the [" + String.valueOf(paramGrantType) + "] grant request."); 
    } 
    if (paramString6 != null)
      this.externalIdentifierService.deleteById(paramString6); 
    return accessToken;
  }
  
  public String createAuthorizationCode(Tenant paramTenant, UUID paramUUID1, String paramString1, URI paramURI, UUID paramUUID2, String paramString2, String paramString3, RefreshToken.MetaData paramMetaData, Map<String, String> paramMap) {
    ExternalIdentifier.ExternalIdData externalIdData = new ExternalIdentifier.ExternalIdData();
    if (paramMap != null) {
      Objects.requireNonNull(externalIdData);
      paramMap.forEach(externalIdData::setAttribute);
    } 
    externalIdData.setAttribute("applicationId", paramUUID1.toString());
    externalIdData.setAttribute("clientId", paramString1);
    externalIdData.setAttribute("redirectURI", paramURI.toString());
    if (paramString2 != null)
      externalIdData.setAttribute("scope", paramString2); 
    if (paramString3 != null)
      externalIdData.setAttribute("codeChallenge", paramString3); 
    if (paramMetaData != null) {
      if (paramMetaData.device.name != null)
        externalIdData.setAttribute("metaData.device.name", paramMetaData.device.name); 
      if (paramMetaData.device.type != null)
        externalIdData.setAttribute("metaData.device.type", paramMetaData.device.type); 
      if (paramMetaData.device.description != null)
        externalIdData.setAttribute("metaData.device.description", paramMetaData.device.description); 
      if (paramMetaData.device.lastAccessedAddress != null)
        externalIdData.setAttribute("metaData.device.lastAccessedAddress", paramMetaData.device.lastAccessedAddress); 
    } 
    return this.externalIdentifierService.createAuthorizationCode(paramTenant, paramUUID2, externalIdData);
  }
  
  public AccessToken createClientCredentialsAccessToken(Tenant paramTenant, Entity paramEntity, Map<String, Entity> paramMap, Map<String, Set<String>> paramMap1, Set<String> paramSet, String paramString) {
    AccessToken accessToken = (new AccessToken()).with(paramAccessToken -> paramAccessToken.clientId = paramEntity.clientId).with(paramAccessToken -> paramAccessToken.scope = (paramSet.size() > 0) ? String.join(" ", paramSet) : null);
    HashMap<Object, Object> hashMap = new HashMap<>();
    if (paramSet != null && paramSet.size() > 0)
      hashMap.put("scope", String.join(" ", (Iterable)paramSet)); 
    JWTService.JWTResult jWTResult = this.jwtService.createJWT(paramTenant, paramEntity, paramMap, paramMap1, GrantType.client_credentials, (Map)hashMap, paramString);
    accessToken.token = jWTResult.encodedJWT;
    accessToken.expiresIn = Integer.valueOf((int)ZonedDateTime.now(ZoneOffset.UTC).until(jWTResult.jwt.expiration, ChronoUnit.SECONDS));
    accessToken.tokenType = (paramString == null) ? TokenType.Bearer : TokenType.DPoP;
    return accessToken;
  }
  
  public void decodeAndRestoreStateFromCallback(Object paramObject, String paramString) {
    if (paramString == null)
      return; 
    try {
      paramString = new String(Base64.getUrlDecoder().decode(paramString.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception exception) {
      EventLogHelper.create(new EventLog(EventLogType.Error, "Unable to Base64 decode state returned from callback.\nReturned state:\n" + paramString));
      return;
    } 
    this.expressionEvaluator.setValue("state", paramObject, new String[] { null }, Collections.emptyMap());
    HashMap<Object, Object> hashMap = new HashMap<>();
    byte[] arrayOfByte = paramString.getBytes(StandardCharsets.UTF_8);
    HTTPTools.parseEncodedData(arrayOfByte, 0, arrayOfByte.length, hashMap);
    hashMap.forEach((paramString, paramList) -> this.expressionEvaluator.setValue(paramString, paramObject, (String[])paramList.toArray((Object[])new String[0]), Collections.emptyMap()));
  }
  
  public String encodeStateForRedirect(Consumer<QueryStringBuilder> paramConsumer) {
    QueryStringBuilder queryStringBuilder = QueryStringBuilder.builder();
    paramConsumer.accept(queryStringBuilder);
    return Base64.getUrlEncoder()
      .withoutPadding()
      .encodeToString(queryStringBuilder.build()
        .getBytes(StandardCharsets.UTF_8));
  }
  
  public OAuthService.OAuthTokenResult exchangeAuthorizationCode(EventInfo paramEventInfo, Tenant paramTenant, Application paramApplication, String paramString1, URI paramURI, String paramString2, String paramString3, List<URI> paramList, String paramString4) {
    Debugger debugger = new Debugger(paramApplication.oauthConfiguration.debug, "OAuth2 exchange authorization code debug log for [" + paramApplication.name + "] with clientId [" + paramString1 + "].");
    OAuthService.OAuthTokenResult oAuthTokenResult = new OAuthService.OAuthTokenResult(new OAuthError(OAuthError.OAuthErrorType.invalid_request, OAuthError.OAuthErrorReason.auth_code_not_found, "Invalid Authorization Code"));
    if (paramString4 != null)
      debugger.log("DPoP proof provided with thumbprint: " + paramString4); 
    debugger.log("Validate the provided authorization code [" + paramString2 + "].");
    ExternalIdentifierReaderService.ValidationResult validationResult = this.externalIdentifierReader.validate(paramTenant, paramString2, new ExternalIdentifier.ExternalIdType[] { ExternalIdentifier.ExternalIdType.AuthorizationCode });
    if (validationResult.id == null || validationResult.id.data == null) {
      debugger.log("Failed to exchange the authorization code. Code [" + paramString2 + "] was not found, has already been used, or has expired.")
        .done();
      return oAuthTokenResult;
    } 
    ExternalIdentifier externalIdentifier = validationResult.id;
    String str1 = externalIdentifier.getAttribute("applicationId");
    String str2 = externalIdentifier.getAttribute("authenticationType");
    String str3 = externalIdentifier.getAttribute("clientId");
    String str4 = externalIdentifier.getAttribute("codeChallenge");
    String str5 = externalIdentifier.getAttribute("dpop_jkt");
    String str6 = externalIdentifier.getAttribute("redirectURI");
    String str7 = externalIdentifier.getAttribute("scope");
    LinkedHashSet<? extends CharSequence> linkedHashSet = new LinkedHashSet(1);
    String str8 = externalIdentifier.getAttribute("sid");
    if (str5 != null) {
      if (paramString4 == null) {
        this.externalIdentifierService.deleteById(externalIdentifier.id);
        debugger.log("Failed to exchange the authorization code. A dpop_jkt thumbprint was provided during the authorization request, but a DPoP proof was omitted from this request.").done();
        return oAuthTokenResult;
      } 
      if (!str5.equals(paramString4)) {
        this.externalIdentifierService.deleteById(externalIdentifier.id);
        debugger.log("Failed to exchange the authorization code. A dpop_jkt thumbprint was provided during the authorization request, but the DPoP proof on this request does not match.").done();
        return oAuthTokenResult;
      } 
    } 
    if (str4 != null && str4.startsWith("fa-surrogate-challenge")) {
      str4 = null;
      paramString3 = null;
    } 
    if ((((str4 != null) ? 1 : 0) ^ ((paramString3 != null) ? 1 : 0)) != 0) {
      this.externalIdentifierService.deleteById(externalIdentifier.id);
      debugger.log("Failed to exchange the authorization code. A PKCE code_challenge was provided during the authorization request, but the required code_verifier was omitted from this request.")
        .done();
      return oAuthTokenResult;
    } 
    if (str4 != null) {
      String str = PKCETools.generateCodeChallenge(paramString3);
      debugger.log("Validate PKCE code_challenge [" + str4 + "] provided during the authorization request with the provided code_verifier [" + paramString3 + "]. Calculated code_challenge [" + str + "].");
      if (!str4.equals(str)) {
        this.externalIdentifierService.deleteById(externalIdentifier.id);
        debugger.log("Failed to exchange the authorization code. The code_challenge generated from the provided code_verifier was not equal to the code_challenge provided during the authorization request.")
          .done();
        return oAuthTokenResult;
      } 
    } else {
      debugger.log("PKCE not utilized on this request.");
    } 
    if (str7 != null) {
      debugger.log("Scopes requested [" + str7 + "]");
      linkedHashSet.addAll(Arrays.asList(str7.split(" ")));
    } else {
      debugger.log("No scopes requested.");
    } 
    debugger.log("Ensure the provided request parameters match those provided the authorization request.");
    if (str1 == null || str3 == null || str6 == null) {
      debugger.log("Failed to exchange the authorization code. This is quite unexpected, but the authorization request did not contain a client_id or redirect_uri.")
        .done();
      return oAuthTokenResult;
    } 
    if (!str3.equals(paramString1) || !str6.equals(paramURI.toString())) {
      this.externalIdentifierService.deleteById(externalIdentifier.id);
      debugger.log("Failed to exchange the authorization code. The " + (
          !str3.equals(paramString1) ? "client_id" : "redirect_uri") + " provided on this request did not match the value provided during the authorization request. Expected [" + (
          
          !str3.equals(paramString1) ? str3 : str6) + "] but [" + (
          !str3.equals(paramString1) ? paramString1 : paramURI.toString()) + "] was provided on this request.")
        .done();
      return oAuthTokenResult;
    } 
    HashMap<Object, Object> hashMap = new HashMap<>(2);
    List<?> list = Optional.<String>ofNullable(externalIdentifier.getAttribute("resource")).filter(paramString -> !paramString.isBlank()).map(paramString -> Arrays.<String>stream(paramString.split(" ")).map(URI::create).toList()).orElse(List.of());
    List list1 = (paramList == null) ? List.of() : paramList;
    if (!paramApplication.oauthConfiguration.authorizedResourceUris.isEmpty()) {
      String str = null;
      if (!Collections.disjoint(list, paramApplication.oauthConfiguration.authorizedResourceUris) && list1.isEmpty()) {
        str = "At least one resource from the authorization request must be provided on the token request.";
      } else {
        for (URI uRI : list1) {
          if (!paramApplication.oauthConfiguration.authorizedResourceUris.contains(uRI)) {
            str = "Invalid resource [" + String.valueOf(uRI) + "]. The requested resource is not on the list of authorized resource URIs for this application.";
            break;
          } 
          if (!list.contains(uRI)) {
            str = "Invalid resource [" + String.valueOf(uRI) + "]. The requested resource was not provided on the authorization request.";
            break;
          } 
        } 
      } 
      if (str != null) {
        this.externalIdentifierService.deleteById(externalIdentifier.id);
        debugger.log("Failed to exchange the authorization code. " + str).done();
        return new OAuthService.OAuthTokenResult(new OAuthError(OAuthError.OAuthErrorType.invalid_target, str));
      } 
      hashMap.put("aud", list1.isEmpty() ? paramString1 : Stream.concat(Stream.of(paramString1), list1.stream().map(URI::toString)).toList());
    } else {
      hashMap.put("aud", paramString1);
    } 
    Long long_ = externalIdentifier.getAttributeAsLong("authTime");
    if (long_ != null)
      hashMap.put("auth_time", long_); 
    if (!linkedHashSet.isEmpty())
      hashMap.put("scope", String.join(" ", linkedHashSet)); 
    UUID uUID1 = UUID.fromString(str1);
    if (!paramApplication.id.equals(uUID1)) {
      debugger.log("Failed to exchange the authorization code. This is quite unexpected, but the application resolved from the client_id during the authorization request does not match the value resolved during this request.")
        .done();
      return oAuthTokenResult;
    } 
    if (paramTenant == null)
      paramTenant = validationResult.tenant; 
    User user = this.userReader.retrieveById(paramTenant.id, externalIdentifier.userId);
    if (user == null) {
      debugger.log("Failed to exchange the authorization code. This is quite unexpected, but the user that requested the authorization code no longer exists.")
        .done();
      return oAuthTokenResult;
    } 
    UserRegistration userRegistration = user.getRegistrationForApplication(uUID1);
    if (userRegistration != null) {
      debugger.log("User is registered for application with Id [" + String.valueOf(uUID1) + "] the [roles] and [applicationId] claims will be added.");
      hashMap.put("applicationId", uUID1);
      hashMap.put("roles", userRegistration.roles);
    } else {
      debugger.log("User is not registered for application with Id [" + String.valueOf(uUID1) + "] the [roles] and [applicationId] claims will be omitted.");
    } 
    int i = this.externalIdentifierService.deleteById(externalIdentifier.id);
    if (i != 1) {
      debugger.log("Failed to exchange the authorization code. This is quite unexpected, but the authorization code has already been used.")
        .done();
      return oAuthTokenResult;
    } 
    boolean bool = (linkedHashSet.contains("offline_access") && paramApplication.oauthConfiguration.generateRefreshTokens) ? true : false;
    UUID uUID2 = bool ? UUID.randomUUID() : null;
    UUID uUID3 = (externalIdentifier.getAttribute("identityProviderId") == null) ? null : UUID.fromString(externalIdentifier.getAttribute("identityProviderId"));
    AuthenticationType authenticationType = (str2 != null) ? AuthenticationType.valueOf(str2) : getAuthenticationType(uUID3);
    hashMap.put("sid", uUID2);
    JWTService.JWTResult jWTResult = this.jwtService.createJWT(paramTenant, user, authenticationType, paramApplication, (Map)hashMap, paramApplication.lambdaConfiguration.accessTokenPopulateId, GrantType.authorization_code, JWTType.AccessToken, (Set)linkedHashSet, paramString4);
    int j = (int)ZonedDateTime.now(ZoneOffset.UTC).until(jWTResult.jwt.expiration, ChronoUnit.SECONDS);
    AccessToken accessToken = (new AccessToken()).with(paramAccessToken -> paramAccessToken.token = paramJWTResult.encodedJWT).with(paramAccessToken -> paramAccessToken.clientId = paramString).with(paramAccessToken -> paramAccessToken.expiresIn = Integer.valueOf(paramInt)).with(paramAccessToken -> paramAccessToken.scope = paramSet.isEmpty() ? null : String.join(" ", paramSet)).with(paramAccessToken -> paramAccessToken.redirectURI = paramURI).with(paramAccessToken -> paramAccessToken.tokenType = (paramString == null) ? TokenType.Bearer : TokenType.DPoP).with(paramAccessToken -> paramAccessToken.userId = paramExternalIdentifier.userId);
    if (linkedHashSet.contains("openid")) {
      hashMap.put("at_hash", OpenIDConnect.at_hash(accessToken.token, jWTResult.algorithm));
      hashMap.put("c_hash", OpenIDConnect.c_hash(paramString2, jWTResult.algorithm));
      String str = externalIdentifier.getAttribute("nonce");
      if (str != null)
        hashMap.put("nonce", str); 
      hashMap.remove("sid");
      if (str8 != null)
        hashMap.put("sid", str8); 
      hashMap.remove("applicationId");
      hashMap.remove("roles");
      accessToken.idToken = (this.jwtService.createJWT(paramTenant, user, authenticationType, paramApplication, (Map)hashMap, paramApplication.lambdaConfiguration.idTokenPopulateId, GrantType.authorization_code, JWTType.IdToken, (Set)linkedHashSet, null)).encodedJWT;
    } 
    if (bool) {
      RefreshToken.MetaData metaData = (new RefreshToken.MetaData()).with(paramMetaData -> paramMetaData.scopes = paramSet);
      metaData.device.name = externalIdentifier.data.attributes.get("metaData.device.name");
      metaData.device.type = externalIdentifier.data.attributes.get("metaData.device.type");
      metaData.device.description = externalIdentifier.data.attributes.get("metaData.device.description");
      metaData.device.lastAccessedAddress = externalIdentifier.data.attributes.get("metaData.device.lastAccessedAddress");
      HashMap<Object, Object> hashMap1 = new HashMap<>(Map.of("grants", List.of(GrantType.authorization_code.grantName()), "auth_time", jWTResult.jwt
            .getLong("auth_time"), "source", "oauth"));
      Object object = jWTResult.jwt.getObject("amr");
      if (object != null)
        hashMap1.put("amr", object); 
      if (str8 != null)
        hashMap1.put("ssoSessionId", str8); 
      if (paramString4 != null)
        hashMap1.put("DPoPThumbprint", paramString4); 
      if (!list.isEmpty())
        metaData.resources = (List)list; 
      RefreshToken refreshToken = this.refreshTokenService.createRefreshToken(uUID2, paramTenant, user, paramApplication, (Map)hashMap1, metaData);
      accessToken.refreshToken = refreshToken.token;
      accessToken.refreshTokenId = refreshToken.id;
    } 
    String str9 = externalIdentifier.getAttribute("userCode");
    if (str9 != null) {
      debugger.log("Request is part of a Device Authorization grant, approve the device with user_code [" + str9 + "]. If the code is expired this will be a no-op.");
      ExternalIdentifierReaderService.ValidationResult validationResult1 = this.externalIdentifierReader.validate(paramTenant, str9, new ExternalIdentifier.ExternalIdType[] { ExternalIdentifier.ExternalIdType.DeviceUserCode });
      ExternalIdentifier externalIdentifier1 = validationResult1.id;
      if (externalIdentifier1 != null) {
        approveDeviceCode(debugger, paramTenant, paramApplication, user, externalIdentifier1, (Set)linkedHashSet);
        validateAndCompleteDeviceLink(debugger, paramEventInfo, externalIdentifier1, paramTenant, user);
      } else {
        debugger.log("The user code [%s] could not be found. The code is either invalid or has expired.", new Object[] { str9 });
      } 
    } 
    String str10 = externalIdentifier.getAttribute("pendingIdPLinkId");
    if (str10 != null) {
      ExternalIdentifierReaderService.ValidationResult validationResult1 = this.externalIdentifierReader.validate(paramTenant, str10, new ExternalIdentifier.ExternalIdType[] { ExternalIdentifier.ExternalIdType.PendingIdPLinkId });
      if (validationResult1.id != null)
        validateAndCompleteLink(null, paramEventInfo, validationResult1.id, paramTenant, user, "Complete the link request started in the [" + String.valueOf(GrantType.authorization_code) + "] grant request."); 
    } 
    debugger.log("The authorization code has been successfully exchanged for an access token.")
      .done();
    return new OAuthService.OAuthTokenResult(accessToken);
  }
  
  public DeviceResponse generateDeviceResponse(Tenant paramTenant, Application paramApplication, Set<String> paramSet, RefreshToken.MetaData paramMetaData, @Nullable String paramString) {
    String str1 = paramSet.isEmpty() ? null : String.join(" ", (Iterable)paramSet);
    Debugger debugger = (new Debugger(paramApplication.oauthConfiguration.debug, "OAuth2 device code authorization debug log for [" + paramApplication.name + "] with clientId [" + paramApplication.oauthConfiguration.clientId + "].")).log("Scopes requested [" + (String)Objects.requireNonNullElse(str1, "") + "]");
    ExternalIdentifier.ExternalIdData externalIdData = (new ExternalIdentifier.ExternalIdData()).with(paramExternalIdData -> paramExternalIdData.device = paramMetaData.device);
    boolean bool = handleDeviceLinkRequest(debugger, paramTenant, paramApplication, externalIdData, paramSet);
    if (!bool) {
      paramSet.removeIf(paramString -> paramString.startsWith("idp-link:"));
      str1 = paramSet.isEmpty() ? null : String.join(" ", (Iterable)paramSet);
    } 
    String str2 = this.externalIdentifierService.createDeviceCode(paramTenant, paramApplication.id, null, (new ExternalIdentifier.ExternalIdData())
        
        .with(paramExternalIdData -> paramExternalIdData.device = paramMetaData.device)
        .setAttribute("clientId", paramApplication.oauthConfiguration.clientId)
        .setAttribute("deviceStatus", ExternalIdentifier.DeviceGrantStatus.Pending)
        .setAttribute("DPoPThumbprint", paramString)
        .setAttribute("scope", str1));
    debugger.log("Generated device code [" + str2 + "]");
    externalIdData.setAttribute("clientId", paramApplication.oauthConfiguration.clientId);
    externalIdData.setAttribute("deviceCode", str2);
    String str3 = this.externalIdentifierService.createDeviceUserCode(paramTenant, paramApplication.id, str2, null, externalIdData);
    debugger.log("Generated device user code [" + str3 + "]");
    URI uRI1 = paramApplication.oauthConfiguration.deviceVerificationURL;
    URI uRI2 = URI.create(QueryStringBuilder.builder(uRI1.toString()).with("user_code", str3).build());
    debugger.log("Base verification URI [" + String.valueOf(uRI1) + "]");
    debugger.log("Complete verification URI [" + String.valueOf(uRI2) + "]");
    DeviceResponse deviceResponse = new DeviceResponse(str2, Integer.valueOf(paramTenant.externalIdentifierConfiguration.deviceCodeTimeToLiveInSeconds), Integer.valueOf(5), str3, uRI1, uRI2);
    debugger.logObjectToJSON("Device response:\n", deviceResponse)
      .done();
    return deviceResponse;
  }
  
  public Set<String> getOAuthScopeNamesForPromptFromApplication(Application paramApplication) {
    Set<String> set = (Set)paramApplication.scopes.stream().map(paramApplicationOAuthScope -> paramApplicationOAuthScope.name).collect(Collectors.toSet());
    ProvidedScopePolicy providedScopePolicy = paramApplication.oauthConfiguration.providedScopePolicy;
    if (providedScopePolicy.address.enabled)
      set.add("address"); 
    if (providedScopePolicy.email.enabled)
      set.add("email"); 
    if (providedScopePolicy.phone.enabled)
      set.add("phone"); 
    if (providedScopePolicy.profile.enabled)
      set.add("profile"); 
    return set;
  }
  
  public void persistUserConsentChoices(Tenant paramTenant, Application paramApplication, User paramUser, Map<String, Boolean> paramMap) {
    Map map = (Map)paramMap.entrySet().stream().collect(
        
        Collectors.partitioningBy(Map.Entry::getValue, 
          
          Collectors.mapping(Map.Entry::getKey, Collectors.toSet())));
    Set<String> set1 = (Set)map.get(Boolean.valueOf(true));
    Set<String> set2 = (Set)map.get(Boolean.valueOf(false));
    ExternalIdentifier externalIdentifier = this.externalIdentifierReader.retrieveByUserIdTypeAndApplicationId(paramUser.id, ExternalIdentifier.ExternalIdType.RememberOAuthScopeConsentChoice, paramApplication.id);
    if (externalIdentifier != null) {
      Set<String> set4 = paramMap.keySet();
      Set<String> set5 = ParameterTools.splitSpaceSeparated(externalIdentifier.getAttribute("approvedScopes"));
      Set<String> set6 = ParameterTools.splitSpaceSeparated(externalIdentifier.getAttribute("declinedScopes"));
      set5.removeAll(set4);
      set6.removeAll(set4);
      set1.addAll(set5);
      set2.addAll(set6);
    } 
    Set<String> set3 = getOAuthScopeNamesForPromptFromApplication(paramApplication);
    set1.retainAll(set3);
    set2.retainAll(set3);
    if (externalIdentifier != null) {
      externalIdentifier.data.setAttribute("approvedScopes", String.join(" ", (Iterable)set1))
        .setAttribute("declinedScopes", String.join(" ", (Iterable)set2));
      this.externalIdentifierService.update(externalIdentifier);
    } else {
      this.externalIdentifierService.createRememberOAuthScopeConsentChoice(paramTenant, paramUser.id, paramApplication.id, (new ExternalIdentifier.ExternalIdData())
          
          .setAttribute("approvedScopes", String.join(" ", (Iterable)set1))
          .setAttribute("declinedScopes", String.join(" ", (Iterable)set2)));
    } 
  }
  
  public OAuthService.OAuthTokenResult refreshAccessToken(Tenant paramTenant, Application paramApplication, User paramUser, RefreshToken paramRefreshToken, Set<String> paramSet, String paramString1, Integer paramInteger, String paramString2, EventInfo paramEventInfo, @Nullable String paramString3, @Nullable List<URI> paramList) {
    List<URI> list = resolveResourcesForRefreshGrant(paramApplication, paramRefreshToken, paramList);
    if (list == null)
      return new OAuthService.OAuthTokenResult(new OAuthError(OAuthError.OAuthErrorType.invalid_target, "Invalid resource. The requested resource was not provided on the authorization request or is not on the list of authorized resource URIs for this application.")); 
    JWTService.RefreshResult refreshResult = this.jwtService.refreshAccessToken(paramTenant, paramUser, paramApplication, paramRefreshToken, paramString2, paramSet, paramInteger, () -> supplyClaimsForRefreshToken(paramUser, paramRefreshToken, paramList), paramEventInfo, paramString3);
    if (refreshResult == null)
      return new OAuthService.OAuthTokenResult(new OAuthError(OAuthError.OAuthErrorType.invalid_grant, OAuthError.OAuthErrorReason.refresh_token_not_found, "The refresh_token is invalid.")); 
    if (paramString1 != null) {
      ExternalIdentifierReaderService.ValidationResult validationResult = this.externalIdentifierReader.validate(paramTenant, paramString1, new ExternalIdentifier.ExternalIdType[] { ExternalIdentifier.ExternalIdType.DeviceUserCode });
      ExternalIdentifier externalIdentifier = validationResult.id;
      if (externalIdentifier != null) {
        approveDeviceCode(null, paramTenant, paramApplication, paramUser, externalIdentifier, paramSet);
        validateAndCompleteDeviceLink(null, paramEventInfo, externalIdentifier, paramTenant, paramUser);
      } 
    } 
    refreshResult.accessToken.refreshToken = refreshResult.refreshToken.token;
    refreshResult.accessToken.refreshTokenId = refreshResult.refreshToken.id;
    return new OAuthService.OAuthTokenResult(refreshResult.accessToken);
  }
  
  public OAuthService.OAuthValidationResult rehydrateIdPInitiatedLoginToOAuth(UUID paramUUID, String paramString1, URI paramURI, String paramString2) {
    OAuthService.OAuthValidationResult oAuthValidationResult = new OAuthService.OAuthValidationResult();
    oAuthValidationResult.client_id = paramString1;
    oAuthValidationResult.tenantId = paramUUID;
    validateClientId(oAuthValidationResult);
    if (oAuthValidationResult.error != null)
      return oAuthValidationResult; 
    validateAndResolveTenantForUniversalApplication(paramUUID, oAuthValidationResult);
    if (oAuthValidationResult.error != null)
      return oAuthValidationResult; 
    URI uRI = (paramString2 != null) ? URI.create(paramString2) : paramURI;
    oAuthValidationResult
      
      .redirect_uri = (uRI != null) ? uRI : oAuthValidationResult.application.oauthConfiguration.getFirstAuthorizedRedirectURLIgnoringPatterns();
    if (oAuthValidationResult.client_id.equals(Application.FUSIONAUTH_APP_ID.toString()))
      oAuthValidationResult.redirect_uri = URI.create("/admin/login"); 
    return oAuthValidationResult;
  }
  
  public OAuthService.OAuthValidationResult validateAppCallbackHandle(String paramString1, String paramString2, URI paramURI) {
    OAuthService.OAuthValidationResult oAuthValidationResult = new OAuthService.OAuthValidationResult();
    oAuthValidationResult.client_id = paramString1;
    validateClientId(oAuthValidationResult);
    if (oAuthValidationResult.error != null)
      return oAuthValidationResult; 
    if (paramURI == null)
      return oAuthValidationResult.withError(new OAuthError(OAuthError.OAuthErrorType.invalid_grant, OAuthError.OAuthErrorReason.missing_redirect_uri, "The request is missing a required parameter: redirect_uri")); 
    boolean bool = validateRedirectURI(oAuthValidationResult, paramURI);
    if (!bool)
      return oAuthValidationResult.withError(new OAuthError(OAuthError.OAuthErrorType.invalid_request, OAuthError.OAuthErrorReason.invalid_redirect_uri, "Invalid redirect_uri " + String.valueOf(paramURI))); 
    return oAuthValidationResult;
  }
  
  public OAuthService.OAuthValidationResult validateAppLogoutRequest(String paramString) {
    OAuthService.OAuthValidationResult oAuthValidationResult = new OAuthService.OAuthValidationResult();
    oAuthValidationResult.client_id = paramString;
    validateClientId(oAuthValidationResult);
    return oAuthValidationResult;
  }
  
  public OAuthService.OAuthValidationResult validateAppRedirectRequest(String paramString, URI paramURI) {
    OAuthService.OAuthValidationResult oAuthValidationResult = new OAuthService.OAuthValidationResult();
    oAuthValidationResult.client_id = paramString;
    oAuthValidationResult.redirect_uri = paramURI;
    validateClientId(oAuthValidationResult);
    if (oAuthValidationResult.error != null)
      return oAuthValidationResult; 
    if (oAuthValidationResult.redirect_uri == null) {
      oAuthValidationResult.redirect_uri = oAuthValidationResult.application.oauthConfiguration.getFirstAuthorizedRedirectURLIgnoringPatterns();
    } else if (!validateRedirectURI(oAuthValidationResult, paramURI)) {
      oAuthValidationResult.appCallback = paramURI;
    } 
    return oAuthValidationResult;
  }
  
  public OAuthService.OAuthValidationResult validateAppRefreshRequest(String paramString) {
    OAuthService.OAuthValidationResult oAuthValidationResult = new OAuthService.OAuthValidationResult();
    oAuthValidationResult.client_id = paramString;
    validateClientId(oAuthValidationResult);
    return oAuthValidationResult;
  }
  
  public OAuthService.OAuthValidationResult validateAuthorizeRequest(UUID paramUUID, String paramString1, String paramString2, URI paramURI, String paramString3, String paramString4, String paramString5, String paramString6, String paramString7, String paramString8, boolean paramBoolean, List<URI> paramList) {
    if (paramURI == null)
      return (new OAuthService.OAuthValidationResult(new OAuthError(OAuthError.OAuthErrorType.invalid_request, OAuthError.OAuthErrorReason.missing_redirect_uri, "The request is missing a required parameter: redirect_uri")))
        .setDoNotRedirect(true); 
    OAuthService.OAuthValidationResult oAuthValidationResult = new OAuthService.OAuthValidationResult();
    oAuthValidationResult.client_id = paramString1;
    oAuthValidationResult.tenantId = paramUUID;
    validateClientId(oAuthValidationResult);
    if (oAuthValidationResult.error != null)
      return oAuthValidationResult; 
    validateAndResolveTenantForUniversalApplication(paramUUID, oAuthValidationResult);
    if (oAuthValidationResult.error != null)
      return oAuthValidationResult; 
    if (paramString7 != null) {
      if (!PKCETools.validateCodeVerifier(paramString7))
        return oAuthValidationResult.withError(new OAuthError(OAuthError.OAuthErrorType.invalid_request, OAuthError.OAuthErrorReason.invalid_pkce_code_challenge, "Invalid code_challenge, see https://tools.ietf.org/html/rfc7636#section-4.2 for requirements."))
          .setDoNotRedirect(true); 
      if (paramString8 != null && 
        !paramString8.equals("S256"))
        return oAuthValidationResult.withError(new OAuthError(OAuthError.OAuthErrorType.invalid_request, OAuthError.OAuthErrorReason.invalid_pkce_code_challenge_method, "The code_challenge_method must be S256."))
          .setDoNotRedirect(true); 
    } 
    oAuthValidationResult.scopes = ParameterTools.splitSpaceSeparated(paramString6);
    boolean bool = validateAuthorizedRedirectURI(oAuthValidationResult, paramURI);
    if (!bool)
      return oAuthValidationResult.withError(new OAuthError(OAuthError.OAuthErrorType.invalid_request, OAuthError.OAuthErrorReason.invalid_redirect_uri, "Invalid redirect_uri " + String.valueOf(paramURI)))
        .setDoNotRedirect(true); 
    if (paramString4 == null)
      return oAuthValidationResult.withError(new OAuthError(OAuthError.OAuthErrorType.invalid_request, OAuthError.OAuthErrorReason.missing_response_type, "The request is missing a required parameter: response_type")); 
    if (paramString4.equals("code")) {
      oAuthValidationResult.grantType = GrantType.authorization_code;
    } else if (ValidImplicitResponseTypes.contains(paramString4)) {
      oAuthValidationResult.grantType = GrantType.implicit;
    } 
    if (oAuthValidationResult.grantType == GrantType.authorization_code && paramString7 == null && oAuthValidationResult.application.oauthConfiguration.proofKeyForCodeExchangePolicy == ProofKeyForCodeExchangePolicy.Required)
      return oAuthValidationResult.withError(new OAuthError(OAuthError.OAuthErrorType.invalid_request, OAuthError.OAuthErrorReason.missing_code_challenge, "The request is missing a required parameter: code_challenge")); 
    if (oAuthValidationResult.scopes.contains("openid")) {
      if (!ValidOIDCResponseTypes.contains(paramString4)) {
        String str = (oAuthValidationResult.grantType == GrantType.implicit) ? "Parameter response_type must be set to 'id_token' or 'id_token token' when requesting the implicit grant." : "Parameter response_type must be set to 'code', 'token', 'id_token' or 'id_token token'.";
        return oAuthValidationResult.withError(new OAuthError(OAuthError.OAuthErrorType.unsupported_response_type, OAuthError.OAuthErrorReason.invalid_response_type, str));
      } 
    } else if (!ValidOAuth2ResponseTypes.contains(paramString4)) {
      String str = (oAuthValidationResult.grantType == GrantType.implicit) ? "Parameter response_type must be set to token when requesting the implicit grant." : "Parameter response_type must be set to code or token.";
      return oAuthValidationResult.withError(new OAuthError(OAuthError.OAuthErrorType.unsupported_response_type, OAuthError.OAuthErrorReason.invalid_response_type, str));
    } 
    if (paramString3 != null) {
      if (!ValidResponseModes.contains(paramString3))
        return oAuthValidationResult.withError(new OAuthError(OAuthError.OAuthErrorType.invalid_request, OAuthError.OAuthErrorReason.invalid_response_mode, "Parameter response_mode must be set to 'form_post', 'fragment', or 'query'.")); 
      if (paramString3.equals("query") && !paramString4.equals("code"))
        return oAuthValidationResult.withError(new OAuthError(OAuthError.OAuthErrorType.invalid_request, OAuthError.OAuthErrorReason.invalid_response_mode, "Parameter response_mode of 'query' may only be use with a response_type of 'code'.")); 
    } 
    boolean bool1 = ((isSAMLSubOAuthRedirect(paramURI, oAuthValidationResult.tenant) || isAccountLoginRedirect(paramURI)) && oAuthValidationResult.grantType == GrantType.authorization_code) ? true : false;
    if (!bool1) {
      if (oAuthValidationResult.grantType == GrantType.authorization_code && !oAuthValidationResult.application.oauthConfiguration.enabledGrants.contains(GrantType.authorization_code))
        return oAuthValidationResult.withError(new OAuthError(OAuthError.OAuthErrorType.unauthorized_client, OAuthError.OAuthErrorReason.grant_type_disabled, "The [authorization_code] Authorization Code grant has been disabled for this client.")); 
      if (oAuthValidationResult.grantType == GrantType.implicit && !oAuthValidationResult.application.oauthConfiguration.enabledGrants.contains(GrantType.implicit))
        return oAuthValidationResult.withError(new OAuthError(OAuthError.OAuthErrorType.unauthorized_client, OAuthError.OAuthErrorReason.grant_type_disabled, "The [implicit] Implicit grant has been disabled for this client.")); 
    } 
    if (paramBoolean && 
      !oAuthValidationResult.application.oauthConfiguration.authorizedOriginURLs.isEmpty()) {
      String str = StringTools.defaultIfNull(this.request.getHeader("Origin"), this.request.getHeader("Referer"));
      if (str != null) {
        if (str.equals("null"))
          return oAuthValidationResult.withError(new OAuthError(OAuthError.OAuthErrorType.invalid_request, OAuthError.OAuthErrorReason.invalid_origin_opaque, "The User Agent sent an opaque (null) Origin header. The request origin cannot be authorized.")); 
        URI uRI1 = URITools.sanitizeOrigin(str);
        URI uRI2 = URI.create(this.request.getBaseURL());
        if (!URITools.isSameBaseURL(uRI1, uRI2) && 
          !validateOrigin(oAuthValidationResult.application.oauthConfiguration, uRI1))
          return oAuthValidationResult.withError(new OAuthError(OAuthError.OAuthErrorType.invalid_request, OAuthError.OAuthErrorReason.invalid_origin, "Invalid origin uri " + String.valueOf(uRI1))); 
      } 
    } 
    validateScopes(oAuthValidationResult);
    oAuthValidationResult.prompts.addAll(ParameterTools.splitSpaceSeparated(paramString2));
    if (oAuthValidationResult.prompts.contains("none") && oAuthValidationResult.prompts.size() > 1)
      return oAuthValidationResult.withError(new OAuthError(OAuthError.OAuthErrorType.invalid_request, OAuthError.OAuthErrorReason.invalid_prompt, "Parameter prompt may only contain a single value when requesting 'none'.")); 
    if (oAuthValidationResult.error == null && paramList != null && !paramList.isEmpty() && !oAuthValidationResult.application.oauthConfiguration.authorizedResourceUris.isEmpty()) {
      for (URI uRI : paramList) {
        if (!oAuthValidationResult.application.oauthConfiguration.authorizedResourceUris.contains(uRI))
          return oAuthValidationResult.withError(new OAuthError(OAuthError.OAuthErrorType.invalid_target, "Invalid resource [" + String.valueOf(uRI) + "]. The requested resource is not on the list of authorized resource URIs for this application.")); 
      } 
      oAuthValidationResult.validatedResources = List.copyOf(paramList);
    } 
    return oAuthValidationResult;
  }
  
  public OAuthService.BootstrapValidationResult validateBootstrapJWT(String paramString, Tenant paramTenant) {
    OAuthService.BootstrapValidationResult bootstrapValidationResult = new OAuthService.BootstrapValidationResult();
    ValidatedJWTResult validatedJWTResult = this.jwtService.validateJWT(paramString, FusionAuthJWTDecoder.JWTConstraints.UserAccessToken, new JWTValidationContext.SuppliedTenantAudienceApplicationIfResolvable(paramTenant));
    if (!validatedJWTResult.valid) {
      logger.debug("Bootstrap JWT. {}", validatedJWTResult.reason);
      return bootstrapValidationResult;
    } 
    bootstrapValidationResult.jwt = validatedJWTResult.jwt;
    UUID uUID = this.fusionAuthConfiguration.allowSubClaimOverride() ? ClaimTools.resolveUserId(bootstrapValidationResult.jwt) : UUIDTools.fromString(bootstrapValidationResult.jwt.subject);
    if (bootstrapValidationResult.jwt.getOtherClaims().get("cnf") != null) {
      logger.debug("Bootstrap JWT is invalid. The JWT is sender constrained.");
      return bootstrapValidationResult;
    } 
    if (!bootstrapValidationResult.jwt.issuer.equals(paramTenant.issuer)) {
      logger.debug("Bootstrap JWT is invalid. The [iss] claim is invalid.");
      return bootstrapValidationResult;
    } 
    if (uUID == null) {
      logger.debug("Bootstrap JWT is invalid. The user identifier claim(s) ([fa_uid] or [sub]) are invalid.");
      return bootstrapValidationResult;
    } 
    try {
      Long long_ = bootstrapValidationResult.jwt.getLong("auth_time");
      bootstrapValidationResult.originalAuthTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(long_.longValue()), ZoneOffset.UTC);
    } catch (Exception exception) {
      logger.debug("Bootstrap JWT invalid. The [auth_time] claim is invalid. Reason [{}]", exception.getMessage());
      return bootstrapValidationResult;
    } 
    User user = this.userReader.retrieveById(paramTenant.id, uUID);
    if (user == null) {
      logger.debug("Bootstrap JWT is invalid. A user could not be found using the [sub] claim value of [{}].", uUID);
      return bootstrapValidationResult;
    } 
    bootstrapValidationResult.user = (new User(user)).secure();
    String str = bootstrapValidationResult.jwt.getString("authenticationType");
    if (str != null)
      try {
        bootstrapValidationResult.authenticationType = AuthenticationType.valueOf(str);
      } catch (IllegalArgumentException illegalArgumentException) {} 
    return bootstrapValidationResult;
  }
  
  public OAuthService.OAuthValidationResult validateConsentedScopes(Application paramApplication, Set<String> paramSet1, Set<String> paramSet2) {
    OAuthService.OAuthValidationResult oAuthValidationResult = new OAuthService.OAuthValidationResult();
    if (!paramSet1.containsAll(paramSet2)) {
      HashSet<String> hashSet1 = new HashSet<>(paramSet2);
      hashSet1.removeAll(paramSet1);
      oAuthValidationResult.withError(new OAuthError(OAuthError.OAuthErrorType.invalid_scope, OAuthError.OAuthErrorReason.unknown_scope, 
            String.format("Unrequested scopes. A user attempted to consent to the following scopes, which were not requested: [%s].", new Object[] { String.join(", ", (Iterable)hashSet1) })));
      return oAuthValidationResult;
    } 
    Set<String> set = getRequiredOAuthScopeNames(paramApplication, paramSet1);
    HashSet<String> hashSet = new HashSet<>(paramSet1);
    hashSet.retainAll(set);
    hashSet.removeAll(paramSet2);
    if (hashSet.isEmpty()) {
      oAuthValidationResult.scopes = (Set<String>)paramSet2.stream().sorted().collect(Collectors.toCollection(LinkedHashSet::new));
    } else {
      oAuthValidationResult.withError(new OAuthError(OAuthError.OAuthErrorType.invalid_scope, OAuthError.OAuthErrorReason.missing_required_scope, 
            String.format("Missing required scopes. The scopes [%s] are required but not granted.", new Object[] { String.join(", ", (Iterable)hashSet) })));
    } 
    return oAuthValidationResult;
  }
  
  public OAuthService.OAuthValidationResult validateDeviceApproveRequest(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, UUID paramUUID) {
    JWT jWT;
    OAuthService.OAuthValidationResult oAuthValidationResult = validateUserCodeInfoRequest(paramString1, paramString2, paramString3, paramString5, paramUUID);
    if (oAuthValidationResult.error != null)
      return oAuthValidationResult; 
    if (paramString4 == null)
      return oAuthValidationResult.withError(new OAuthError(OAuthError.OAuthErrorType.invalid_request, OAuthError.OAuthErrorReason.missing_token, "The request is missing a required parameter: token")); 
    try {
      ValidatedJWTResult validatedJWTResult = this.jwtService.validateJWT(paramString4, FusionAuthJWTDecoder.JWTConstraints.UserAccessTokenWithAudience, new JWTValidationContext.SuppliedTenantAndApplication(oAuthValidationResult.tenant, oAuthValidationResult.application));
      if (!validatedJWTResult.valid) {
        if (validatedJWTResult.exception != null)
          throw validatedJWTResult.exception; 
        String str = ("aud".equals(validatedJWTResult.requiredClaim) || validatedJWTResult.reasonCode == ValidatedJWTResult.ReasonCode.invalidAudience) ? validatedJWTResult.reason : "The token is not suitable for the requested use.";
        return oAuthValidationResult.withError(new OAuthError(OAuthError.OAuthErrorType.invalid_token, OAuthError.OAuthErrorReason.access_token_invalid, str));
      } 
      jWT = validatedJWTResult.jwt;
    } catch (Exception exception) {
      return oAuthValidationResult.withError(OAuthService.handleJWTDecodingExceptions(exception));
    } 
    UUID uUID = UUIDTools.fromString(jWT.subject);
    oAuthValidationResult.user = this.userReader.retrieveById(oAuthValidationResult.tenant.id, uUID);
    if (oAuthValidationResult.user == null || !oAuthValidationResult.user.active || (oAuthValidationResult.user.expiry != null && oAuthValidationResult.user.expiry.isBefore(ZonedDateTime.now(ZoneOffset.UTC))))
      return oAuthValidationResult.withError(new OAuthError(OAuthError.OAuthErrorType.invalid_token, OAuthError.OAuthErrorReason.access_token_invalid, "The [sub] claim is invalid.")); 
    oAuthValidationResult.scopes = ParameterTools.splitSpaceSeparated(jWT.getString("scope"));
    return oAuthValidationResult;
  }
  
  public OAuthService.OAuthValidationResult validateDeviceRequest(UUID paramUUID, String paramString1, String paramString2, HTTPRequest paramHTTPRequest) {
    OAuthService.OAuthValidationResult oAuthValidationResult = new OAuthService.OAuthValidationResult();
    oAuthValidationResult.client_id = paramString1;
    oAuthValidationResult.tenantId = paramUUID;
    validateClientId(oAuthValidationResult);
    if (oAuthValidationResult.error != null)
      return oAuthValidationResult; 
    validateAndResolveTenantForUniversalApplication(paramUUID, oAuthValidationResult);
    if (oAuthValidationResult.error != null)
      return oAuthValidationResult; 
    if (!oAuthValidationResult.application.oauthConfiguration.enabledGrants.contains(GrantType.device_code))
      return oAuthValidationResult.withError(new OAuthError(OAuthError.OAuthErrorType.unauthorized_client, OAuthError.OAuthErrorReason.grant_type_disabled, "The [" + GrantType.device_code.grantName() + "] Device code grant has been disabled for this client.")); 
    DPoPService.DPoPResult dPoPResult = this.dPoPService.parseDPoP(paramHTTPRequest);
    if (dPoPResult.isError()) {
      oAuthValidationResult.error = dPoPResult.error();
      return oAuthValidationResult;
    } 
    oAuthValidationResult.dPoPThumbprint = dPoPResult.dPoPThumbprint();
    oAuthValidationResult.grantType = GrantType.device_code;
    oAuthValidationResult.scopes = ParameterTools.splitSpaceSeparated(paramString2);
    validateScopes(oAuthValidationResult);
    return oAuthValidationResult;
  }
  
  public OAuthService.OAuthValidationResult validateIntrospectRequest(String paramString1, String paramString2, String paramString3, UUID paramUUID, String paramString4, String paramString5) {
    OAuthService.OAuthValidationResult oAuthValidationResult = new OAuthService.OAuthValidationResult();
    oAuthValidationResult.client_id = paramString2;
    oAuthValidationResult.client_secret = paramString3;
    oAuthValidationResult.tenantId = paramUUID;
    if (paramString1 != null) {
      OAuthService.CredentialResult credentialResult = OAuthService.parseCredentials(paramString1, paramString2);
      oAuthValidationResult.visit(credentialResult);
      if (oAuthValidationResult.error != null)
        return oAuthValidationResult; 
    } 
    if (oAuthValidationResult.client_id == null)
      return oAuthValidationResult.withError(new OAuthError(OAuthError.OAuthErrorType.invalid_request, OAuthError.OAuthErrorReason.missing_client_id, "The request is missing a required parameter: client_id"))
        .setDoNotRedirect(true); 
    if (paramString4 == null) {
      oAuthValidationResult.withError(new OAuthError(OAuthError.OAuthErrorType.invalid_request, OAuthError.OAuthErrorReason.missing_token, "The request is missing a required parameter: token"));
      return oAuthValidationResult;
    } 
    JWT jWT = null;
    if (paramString5 != null) {
      if (!TokenTypeHints.contains(paramString5)) {
        oAuthValidationResult.withError(new OAuthError(OAuthError.OAuthErrorType.unsupported_token_type, OAuthError.OAuthErrorReason.unsupported_token_type, "Unsupported token type specified in the parameter: token_type_hint. The value [" + paramString5 + "] is not a supported token type."));
        return oAuthValidationResult;
      } 
      if ("access_token".equals(paramString5) || "id_token".equals(paramString5)) {
        JWTType jWTType = "access_token".equals(paramString5) ? JWTType.AccessToken : JWTType.IdToken;
        try {
          jWT = JWTUtils.decodePayload(paramString4);
        } catch (Exception exception) {}
        if (jWT == null || JWTType.isNot(jWT, jWTType)) {
          oAuthValidationResult.withError(new OAuthError(OAuthError.OAuthErrorType.invalid_request, OAuthError.OAuthErrorReason.token_type_hint_mismatch, "The provided token is not an [" + paramString5 + "] as specified in the [token_type_hint]."));
          return oAuthValidationResult;
        } 
      } 
    } 
    if (!"refresh_token".equals(paramString5))
      try {
        if (jWT == null)
          jWT = JWTUtils.decodePayload(paramString4); 
        oAuthValidationResult.grantType = ClaimTools.getPrimaryGrantType(jWT);
      } catch (Exception exception) {
        oAuthValidationResult.unsafeIntrospectDecodeFailed = true;
        return oAuthValidationResult;
      }  
    if (oAuthValidationResult.grantType == GrantType.client_credentials) {
      oAuthValidationResult.error = commonValidateClientCredentials(oAuthValidationResult);
      if (oAuthValidationResult.error != null)
        return oAuthValidationResult; 
    } else {
      validateClientId(oAuthValidationResult);
      if (oAuthValidationResult.error != null)
        return oAuthValidationResult; 
      validateAndResolveTenantForUniversalApplication(paramUUID, oAuthValidationResult);
      if (oAuthValidationResult.error != null)
        return oAuthValidationResult; 
      if (oAuthValidationResult.client_secret != null && !oAuthValidationResult.client_secret.equals(oAuthValidationResult.application.oauthConfiguration.clientSecret)) {
        oAuthValidationResult.error = new OAuthError(OAuthError.OAuthErrorType.invalid_client, OAuthError.OAuthErrorReason.invalid_client_authentication, "Invalid client authentication credentials.");
        return oAuthValidationResult;
      } 
    } 
    if ("refresh_token".equals(paramString5)) {
      RefreshToken refreshToken = this.refreshTokenService.retrieveRefreshToken(paramString4);
      if (refreshToken != null)
        if (refreshToken.applicationId != null && refreshToken.applicationId.equals(oAuthValidationResult.application.id))
          if (!refreshToken.isExpired(oAuthValidationResult.tenant, oAuthValidationResult.application))
            oAuthValidationResult.refreshToken = refreshToken;   
    } 
    return oAuthValidationResult;
  }
  
  public OAuthService.OAuthValidationResult validateLogoutRequest(UUID paramUUID, String paramString1, String paramString2, String paramString3, SSOService.SSOSession paramSSOSession) {
    if (paramString1 == null && paramString2 == null)
      return new OAuthService.OAuthValidationResult(); 
    OAuthService.OAuthValidationResult oAuthValidationResult = validateAndResolveApplicationForLogout(paramString1, paramString2);
    if (oAuthValidationResult.error != null)
      return oAuthValidationResult; 
    validateAndResolveTenantForUniversalApplication(paramUUID, oAuthValidationResult);
    if (oAuthValidationResult.error != null)
      return oAuthValidationResult; 
    String str1 = null;
    boolean bool = false;
    URI uRI1 = null;
    if (paramString3 != null) {
      boolean bool1 = false;
      try {
        uRI1 = new URI(paramString3);
        bool1 = validatePostLogoutRedirectURI(oAuthValidationResult, uRI1);
      } catch (URISyntaxException uRISyntaxException) {}
      if (!bool1)
        return new OAuthService.OAuthValidationResult(new OAuthError(OAuthError.OAuthErrorType.invalid_request, OAuthError.OAuthErrorReason.invalid_post_logout_redirect_uri, "Invalid post_logout_redirect_uri " + paramString3)); 
      bool = true;
      str1 = uRI1.toString();
    } 
    if (uRI1 == null) {
      if (oAuthValidationResult.application.oauthConfiguration.logoutURL != null && 

        
        !oAuthValidationResult.application.oauthConfiguration.logoutURL.getPath().startsWith("/app/logout")) {
        uRI1 = oAuthValidationResult.application.oauthConfiguration.logoutURL;
      } else if (oAuthValidationResult.tenant.logoutURL != null) {
        uRI1 = oAuthValidationResult.tenant.logoutURL;
      } 
      if (uRI1 != null)
        str1 = uRI1.toString(); 
    } 
    if (oAuthValidationResult.application.oauthConfiguration.logoutBehavior.equals(LogoutBehavior.RedirectOnly))
      return new OAuthService.OAuthValidationResult(oAuthValidationResult.tenant, oAuthValidationResult.application, str1); 
    String str2 = oAuthValidationResult.sid;
    URI uRI2 = uRI1;
    List<?> list = (paramSSOSession != null) ? getRegisteredUniversalApplications(paramSSOSession.user) : List.of();
    Stream<?> stream = this.applicationCache.getAll().stream().filter(paramApplication -> (paramApplication.tenantId != null && paramApplication.tenantId.equals(paramOAuthValidationResult.tenant.id)));
    Stream.concat(list.stream(), stream)
      .filter(paramApplication -> (paramApplication.oauthConfiguration.logoutURL != null))
      .filter(paramApplication -> !paramApplication.oauthConfiguration.logoutURL.equals(paramURI))
      .forEach(paramApplication -> {
          String str = List.of(Application.FUSIONAUTH_APP_ID, this.tenantManagerApplicationId).contains(paramApplication.id) ? paramApplication.oauthConfiguration.logoutURL.toString() : QueryStringBuilder.builder(paramApplication.oauthConfiguration.logoutURL.toString()).with("iss", (paramString == null) ? null : paramOAuthValidationResult.tenant.issuer).with("sid", paramString).build();
          paramOAuthValidationResult.logoutURLs.put(paramApplication.id, str);
        });
    if (oAuthValidationResult.tenant.logoutURL != null && !oAuthValidationResult.tenant.logoutURL.equals(uRI2))
      oAuthValidationResult.logoutURLs.put((UUID)null, QueryStringBuilder.builder(oAuthValidationResult.tenant.logoutURL.toString())
          
          .with("iss", (str2 == null) ? null : oAuthValidationResult.tenant.issuer)
          .with("sid", str2)
          .build()); 
    if (!bool && !List.of(Application.FUSIONAUTH_APP_ID, this.tenantManagerApplicationId).contains(oAuthValidationResult.application.id)) {
      oAuthValidationResult


        
        .redirectURL = QueryStringBuilder.builder(StringTools.defaultIfEmpty(str1, "/")).with("iss", (str2 == null) ? null : oAuthValidationResult.tenant.issuer).with("sid", str2).build();
    } else {
      oAuthValidationResult.redirectURL = str1;
    } 
    return oAuthValidationResult;
  }
  
  public boolean validateOrigin(OAuth2Configuration paramOAuth2Configuration, URI paramURI) {
    Map map = (Map)paramOAuth2Configuration.authorizedOriginURLs.stream().collect(Collectors.groupingBy(paramURI -> Boolean.valueOf(paramURI.toString().contains("*"))));
    List list = (List)map.getOrDefault(Boolean.valueOf(false), Collections.emptyList());
    boolean bool = list.stream().anyMatch(paramURI2 -> {
          boolean bool = (paramURI1.getScheme().equalsIgnoreCase(paramURI2.getScheme()) && paramURI1.getHost().equalsIgnoreCase(paramURI2.getHost())) ? true : false;
          return !bool ? false : ((paramURI2.getPort() == -1) ? true : ((paramURI1.getPort() == paramURI2.getPort())));
        });
    if (bool || paramOAuth2Configuration.authorizedURLValidationPolicy == Oauth2AuthorizedURLValidationPolicy.ExactMatch)
      return bool; 
    return URITools.anyMatch(paramURI, (List<URI>)map.getOrDefault(Boolean.valueOf(true), Collections.emptyList()));
  }
  
  public OAuthService.OAuthValidationResult validateSelfServiceRequest(UUID paramUUID, String paramString) {
    OAuthService.OAuthValidationResult oAuthValidationResult = new OAuthService.OAuthValidationResult();
    oAuthValidationResult.client_id = paramString;
    oAuthValidationResult.tenantId = paramUUID;
    validateClientId(oAuthValidationResult);
    if (oAuthValidationResult.error != null)
      return oAuthValidationResult; 
    validateAndResolveTenantForUniversalApplication(paramUUID, oAuthValidationResult);
    if (oAuthValidationResult.error != null)
      return oAuthValidationResult; 
    return oAuthValidationResult;
  }
  
  public OAuthService.OAuthValidationResult validateTokenRequest(UUID paramUUID, String paramString1, String paramString2, URI paramURI, String paramString3, String paramString4, String paramString5, String paramString6, String paramString7, String paramString8, String paramString9, String paramString10, Integer paramInteger, HTTPRequest paramHTTPRequest) {
    if (paramString5 == null)
      return new OAuthService.OAuthValidationResult(new OAuthError(OAuthError.OAuthErrorType.invalid_request, OAuthError.OAuthErrorReason.missing_grant_type, "The request is missing a required parameter: grant_type")); 
    GrantType grantType = GrantType.forValue(paramString5);
    if (!VALID_TOKEN_ENDPOINT_GRANT_TYPES.contains(grantType))
      return new OAuthService.OAuthValidationResult(new OAuthError(OAuthError.OAuthErrorType.unsupported_grant_type, "The requested authorization grant type is not supported by the authorization server. Parameter grant_type must be set to authorization_code, client_credentials, password, refresh_token or urn:ietf:params:oauth:grant-type:device_code.")); 
    OAuthService.OAuthValidationResult oAuthValidationResult = new OAuthService.OAuthValidationResult();
    oAuthValidationResult.grantType = grantType;
    oAuthValidationResult.scopes = ParameterTools.splitSpaceSeparated(paramString6);
    oAuthValidationResult.tenantId = paramUUID;
    if (paramString7 != null) {
      OAuthService.CredentialResult credentialResult = OAuthService.parseCredentials(paramString7, paramString1);
      oAuthValidationResult.visit(credentialResult);
      if (oAuthValidationResult.error != null)
        return oAuthValidationResult; 
    } else {
      oAuthValidationResult.client_id = paramString1;
      oAuthValidationResult.client_secret = paramString2;
    } 
    DPoPService.DPoPResult dPoPResult = this.dPoPService.parseDPoP(paramHTTPRequest);
    if (dPoPResult != DPoPService.DPoPResult.None && ReactorStatusValidator.isNotLicensedFor(this.reactorStatusService.retrieveStatus(), paramReactorStatus -> paramReactorStatus.dPoP)) {
      OAuthError oAuthError = new OAuthError(OAuthError.OAuthErrorType.not_licensed, OAuthError.OAuthErrorReason.not_licensed, "DPoP is only supported with a valid license");
      return new OAuthService.OAuthValidationResult(oAuthError);
    } 
    if (dPoPResult.isError()) {
      oAuthValidationResult.error = dPoPResult.error();
      return oAuthValidationResult;
    } 
    oAuthValidationResult.dPoPThumbprint = dPoPResult.dPoPThumbprint();
    if (grantType == GrantType.authorization_code)
      return validateTokenEndpointAuthorizationGrant(oAuthValidationResult, paramURI, paramString3, paramString8); 
    if (grantType == GrantType.refresh_token)
      return validateTokenEndpointRefreshTokenGrant(oAuthValidationResult, paramString4, paramString10, paramInteger); 
    if (grantType == GrantType.device_code)
      return validateTokenEndpointDeviceCodeGrant(oAuthValidationResult, paramString9); 
    if (grantType == GrantType.password)
      return validateTokenEndpointPasswordGrant(oAuthValidationResult); 
    if (grantType == GrantType.client_credentials)
      return validateTokenEndpointClientCredentialsGrant(oAuthValidationResult); 
    return oAuthValidationResult;
  }
  
  public OAuthService.OAuthValidationResult validateUserCodeInfoRequest(String paramString1, String paramString2, String paramString3, String paramString4, UUID paramUUID) {
    OAuthService.OAuthValidationResult oAuthValidationResult = new OAuthService.OAuthValidationResult();
    oAuthValidationResult.client_id = paramString2;
    oAuthValidationResult.client_secret = paramString3;
    oAuthValidationResult.tenantId = paramUUID;
    if (paramString1 != null) {
      OAuthService.CredentialResult credentialResult = OAuthService.parseCredentials(paramString1, paramString2);
      oAuthValidationResult.visit(credentialResult);
      if (oAuthValidationResult.error != null)
        return oAuthValidationResult; 
    } 
    validateClientId(oAuthValidationResult);
    if (oAuthValidationResult.error != null)
      return oAuthValidationResult; 
    validateClientCredentials(oAuthValidationResult);
    if (oAuthValidationResult.error != null)
      return oAuthValidationResult; 
    validateAndResolveTenantForUniversalApplication(paramUUID, oAuthValidationResult);
    if (oAuthValidationResult.error != null)
      return oAuthValidationResult; 
    if (!oAuthValidationResult.application.oauthConfiguration.enabledGrants.contains(GrantType.device_code))
      return oAuthValidationResult.withError(new OAuthError(OAuthError.OAuthErrorType.unauthorized_client, OAuthError.OAuthErrorReason.grant_type_disabled, "The [" + GrantType.device_code.grantName() + "] Device code grant has been disabled for this client.")); 
    if (paramString4 == null)
      return new OAuthService.OAuthValidationResult(new OAuthError(OAuthError.OAuthErrorType.invalid_request, OAuthError.OAuthErrorReason.missing_user_code, "The request is missing a required parameter: user_code")); 
    oAuthValidationResult.user_code = ExternalIdentifier.normalizeDeviceUserCodeId(paramString4);
    oAuthValidationResult.externalIdentifier = this.externalIdentifierReader.retrieveByType(oAuthValidationResult.tenant, oAuthValidationResult.user_code, ExternalIdentifier.ExternalIdType.DeviceUserCode);
    if (oAuthValidationResult.externalIdentifier == null)
      return new OAuthService.OAuthValidationResult(new OAuthError(OAuthError.OAuthErrorType.invalid_grant, OAuthError.OAuthErrorReason.invalid_user_code, String.format("user_code: %s is not valid.", new Object[] { paramString4 }))); 
    ExternalIdentifier externalIdentifier = this.externalIdentifierReader.retrieveByType(oAuthValidationResult.tenant, oAuthValidationResult.externalIdentifier.getAttribute("deviceCode"), ExternalIdentifier.ExternalIdType.DeviceCode);
    if (externalIdentifier == null || externalIdentifier.isExpired(oAuthValidationResult.tenant))
      return new OAuthService.OAuthValidationResult(new OAuthError(OAuthError.OAuthErrorType.invalid_grant, OAuthError.OAuthErrorReason.user_code_expired, String.format("user_code: %s has expired and is no longer valid.", new Object[] { paramString4 }))); 
    oAuthValidationResult.scopes = parseScopesForDeviceCodeRedirect(externalIdentifier.getAttribute("scope"));
    return oAuthValidationResult;
  }
  
  public OAuthService.OAuthValidationResult validateUserCodeInfoRequestUsingAPIKey(UUID paramUUID, String paramString) {
    OAuthService.OAuthValidationResult oAuthValidationResult = new OAuthService.OAuthValidationResult();
    if (paramString == null)
      return new OAuthService.OAuthValidationResult(new OAuthError(OAuthError.OAuthErrorType.invalid_request, OAuthError.OAuthErrorReason.missing_user_code, "The request is missing a required parameter: user_code")); 
    oAuthValidationResult.user_code = ExternalIdentifier.normalizeDeviceUserCodeId(paramString);
    oAuthValidationResult.tenant = (paramUUID != null) ? (Tenant)this.tenantCache.get(paramUUID) : null;
    oAuthValidationResult.externalIdentifier = this.externalIdentifierReader.retrieveByType(oAuthValidationResult.tenant, oAuthValidationResult.user_code, ExternalIdentifier.ExternalIdType.DeviceUserCode);
    if (oAuthValidationResult.externalIdentifier == null)
      return new OAuthService.OAuthValidationResult(new OAuthError(OAuthError.OAuthErrorType.invalid_grant, OAuthError.OAuthErrorReason.invalid_user_code, String.format("user_code: %s is not valid.", new Object[] { paramString }))); 
    if (oAuthValidationResult.tenant == null)
      oAuthValidationResult.tenant = (Tenant)this.tenantCache.get(oAuthValidationResult.externalIdentifier.tenantId); 
    oAuthValidationResult.application = (Application)this.applicationCache.get(oAuthValidationResult.externalIdentifier.applicationId);
    if (!oAuthValidationResult.application.oauthConfiguration.enabledGrants.contains(GrantType.device_code))
      return oAuthValidationResult.withError(new OAuthError(OAuthError.OAuthErrorType.unauthorized_client, OAuthError.OAuthErrorReason.grant_type_disabled, "The [" + GrantType.device_code.grantName() + "] Device code grant has been disabled for this client.")); 
    ExternalIdentifier externalIdentifier = this.externalIdentifierReader.retrieveByType(oAuthValidationResult.tenant, oAuthValidationResult.externalIdentifier.getAttribute("deviceCode"), ExternalIdentifier.ExternalIdType.DeviceCode);
    if (externalIdentifier == null || externalIdentifier.isExpired(oAuthValidationResult.tenant))
      return new OAuthService.OAuthValidationResult(new OAuthError(OAuthError.OAuthErrorType.invalid_grant, OAuthError.OAuthErrorReason.user_code_expired, String.format("user_code: %s has expired and is no longer valid.", new Object[] { paramString }))); 
    oAuthValidationResult.scopes = parseScopesForDeviceCodeRedirect(externalIdentifier.getAttribute("scope"));
    oAuthValidationResult.client_id = oAuthValidationResult.application.oauthConfiguration.clientId;
    return oAuthValidationResult;
  }
  
  public OAuthService.OAuthValidationResult validateUserCodeRequest(UUID paramUUID, String paramString1, String paramString2) {
    OAuthService.OAuthValidationResult oAuthValidationResult = new OAuthService.OAuthValidationResult();
    oAuthValidationResult.client_id = paramString1;
    oAuthValidationResult.tenantId = paramUUID;
    validateClientId(oAuthValidationResult);
    if (oAuthValidationResult.error != null)
      return oAuthValidationResult; 
    validateAndResolveTenantForUniversalApplication(paramUUID, oAuthValidationResult);
    if (oAuthValidationResult.error != null)
      return oAuthValidationResult; 
    if (paramString2 == null)
      return new OAuthService.OAuthValidationResult(new OAuthError(OAuthError.OAuthErrorType.invalid_request, OAuthError.OAuthErrorReason.missing_user_code, "The request is missing a required parameter: user_code")); 
    String str = ExternalIdentifier.normalizeDeviceUserCodeId(paramString2);
    oAuthValidationResult.externalIdentifier = this.externalIdentifierReader.retrieveByType(oAuthValidationResult.tenant, str, ExternalIdentifier.ExternalIdType.DeviceUserCode);
    if (oAuthValidationResult.externalIdentifier == null)
      return new OAuthService.OAuthValidationResult(new OAuthError(OAuthError.OAuthErrorType.invalid_grant, OAuthError.OAuthErrorReason.invalid_user_code, String.format("user_code: %s is not valid.", new Object[] { paramString2 }))); 
    ExternalIdentifier externalIdentifier = this.externalIdentifierReader.retrieveByType(oAuthValidationResult.tenant, oAuthValidationResult.externalIdentifier.getAttribute("deviceCode"), ExternalIdentifier.ExternalIdType.DeviceCode);
    if (externalIdentifier == null || externalIdentifier.isExpired(oAuthValidationResult.tenant))
      return new OAuthService.OAuthValidationResult(new OAuthError(OAuthError.OAuthErrorType.invalid_grant, OAuthError.OAuthErrorReason.user_code_expired, String.format("user_code: %s has expired and is no longer valid.", new Object[] { paramString2 }))); 
    oAuthValidationResult.scopes = parseScopesForDeviceCodeRedirect(externalIdentifier.getAttribute("scope"));
    oAuthValidationResult.user_code = str;
    return oAuthValidationResult;
  }
  
  protected void validateClientCredentials(OAuthService.OAuthValidationResult paramOAuthValidationResult) {
    ClientAuthenticationPolicy clientAuthenticationPolicy = paramOAuthValidationResult.application.oauthConfiguration.clientAuthenticationPolicy;
    boolean bool = (clientAuthenticationPolicy == ClientAuthenticationPolicy.Required || (clientAuthenticationPolicy == ClientAuthenticationPolicy.NotRequiredWhenUsingPKCE && paramOAuthValidationResult.grantType != GrantType.authorization_code)) ? true : false;
    if (bool && (paramOAuthValidationResult.client_id == null || paramOAuthValidationResult.client_secret == null)) {
      paramOAuthValidationResult.error = new OAuthError(OAuthError.OAuthErrorType.invalid_client, OAuthError.OAuthErrorReason.client_authentication_missing, "Client authentication missing as Basic Authorization header or credentials in the body (or some combination of them).");
      return;
    } 
    if (paramOAuthValidationResult.client_secret != null && !paramOAuthValidationResult.client_secret.equals(paramOAuthValidationResult.application.oauthConfiguration.clientSecret))
      paramOAuthValidationResult.error = new OAuthError(OAuthError.OAuthErrorType.invalid_client, OAuthError.OAuthErrorReason.invalid_client_authentication, "Invalid client authentication credentials."); 
  }
  
  private boolean allowedRelativeRedirect(URI paramURI, Tenant paramTenant) {
    String str = paramURI.toString();
    return (AllowedRelativeRedirects.stream()
      .anyMatch(paramString2 -> (paramString1.equals(paramString2) || paramString1.startsWith(paramString2 + "?client_id"))) || 
      isSAMLSubOAuthRedirect(paramURI, paramTenant));
  }
  
  private OAuthService.DeviceApproveResult approveDeviceCode(Debugger paramDebugger, Tenant paramTenant, Application paramApplication, User paramUser, ExternalIdentifier paramExternalIdentifier, Set<String> paramSet) {
    OAuthService.DeviceApproveResult deviceApproveResult = new OAuthService.DeviceApproveResult();
    deviceApproveResult.userId = paramUser.id;
    deviceApproveResult.tenantId = paramTenant.id;
    String str = paramExternalIdentifier.getAttribute("clientId");
    if (!paramApplication.oauthConfiguration.clientId.equals(str)) {
      if (paramDebugger != null)
        paramDebugger.log("The client_id provided on this request did not match the value provided during the device authorization request. Expected [%s] but [%s] was provided on this request.", new Object[] { str, paramApplication.oauthConfiguration.clientId }); 
      return deviceApproveResult;
    } 
    ExternalIdentifier externalIdentifier = this.externalIdentifierReader.retrieveById(paramExternalIdentifier.getAttribute("deviceCode"));
    if (externalIdentifier != null) {
      deviceApproveResult.applicationId = externalIdentifier.applicationId;
      deviceApproveResult.deviceInfo = externalIdentifier.data.device;
      externalIdentifier.userId = paramUser.id;
      externalIdentifier.data.setAttribute("deviceStatus", ExternalIdentifier.DeviceGrantStatus.Approved);
      deviceApproveResult.deviceGrantStatus = ExternalIdentifier.DeviceGrantStatus.Approved.name();
      if (paramApplication.oauthConfiguration.relationship.equals(OAuthApplicationRelationship.ThirdParty) && externalIdentifier.data.getAttribute("scope") != null) {
        Set<? extends CharSequence> set = (Set)Arrays.<String>stream(externalIdentifier.data.getAttribute("scope").split(" ")).filter(paramString -> (paramSet.contains(paramString) || paramString.startsWith("idp-link:") || paramString.equals("openid") || paramString.equals("offline_access"))).collect(Collectors.toSet());
        externalIdentifier.data.setAttribute("scope", String.join(" ", set));
      } 
      this.externalIdentifierService.update(externalIdentifier);
      this.externalIdentifierService.deleteById(paramExternalIdentifier.id);
    } 
    return deviceApproveResult;
  }
  
  private OAuthError commonValidateClientCredentials(OAuthService.OAuthValidationResult paramOAuthValidationResult) {
    if (ReactorStatusValidator.isNotLicensedFor(this.reactorStatusService.retrieveStatus(), paramReactorStatus -> paramReactorStatus.entityManagement))
      return new OAuthError(OAuthError.OAuthErrorType.not_licensed, OAuthError.OAuthErrorReason.not_licensed, "The Entity Management feature of FusionAuth, which include the Client Credentials Grant, is only supported with a valid license"); 
    if (StringTools.isTrimmedEmpty(paramOAuthValidationResult.client_id))
      return new OAuthError(OAuthError.OAuthErrorType.invalid_request, OAuthError.OAuthErrorReason.missing_client_id, "The request is missing a required parameter: client_id"); 
    if (StringTools.isTrimmedEmpty(paramOAuthValidationResult.client_secret))
      return new OAuthError(OAuthError.OAuthErrorType.invalid_request, OAuthError.OAuthErrorReason.missing_client_secret, "The request is missing a required parameter: client_secret"); 
    paramOAuthValidationResult.recipientEntity = this.entityService.retrieveByClientId(paramOAuthValidationResult.tenantId, paramOAuthValidationResult.client_id);
    if (paramOAuthValidationResult.recipientEntity == null)
      return new OAuthError(OAuthError.OAuthErrorType.invalid_client, OAuthError.OAuthErrorReason.invalid_client_id, String.format("client_id: %s is not valid.", new Object[] { paramOAuthValidationResult.client_id })); 
    if (!paramOAuthValidationResult.recipientEntity.clientSecret.equals(paramOAuthValidationResult.client_secret))
      return new OAuthError(OAuthError.OAuthErrorType.invalid_client, OAuthError.OAuthErrorReason.invalid_client_authentication, "Invalid client authentication credentials."); 
    Objects.requireNonNull(this.tenantReader);
    paramOAuthValidationResult.tenant = this.tenantCache.get(paramOAuthValidationResult.recipientEntity.tenantId, this.tenantReader::retrieveById);
    return null;
  }
  
  private boolean enforcePKCE(Tenant paramTenant, String paramString) {
    ExternalIdentifier externalIdentifier = this.externalIdentifierReader.retrieveByType(paramTenant, paramString, ExternalIdentifier.ExternalIdType.AuthorizationCode);
    if (externalIdentifier != null) {
      String str = externalIdentifier.getAttribute("codeChallenge");
      if (str != null && str.startsWith("fa-surrogate-challenge"))
        try {
          byte[] arrayOfByte1 = Base64.getUrlDecoder().decode(str.substring("fa-surrogate-challenge".length()));
          byte[] arrayOfByte2 = this.cipherService.decrypt(arrayOfByte1);
          UUID uUID = UUID.fromString(new String(arrayOfByte2, StandardCharsets.UTF_8));
          BaseIdentityProvider<?> baseIdentityProvider = (BaseIdentityProvider)this.identityProviderCache.get(uUID);
          return !IdentityProviderTools.isIdpInitAble(baseIdentityProvider);
        } catch (Exception exception) {} 
    } 
    return true;
  }
  
  private AuthenticationType getAuthenticationType(UUID paramUUID) {
    if (paramUUID != null) {
      BaseIdentityProvider baseIdentityProvider = (BaseIdentityProvider)this.identityProviderCache.get(paramUUID);
      if (baseIdentityProvider != null) {
        IdentityProviderAuthenticationService identityProviderAuthenticationService = this.identityProviderCache.identityProviderAuthenticationServices.get(baseIdentityProvider.getType());
        return identityProviderAuthenticationService.authenticationType();
      } 
    } 
    return AuthenticationType.PASSWORD;
  }
  
  private List<Application> getRegisteredUniversalApplications(User paramUser) {
    return (paramUser != null) ? 

      
      this.userMapper.retrieveUniversalRegistrationsByUserId(paramUser.id).stream().map(paramUserRegistration -> (Application)this.applicationCache.get(paramUserRegistration.applicationId)).toList() : 
      List.of();
  }
  
  private Set<String> getRequiredOAuthScopeNames(Application paramApplication, Set<String> paramSet) {
    Set<String> set1 = (Set)paramApplication.scopes.stream().filter(paramApplicationOAuthScope -> paramApplicationOAuthScope.required).map(paramApplicationOAuthScope -> paramApplicationOAuthScope.name).collect(Collectors.toSet());
    ProvidedScopePolicy providedScopePolicy = paramApplication.oauthConfiguration.providedScopePolicy;
    if (providedScopePolicy.address.enabled && providedScopePolicy.address.required)
      set1.add("address"); 
    if (providedScopePolicy.email.enabled && providedScopePolicy.email.required)
      set1.add("email"); 
    if (providedScopePolicy.phone.enabled && providedScopePolicy.phone.required)
      set1.add("phone"); 
    if (providedScopePolicy.profile.enabled && providedScopePolicy.profile.required)
      set1.add("profile"); 
    Set<String> set2 = getOAuthScopeNamesForPromptFromApplication(paramApplication);
    set1.addAll((Collection<? extends String>)paramSet.stream().filter(paramString -> !paramSet.contains(paramString)).collect(Collectors.toSet()));
    return set1;
  }
  
  private boolean handleDeviceLinkRequest(Debugger paramDebugger, Tenant paramTenant, Application paramApplication, ExternalIdentifier.ExternalIdData paramExternalIdData, Set<String> paramSet) {
    OAuthService.LinkingTokenDetails linkingTokenDetails;
    String str = paramSet.stream().filter(paramString -> paramString.startsWith("idp-link:")).findFirst().orElse(null);
    if (str == null)
      return true; 
    paramDebugger.log("An account link request has been requested.")
      .log(str);
    String[] arrayOfString = str.split(":");
    if (arrayOfString.length < 3) {
      paramDebugger.log("Invalid linking scope. The link must be provided in this format: idp-link:{identityProviderId}:{token}\nIf you have more than one token to provide, append them in order according to the documentation separating each token with a colon. For example, if you have two tokens to provide, the scope format would be: idp-link:{identityProviderId}:{token1}:{token2}");
      return false;
    } 
    UUID uUID = StringTools.parseUUID(arrayOfString[1]);
    if (uUID == null) {
      paramDebugger.log("The value found in scope [idp:" + arrayOfString[1] + "] is not a valid UUID.");
      return false;
    } 
    BaseIdentityProvider baseIdentityProvider = (BaseIdentityProvider)this.identityProviderCache.get(uUID);
    if (baseIdentityProvider == null) {
      paramDebugger.log("The value found in scope [idp:" + arrayOfString[1] + "] is not a valid Identity Provider Id.");
      return false;
    } 
    paramDebugger.log("Verify the token for Identity Provider [" + baseIdentityProvider.name + "] and return user claims. Optionally enable debug for the identity provider for additional insight into this request.");
    IdentityProviderAuthenticationService identityProviderAuthenticationService = this.identityProviderCache.identityProviderAuthenticationServices.get(baseIdentityProvider.getType());
    try {
      String str1 = String.join(":", Arrays.<CharSequence>copyOfRange((CharSequence[])arrayOfString, 2, arrayOfString.length));
      linkingTokenDetails = identityProviderAuthenticationService.verifyLinkingToken(paramTenant, paramApplication, uUID, str1);
      if (linkingTokenDetails == null) {
        paramDebugger.log("The token was invalid or expired. No results were returned.");
        return false;
      } 
      if (linkingTokenDetails.identityProviderUserId == null || linkingTokenDetails.identityProviderUserId.length() == 0) {
        paramDebugger.log("A unique userId could not be identified using the provided token.");
        paramDebugger.logObjectToJSON("Token details:\n", linkingTokenDetails);
        return false;
      } 
    } catch (Exception exception) {
      paramDebugger.log("The token was invalid or expired. An exception occurred while attempting to verify the token. Enable debug on the IdP or review the event log for an error to identify the cause.")
        .log("Continue the device request, a link will not be established.");
      return false;
    } 
    paramExternalIdData.setAttribute("identityProviderId", baseIdentityProvider.id)
      .setAttribute("identityProviderName", baseIdentityProvider.name)
      .setAttribute("identityProviderType", baseIdentityProvider.getType())
      .setAttribute("identityProviderDisplayName", linkingTokenDetails.identityProviderDisplayName)
      .setAttribute("identityProviderUserId", linkingTokenDetails.identityProviderUserId);
    paramDebugger.log("Capture user details from the IdP so that we can complete the link when the device code grant is completed. Details:")
      .log("IdentityProviderId: " + String.valueOf(baseIdentityProvider.id))
      .log("IdentityProviderName: " + baseIdentityProvider.name)
      .log("IdentityProviderDisplayName: " + linkingTokenDetails.identityProviderDisplayName)
      .log("IdentityProviderUserId: " + linkingTokenDetails.identityProviderUserId);
    return true;
  }
  
  private boolean isAccountLoginRedirect(URI paramURI) {
    if (paramURI == null)
      return false; 
    String str = paramURI.toString();
    return (str.equals("/account/login") || str.startsWith("/account/login?client_id"));
  }
  
  private boolean isSAMLSubOAuthRedirect(URI paramURI, Tenant paramTenant) {
    if (paramURI == null)
      return false; 
    int i = paramURI.toString().indexOf("/" + paramTenant.id.toString());
    if (i != -1) {
      URI uRI = URI.create(paramURI.toString().substring(0, i));
      return uRI.equals(SAMLRedirect);
    } 
    return false;
  }
  
  private Set<String> parseResponseType(String paramString) {
    if (paramString == null)
      return Collections.emptySet(); 
    return new LinkedHashSet<>(Arrays.asList(paramString.split(" ")));
  }
  
  private Set<String> parseScopesForDeviceCodeRedirect(String paramString) {
    Set<String> set = ParameterTools.splitSpaceSeparated(paramString);
    set.removeIf(paramString -> (paramString.startsWith("idp-link:") || paramString.equals("openid") || paramString.equals("offline_access")));
    return set;
  }
  
  private List<URI> resolveResourcesForRefreshGrant(Application paramApplication, RefreshToken paramRefreshToken, List<URI> paramList) {
    List list = Optional.<List>ofNullable(paramRefreshToken.metaData.resources).orElse(List.of());
    if (paramApplication.oauthConfiguration.authorizedResourceUris.isEmpty())
      return List.of(); 
    List<URI> list1 = (paramList == null) ? List.of() : paramList;
    if (list1.isEmpty())
      return (List<URI>)list.stream()
        .filter(paramURI -> paramApplication.oauthConfiguration.authorizedResourceUris.contains(paramURI))
        .collect(Collectors.toList()); 
    for (URI uRI : list1) {
      if (!paramApplication.oauthConfiguration.authorizedResourceUris.contains(uRI) || !list.contains(uRI))
        return null; 
    } 
    return list1;
  }
  
  private DeviceInfo safelyRetrieveDeviceFromMetaData(Tenant paramTenant, String paramString, ExternalIdentifier.ExternalIdType paramExternalIdType) {
    ExternalIdentifier externalIdentifier = this.externalIdentifierReader.retrieveByType(paramTenant, paramString, paramExternalIdType);
    if (externalIdentifier != null)
      return externalIdentifier.data.device; 
    return new DeviceInfo();
  }
  
  private Map<String, Object> supplyClaimsForRefreshToken(User paramUser, RefreshToken paramRefreshToken, List<URI> paramList) {
    HashMap<Object, Object> hashMap = new HashMap<>();
    UserRegistration userRegistration = paramUser.getRegistrationForApplication(paramRefreshToken.applicationId);
    if (userRegistration != null) {
      hashMap.put("applicationId", paramRefreshToken.applicationId);
      hashMap.put("roles", userRegistration.roles);
    } 
    if (paramList.isEmpty()) {
      hashMap.put("aud", paramRefreshToken.applicationId);
    } else {
      hashMap.put("aud", Stream.concat(Stream.of(paramRefreshToken.applicationId.toString()), paramList.stream().map(URI::toString)).collect(Collectors.toList()));
    } 
    return (Map)hashMap;
  }
  
  private IdentityProviderLink validateAndCompleteDeviceLink(Debugger paramDebugger, EventInfo paramEventInfo, ExternalIdentifier paramExternalIdentifier, Tenant paramTenant, User paramUser) {
    return validateAndCompleteLink(paramDebugger, paramEventInfo, paramExternalIdentifier, paramTenant, paramUser, "Complete the link request started in the Device Authorization request.");
  }
  
  private IdentityProviderLink validateAndCompleteLink(Debugger paramDebugger, EventInfo paramEventInfo, ExternalIdentifier paramExternalIdentifier, Tenant paramTenant, User paramUser, String paramString) {
    String str1 = paramExternalIdentifier.getAttribute("identityProviderId");
    if (str1 == null)
      return null; 
    UUID uUID = StringTools.parseUUID(str1);
    BaseIdentityProvider<?> baseIdentityProvider = (BaseIdentityProvider)this.identityProviderCache.get(uUID);
    if (baseIdentityProvider == null)
      return null; 
    if (baseIdentityProvider.tenantId != null && !baseIdentityProvider.tenantId.equals(paramTenant.id))
      return null; 
    String str2 = paramExternalIdentifier.getAttribute("identityProviderUserId");
    if (paramDebugger != null)
      paramDebugger.log(paramString)
        .log("Link [" + baseIdentityProvider.name + "] user [" + str2 + "] to FusionAuth User [" + String.valueOf(paramUser.id) + "]."); 
    IdentityProviderTenantConfiguration identityProviderTenantConfiguration = baseIdentityProvider.tenantConfiguration.get(paramTenant.id);
    if (identityProviderTenantConfiguration != null && identityProviderTenantConfiguration.limitUserLinkCount.enabled) {
      List<IdentityProviderLink> list = this.identityProviderUserService.retrieveIdentityProviderUsersByUser(paramTenant, baseIdentityProvider, paramUser.id);
      if (list.stream().noneMatch(paramIdentityProviderLink -> paramIdentityProviderLink.identityProviderUserId.equals(paramString)) && 
        list.size() >= identityProviderTenantConfiguration.limitUserLinkCount.maximumLinks) {
        if (paramDebugger != null)
          paramDebugger.log("Unable to complete the requested device link. The user already has [" + list.size() + "] links to this IdP. The configured maximum is [" + identityProviderTenantConfiguration.limitUserLinkCount.maximumLinks + "]."); 
        return null;
      } 
    } 
    return this.identityProviderUserService.link(paramEventInfo, paramTenant, paramUser, baseIdentityProvider, str2, paramExternalIdentifier



        
        .getAttribute("identityProviderDisplayName"), null, null);
  }
  
  private OAuthService.OAuthValidationResult validateAndResolveApplicationForLogout(String paramString1, String paramString2) {
    Application application;
    String str = null;
    if (paramString2 != null) {
      ValidatedJWTResult validatedJWTResult = this.jwtService.validateIdTokenHint(paramString2);
      if (!validatedJWTResult.valid) {
        if (validatedJWTResult.reasonCode == ValidatedJWTResult.ReasonCode.invalidAudience || validatedJWTResult.reasonCode == ValidatedJWTResult.ReasonCode.unresolvedContext)
          return new OAuthService.OAuthValidationResult(new OAuthError(OAuthError.OAuthErrorType.invalid_request, OAuthError.OAuthErrorReason.invalid_id_token_hint, "The [aud] and/or [tid] claims are invalid.")); 
        return new OAuthService.OAuthValidationResult(new OAuthError(OAuthError.OAuthErrorType.invalid_request, OAuthError.OAuthErrorReason.invalid_id_token_hint, "The token is not suitable for the requested use."));
      } 
      JWT jWT = validatedJWTResult.jwt;
      return validatedJWTResult.legacyIdTokenHint ? 
        validateAndResolveLegacyApplicationForLogout(paramString1, jWT) : 
        validateAndResolveModernApplicationForLogout(paramString1, jWT);
    } 
    if (paramString1 != null) {
      UUID uUID = UUIDTools.fromString(paramString1);
      application = (Application)this.applicationCache.get(uUID);
      if (application == null)
        return new OAuthService.OAuthValidationResult(new OAuthError(OAuthError.OAuthErrorType.invalid_client, OAuthError.OAuthErrorReason.invalid_client_id, String.format("client_id: %s is not valid.", new Object[] { paramString1 }))); 
    } else {
      return new OAuthService.OAuthValidationResult(new OAuthError(OAuthError.OAuthErrorType.invalid_client, OAuthError.OAuthErrorReason.invalid_client_id, "Either id_token_hint or client_id must be provided for logout."));
    } 
    Tenant tenant = (Tenant)this.tenantCache.get(application.tenantId);
    OAuthService.OAuthValidationResult oAuthValidationResult = new OAuthService.OAuthValidationResult(tenant, application);
    oAuthValidationResult.sid = str;
    return oAuthValidationResult;
  }
  
  private OAuthService.OAuthValidationResult validateAndResolveLegacyApplicationForLogout(String paramString, JWT paramJWT) {
    UUID uUID = ClaimTools.resolveApplicationId(paramJWT);
    Application application = (uUID == null) ? null : (Application)this.applicationCache.get(uUID);
    if (application == null)
      return new OAuthService.OAuthValidationResult(new OAuthError(OAuthError.OAuthErrorType.invalid_request, OAuthError.OAuthErrorReason.invalid_id_token_hint, "The [aud] claim is invalid.")); 
    OAuthError oAuthError = validateIdTokenHintClientId(application, paramString);
    if (oAuthError != null)
      return new OAuthService.OAuthValidationResult(oAuthError); 
    OAuthService.OAuthValidationResult oAuthValidationResult = new OAuthService.OAuthValidationResult((Tenant)this.tenantCache.get(application.tenantId), application);
    oAuthValidationResult.sid = paramJWT.getString("sid");
    return oAuthValidationResult;
  }
  
  private OAuthService.OAuthValidationResult validateAndResolveModernApplicationForLogout(String paramString, JWT paramJWT) {
    UUID uUID1 = ClaimTools.resolveTenantId(paramJWT);
    Objects.requireNonNull(this.tenantReader);
    Tenant tenant1 = this.tenantCache.get(uUID1, this.tenantReader::retrieveById);
    UUID uUID2 = ClaimTools.resolveApplicationId(paramJWT);
    Objects.requireNonNull(this.applicationReader);
    Application application = (tenant1 == null || uUID2 == null) ? null : this.applicationCache.get(tenant1.id, uUID2, this.applicationReader::retrieveById);
    if (application == null)
      return new OAuthService.OAuthValidationResult(new OAuthError(OAuthError.OAuthErrorType.invalid_request, OAuthError.OAuthErrorReason.invalid_id_token_hint, "The [aud] claim is invalid.")); 
    OAuthError oAuthError = validateIdTokenHintClientId(application, paramString);
    if (oAuthError != null)
      return new OAuthService.OAuthValidationResult(oAuthError); 
    Objects.requireNonNull(this.tenantReader);
    Tenant tenant2 = application.universalConfiguration.universal ? tenant1 : this.tenantCache.get(application.tenantId, this.tenantReader::retrieveById);
    OAuthService.OAuthValidationResult oAuthValidationResult = new OAuthService.OAuthValidationResult(tenant2, application);
    oAuthValidationResult.sid = paramJWT.getString("sid");
    return oAuthValidationResult;
  }
  
  private void validateAndResolveTenantForUniversalApplication(UUID paramUUID, OAuthService.OAuthValidationResult paramOAuthValidationResult) {
    if (paramOAuthValidationResult.tenant == null && paramOAuthValidationResult.application.universalConfiguration.universal) {
      if (paramUUID == null) {
        paramOAuthValidationResult.withError(new OAuthError(OAuthError.OAuthErrorType.invalid_request, OAuthError.OAuthErrorReason.missing_tenant_id, "The request is missing a required parameter: tenantId"))
          .setDoNotRedirect(true);
        return;
      } 
      paramOAuthValidationResult.tenant = (Tenant)this.tenantCache.get(paramUUID);
      if (paramOAuthValidationResult.tenant == null)
        paramOAuthValidationResult.withError(new OAuthError(OAuthError.OAuthErrorType.invalid_request, OAuthError.OAuthErrorReason.invalid_tenant_id, String.format("tenantId: %s is not valid.", new Object[] { paramUUID }))).setDoNotRedirect(true); 
    } 
  }
  
  private boolean validateAuthorizedRedirectURI(OAuthService.OAuthValidationResult paramOAuthValidationResult, URI paramURI) {
    if (allowedRelativeRedirect(paramURI, paramOAuthValidationResult.tenant))
      return true; 
    return validateRedirectURI(paramOAuthValidationResult, paramURI);
  }
  
  private void validateClientId(OAuthService.OAuthValidationResult paramOAuthValidationResult) {
    UUID uUID;
    if (paramOAuthValidationResult.client_id == null) {
      paramOAuthValidationResult.error = new OAuthError(OAuthError.OAuthErrorType.invalid_request, OAuthError.OAuthErrorReason.missing_client_id, "The request is missing a required parameter: client_id");
      paramOAuthValidationResult.setDoNotRedirect(true);
      return;
    } 
    try {
      uUID = UUID.fromString(paramOAuthValidationResult.client_id);
    } catch (IllegalArgumentException illegalArgumentException) {
      paramOAuthValidationResult.error = new OAuthError(OAuthError.OAuthErrorType.invalid_client, OAuthError.OAuthErrorReason.invalid_client_id, String.format("client_id: %s is not valid.", new Object[] { paramOAuthValidationResult.client_id }));
      paramOAuthValidationResult.setDoNotRedirect(true);
      return;
    } 
    paramOAuthValidationResult.application = (Application)this.applicationCache.get(uUID);
    if (paramOAuthValidationResult.application == null) {
      paramOAuthValidationResult.error = new OAuthError(OAuthError.OAuthErrorType.invalid_client, OAuthError.OAuthErrorReason.invalid_client_id, String.format("client_id: %s is not valid.", new Object[] { paramOAuthValidationResult.client_id }));
      paramOAuthValidationResult.setDoNotRedirect(true);
      return;
    } 
    if (paramOAuthValidationResult.application.tenantId != null && paramOAuthValidationResult.tenantId != null && !paramOAuthValidationResult.application.tenantId.equals(paramOAuthValidationResult.tenantId)) {
      paramOAuthValidationResult.error = new OAuthError(OAuthError.OAuthErrorType.invalid_client, OAuthError.OAuthErrorReason.invalid_client_id, String.format("client_id: %s is not valid.", new Object[] { paramOAuthValidationResult.client_id }));
      paramOAuthValidationResult.setDoNotRedirect(true);
    } 
    paramOAuthValidationResult.tenant = (Tenant)this.tenantCache.get(paramOAuthValidationResult.application.tenantId);
  }
  
  private OAuthError validateIdTokenHintClientId(Application paramApplication, String paramString) {
    if (paramString != null && !paramApplication.id.equals(UUIDTools.fromString(paramString)))
      return new OAuthError(OAuthError.OAuthErrorType.invalid_request, OAuthError.OAuthErrorReason.invalid_id_token_hint, "The client_id does not match the provided id_token_hint."); 
    return null;
  }
  
  private boolean validatePostLogoutRedirectURI(OAuthService.OAuthValidationResult paramOAuthValidationResult, URI paramURI) {
    if (AllowedLogoutRelativeRedirectRootPaths.stream().anyMatch(paramString -> paramURI.toString().startsWith(paramString)))
      return true; 
    return validateRedirectURI(paramOAuthValidationResult, paramURI);
  }
  
  private boolean validateRedirectURI(OAuthService.OAuthValidationResult paramOAuthValidationResult, URI paramURI) {
    if (paramOAuthValidationResult.application.oauthConfiguration.authorizedURLValidationPolicy == Oauth2AuthorizedURLValidationPolicy.ExactMatch)
      return paramOAuthValidationResult.application.oauthConfiguration.authorizedRedirectURLs.stream()
        .filter(paramURI -> !paramURI.toString().contains("*"))
        .anyMatch(paramURI2 -> paramURI2.equals(paramURI1)); 
    Map map = (Map)paramOAuthValidationResult.application.oauthConfiguration.authorizedRedirectURLs.stream().collect(Collectors.groupingBy(paramURI -> Boolean.valueOf(paramURI.toString().contains("*"))));
    List list = (List)map.getOrDefault(Boolean.valueOf(false), Collections.emptyList());
    if (list.size() > 0 && 
      list.stream().anyMatch(paramURI2 -> paramURI2.equals(paramURI1)))
      return true; 
    return URITools.anyMatch(paramURI, (List<URI>)map.getOrDefault(Boolean.valueOf(true), Collections.emptyList()));
  }
  
  private void validateScopes(OAuthService.OAuthValidationResult paramOAuthValidationResult) {
    UnknownScopePolicy unknownScopePolicy = paramOAuthValidationResult.application.oauthConfiguration.unknownScopePolicy;
    if (paramOAuthValidationResult.scopes.isEmpty() || unknownScopePolicy.equals(UnknownScopePolicy.Allow))
      return; 
    LinkedHashSet<String> linkedHashSet = new LinkedHashSet();
    for (Iterator<String> iterator = paramOAuthValidationResult.scopes.iterator(); iterator.hasNext(); ) {
      String str = iterator.next();
      if (ReservedScopeNames.contains(str)) {
        if (!str.equals("openid") && !str.equals("offline_access")) {
          Requirable requirable = paramOAuthValidationResult.application.oauthConfiguration.providedScopePolicy.getScopePolicy(str);
          if (!requirable.enabled) {
            if (unknownScopePolicy.equals(UnknownScopePolicy.Remove)) {
              iterator.remove();
              continue;
            } 
            linkedHashSet.add(str);
          } 
        } 
        continue;
      } 
      if (paramOAuthValidationResult.grantType != null && paramOAuthValidationResult.grantType.equals(GrantType.device_code) && str.startsWith("idp-link:"))
        continue; 
      if (paramOAuthValidationResult.application.getOAuthScope(str) == null) {
        if (unknownScopePolicy.equals(UnknownScopePolicy.Remove)) {
          iterator.remove();
          continue;
        } 
        linkedHashSet.add(str);
      } 
    } 
    if (!linkedHashSet.isEmpty())
      paramOAuthValidationResult
        .error = new OAuthError(OAuthError.OAuthErrorType.invalid_scope, OAuthError.OAuthErrorReason.unknown_scope, String.format("Invalid scope. The scopes [%s] are unknown.", new Object[] { String.join(", ", (Iterable)linkedHashSet) })); 
  }
  
  private OAuthService.OAuthValidationResult validateTokenEndpointAuthorizationGrant(OAuthService.OAuthValidationResult paramOAuthValidationResult, URI paramURI, String paramString1, String paramString2) {
    if (paramString2 != null && !PKCETools.validateCodeVerifier(paramString2)) {
      paramOAuthValidationResult.error = new OAuthError(OAuthError.OAuthErrorType.invalid_request, OAuthError.OAuthErrorReason.invalid_pkce_code_verifier, "Invalid code_verifier, see https://tools.ietf.org/html/rfc7636#section-4.1 for requirements.");
      return paramOAuthValidationResult;
    } 
    validateClientId(paramOAuthValidationResult);
    if (paramOAuthValidationResult.error != null)
      return paramOAuthValidationResult; 
    validateClientCredentials(paramOAuthValidationResult);
    if (paramOAuthValidationResult.error != null)
      return paramOAuthValidationResult; 
    validateAndResolveTenantForUniversalApplication(paramOAuthValidationResult.tenantId, paramOAuthValidationResult);
    if (paramOAuthValidationResult.error != null)
      return paramOAuthValidationResult; 
    if (paramOAuthValidationResult.application.oauthConfiguration.clientAuthenticationPolicy == ClientAuthenticationPolicy.NotRequiredWhenUsingPKCE && paramOAuthValidationResult.client_secret == null && paramString2 == null) {
      paramOAuthValidationResult.error = new OAuthError(OAuthError.OAuthErrorType.invalid_client, OAuthError.OAuthErrorReason.client_authentication_missing, "Client authentication missing as Basic Authorization header or credentials in the body (or some combination of them).");
      return paramOAuthValidationResult;
    } 
    ProofKeyForCodeExchangePolicy proofKeyForCodeExchangePolicy = paramOAuthValidationResult.application.oauthConfiguration.proofKeyForCodeExchangePolicy;
    boolean bool1 = (proofKeyForCodeExchangePolicy == ProofKeyForCodeExchangePolicy.Required || (proofKeyForCodeExchangePolicy == ProofKeyForCodeExchangePolicy.NotRequiredWhenUsingClientAuthentication && paramOAuthValidationResult.client_secret == null)) ? true : false;
    if (bool1 && paramString2 == null)
      if (enforcePKCE(paramOAuthValidationResult.tenant, paramString1)) {
        paramOAuthValidationResult.error = new OAuthError(OAuthError.OAuthErrorType.invalid_client, OAuthError.OAuthErrorReason.missing_code_verifier, "The request is missing a required parameter: code_verifier");
        return paramOAuthValidationResult;
      }  
    boolean bool2 = (isSAMLSubOAuthRedirect(paramURI, paramOAuthValidationResult.tenant) || isAccountLoginRedirect(paramURI)) ? true : false;
    if (!bool2 && !paramOAuthValidationResult.application.oauthConfiguration.enabledGrants.contains(GrantType.authorization_code)) {
      paramOAuthValidationResult.error = new OAuthError(OAuthError.OAuthErrorType.unauthorized_client, OAuthError.OAuthErrorReason.grant_type_disabled, "The [authorization_code] Authorization Code grant has been disabled for this client.");
      return paramOAuthValidationResult;
    } 
    if (paramString1 == null) {
      paramOAuthValidationResult.error = new OAuthError(OAuthError.OAuthErrorType.invalid_request, OAuthError.OAuthErrorReason.missing_code, "The request is missing a required parameter: code");
      return paramOAuthValidationResult;
    } 
    if (paramURI == null) {
      paramOAuthValidationResult.error = new OAuthError(OAuthError.OAuthErrorType.invalid_request, OAuthError.OAuthErrorReason.missing_redirect_uri, "The request is missing a required parameter: redirect_uri");
      return paramOAuthValidationResult;
    } 
    boolean bool = validateAuthorizedRedirectURI(paramOAuthValidationResult, paramURI);
    if (!bool) {
      paramOAuthValidationResult.error = new OAuthError(OAuthError.OAuthErrorType.invalid_grant, OAuthError.OAuthErrorReason.invalid_redirect_uri, String.format("redirect_uri: %s is not valid.", new Object[] { paramURI }));
      return paramOAuthValidationResult;
    } 
    return paramOAuthValidationResult;
  }
  
  private OAuthService.OAuthValidationResult validateTokenEndpointClientCredentialsGrant(OAuthService.OAuthValidationResult paramOAuthValidationResult) {
    paramOAuthValidationResult.error = commonValidateClientCredentials(paramOAuthValidationResult);
    if (paramOAuthValidationResult.error != null)
      return paramOAuthValidationResult; 
    if (paramOAuthValidationResult.scopes.size() > 0) {
      paramOAuthValidationResult.scopes.removeIf(paramString -> (!paramString.startsWith("target-entity:") || paramString.length() <= "target-entity:".length()));
      if (paramOAuthValidationResult.scopes.isEmpty())
        return paramOAuthValidationResult; 
      for (String str1 : paramOAuthValidationResult.scopes) {
        Entity entity;
        String[] arrayOfString = str1.split("target-entity:");
        String str2 = arrayOfString[1];
        String str3 = "";
        if (arrayOfString.length == 2) {
          int i = str2.indexOf(':');
          if (i != -1) {
            str3 = str2.substring(i + 1);
            str2 = str2.substring(0, i);
          } 
        } else {
          str3 = arrayOfString[2];
        } 
        TreeSet<?> treeSet = str3.equals("") ? new TreeSet() : new TreeSet(Set.of((Object[])str3.split(",")));
        try {
          UUID uUID = UUID.fromString(str2);
          entity = this.entityService.retrieveEntityById(null, uUID);
          if (entity == null) {
            paramOAuthValidationResult
              .error = new OAuthError(OAuthError.OAuthErrorType.invalid_scope, OAuthError.OAuthErrorReason.invalid_target_entity_scope, String.format("Invalid target-entity scope. The id [%s] does not reference a valid entity.", new Object[] { str2 }));
            return paramOAuthValidationResult;
          } 
          paramOAuthValidationResult.targetEntities.put(uUID.toString(), entity);
        } catch (Exception exception) {
          paramOAuthValidationResult
            .error = new OAuthError(OAuthError.OAuthErrorType.invalid_scope, OAuthError.OAuthErrorReason.invalid_target_entity_scope, String.format("Invalid target-entity scope. The id [%s] is not a UUID.", new Object[] { str2 }));
          return paramOAuthValidationResult;
        } 
        EntityGrant entityGrant = this.entityService.retrieveEntityGrantForEntity(paramOAuthValidationResult.recipientEntity, entity);
        if (entityGrant == null) {
          paramOAuthValidationResult
            .error = new OAuthError(OAuthError.OAuthErrorType.invalid_scope, OAuthError.OAuthErrorReason.invalid_target_entity_scope, String.format("Invalid target-entity scope. The client entity [%s] does not have a grant to the target entity [%s].", new Object[] { paramOAuthValidationResult.recipientEntity.id, entity.id }));
          return paramOAuthValidationResult;
        } 
        if (!entityGrant.permissions.containsAll(treeSet)) {
          treeSet.removeAll(entityGrant.permissions);
          paramOAuthValidationResult
            .error = new OAuthError(OAuthError.OAuthErrorType.invalid_scope, OAuthError.OAuthErrorReason.invalid_entity_permission_scope, String.format("Invalid target-entity scope. The permission names [%s] are invalid.", new Object[] { String.join(", ", (Iterable)treeSet) }));
          return paramOAuthValidationResult;
        } 
        if (treeSet.size() > 0) {
          paramOAuthValidationResult.entityGrantPermissions.put(str2, treeSet);
          continue;
        } 
        paramOAuthValidationResult.entityGrantPermissions.put(str2, entityGrant.permissions);
      } 
    } 
    return paramOAuthValidationResult;
  }
  
  private OAuthService.OAuthValidationResult validateTokenEndpointDeviceCodeGrant(OAuthService.OAuthValidationResult paramOAuthValidationResult, String paramString) {
    if (paramString == null) {
      paramOAuthValidationResult.error = new OAuthError(OAuthError.OAuthErrorType.invalid_request, OAuthError.OAuthErrorReason.missing_device_code, "The request is missing a required parameter: device_code");
      return paramOAuthValidationResult;
    } 
    validateClientId(paramOAuthValidationResult);
    if (paramOAuthValidationResult.error != null)
      return paramOAuthValidationResult; 
    validateAndResolveTenantForUniversalApplication(paramOAuthValidationResult.tenantId, paramOAuthValidationResult);
    if (paramOAuthValidationResult.error != null)
      return paramOAuthValidationResult; 
    if (!paramOAuthValidationResult.application.oauthConfiguration.enabledGrants.contains(GrantType.device_code)) {
      paramOAuthValidationResult.error = new OAuthError(OAuthError.OAuthErrorType.unauthorized_client, OAuthError.OAuthErrorReason.grant_type_disabled, "The [" + GrantType.device_code.grantName() + "] Device code grant has been disabled for this client.");
      return paramOAuthValidationResult;
    } 
    Debugger debugger = new Debugger(paramOAuthValidationResult.application.oauthConfiguration.debug, "OAuth2 validate device grant token request log for [" + paramOAuthValidationResult.application.name + "] with clientId [" + paramOAuthValidationResult.application.oauthConfiguration.clientId + "].");
    ExternalIdentifier externalIdentifier = this.externalIdentifierReader.retrieveByType(paramOAuthValidationResult.tenant, paramString, ExternalIdentifier.ExternalIdType.DeviceCode);
    if (externalIdentifier == null) {
      debugger.log("Device code [" + paramString + "] was not found, or was previously valid and has since expired.")
        .done();
      paramOAuthValidationResult.error = new OAuthError(OAuthError.OAuthErrorType.invalid_request, OAuthError.OAuthErrorReason.invalid_device_code, "The request has an invalid parameter: device_code");
      return paramOAuthValidationResult;
    } 
    String str1 = (externalIdentifier.data != null) ? externalIdentifier.data.getAttribute("DPoPThumbprint") : null;
    if (str1 != null) {
      if (paramOAuthValidationResult.dPoPThumbprint == null)
        return new OAuthService.OAuthValidationResult(new OAuthError(OAuthError.OAuthErrorType.invalid_grant, "The request is missing a DPoP proof header.")); 
      if (!paramOAuthValidationResult.dPoPThumbprint.equals(str1))
        return new OAuthService.OAuthValidationResult(new OAuthError(OAuthError.OAuthErrorType.invalid_grant, "Invalid DPoP proof header")); 
    } else if (paramOAuthValidationResult.dPoPThumbprint != null) {
      return new OAuthService.OAuthValidationResult(new OAuthError(OAuthError.OAuthErrorType.invalid_grant, "Invalid DPoP proof header"));
    } 
    if (externalIdentifier.isExpired(paramOAuthValidationResult.tenant)) {
      debugger.log("Device code [" + paramString + "] exists but is expired and cannot be used.")
        .done();
      paramOAuthValidationResult.error = new OAuthError(OAuthError.OAuthErrorType.expired_token, "The device_code has expired, and the device authorization session has concluded.");
      return paramOAuthValidationResult;
    } 
    String str2 = externalIdentifier.getAttribute("clientId");
    if (!paramOAuthValidationResult.client_id.equals(str2)) {
      this.externalIdentifierService.deleteById(externalIdentifier.id);
      debugger.log("The client_id provided on this request did not match the value provided during the device authorization request. Expected [%s] but [%s] was provided on this request. Delete device_code [%s], the device grant will need to be restarted.", new Object[] { str2, paramOAuthValidationResult.application.oauthConfiguration.clientId, externalIdentifier.id }).done();
      paramOAuthValidationResult.error = new OAuthError(OAuthError.OAuthErrorType.invalid_request, OAuthError.OAuthErrorReason.invalid_device_code, "The request has an invalid parameter: device_code");
      return paramOAuthValidationResult;
    } 
    ExternalIdentifier.DeviceGrantStatus deviceGrantStatus = ExternalIdentifier.DeviceGrantStatus.valueOf(externalIdentifier.getAttribute("deviceStatus"));
    if (deviceGrantStatus == ExternalIdentifier.DeviceGrantStatus.Pending) {
      paramOAuthValidationResult.error = new OAuthError(OAuthError.OAuthErrorType.authorization_pending, "The authorization request is still pending.");
      return paramOAuthValidationResult;
    } 
    paramOAuthValidationResult.user = this.userReader.retrieveById(paramOAuthValidationResult.tenant.id, externalIdentifier.userId);
    if (paramOAuthValidationResult.user == null) {
      paramOAuthValidationResult.error = new OAuthError(OAuthError.OAuthErrorType.invalid_request, OAuthError.OAuthErrorReason.invalid_device_code, "The request has an invalid parameter: device_code");
      return paramOAuthValidationResult;
    } 
    paramOAuthValidationResult.scopes = ParameterTools.splitSpaceSeparated(externalIdentifier.getAttribute("scope"));
    paramOAuthValidationResult.scopes.removeIf(paramString -> paramString.startsWith("idp-link:"));
    return paramOAuthValidationResult;
  }
  
  private OAuthService.OAuthValidationResult validateTokenEndpointPasswordGrant(OAuthService.OAuthValidationResult paramOAuthValidationResult) {
    validateClientId(paramOAuthValidationResult);
    if (paramOAuthValidationResult.error != null)
      return paramOAuthValidationResult; 
    validateClientCredentials(paramOAuthValidationResult);
    if (paramOAuthValidationResult.error != null)
      return paramOAuthValidationResult; 
    validateAndResolveTenantForUniversalApplication(paramOAuthValidationResult.tenantId, paramOAuthValidationResult);
    if (paramOAuthValidationResult.error != null)
      return paramOAuthValidationResult; 
    if (!paramOAuthValidationResult.application.oauthConfiguration.enabledGrants.contains(GrantType.password)) {
      paramOAuthValidationResult.error = new OAuthError(OAuthError.OAuthErrorType.unauthorized_client, OAuthError.OAuthErrorReason.grant_type_disabled, "The [password] Password grant has been disabled for this client.");
      return paramOAuthValidationResult;
    } 
    validateScopes(paramOAuthValidationResult);
    return paramOAuthValidationResult;
  }
  
  private OAuthService.OAuthValidationResult validateTokenEndpointRefreshTokenGrant(OAuthService.OAuthValidationResult paramOAuthValidationResult, String paramString1, String paramString2, Integer paramInteger) {
    if (paramString1 == null) {
      paramOAuthValidationResult.error = new OAuthError(OAuthError.OAuthErrorType.invalid_request, OAuthError.OAuthErrorReason.missing_refresh_token, "The request is missing a required parameter: refresh_token");
      return paramOAuthValidationResult;
    } 
    validateClientId(paramOAuthValidationResult);
    if (paramOAuthValidationResult.error != null)
      return paramOAuthValidationResult; 
    validateClientCredentials(paramOAuthValidationResult);
    if (paramOAuthValidationResult.error != null)
      return paramOAuthValidationResult; 
    validateAndResolveTenantForUniversalApplication(paramOAuthValidationResult.tenantId, paramOAuthValidationResult);
    if (paramOAuthValidationResult.error != null)
      return paramOAuthValidationResult; 
    if (!paramOAuthValidationResult.application.oauthConfiguration.enabledGrants.contains(GrantType.refresh_token)) {
      paramOAuthValidationResult.error = new OAuthError(OAuthError.OAuthErrorType.unauthorized_client, OAuthError.OAuthErrorReason.grant_type_disabled, "The [refresh_token] Refresh Token grant has been disabled for this client.");
      return paramOAuthValidationResult;
    } 
    paramOAuthValidationResult.refreshToken = this.refreshTokenService.retrieveRefreshTokenUsingSeed(paramString1);
    if (paramOAuthValidationResult.refreshToken != null)
      if (paramOAuthValidationResult.refreshToken.isExpired(paramOAuthValidationResult.tenant, paramOAuthValidationResult.application)) {
        paramOAuthValidationResult.refreshToken = null;
      } else if (!paramOAuthValidationResult.application.id.equals(paramOAuthValidationResult.refreshToken.applicationId)) {
        paramOAuthValidationResult.refreshToken = null;
      }  
    if (paramOAuthValidationResult.refreshToken == null)
      return new OAuthService.OAuthValidationResult(new OAuthError(OAuthError.OAuthErrorType.invalid_grant, OAuthError.OAuthErrorReason.refresh_token_not_found, "The refresh_token is invalid.")); 
    OAuthError oAuthError = this.dPoPService.checkRefreshToken(paramOAuthValidationResult.refreshToken, paramOAuthValidationResult.dPoPThumbprint);
    if (oAuthError != null)
      return new OAuthService.OAuthValidationResult(oAuthError); 
    paramOAuthValidationResult.user = this.userReader.retrieveById(paramOAuthValidationResult.tenant.id, paramOAuthValidationResult.refreshToken.userId);
    if (paramOAuthValidationResult.user == null)
      return new OAuthService.OAuthValidationResult(new OAuthError(OAuthError.OAuthErrorType.invalid_grant, OAuthError.OAuthErrorReason.refresh_token_not_found, "The refresh_token is invalid.")); 
    if (paramString2 != null)
      if (this.jwtService.validateAccessTokenOwnershipForRefresh(paramOAuthValidationResult.refreshToken, paramString2, paramOAuthValidationResult.tenant, paramOAuthValidationResult.application))
        paramOAuthValidationResult.accessToken = paramString2;  
    if (paramInteger != null) {
      int i = (paramOAuthValidationResult.tenant.lookupJWTConfiguration(paramOAuthValidationResult.application)).timeToLiveInSeconds;
      if (paramInteger.intValue() > i)
        return new OAuthService.OAuthValidationResult(new OAuthError(OAuthError.OAuthErrorType.invalid_request, OAuthError.OAuthErrorReason.invalid_expires_in, "Invalid expires_in. Must be less than or equal to the configured value of " + i + ".")); 
      paramOAuthValidationResult.expiresInSeconds = paramInteger;
    } 
    Set<? extends String> set = (Set)Objects.requireNonNullElse(paramOAuthValidationResult.refreshToken.metaData.scopes, Collections.emptySet());
    if (!paramOAuthValidationResult.scopes.isEmpty()) {
      if (!set.containsAll(paramOAuthValidationResult.scopes))
        return new OAuthService.OAuthValidationResult(new OAuthError(OAuthError.OAuthErrorType.invalid_scope, "The scope provided is different than the scope used to generate the refresh token.")); 
    } else {
      paramOAuthValidationResult.scopes = new LinkedHashSet<>(set);
    } 
    if (paramOAuthValidationResult.application.oauthConfiguration.unknownScopePolicy == UnknownScopePolicy.Reject) {
      OAuthService.OAuthValidationResult oAuthValidationResult = new OAuthService.OAuthValidationResult(paramOAuthValidationResult.tenant, paramOAuthValidationResult.application);
      oAuthValidationResult.scopes = new LinkedHashSet<>(set);
      validateScopes(oAuthValidationResult);
      if (oAuthValidationResult.error != null) {
        this.refreshTokenService.revokeRefreshToken(paramOAuthValidationResult.tenant, paramOAuthValidationResult.application, paramOAuthValidationResult.refreshToken, paramOAuthValidationResult.user, new EventInfo());
        return paramOAuthValidationResult.withError(oAuthValidationResult.error);
      } 
    } 
    validateScopes(paramOAuthValidationResult);
    return paramOAuthValidationResult;
  }
}
