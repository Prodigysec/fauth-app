package io.fusionauth.api.service.oauth2;

import com.fasterxml.jackson.databind.JsonNode;
import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.api.service.authentication.AuthenticationType;
import io.fusionauth.app.service.security.SSOService;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.Entity;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.IdentityProviderLink;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.User;
import io.fusionauth.domain.jwt.DeviceInfo;
import io.fusionauth.domain.jwt.RefreshToken;
import io.fusionauth.domain.oauth2.AccessToken;
import io.fusionauth.domain.oauth2.DeviceResponse;
import io.fusionauth.domain.oauth2.GrantType;
import io.fusionauth.domain.oauth2.OAuth2Configuration;
import io.fusionauth.domain.oauth2.OAuthError;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.domain.provider.IdentityProviderType;
import io.fusionauth.http.server.HTTPRequest;
import io.fusionauth.jwt.domain.JWT;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.primeframework.mvc.util.QueryStringBuilder;

public interface OAuthService {
  static OAuthError handleJWTDecodingExceptions(Exception paramException) {
    if (paramException instanceof io.fusionauth.jwt.InvalidJWTException)
      return new OAuthError(OAuthError.OAuthErrorType.invalid_token, OAuthError.OAuthErrorReason.access_token_malformed, "The access token cannot be parsed."); 
    if (paramException instanceof io.fusionauth.jwt.JWTExpiredException)
      return new OAuthError(OAuthError.OAuthErrorType.invalid_token, OAuthError.OAuthErrorReason.access_token_expired, "The access token has expired."); 
    if (paramException instanceof io.fusionauth.jwt.JWTUnavailableForProcessingException)
      return new OAuthError(OAuthError.OAuthErrorType.invalid_token, OAuthError.OAuthErrorReason.access_token_unavailable_for_processing, "The access token is unavailable for processing."); 
    return new OAuthError(OAuthError.OAuthErrorType.invalid_token, OAuthError.OAuthErrorReason.access_token_failed_processing, "The access token failed to be decoded.");
  }
  
  static CredentialResult parseCredentials(@Nonnull String paramString1, String paramString2) {
    String str3, str1 = "basic ";
    if (!paramString1.toLowerCase().startsWith(str1))
      return new CredentialResult.Failure(new OAuthError(OAuthError.OAuthErrorType.invalid_client, OAuthError.OAuthErrorReason.invalid_client_authentication_scheme, "Invalid client authentication. Authorization header was not the Basic scheme.")); 
    String str2 = paramString1.substring(str1.length());
    try {
      str3 = new String(Base64.getDecoder().decode(str2), StandardCharsets.UTF_8);
    } catch (Exception exception) {
      return new CredentialResult.Failure(new OAuthError(OAuthError.OAuthErrorType.invalid_client, OAuthError.OAuthErrorReason.invalid_client_authentication, "Invalid client authentication credentials. The value is not correctly Base64 encoded."));
    } 
    int i = str3.indexOf(':');
    if (i < 0)
      return new CredentialResult.Failure(new OAuthError(OAuthError.OAuthErrorType.invalid_client, OAuthError.OAuthErrorReason.invalid_client_authentication, "Invalid client authentication credentials. Missing a username and password in the Basic Authorization header.")); 
    String str4 = str3.substring(0, i);
    if (paramString2 != null && !paramString2.equals(str4))
      return new CredentialResult.Failure(new OAuthError(OAuthError.OAuthErrorType.invalid_client, OAuthError.OAuthErrorReason.client_id_mismatch, "Invalid client authentication credentials. A client_id was provided in the request body and in the Authorization header, and they do not match.")); 
    return new CredentialResult.Success(str4, str3.substring(i + 1));
  }
  
  DeviceApproveResult approveDevice(EventInfo paramEventInfo, Tenant paramTenant, Application paramApplication, User paramUser, ExternalIdentifier paramExternalIdentifier, Set<String> paramSet);
  
  boolean checkPersistedUserConsentChoices(Tenant paramTenant, Application paramApplication, UUID paramUUID, Set<String> paramSet);
  
  AccessToken createAccessToken(EventInfo paramEventInfo, Tenant paramTenant, Application paramApplication, User paramUser, String paramString1, URI paramURI, GrantType paramGrantType, String paramString2, Set<String> paramSet, String paramString3, String paramString4, UUID paramUUID1, String paramString5, String paramString6, UUID paramUUID2, RefreshToken.MetaData paramMetaData, AuthenticationType paramAuthenticationType, String paramString7, @Nullable String paramString8);
  
  String createAuthorizationCode(Tenant paramTenant, UUID paramUUID1, String paramString1, URI paramURI, UUID paramUUID2, String paramString2, String paramString3, RefreshToken.MetaData paramMetaData, Map<String, String> paramMap);
  
  AccessToken createClientCredentialsAccessToken(Tenant paramTenant, Entity paramEntity, Map<String, Entity> paramMap, Map<String, Set<String>> paramMap1, Set<String> paramSet, @Nullable String paramString);
  
  void decodeAndRestoreStateFromCallback(Object paramObject, String paramString);
  
  String encodeStateForRedirect(Consumer<QueryStringBuilder> paramConsumer);
  
  OAuthTokenResult exchangeAuthorizationCode(EventInfo paramEventInfo, Tenant paramTenant, Application paramApplication, String paramString1, URI paramURI, String paramString2, String paramString3, List<URI> paramList, @Nullable String paramString4);
  
  DeviceResponse generateDeviceResponse(Tenant paramTenant, Application paramApplication, Set<String> paramSet, RefreshToken.MetaData paramMetaData, @Nullable String paramString);
  
  Set<String> getOAuthScopeNamesForPromptFromApplication(Application paramApplication);
  
  void persistUserConsentChoices(Tenant paramTenant, Application paramApplication, User paramUser, Map<String, Boolean> paramMap);
  
  OAuthTokenResult refreshAccessToken(Tenant paramTenant, Application paramApplication, User paramUser, RefreshToken paramRefreshToken, Set<String> paramSet, String paramString1, Integer paramInteger, String paramString2, EventInfo paramEventInfo, @Nullable String paramString3, @Nullable List<URI> paramList);
  
  OAuthValidationResult rehydrateIdPInitiatedLoginToOAuth(UUID paramUUID, String paramString1, URI paramURI, String paramString2);
  
  OAuthValidationResult validateAppCallbackHandle(String paramString1, String paramString2, URI paramURI);
  
  OAuthValidationResult validateAppLogoutRequest(String paramString);
  
  OAuthValidationResult validateAppRedirectRequest(String paramString, URI paramURI);
  
  OAuthValidationResult validateAppRefreshRequest(String paramString);
  
  OAuthValidationResult validateAuthorizeRequest(UUID paramUUID, String paramString1, String paramString2, URI paramURI, String paramString3, String paramString4, String paramString5, String paramString6, String paramString7, String paramString8, boolean paramBoolean, List<URI> paramList);
  
  BootstrapValidationResult validateBootstrapJWT(String paramString, Tenant paramTenant);
  
  OAuthValidationResult validateConsentedScopes(Application paramApplication, Set<String> paramSet1, Set<String> paramSet2);
  
  OAuthValidationResult validateDeviceApproveRequest(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, UUID paramUUID);
  
  OAuthValidationResult validateDeviceRequest(UUID paramUUID, String paramString1, String paramString2, HTTPRequest paramHTTPRequest);
  
  OAuthValidationResult validateIntrospectRequest(String paramString1, String paramString2, String paramString3, UUID paramUUID, String paramString4, String paramString5);
  
  OAuthValidationResult validateLogoutRequest(UUID paramUUID, String paramString1, String paramString2, String paramString3, SSOService.SSOSession paramSSOSession);
  
  boolean validateOrigin(OAuth2Configuration paramOAuth2Configuration, URI paramURI);
  
  OAuthValidationResult validateSelfServiceRequest(UUID paramUUID, String paramString);
  
  OAuthValidationResult validateTokenRequest(UUID paramUUID, String paramString1, String paramString2, URI paramURI, String paramString3, String paramString4, String paramString5, String paramString6, String paramString7, String paramString8, String paramString9, String paramString10, Integer paramInteger, HTTPRequest paramHTTPRequest);
  
  OAuthValidationResult validateUserCodeInfoRequest(String paramString1, String paramString2, String paramString3, String paramString4, UUID paramUUID);
  
  OAuthValidationResult validateUserCodeInfoRequestUsingAPIKey(UUID paramUUID, String paramString);
  
  OAuthValidationResult validateUserCodeRequest(UUID paramUUID, String paramString1, String paramString2);
  
  public static final class Failure extends Record implements CredentialResult {
    private final OAuthError error;
    
    public Failure(OAuthError param1OAuthError) {
      this.error = param1OAuthError;
    }
    
    public final String toString() {
      // Byte code:
      //   0: aload_0
      //   1: <illegal opcode> toString : (Lio/fusionauth/api/service/oauth2/OAuthService$CredentialResult$Failure;)Ljava/lang/String;
      //   6: areturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #513	-> 0
    }
    
    public final int hashCode() {
      // Byte code:
      //   0: aload_0
      //   1: <illegal opcode> hashCode : (Lio/fusionauth/api/service/oauth2/OAuthService$CredentialResult$Failure;)I
      //   6: ireturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #513	-> 0
    }
    
    public final boolean equals(Object param1Object) {
      // Byte code:
      //   0: aload_0
      //   1: aload_1
      //   2: <illegal opcode> equals : (Lio/fusionauth/api/service/oauth2/OAuthService$CredentialResult$Failure;Ljava/lang/Object;)Z
      //   7: ireturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #513	-> 0
    }
    
    public OAuthError error() {
      return this.error;
    }
  }
  
  public static interface CredentialResult {
    public static final class Failure extends Record implements CredentialResult {
      private final OAuthError error;
      
      public Failure(OAuthError param2OAuthError) {
        this.error = param2OAuthError;
      }
      
      public final String toString() {
        // Byte code:
        //   0: aload_0
        //   1: <illegal opcode> toString : (Lio/fusionauth/api/service/oauth2/OAuthService$CredentialResult$Failure;)Ljava/lang/String;
        //   6: areturn
        // Line number table:
        //   Java source line number -> byte code offset
        //   #513	-> 0
      }
      
      public final int hashCode() {
        // Byte code:
        //   0: aload_0
        //   1: <illegal opcode> hashCode : (Lio/fusionauth/api/service/oauth2/OAuthService$CredentialResult$Failure;)I
        //   6: ireturn
        // Line number table:
        //   Java source line number -> byte code offset
        //   #513	-> 0
      }
      
      public final boolean equals(Object param2Object) {
        // Byte code:
        //   0: aload_0
        //   1: aload_1
        //   2: <illegal opcode> equals : (Lio/fusionauth/api/service/oauth2/OAuthService$CredentialResult$Failure;Ljava/lang/Object;)Z
        //   7: ireturn
        // Line number table:
        //   Java source line number -> byte code offset
        //   #513	-> 0
      }
      
      public OAuthError error() {
        return this.error;
      }
    }
    
    public static final class Success extends Record implements CredentialResult {
      private final String client_id;
      
      private final String client_secret;
      
      public Success(String param2String1, String param2String2) {
        this.client_id = param2String1;
        this.client_secret = param2String2;
      }
      
      public final String toString() {
        // Byte code:
        //   0: aload_0
        //   1: <illegal opcode> toString : (Lio/fusionauth/api/service/oauth2/OAuthService$CredentialResult$Success;)Ljava/lang/String;
        //   6: areturn
        // Line number table:
        //   Java source line number -> byte code offset
        //   #522	-> 0
      }
      
      public final int hashCode() {
        // Byte code:
        //   0: aload_0
        //   1: <illegal opcode> hashCode : (Lio/fusionauth/api/service/oauth2/OAuthService$CredentialResult$Success;)I
        //   6: ireturn
        // Line number table:
        //   Java source line number -> byte code offset
        //   #522	-> 0
      }
      
      public final boolean equals(Object param2Object) {
        // Byte code:
        //   0: aload_0
        //   1: aload_1
        //   2: <illegal opcode> equals : (Lio/fusionauth/api/service/oauth2/OAuthService$CredentialResult$Success;Ljava/lang/Object;)Z
        //   7: ireturn
        // Line number table:
        //   Java source line number -> byte code offset
        //   #522	-> 0
      }
      
      public String client_id() {
        return this.client_id;
      }
      
      public String client_secret() {
        return this.client_secret;
      }
    }
  }
  
  public static final class Success extends Record implements CredentialResult {
    private final String client_id;
    
    private final String client_secret;
    
    public Success(String param1String1, String param1String2) {
      this.client_id = param1String1;
      this.client_secret = param1String2;
    }
    
    public final String toString() {
      // Byte code:
      //   0: aload_0
      //   1: <illegal opcode> toString : (Lio/fusionauth/api/service/oauth2/OAuthService$CredentialResult$Success;)Ljava/lang/String;
      //   6: areturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #522	-> 0
    }
    
    public final int hashCode() {
      // Byte code:
      //   0: aload_0
      //   1: <illegal opcode> hashCode : (Lio/fusionauth/api/service/oauth2/OAuthService$CredentialResult$Success;)I
      //   6: ireturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #522	-> 0
    }
    
    public final boolean equals(Object param1Object) {
      // Byte code:
      //   0: aload_0
      //   1: aload_1
      //   2: <illegal opcode> equals : (Lio/fusionauth/api/service/oauth2/OAuthService$CredentialResult$Success;Ljava/lang/Object;)Z
      //   7: ireturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #522	-> 0
    }
    
    public String client_id() {
      return this.client_id;
    }
    
    public String client_secret() {
      return this.client_secret;
    }
  }
  
  public static class AppOAuthState {
    public String c;
    
    public URI r;
    
    public String s;
    
    @JacksonConstructor
    public AppOAuthState() {}
    
    public AppOAuthState(String param1String1, URI param1URI, String param1String2) {
      this.c = param1String1;
      this.r = param1URI;
      this.s = param1String2;
    }
  }
  
  public static class BootstrapValidationResult {
    public AuthenticationType authenticationType;
    
    public JWT jwt;
    
    public ZonedDateTime originalAuthTime;
    
    public User user;
  }
  
  public static class DeviceApproveResult {
    public UUID applicationId;
    
    public String deviceGrantStatus;
    
    public DeviceInfo deviceInfo;
    
    public OAuthError error;
    
    public IdentityProviderLink identityProviderLink;
    
    public UUID tenantId;
    
    public UUID userId;
  }
  
  public static class LinkingTokenDetails implements Buildable<LinkingTokenDetails> {
    public String identityProviderDisplayName;
    
    public UUID identityProviderId;
    
    public String identityProviderName;
    
    public IdentityProviderType identityProviderType;
    
    public String identityProviderUserId;
    
    public JsonNode raw;
    
    public LinkingTokenDetails() {}
    
    public LinkingTokenDetails(BaseIdentityProvider<?> param1BaseIdentityProvider) {
      this.identityProviderId = param1BaseIdentityProvider.id;
      this.identityProviderName = param1BaseIdentityProvider.name;
      this.identityProviderType = param1BaseIdentityProvider.getType();
    }
    
    public boolean equals(Object param1Object) {
      if (this == param1Object)
        return true; 
      if (param1Object == null || getClass() != param1Object.getClass())
        return false; 
      LinkingTokenDetails linkingTokenDetails = (LinkingTokenDetails)param1Object;
      return (Objects.equals(this.identityProviderDisplayName, linkingTokenDetails.identityProviderDisplayName) && Objects.equals(this.identityProviderId, linkingTokenDetails.identityProviderId) && Objects.equals(this.identityProviderName, linkingTokenDetails.identityProviderName) && this.identityProviderType == linkingTokenDetails.identityProviderType && Objects.equals(this.identityProviderUserId, linkingTokenDetails.identityProviderUserId) && Objects.equals(this.raw, linkingTokenDetails.raw));
    }
    
    public int hashCode() {
      return Objects.hash(new Object[] { this.identityProviderDisplayName, this.identityProviderId, this.identityProviderName, this.identityProviderType, this.identityProviderUserId, this.raw });
    }
    
    public String toString() {
      return ToString.toString(this);
    }
  }
  
  public static class OAuthTokenResult {
    public AccessToken accessToken;
    
    public OAuthError error;
    
    public OAuthTokenResult(AccessToken param1AccessToken) {
      this.accessToken = param1AccessToken;
    }
    
    public OAuthTokenResult(OAuthError param1OAuthError) {
      this.error = param1OAuthError;
    }
  }
  
  public static class OAuthValidationResult {
    public String accessToken;
    
    public URI appCallback;
    
    public Application application;
    
    public boolean authHeaderUsed;
    
    public String client_id;
    
    public String client_secret;
    
    public String dPoPThumbprint;
    
    public boolean doNotRedirect;
    
    public Map<String, Set<String>> entityGrantPermissions = new LinkedHashMap<>();
    
    public OAuthError error;
    
    public Integer expiresInSeconds;
    
    public ExternalIdentifier externalIdentifier;
    
    public GrantType grantType;
    
    public Map<UUID, String> logoutURLs = new HashMap<>();
    
    public Set<String> prompts = new LinkedHashSet<>();
    
    public Entity recipientEntity;
    
    public String redirectURL;
    
    public URI redirect_uri;
    
    public RefreshToken refreshToken;
    
    public Set<String> scopes;
    
    public String sid;
    
    public Map<String, Entity> targetEntities = new HashMap<>();
    
    public Tenant tenant;
    
    public UUID tenantId;
    
    public boolean unsafeIntrospectDecodeFailed;
    
    public User user;
    
    public String user_code;
    
    public List<URI> validatedResources = new ArrayList<>();
    
    @JacksonConstructor
    public OAuthValidationResult() {}
    
    public OAuthValidationResult(Tenant param1Tenant, Application param1Application) {
      this.tenant = param1Tenant;
      this.application = param1Application;
    }
    
    public OAuthValidationResult(OAuthError param1OAuthError) {
      this.error = param1OAuthError;
    }
    
    public OAuthValidationResult(Tenant param1Tenant, Application param1Application, String param1String) {
      this.tenant = param1Tenant;
      this.application = param1Application;
      this.redirectURL = param1String;
    }
    
    public OAuthValidationResult(Tenant param1Tenant, Application param1Application, Map<UUID, String> param1Map, String param1String) {
      this.tenant = param1Tenant;
      this.application = param1Application;
      this.logoutURLs = param1Map;
      this.redirectURL = param1String;
    }
    
    public OAuthValidationResult setDoNotRedirect(boolean param1Boolean) {
      this.doNotRedirect = param1Boolean;
      return this;
    }
    
    public void visit(OAuthService.CredentialResult param1CredentialResult) {
      // Byte code:
      //   0: aload_0
      //   1: iconst_1
      //   2: putfield authHeaderUsed : Z
      //   5: aload_1
      //   6: dup
      //   7: invokestatic requireNonNull : (Ljava/lang/Object;)Ljava/lang/Object;
      //   10: pop
      //   11: astore_2
      //   12: iconst_0
      //   13: istore_3
      //   14: aload_2
      //   15: iload_3
      //   16: <illegal opcode> typeSwitch : (Lio/fusionauth/api/service/oauth2/OAuthService$CredentialResult;I)I
      //   21: lookupswitch default -> 48, 0 -> 58, 1 -> 85
      //   48: new java/lang/MatchException
      //   51: dup
      //   52: aconst_null
      //   53: aconst_null
      //   54: invokespecial <init> : (Ljava/lang/String;Ljava/lang/Throwable;)V
      //   57: athrow
      //   58: aload_2
      //   59: checkcast io/fusionauth/api/service/oauth2/OAuthService$CredentialResult$Success
      //   62: astore #4
      //   64: aload_0
      //   65: aload #4
      //   67: invokevirtual client_id : ()Ljava/lang/String;
      //   70: putfield client_id : Ljava/lang/String;
      //   73: aload_0
      //   74: aload #4
      //   76: invokevirtual client_secret : ()Ljava/lang/String;
      //   79: putfield client_secret : Ljava/lang/String;
      //   82: goto -> 100
      //   85: aload_2
      //   86: checkcast io/fusionauth/api/service/oauth2/OAuthService$CredentialResult$Failure
      //   89: astore #5
      //   91: aload_0
      //   92: aload #5
      //   94: invokevirtual error : ()Lio/fusionauth/domain/oauth2/OAuthError;
      //   97: putfield error : Lio/fusionauth/domain/oauth2/OAuthError;
      //   100: return
      // Line number table:
      //   Java source line number -> byte code offset
      //   #740	-> 0
      //   #742	-> 5
      //   #743	-> 58
      //   #744	-> 64
      //   #745	-> 73
      //   #746	-> 82
      //   #747	-> 85
      //   #749	-> 100
    }
    
    public OAuthValidationResult withError(OAuthError param1OAuthError) {
      this.error = param1OAuthError;
      return this;
    }
  }
}
