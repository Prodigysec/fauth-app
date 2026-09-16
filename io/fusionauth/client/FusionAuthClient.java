package io.fusionauth.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.inversoft.error.Error;
import com.inversoft.error.Errors;
import com.inversoft.json.JacksonModule;
import com.inversoft.rest.ClientResponse;
import com.inversoft.rest.FormDataBodyHandler;
import com.inversoft.rest.JSONBodyHandler;
import com.inversoft.rest.JSONResponseHandler;
import com.inversoft.rest.RESTClient;
import com.inversoft.rest.RetryConfiguration;
import io.fusionauth.client.json.FusionAuthJacksonModule;
import io.fusionauth.domain.LambdaType;
import io.fusionauth.domain.OpenIdConfiguration;
import io.fusionauth.domain.api.APIKeyRequest;
import io.fusionauth.domain.api.APIKeyResponse;
import io.fusionauth.domain.api.ApplicationOAuthScopeRequest;
import io.fusionauth.domain.api.ApplicationOAuthScopeResponse;
import io.fusionauth.domain.api.ApplicationRequest;
import io.fusionauth.domain.api.ApplicationResponse;
import io.fusionauth.domain.api.ApplicationSearchRequest;
import io.fusionauth.domain.api.ApplicationSearchResponse;
import io.fusionauth.domain.api.AuditLogRequest;
import io.fusionauth.domain.api.AuditLogResponse;
import io.fusionauth.domain.api.AuditLogSearchRequest;
import io.fusionauth.domain.api.AuditLogSearchResponse;
import io.fusionauth.domain.api.ConnectorRequest;
import io.fusionauth.domain.api.ConnectorResponse;
import io.fusionauth.domain.api.ConsentRequest;
import io.fusionauth.domain.api.ConsentResponse;
import io.fusionauth.domain.api.ConsentSearchRequest;
import io.fusionauth.domain.api.ConsentSearchResponse;
import io.fusionauth.domain.api.EmailTemplateRequest;
import io.fusionauth.domain.api.EmailTemplateResponse;
import io.fusionauth.domain.api.EmailTemplateSearchRequest;
import io.fusionauth.domain.api.EmailTemplateSearchResponse;
import io.fusionauth.domain.api.EntityGrantRequest;
import io.fusionauth.domain.api.EntityGrantResponse;
import io.fusionauth.domain.api.EntityGrantSearchRequest;
import io.fusionauth.domain.api.EntityGrantSearchResponse;
import io.fusionauth.domain.api.EntityRequest;
import io.fusionauth.domain.api.EntityResponse;
import io.fusionauth.domain.api.EntitySearchRequest;
import io.fusionauth.domain.api.EntitySearchResponse;
import io.fusionauth.domain.api.EntityTypeRequest;
import io.fusionauth.domain.api.EntityTypeResponse;
import io.fusionauth.domain.api.EntityTypeSearchRequest;
import io.fusionauth.domain.api.EntityTypeSearchResponse;
import io.fusionauth.domain.api.EventLogResponse;
import io.fusionauth.domain.api.EventLogSearchRequest;
import io.fusionauth.domain.api.EventLogSearchResponse;
import io.fusionauth.domain.api.FamilyEmailRequest;
import io.fusionauth.domain.api.FamilyRequest;
import io.fusionauth.domain.api.FamilyResponse;
import io.fusionauth.domain.api.FormFieldRequest;
import io.fusionauth.domain.api.FormFieldResponse;
import io.fusionauth.domain.api.FormRequest;
import io.fusionauth.domain.api.FormResponse;
import io.fusionauth.domain.api.GroupMemberSearchRequest;
import io.fusionauth.domain.api.GroupMemberSearchResponse;
import io.fusionauth.domain.api.GroupRequest;
import io.fusionauth.domain.api.GroupResponse;
import io.fusionauth.domain.api.GroupSearchRequest;
import io.fusionauth.domain.api.GroupSearchResponse;
import io.fusionauth.domain.api.IPAccessControlListRequest;
import io.fusionauth.domain.api.IPAccessControlListResponse;
import io.fusionauth.domain.api.IPAccessControlListSearchRequest;
import io.fusionauth.domain.api.IPAccessControlListSearchResponse;
import io.fusionauth.domain.api.IdentityProviderRequest;
import io.fusionauth.domain.api.IdentityProviderResponse;
import io.fusionauth.domain.api.IdentityProviderSearchRequest;
import io.fusionauth.domain.api.IdentityProviderSearchResponse;
import io.fusionauth.domain.api.IntegrationRequest;
import io.fusionauth.domain.api.IntegrationResponse;
import io.fusionauth.domain.api.KeyRequest;
import io.fusionauth.domain.api.KeyResponse;
import io.fusionauth.domain.api.KeySearchRequest;
import io.fusionauth.domain.api.KeySearchResponse;
import io.fusionauth.domain.api.LambdaRequest;
import io.fusionauth.domain.api.LambdaResponse;
import io.fusionauth.domain.api.LambdaSearchRequest;
import io.fusionauth.domain.api.LambdaSearchResponse;
import io.fusionauth.domain.api.LoginPingRequest;
import io.fusionauth.domain.api.LoginRecordSearchRequest;
import io.fusionauth.domain.api.LoginRecordSearchResponse;
import io.fusionauth.domain.api.LoginRequest;
import io.fusionauth.domain.api.LoginResponse;
import io.fusionauth.domain.api.LogoutRequest;
import io.fusionauth.domain.api.MemberDeleteRequest;
import io.fusionauth.domain.api.MemberRequest;
import io.fusionauth.domain.api.MemberResponse;
import io.fusionauth.domain.api.MessageTemplateRequest;
import io.fusionauth.domain.api.MessageTemplateResponse;
import io.fusionauth.domain.api.MessengerRequest;
import io.fusionauth.domain.api.MessengerResponse;
import io.fusionauth.domain.api.OAuthConfigurationResponse;
import io.fusionauth.domain.api.PasswordValidationRulesResponse;
import io.fusionauth.domain.api.PendingResponse;
import io.fusionauth.domain.api.PreviewMessageTemplateRequest;
import io.fusionauth.domain.api.PreviewMessageTemplateResponse;
import io.fusionauth.domain.api.PreviewRequest;
import io.fusionauth.domain.api.PreviewResponse;
import io.fusionauth.domain.api.PublicKeyResponse;
import io.fusionauth.domain.api.ReactorMetricsResponse;
import io.fusionauth.domain.api.ReactorRequest;
import io.fusionauth.domain.api.ReactorResponse;
import io.fusionauth.domain.api.ReindexRequest;
import io.fusionauth.domain.api.StatusResponse;
import io.fusionauth.domain.api.SystemConfigurationRequest;
import io.fusionauth.domain.api.SystemConfigurationResponse;
import io.fusionauth.domain.api.TenantDeleteRequest;
import io.fusionauth.domain.api.TenantRequest;
import io.fusionauth.domain.api.TenantResponse;
import io.fusionauth.domain.api.TenantSearchRequest;
import io.fusionauth.domain.api.TenantSearchResponse;
import io.fusionauth.domain.api.ThemeRequest;
import io.fusionauth.domain.api.ThemeResponse;
import io.fusionauth.domain.api.ThemeSearchRequest;
import io.fusionauth.domain.api.ThemeSearchResponse;
import io.fusionauth.domain.api.TwoFactorDisableRequest;
import io.fusionauth.domain.api.TwoFactorRecoveryCodeResponse;
import io.fusionauth.domain.api.TwoFactorRequest;
import io.fusionauth.domain.api.TwoFactorResponse;
import io.fusionauth.domain.api.TwoFactorUpdateRequest;
import io.fusionauth.domain.api.UserActionReasonRequest;
import io.fusionauth.domain.api.UserActionReasonResponse;
import io.fusionauth.domain.api.UserActionRequest;
import io.fusionauth.domain.api.UserActionResponse;
import io.fusionauth.domain.api.UserCommentRequest;
import io.fusionauth.domain.api.UserCommentResponse;
import io.fusionauth.domain.api.UserCommentSearchRequest;
import io.fusionauth.domain.api.UserCommentSearchResponse;
import io.fusionauth.domain.api.UserConsentRequest;
import io.fusionauth.domain.api.UserConsentResponse;
import io.fusionauth.domain.api.UserDeleteRequest;
import io.fusionauth.domain.api.UserDeleteResponse;
import io.fusionauth.domain.api.UserDeleteSingleRequest;
import io.fusionauth.domain.api.UserRequest;
import io.fusionauth.domain.api.UserResponse;
import io.fusionauth.domain.api.VersionResponse;
import io.fusionauth.domain.api.WebAuthnAssertResponse;
import io.fusionauth.domain.api.WebAuthnCredentialImportRequest;
import io.fusionauth.domain.api.WebAuthnCredentialResponse;
import io.fusionauth.domain.api.WebAuthnLoginRequest;
import io.fusionauth.domain.api.WebAuthnRegisterCompleteRequest;
import io.fusionauth.domain.api.WebAuthnRegisterCompleteResponse;
import io.fusionauth.domain.api.WebAuthnRegisterStartRequest;
import io.fusionauth.domain.api.WebAuthnRegisterStartResponse;
import io.fusionauth.domain.api.WebAuthnStartRequest;
import io.fusionauth.domain.api.WebAuthnStartResponse;
import io.fusionauth.domain.api.WebhookAttemptLogResponse;
import io.fusionauth.domain.api.WebhookEventLogResponse;
import io.fusionauth.domain.api.WebhookEventLogSearchRequest;
import io.fusionauth.domain.api.WebhookEventLogSearchResponse;
import io.fusionauth.domain.api.WebhookRequest;
import io.fusionauth.domain.api.WebhookResponse;
import io.fusionauth.domain.api.WebhookSearchRequest;
import io.fusionauth.domain.api.WebhookSearchResponse;
import io.fusionauth.domain.api.email.SendRequest;
import io.fusionauth.domain.api.email.SendResponse;
import io.fusionauth.domain.api.identity.verify.VerifyCompleteRequest;
import io.fusionauth.domain.api.identity.verify.VerifyCompleteResponse;
import io.fusionauth.domain.api.identity.verify.VerifyRequest;
import io.fusionauth.domain.api.identity.verify.VerifySendRequest;
import io.fusionauth.domain.api.identity.verify.VerifyStartRequest;
import io.fusionauth.domain.api.identity.verify.VerifyStartResponse;
import io.fusionauth.domain.api.identityProvider.IdentityProviderConnectionTestRequest;
import io.fusionauth.domain.api.identityProvider.IdentityProviderConnectionTestResponse;
import io.fusionauth.domain.api.identityProvider.IdentityProviderLinkRequest;
import io.fusionauth.domain.api.identityProvider.IdentityProviderLinkResponse;
import io.fusionauth.domain.api.identityProvider.IdentityProviderLoginRequest;
import io.fusionauth.domain.api.identityProvider.IdentityProviderPendingLinkResponse;
import io.fusionauth.domain.api.identityProvider.IdentityProviderStartLoginRequest;
import io.fusionauth.domain.api.identityProvider.IdentityProviderStartLoginResponse;
import io.fusionauth.domain.api.identityProvider.LookupResponse;
import io.fusionauth.domain.api.jwt.IssueResponse;
import io.fusionauth.domain.api.jwt.JWTRefreshResponse;
import io.fusionauth.domain.api.jwt.JWTVendRequest;
import io.fusionauth.domain.api.jwt.JWTVendResponse;
import io.fusionauth.domain.api.jwt.RefreshRequest;
import io.fusionauth.domain.api.jwt.RefreshTokenResponse;
import io.fusionauth.domain.api.jwt.RefreshTokenRevokeRequest;
import io.fusionauth.domain.api.jwt.ValidateResponse;
import io.fusionauth.domain.api.passwordless.PasswordlessLoginRequest;
import io.fusionauth.domain.api.passwordless.PasswordlessSendRequest;
import io.fusionauth.domain.api.passwordless.PasswordlessStartRequest;
import io.fusionauth.domain.api.passwordless.PasswordlessStartResponse;
import io.fusionauth.domain.api.report.DailyActiveUserReportResponse;
import io.fusionauth.domain.api.report.LoginReportResponse;
import io.fusionauth.domain.api.report.MonthlyActiveUserReportResponse;
import io.fusionauth.domain.api.report.RegistrationReportResponse;
import io.fusionauth.domain.api.report.TotalsReportResponse;
import io.fusionauth.domain.api.tenantManager.TenantManagerConfigurationRequest;
import io.fusionauth.domain.api.tenantManager.TenantManagerConfigurationResponse;
import io.fusionauth.domain.api.tenantManager.TenantManagerIdentityProviderTypeConfigurationRequest;
import io.fusionauth.domain.api.tenantManager.TenantManagerIdentityProviderTypeConfigurationResponse;
import io.fusionauth.domain.api.twoFactor.SecretResponse;
import io.fusionauth.domain.api.twoFactor.TwoFactorLoginRequest;
import io.fusionauth.domain.api.twoFactor.TwoFactorSendRequest;
import io.fusionauth.domain.api.twoFactor.TwoFactorStartRequest;
import io.fusionauth.domain.api.twoFactor.TwoFactorStartResponse;
import io.fusionauth.domain.api.twoFactor.TwoFactorStatusRequest;
import io.fusionauth.domain.api.twoFactor.TwoFactorStatusResponse;
import io.fusionauth.domain.api.user.ActionRequest;
import io.fusionauth.domain.api.user.ActionResponse;
import io.fusionauth.domain.api.user.ChangePasswordRequest;
import io.fusionauth.domain.api.user.ChangePasswordResponse;
import io.fusionauth.domain.api.user.ForgotPasswordRequest;
import io.fusionauth.domain.api.user.ForgotPasswordResponse;
import io.fusionauth.domain.api.user.ImportRequest;
import io.fusionauth.domain.api.user.RecentLoginResponse;
import io.fusionauth.domain.api.user.RefreshTokenImportRequest;
import io.fusionauth.domain.api.user.RegistrationDeleteRequest;
import io.fusionauth.domain.api.user.RegistrationRequest;
import io.fusionauth.domain.api.user.RegistrationResponse;
import io.fusionauth.domain.api.user.SearchRequest;
import io.fusionauth.domain.api.user.SearchResponse;
import io.fusionauth.domain.api.user.VerifyEmailRequest;
import io.fusionauth.domain.api.user.VerifyEmailResponse;
import io.fusionauth.domain.api.user.VerifyRegistrationRequest;
import io.fusionauth.domain.api.user.VerifyRegistrationResponse;
import io.fusionauth.domain.oauth2.AccessToken;
import io.fusionauth.domain.oauth2.AccessTokenIntrospectRequest;
import io.fusionauth.domain.oauth2.ClientCredentialsAccessTokenIntrospectRequest;
import io.fusionauth.domain.oauth2.ClientCredentialsGrantRequest;
import io.fusionauth.domain.oauth2.DeviceApprovalRequest;
import io.fusionauth.domain.oauth2.DeviceApprovalResponse;
import io.fusionauth.domain.oauth2.DeviceAuthorizationRequest;
import io.fusionauth.domain.oauth2.DeviceResponse;
import io.fusionauth.domain.oauth2.IntrospectResponse;
import io.fusionauth.domain.oauth2.JWKSResponse;
import io.fusionauth.domain.oauth2.OAuthCodeAccessTokenRequest;
import io.fusionauth.domain.oauth2.OAuthCodePKCEAccessTokenRequest;
import io.fusionauth.domain.oauth2.OAuthError;
import io.fusionauth.domain.oauth2.RefreshTokenAccessTokenRequest;
import io.fusionauth.domain.oauth2.RetrieveUserCodeRequest;
import io.fusionauth.domain.oauth2.RetrieveUserCodeUsingAPIKeyRequest;
import io.fusionauth.domain.oauth2.UserCredentialsAccessTokenRequest;
import io.fusionauth.domain.oauth2.UserinfoResponse;
import io.fusionauth.domain.oauth2.ValidateDeviceRequest;
import io.fusionauth.domain.provider.IdentityProviderType;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FusionAuthClient {
  public static String TENANT_ID_HEADER = "X-FusionAuth-TenantId";
  
  public static final ObjectMapper objectMapper = (new ObjectMapper()).setSerializationInclusion(JsonInclude.Include.NON_NULL)
    .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
    .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
    .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
    .configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true)
    .configure(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS, true)
    .registerModule((Module)new JacksonModule())
    .registerModule((Module)new FusionAuthJacksonModule());
  
  public static RetryConfiguration BASIC_RETRY_CONFIGURATION;
  
  private final String apiKey;
  
  private final String baseURL;
  
  private final ObjectMapper customMapper;
  
  private final String tenantId;
  
  public int connectTimeout;
  
  public int readTimeout;
  
  public RetryConfiguration retryConfiguration;
  
  static {
    BASIC_RETRY_CONFIGURATION = (RetryConfiguration)(new RetryConfiguration()).with(paramRetryConfiguration -> paramRetryConfiguration.retryFunction = (()));
  }
  
  public FusionAuthClient(String paramString1, String paramString2) {
    this(paramString1, paramString2, null);
  }
  
  public FusionAuthClient(String paramString1, String paramString2, String paramString3) {
    this(paramString1, paramString2, 2000, 2000, paramString3);
  }
  
  public FusionAuthClient(String paramString1, String paramString2, int paramInt1, int paramInt2) {
    this(paramString1, paramString2, paramInt1, paramInt2, null);
  }
  
  public FusionAuthClient(String paramString1, String paramString2, int paramInt1, int paramInt2, String paramString3) {
    this(paramString1, paramString2, paramInt1, paramInt2, paramString3, null);
  }
  
  public FusionAuthClient(String paramString1, String paramString2, int paramInt1, int paramInt2, String paramString3, ObjectMapper paramObjectMapper) {
    this.apiKey = paramString1;
    this.baseURL = paramString2;
    this.connectTimeout = paramInt1;
    this.readTimeout = paramInt2;
    this.tenantId = paramString3;
    this.customMapper = paramObjectMapper;
  }
  
  public FusionAuthClient setTenantId(UUID paramUUID) {
    if (paramUUID == null)
      return this; 
    FusionAuthClient fusionAuthClient = new FusionAuthClient(this.apiKey, this.baseURL, this.connectTimeout, this.readTimeout, paramUUID.toString());
    fusionAuthClient.retryConfiguration = this.retryConfiguration;
    return fusionAuthClient;
  }
  
  public FusionAuthClient setObjectMapper(ObjectMapper paramObjectMapper) {
    FusionAuthClient fusionAuthClient = new FusionAuthClient(this.apiKey, this.baseURL, this.connectTimeout, this.readTimeout, this.tenantId, paramObjectMapper);
    fusionAuthClient.retryConfiguration = this.retryConfiguration;
    return fusionAuthClient;
  }
  
  public ClientResponse<ActionResponse, Errors> actionUser(ActionRequest paramActionRequest) {
    return start((Class)ActionResponse.class, (Class)Errors.class)
      .uri("/api/user/action")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramActionRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<Void, Errors> activateReactor(ReactorRequest paramReactorRequest) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/reactor")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramReactorRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<FamilyResponse, Errors> addUserToFamily(UUID paramUUID, FamilyRequest paramFamilyRequest) {
    return start((Class)FamilyResponse.class, (Class)Errors.class)
      .uri("/api/user/family")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramFamilyRequest, objectMapper()))
      .put()
      .go();
  }
  
  public ClientResponse<DeviceApprovalResponse, Errors> approveDevice(String paramString1, String paramString2, String paramString3, String paramString4) {
    HashMap<Object, Object> hashMap = new HashMap<>();
    hashMap.put("client_id", Arrays.asList(new String[] { paramString1 }));
    hashMap.put("client_secret", Arrays.asList(new String[] { paramString2 }));
    hashMap.put("token", Arrays.asList(new String[] { paramString3 }));
    hashMap.put("user_code", Arrays.asList(new String[] { paramString4 }));
    return start((Class)DeviceApprovalResponse.class, (Class)Errors.class)
      .uri("/oauth2/device/approve")
      .bodyHandler((RESTClient.BodyHandler)new FormDataBodyHandler(hashMap))
      .post()
      .go();
  }
  
  public ClientResponse<DeviceApprovalResponse, Errors> approveDeviceWithRequest(DeviceApprovalRequest paramDeviceApprovalRequest) {
    HashMap<Object, Object> hashMap = new HashMap<>();
    hashMap.put("client_id", Arrays.asList(new String[] { paramDeviceApprovalRequest.client_id }));
    hashMap.put("client_secret", Arrays.asList(new String[] { paramDeviceApprovalRequest.client_secret }));
    if (paramDeviceApprovalRequest.tenantId != null)
      hashMap.put("tenantId", Arrays.asList(new String[] { paramDeviceApprovalRequest.tenantId.toString() })); 
    hashMap.put("token", Arrays.asList(new String[] { paramDeviceApprovalRequest.token }));
    hashMap.put("user_code", Arrays.asList(new String[] { paramDeviceApprovalRequest.user_code }));
    return start((Class)DeviceApprovalResponse.class, (Class)Errors.class)
      .uri("/oauth2/device/approve")
      .bodyHandler((RESTClient.BodyHandler)new FormDataBodyHandler(hashMap))
      .post()
      .go();
  }
  
  public ClientResponse<ActionResponse, Errors> cancelAction(UUID paramUUID, ActionRequest paramActionRequest) {
    return start((Class)ActionResponse.class, (Class)Errors.class)
      .uri("/api/user/action")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramActionRequest, objectMapper()))
      .delete()
      .go();
  }
  
  public ClientResponse<ChangePasswordResponse, Errors> changePassword(String paramString, ChangePasswordRequest paramChangePasswordRequest) {
    return startAnonymous((Class)ChangePasswordResponse.class, (Class)Errors.class)
      .uri("/api/user/change-password")
      .urlSegment(paramString)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramChangePasswordRequest, objectMapper()))
      .post()
      .go();
  }
  
  @Deprecated
  public ClientResponse<ChangePasswordResponse, Errors> changePasswordByJWT(String paramString, ChangePasswordRequest paramChangePasswordRequest) {
    return startAnonymous((Class)ChangePasswordResponse.class, (Class)Errors.class)
      .uri("/api/user/change-password")
      .authorization("Bearer " + paramString)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramChangePasswordRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<Void, Errors> changePasswordByIdentity(ChangePasswordRequest paramChangePasswordRequest) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/user/change-password")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramChangePasswordRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<ChangePasswordResponse, Errors> changePasswordUsingJWT(String paramString, ChangePasswordRequest paramChangePasswordRequest) {
    return startAnonymous((Class)ChangePasswordResponse.class, (Class)Errors.class)
      .uri("/api/user/change-password")
      .authorization("Bearer " + paramString)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramChangePasswordRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<Void, Errors> checkChangePasswordUsingId(String paramString) {
    return startAnonymous((Class)void.class, (Class)Errors.class)
      .uri("/api/user/change-password")
      .urlSegment(paramString)
      .get()
      .go();
  }
  
  public ClientResponse<Void, Errors> checkChangePasswordUsingIdAndIPAddress(String paramString1, String paramString2) {
    return startAnonymous((Class)void.class, (Class)Errors.class)
      .uri("/api/user/change-password")
      .urlSegment(paramString1)
      .urlParameter("ipAddress", paramString2)
      .get()
      .go();
  }
  
  public ClientResponse<Void, Errors> checkChangePasswordUsingJWT(String paramString) {
    return startAnonymous((Class)void.class, (Class)Errors.class)
      .uri("/api/user/change-password")
      .authorization("Bearer " + paramString)
      .get()
      .go();
  }
  
  public ClientResponse<Void, Errors> checkChangePasswordUsingJWTAndIPAddress(String paramString1, String paramString2) {
    return startAnonymous((Class)void.class, (Class)Errors.class)
      .uri("/api/user/change-password")
      .authorization("Bearer " + paramString1)
      .urlParameter("ipAddress", paramString2)
      .get()
      .go();
  }
  
  public ClientResponse<Void, Errors> checkChangePasswordUsingLoginId(String paramString) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/user/change-password")
      .urlParameter("loginId", paramString)
      .get()
      .go();
  }
  
  public ClientResponse<Void, Errors> checkChangePasswordUsingLoginIdAndIPAddress(String paramString1, String paramString2) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/user/change-password")
      .urlParameter("loginId", paramString1)
      .urlParameter("ipAddress", paramString2)
      .get()
      .go();
  }
  
  public ClientResponse<Void, Errors> checkChangePasswordUsingLoginIdAndLoginIdTypes(String paramString, List<String> paramList) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/user/change-password")
      .urlParameter("loginId", paramString)
      .urlParameter("loginIdTypes", paramList)
      .get()
      .go();
  }
  
  public ClientResponse<Void, Errors> checkChangePasswordUsingLoginIdAndLoginIdTypesAndIPAddress(String paramString1, List<String> paramList, String paramString2) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/user/change-password")
      .urlParameter("loginId", paramString1)
      .urlParameter("loginIdTypes", paramList)
      .urlParameter("ipAddress", paramString2)
      .get()
      .go();
  }
  
  public ClientResponse<AccessToken, OAuthError> clientCredentialsGrant(String paramString1, String paramString2, String paramString3) {
    HashMap<Object, Object> hashMap = new HashMap<>();
    hashMap.put("client_id", Arrays.asList(new String[] { paramString1 }));
    hashMap.put("client_secret", Arrays.asList(new String[] { paramString2 }));
    hashMap.put("grant_type", Arrays.asList(new String[] { "client_credentials" }));
    hashMap.put("scope", Arrays.asList(new String[] { paramString3 }));
    return startAnonymous((Class)AccessToken.class, (Class)OAuthError.class)
      .uri("/oauth2/token")
      .bodyHandler((RESTClient.BodyHandler)new FormDataBodyHandler(hashMap))
      .post()
      .go();
  }
  
  public ClientResponse<AccessToken, OAuthError> clientCredentialsGrantWithRequest(ClientCredentialsGrantRequest paramClientCredentialsGrantRequest) {
    HashMap<Object, Object> hashMap = new HashMap<>();
    hashMap.put("client_id", Arrays.asList(new String[] { paramClientCredentialsGrantRequest.client_id }));
    hashMap.put("client_secret", Arrays.asList(new String[] { paramClientCredentialsGrantRequest.client_secret }));
    hashMap.put("grant_type", Arrays.asList(new String[] { paramClientCredentialsGrantRequest.grant_type }));
    hashMap.put("scope", Arrays.asList(new String[] { paramClientCredentialsGrantRequest.scope }));
    hashMap.put("tenantId", Arrays.asList(new String[] { paramClientCredentialsGrantRequest.tenantId }));
    return startAnonymous((Class)AccessToken.class, (Class)OAuthError.class)
      .uri("/oauth2/token")
      .bodyHandler((RESTClient.BodyHandler)new FormDataBodyHandler(hashMap))
      .post()
      .go();
  }
  
  public ClientResponse<UserCommentResponse, Errors> commentOnUser(UserCommentRequest paramUserCommentRequest) {
    return start((Class)UserCommentResponse.class, (Class)Errors.class)
      .uri("/api/user/comment")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramUserCommentRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<VerifyCompleteResponse, Errors> completeVerifyIdentity(VerifyCompleteRequest paramVerifyCompleteRequest) {
    return start((Class)VerifyCompleteResponse.class, (Class)Errors.class)
      .uri("/api/identity/verify/complete")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramVerifyCompleteRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<WebAuthnAssertResponse, Errors> completeWebAuthnAssertion(WebAuthnLoginRequest paramWebAuthnLoginRequest) {
    return startAnonymous((Class)WebAuthnAssertResponse.class, (Class)Errors.class)
      .uri("/api/webauthn/assert")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramWebAuthnLoginRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<LoginResponse, Errors> completeWebAuthnLogin(WebAuthnLoginRequest paramWebAuthnLoginRequest) {
    return startAnonymous((Class)LoginResponse.class, (Class)Errors.class)
      .uri("/api/webauthn/login")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramWebAuthnLoginRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<WebAuthnRegisterCompleteResponse, Errors> completeWebAuthnRegistration(WebAuthnRegisterCompleteRequest paramWebAuthnRegisterCompleteRequest) {
    return start((Class)WebAuthnRegisterCompleteResponse.class, (Class)Errors.class)
      .uri("/api/webauthn/register/complete")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramWebAuthnRegisterCompleteRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<APIKeyResponse, Errors> createAPIKey(UUID paramUUID, APIKeyRequest paramAPIKeyRequest) {
    return start((Class)APIKeyResponse.class, (Class)Errors.class)
      .uri("/api/api-key")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramAPIKeyRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<ApplicationResponse, Errors> createApplication(UUID paramUUID, ApplicationRequest paramApplicationRequest) {
    return start((Class)ApplicationResponse.class, (Class)Errors.class)
      .uri("/api/application")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramApplicationRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<ApplicationResponse, Errors> createApplicationRole(UUID paramUUID1, UUID paramUUID2, ApplicationRequest paramApplicationRequest) {
    return start((Class)ApplicationResponse.class, (Class)Errors.class)
      .uri("/api/application")
      .urlSegment(paramUUID1)
      .urlSegment("role")
      .urlSegment(paramUUID2)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramApplicationRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<AuditLogResponse, Errors> createAuditLog(AuditLogRequest paramAuditLogRequest) {
    return start((Class)AuditLogResponse.class, (Class)Errors.class)
      .uri("/api/system/audit-log")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramAuditLogRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<ConnectorResponse, Errors> createConnector(UUID paramUUID, ConnectorRequest paramConnectorRequest) {
    return start((Class)ConnectorResponse.class, (Class)Errors.class)
      .uri("/api/connector")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramConnectorRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<ConsentResponse, Errors> createConsent(UUID paramUUID, ConsentRequest paramConsentRequest) {
    return start((Class)ConsentResponse.class, (Class)Errors.class)
      .uri("/api/consent")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramConsentRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<EmailTemplateResponse, Errors> createEmailTemplate(UUID paramUUID, EmailTemplateRequest paramEmailTemplateRequest) {
    return start((Class)EmailTemplateResponse.class, (Class)Errors.class)
      .uri("/api/email/template")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramEmailTemplateRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<EntityResponse, Errors> createEntity(UUID paramUUID, EntityRequest paramEntityRequest) {
    return start((Class)EntityResponse.class, (Class)Errors.class)
      .uri("/api/entity")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramEntityRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<EntityTypeResponse, Errors> createEntityType(UUID paramUUID, EntityTypeRequest paramEntityTypeRequest) {
    return start((Class)EntityTypeResponse.class, (Class)Errors.class)
      .uri("/api/entity/type")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramEntityTypeRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<EntityTypeResponse, Errors> createEntityTypePermission(UUID paramUUID1, UUID paramUUID2, EntityTypeRequest paramEntityTypeRequest) {
    return start((Class)EntityTypeResponse.class, (Class)Errors.class)
      .uri("/api/entity/type")
      .urlSegment(paramUUID1)
      .urlSegment("permission")
      .urlSegment(paramUUID2)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramEntityTypeRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<FamilyResponse, Errors> createFamily(UUID paramUUID, FamilyRequest paramFamilyRequest) {
    return start((Class)FamilyResponse.class, (Class)Errors.class)
      .uri("/api/user/family")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramFamilyRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<FormResponse, Errors> createForm(UUID paramUUID, FormRequest paramFormRequest) {
    return start((Class)FormResponse.class, (Class)Errors.class)
      .uri("/api/form")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramFormRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<FormFieldResponse, Errors> createFormField(UUID paramUUID, FormFieldRequest paramFormFieldRequest) {
    return start((Class)FormFieldResponse.class, (Class)Errors.class)
      .uri("/api/form/field")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramFormFieldRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<GroupResponse, Errors> createGroup(UUID paramUUID, GroupRequest paramGroupRequest) {
    return start((Class)GroupResponse.class, (Class)Errors.class)
      .uri("/api/group")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramGroupRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<MemberResponse, Errors> createGroupMembers(MemberRequest paramMemberRequest) {
    return start((Class)MemberResponse.class, (Class)Errors.class)
      .uri("/api/group/member")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramMemberRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<IPAccessControlListResponse, Errors> createIPAccessControlList(UUID paramUUID, IPAccessControlListRequest paramIPAccessControlListRequest) {
    return start((Class)IPAccessControlListResponse.class, (Class)Errors.class)
      .uri("/api/ip-acl")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramIPAccessControlListRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<IdentityProviderResponse, Errors> createIdentityProvider(UUID paramUUID, IdentityProviderRequest paramIdentityProviderRequest) {
    return start((Class)IdentityProviderResponse.class, (Class)Errors.class)
      .uri("/api/identity-provider")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramIdentityProviderRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<LambdaResponse, Errors> createLambda(UUID paramUUID, LambdaRequest paramLambdaRequest) {
    return start((Class)LambdaResponse.class, (Class)Errors.class)
      .uri("/api/lambda")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramLambdaRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<MessageTemplateResponse, Errors> createMessageTemplate(UUID paramUUID, MessageTemplateRequest paramMessageTemplateRequest) {
    return start((Class)MessageTemplateResponse.class, (Class)Errors.class)
      .uri("/api/message/template")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramMessageTemplateRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<MessengerResponse, Errors> createMessenger(UUID paramUUID, MessengerRequest paramMessengerRequest) {
    return start((Class)MessengerResponse.class, (Class)Errors.class)
      .uri("/api/messenger")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramMessengerRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<ApplicationOAuthScopeResponse, Errors> createOAuthScope(UUID paramUUID1, UUID paramUUID2, ApplicationOAuthScopeRequest paramApplicationOAuthScopeRequest) {
    return start((Class)ApplicationOAuthScopeResponse.class, (Class)Errors.class)
      .uri("/api/application")
      .urlSegment(paramUUID1)
      .urlSegment("scope")
      .urlSegment(paramUUID2)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramApplicationOAuthScopeRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<TenantResponse, Errors> createTenant(UUID paramUUID, TenantRequest paramTenantRequest) {
    return start((Class)TenantResponse.class, (Class)Errors.class)
      .uri("/api/tenant")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramTenantRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<TenantManagerIdentityProviderTypeConfigurationResponse, Errors> createTenantManagerIdentityProviderTypeConfiguration(IdentityProviderType paramIdentityProviderType, TenantManagerIdentityProviderTypeConfigurationRequest paramTenantManagerIdentityProviderTypeConfigurationRequest) {
    return start((Class)TenantManagerIdentityProviderTypeConfigurationResponse.class, (Class)Errors.class)
      .uri("/api/tenant-manager/identity-provider")
      .urlSegment(paramIdentityProviderType)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramTenantManagerIdentityProviderTypeConfigurationRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<ThemeResponse, Errors> createTheme(UUID paramUUID, ThemeRequest paramThemeRequest) {
    return start((Class)ThemeResponse.class, (Class)Errors.class)
      .uri("/api/theme")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramThemeRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<UserResponse, Errors> createUser(UUID paramUUID, UserRequest paramUserRequest) {
    return start((Class)UserResponse.class, (Class)Errors.class)
      .uri("/api/user")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramUserRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<UserActionResponse, Errors> createUserAction(UUID paramUUID, UserActionRequest paramUserActionRequest) {
    return start((Class)UserActionResponse.class, (Class)Errors.class)
      .uri("/api/user-action")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramUserActionRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<UserActionReasonResponse, Errors> createUserActionReason(UUID paramUUID, UserActionReasonRequest paramUserActionReasonRequest) {
    return start((Class)UserActionReasonResponse.class, (Class)Errors.class)
      .uri("/api/user-action-reason")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramUserActionReasonRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<UserConsentResponse, Errors> createUserConsent(UUID paramUUID, UserConsentRequest paramUserConsentRequest) {
    return start((Class)UserConsentResponse.class, (Class)Errors.class)
      .uri("/api/user/consent")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramUserConsentRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<IdentityProviderLinkResponse, Errors> createUserLink(IdentityProviderLinkRequest paramIdentityProviderLinkRequest) {
    return start((Class)IdentityProviderLinkResponse.class, (Class)Errors.class)
      .uri("/api/identity-provider/link")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramIdentityProviderLinkRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<WebhookResponse, Errors> createWebhook(UUID paramUUID, WebhookRequest paramWebhookRequest) {
    return start((Class)WebhookResponse.class, (Class)Errors.class)
      .uri("/api/webhook")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramWebhookRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<Void, Errors> deactivateApplication(UUID paramUUID) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/application")
      .urlSegment(paramUUID)
      .delete()
      .go();
  }
  
  public ClientResponse<Void, Void> deactivateReactor() {
    return start((Class)void.class, (Class)void.class)
      .uri("/api/reactor")
      .delete()
      .go();
  }
  
  public ClientResponse<Void, Errors> deactivateUser(UUID paramUUID) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/user")
      .urlSegment(paramUUID)
      .delete()
      .go();
  }
  
  public ClientResponse<Void, Errors> deactivateUserAction(UUID paramUUID) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/user-action")
      .urlSegment(paramUUID)
      .delete()
      .go();
  }
  
  @Deprecated
  public ClientResponse<UserDeleteResponse, Errors> deactivateUsers(Collection<UUID> paramCollection) {
    return start((Class)UserDeleteResponse.class, (Class)Errors.class)
      .uri("/api/user/bulk")
      .urlParameter("userId", paramCollection)
      .urlParameter("dryRun", Boolean.valueOf(false))
      .urlParameter("hardDelete", Boolean.valueOf(false))
      .delete()
      .go();
  }
  
  public ClientResponse<UserDeleteResponse, Errors> deactivateUsersByIds(Collection<UUID> paramCollection) {
    return start((Class)UserDeleteResponse.class, (Class)Errors.class)
      .uri("/api/user/bulk")
      .urlParameter("userId", paramCollection)
      .urlParameter("dryRun", Boolean.valueOf(false))
      .urlParameter("hardDelete", Boolean.valueOf(false))
      .delete()
      .go();
  }
  
  public ClientResponse<Void, Errors> deleteAPIKey(UUID paramUUID) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/api-key")
      .urlSegment(paramUUID)
      .delete()
      .go();
  }
  
  public ClientResponse<Void, Errors> deleteApplication(UUID paramUUID) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/application")
      .urlSegment(paramUUID)
      .urlParameter("hardDelete", Boolean.valueOf(true))
      .delete()
      .go();
  }
  
  public ClientResponse<Void, Errors> deleteApplicationRole(UUID paramUUID1, UUID paramUUID2) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/application")
      .urlSegment(paramUUID1)
      .urlSegment("role")
      .urlSegment(paramUUID2)
      .delete()
      .go();
  }
  
  public ClientResponse<Void, Errors> deleteConnector(UUID paramUUID) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/connector")
      .urlSegment(paramUUID)
      .delete()
      .go();
  }
  
  public ClientResponse<Void, Errors> deleteConsent(UUID paramUUID) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/consent")
      .urlSegment(paramUUID)
      .delete()
      .go();
  }
  
  public ClientResponse<Void, Errors> deleteEmailTemplate(UUID paramUUID) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/email/template")
      .urlSegment(paramUUID)
      .delete()
      .go();
  }
  
  public ClientResponse<Void, Errors> deleteEntity(UUID paramUUID) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/entity")
      .urlSegment(paramUUID)
      .delete()
      .go();
  }
  
  public ClientResponse<Void, Errors> deleteEntityGrant(UUID paramUUID1, UUID paramUUID2, UUID paramUUID3) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/entity")
      .urlSegment(paramUUID1)
      .urlSegment("grant")
      .urlParameter("recipientEntityId", paramUUID2)
      .urlParameter("userId", paramUUID3)
      .delete()
      .go();
  }
  
  public ClientResponse<Void, Errors> deleteEntityType(UUID paramUUID) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/entity/type")
      .urlSegment(paramUUID)
      .delete()
      .go();
  }
  
  public ClientResponse<Void, Errors> deleteEntityTypePermission(UUID paramUUID1, UUID paramUUID2) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/entity/type")
      .urlSegment(paramUUID1)
      .urlSegment("permission")
      .urlSegment(paramUUID2)
      .delete()
      .go();
  }
  
  public ClientResponse<Void, Errors> deleteForm(UUID paramUUID) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/form")
      .urlSegment(paramUUID)
      .delete()
      .go();
  }
  
  public ClientResponse<Void, Errors> deleteFormField(UUID paramUUID) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/form/field")
      .urlSegment(paramUUID)
      .delete()
      .go();
  }
  
  public ClientResponse<Void, Errors> deleteGroup(UUID paramUUID) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/group")
      .urlSegment(paramUUID)
      .delete()
      .go();
  }
  
  public ClientResponse<Void, Errors> deleteGroupMembers(MemberDeleteRequest paramMemberDeleteRequest) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/group/member")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramMemberDeleteRequest, objectMapper()))
      .delete()
      .go();
  }
  
  public ClientResponse<Void, Errors> deleteIPAccessControlList(UUID paramUUID) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/ip-acl")
      .urlSegment(paramUUID)
      .delete()
      .go();
  }
  
  public ClientResponse<Void, Errors> deleteIdentityProvider(UUID paramUUID) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/identity-provider")
      .urlSegment(paramUUID)
      .delete()
      .go();
  }
  
  public ClientResponse<Void, Errors> deleteKey(UUID paramUUID) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/key")
      .urlSegment(paramUUID)
      .delete()
      .go();
  }
  
  public ClientResponse<Void, Errors> deleteLambda(UUID paramUUID) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/lambda")
      .urlSegment(paramUUID)
      .delete()
      .go();
  }
  
  public ClientResponse<Void, Errors> deleteMessageTemplate(UUID paramUUID) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/message/template")
      .urlSegment(paramUUID)
      .delete()
      .go();
  }
  
  public ClientResponse<Void, Errors> deleteMessenger(UUID paramUUID) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/messenger")
      .urlSegment(paramUUID)
      .delete()
      .go();
  }
  
  public ClientResponse<Void, Errors> deleteOAuthScope(UUID paramUUID1, UUID paramUUID2) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/application")
      .urlSegment(paramUUID1)
      .urlSegment("scope")
      .urlSegment(paramUUID2)
      .delete()
      .go();
  }
  
  public ClientResponse<Void, Errors> deleteRegistration(UUID paramUUID1, UUID paramUUID2) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/user/registration")
      .urlSegment(paramUUID1)
      .urlSegment(paramUUID2)
      .delete()
      .go();
  }
  
  public ClientResponse<Void, Errors> deleteRegistrationWithRequest(UUID paramUUID1, UUID paramUUID2, RegistrationDeleteRequest paramRegistrationDeleteRequest) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/user/registration")
      .urlSegment(paramUUID1)
      .urlSegment(paramUUID2)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramRegistrationDeleteRequest, objectMapper()))
      .delete()
      .go();
  }
  
  public ClientResponse<Void, Errors> deleteTenant(UUID paramUUID) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/tenant")
      .urlSegment(paramUUID)
      .delete()
      .go();
  }
  
  public ClientResponse<Void, Errors> deleteTenantAsync(UUID paramUUID) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/tenant")
      .urlSegment(paramUUID)
      .urlParameter("async", Boolean.valueOf(true))
      .delete()
      .go();
  }
  
  public ClientResponse<Void, Errors> deleteTenantManagerIdentityProviderTypeConfiguration(IdentityProviderType paramIdentityProviderType) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/tenant-manager/identity-provider")
      .urlSegment(paramIdentityProviderType)
      .delete()
      .go();
  }
  
  public ClientResponse<Void, Errors> deleteTenantWithRequest(UUID paramUUID, TenantDeleteRequest paramTenantDeleteRequest) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/tenant")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramTenantDeleteRequest, objectMapper()))
      .delete()
      .go();
  }
  
  public ClientResponse<Void, Errors> deleteTheme(UUID paramUUID) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/theme")
      .urlSegment(paramUUID)
      .delete()
      .go();
  }
  
  public ClientResponse<Void, Errors> deleteUser(UUID paramUUID) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/user")
      .urlSegment(paramUUID)
      .urlParameter("hardDelete", Boolean.valueOf(true))
      .delete()
      .go();
  }
  
  public ClientResponse<Void, Errors> deleteUserAction(UUID paramUUID) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/user-action")
      .urlSegment(paramUUID)
      .urlParameter("hardDelete", Boolean.valueOf(true))
      .delete()
      .go();
  }
  
  public ClientResponse<Void, Errors> deleteUserActionReason(UUID paramUUID) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/user-action-reason")
      .urlSegment(paramUUID)
      .delete()
      .go();
  }
  
  public ClientResponse<IdentityProviderLinkResponse, Errors> deleteUserLink(UUID paramUUID1, String paramString, UUID paramUUID2) {
    return start((Class)IdentityProviderLinkResponse.class, (Class)Errors.class)
      .uri("/api/identity-provider/link")
      .urlParameter("identityProviderId", paramUUID1)
      .urlParameter("identityProviderUserId", paramString)
      .urlParameter("userId", paramUUID2)
      .delete()
      .go();
  }
  
  public ClientResponse<Void, Errors> deleteUserWithRequest(UUID paramUUID, UserDeleteSingleRequest paramUserDeleteSingleRequest) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/user")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramUserDeleteSingleRequest, objectMapper()))
      .delete()
      .go();
  }
  
  @Deprecated
  public ClientResponse<UserDeleteResponse, Errors> deleteUsers(UserDeleteRequest paramUserDeleteRequest) {
    return start((Class)UserDeleteResponse.class, (Class)Errors.class)
      .uri("/api/user/bulk")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramUserDeleteRequest, objectMapper()))
      .delete()
      .go();
  }
  
  public ClientResponse<UserDeleteResponse, Errors> deleteUsersByQuery(UserDeleteRequest paramUserDeleteRequest) {
    return start((Class)UserDeleteResponse.class, (Class)Errors.class)
      .uri("/api/user/bulk")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramUserDeleteRequest, objectMapper()))
      .delete()
      .go();
  }
  
  public ClientResponse<Void, Errors> deleteWebAuthnCredential(UUID paramUUID) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/webauthn")
      .urlSegment(paramUUID)
      .delete()
      .go();
  }
  
  public ClientResponse<Void, Errors> deleteWebAuthnCredentialsForUser(UUID paramUUID) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/webauthn")
      .urlParameter("userId", paramUUID)
      .delete()
      .go();
  }
  
  public ClientResponse<Void, Errors> deleteWebhook(UUID paramUUID) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/webhook")
      .urlSegment(paramUUID)
      .delete()
      .go();
  }
  
  public ClientResponse<DeviceResponse, OAuthError> deviceAuthorize(String paramString1, String paramString2, String paramString3) {
    HashMap<Object, Object> hashMap = new HashMap<>();
    hashMap.put("client_id", Arrays.asList(new String[] { paramString1 }));
    hashMap.put("client_secret", Arrays.asList(new String[] { paramString2 }));
    hashMap.put("scope", Arrays.asList(new String[] { paramString3 }));
    return startAnonymous((Class)DeviceResponse.class, (Class)OAuthError.class)
      .uri("/oauth2/device_authorize")
      .bodyHandler((RESTClient.BodyHandler)new FormDataBodyHandler(hashMap))
      .post()
      .go();
  }
  
  public ClientResponse<DeviceResponse, OAuthError> deviceAuthorizeWithRequest(DeviceAuthorizationRequest paramDeviceAuthorizationRequest) {
    HashMap<Object, Object> hashMap = new HashMap<>();
    hashMap.put("client_id", Arrays.asList(new String[] { paramDeviceAuthorizationRequest.client_id }));
    hashMap.put("client_secret", Arrays.asList(new String[] { paramDeviceAuthorizationRequest.client_secret }));
    hashMap.put("scope", Arrays.asList(new String[] { paramDeviceAuthorizationRequest.scope }));
    if (paramDeviceAuthorizationRequest.tenantId != null)
      hashMap.put("tenantId", Arrays.asList(new String[] { paramDeviceAuthorizationRequest.tenantId.toString() })); 
    return startAnonymous((Class)DeviceResponse.class, (Class)OAuthError.class)
      .uri("/oauth2/device_authorize")
      .bodyHandler((RESTClient.BodyHandler)new FormDataBodyHandler(hashMap))
      .post()
      .go();
  }
  
  public ClientResponse<Void, Errors> disableTwoFactor(UUID paramUUID, String paramString1, String paramString2) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/user/two-factor")
      .urlSegment(paramUUID)
      .urlParameter("methodId", paramString1)
      .urlParameter("code", paramString2)
      .delete()
      .go();
  }
  
  public ClientResponse<Void, Errors> disableTwoFactorWithRequest(UUID paramUUID, TwoFactorDisableRequest paramTwoFactorDisableRequest) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/user/two-factor")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramTwoFactorDisableRequest, objectMapper()))
      .delete()
      .go();
  }
  
  public ClientResponse<TwoFactorResponse, Errors> enableTwoFactor(UUID paramUUID, TwoFactorRequest paramTwoFactorRequest) {
    return start((Class)TwoFactorResponse.class, (Class)Errors.class)
      .uri("/api/user/two-factor")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramTwoFactorRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<AccessToken, OAuthError> exchangeOAuthCodeForAccessToken(String paramString1, String paramString2, String paramString3, String paramString4) {
    HashMap<Object, Object> hashMap = new HashMap<>();
    hashMap.put("code", Arrays.asList(new String[] { paramString1 }));
    hashMap.put("client_id", Arrays.asList(new String[] { paramString2 }));
    hashMap.put("client_secret", Arrays.asList(new String[] { paramString3 }));
    hashMap.put("grant_type", Arrays.asList(new String[] { "authorization_code" }));
    hashMap.put("redirect_uri", Arrays.asList(new String[] { paramString4 }));
    return startAnonymous((Class)AccessToken.class, (Class)OAuthError.class)
      .uri("/oauth2/token")
      .bodyHandler((RESTClient.BodyHandler)new FormDataBodyHandler(hashMap))
      .post()
      .go();
  }
  
  public ClientResponse<AccessToken, OAuthError> exchangeOAuthCodeForAccessTokenUsingPKCE(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5) {
    HashMap<Object, Object> hashMap = new HashMap<>();
    hashMap.put("code", Arrays.asList(new String[] { paramString1 }));
    hashMap.put("client_id", Arrays.asList(new String[] { paramString2 }));
    hashMap.put("client_secret", Arrays.asList(new String[] { paramString3 }));
    hashMap.put("grant_type", Arrays.asList(new String[] { "authorization_code" }));
    hashMap.put("redirect_uri", Arrays.asList(new String[] { paramString4 }));
    hashMap.put("code_verifier", Arrays.asList(new String[] { paramString5 }));
    return startAnonymous((Class)AccessToken.class, (Class)OAuthError.class)
      .uri("/oauth2/token")
      .bodyHandler((RESTClient.BodyHandler)new FormDataBodyHandler(hashMap))
      .post()
      .go();
  }
  
  public ClientResponse<AccessToken, OAuthError> exchangeOAuthCodeForAccessTokenUsingPKCEWithRequest(OAuthCodePKCEAccessTokenRequest paramOAuthCodePKCEAccessTokenRequest) {
    HashMap<Object, Object> hashMap = new HashMap<>();
    hashMap.put("client_id", Arrays.asList(new String[] { paramOAuthCodePKCEAccessTokenRequest.client_id }));
    hashMap.put("client_secret", Arrays.asList(new String[] { paramOAuthCodePKCEAccessTokenRequest.client_secret }));
    hashMap.put("code", Arrays.asList(new String[] { paramOAuthCodePKCEAccessTokenRequest.code }));
    hashMap.put("code_verifier", Arrays.asList(new String[] { paramOAuthCodePKCEAccessTokenRequest.code_verifier }));
    hashMap.put("grant_type", Arrays.asList(new String[] { paramOAuthCodePKCEAccessTokenRequest.grant_type }));
    hashMap.put("redirect_uri", Arrays.asList(new String[] { paramOAuthCodePKCEAccessTokenRequest.redirect_uri }));
    if (paramOAuthCodePKCEAccessTokenRequest.tenantId != null)
      hashMap.put("tenantId", Arrays.asList(new String[] { paramOAuthCodePKCEAccessTokenRequest.tenantId.toString() })); 
    return startAnonymous((Class)AccessToken.class, (Class)OAuthError.class)
      .uri("/oauth2/token")
      .bodyHandler((RESTClient.BodyHandler)new FormDataBodyHandler(hashMap))
      .post()
      .go();
  }
  
  public ClientResponse<AccessToken, OAuthError> exchangeOAuthCodeForAccessTokenWithRequest(OAuthCodeAccessTokenRequest paramOAuthCodeAccessTokenRequest) {
    HashMap<Object, Object> hashMap = new HashMap<>();
    hashMap.put("client_id", Arrays.asList(new String[] { paramOAuthCodeAccessTokenRequest.client_id }));
    hashMap.put("client_secret", Arrays.asList(new String[] { paramOAuthCodeAccessTokenRequest.client_secret }));
    hashMap.put("code", Arrays.asList(new String[] { paramOAuthCodeAccessTokenRequest.code }));
    hashMap.put("grant_type", Arrays.asList(new String[] { paramOAuthCodeAccessTokenRequest.grant_type }));
    hashMap.put("redirect_uri", Arrays.asList(new String[] { paramOAuthCodeAccessTokenRequest.redirect_uri }));
    hashMap.put("tenantId", Arrays.asList(new String[] { paramOAuthCodeAccessTokenRequest.tenantId }));
    return startAnonymous((Class)AccessToken.class, (Class)OAuthError.class)
      .uri("/oauth2/token")
      .bodyHandler((RESTClient.BodyHandler)new FormDataBodyHandler(hashMap))
      .post()
      .go();
  }
  
  public ClientResponse<AccessToken, OAuthError> exchangeRefreshTokenForAccessToken(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5) {
    HashMap<Object, Object> hashMap = new HashMap<>();
    hashMap.put("refresh_token", Arrays.asList(new String[] { paramString1 }));
    hashMap.put("client_id", Arrays.asList(new String[] { paramString2 }));
    hashMap.put("client_secret", Arrays.asList(new String[] { paramString3 }));
    hashMap.put("grant_type", Arrays.asList(new String[] { "refresh_token" }));
    hashMap.put("scope", Arrays.asList(new String[] { paramString4 }));
    hashMap.put("user_code", Arrays.asList(new String[] { paramString5 }));
    return startAnonymous((Class)AccessToken.class, (Class)OAuthError.class)
      .uri("/oauth2/token")
      .bodyHandler((RESTClient.BodyHandler)new FormDataBodyHandler(hashMap))
      .post()
      .go();
  }
  
  public ClientResponse<AccessToken, OAuthError> exchangeRefreshTokenForAccessTokenWithRequest(RefreshTokenAccessTokenRequest paramRefreshTokenAccessTokenRequest) {
    HashMap<Object, Object> hashMap = new HashMap<>();
    hashMap.put("client_id", Arrays.asList(new String[] { paramRefreshTokenAccessTokenRequest.client_id }));
    hashMap.put("client_secret", Arrays.asList(new String[] { paramRefreshTokenAccessTokenRequest.client_secret }));
    hashMap.put("grant_type", Arrays.asList(new String[] { paramRefreshTokenAccessTokenRequest.grant_type }));
    hashMap.put("refresh_token", Arrays.asList(new String[] { paramRefreshTokenAccessTokenRequest.refresh_token }));
    hashMap.put("scope", Arrays.asList(new String[] { paramRefreshTokenAccessTokenRequest.scope }));
    if (paramRefreshTokenAccessTokenRequest.tenantId != null)
      hashMap.put("tenantId", Arrays.asList(new String[] { paramRefreshTokenAccessTokenRequest.tenantId.toString() })); 
    hashMap.put("user_code", Arrays.asList(new String[] { paramRefreshTokenAccessTokenRequest.user_code }));
    return startAnonymous((Class)AccessToken.class, (Class)OAuthError.class)
      .uri("/oauth2/token")
      .bodyHandler((RESTClient.BodyHandler)new FormDataBodyHandler(hashMap))
      .post()
      .go();
  }
  
  public ClientResponse<JWTRefreshResponse, Errors> exchangeRefreshTokenForJWT(RefreshRequest paramRefreshRequest) {
    return startAnonymous((Class)JWTRefreshResponse.class, (Class)Errors.class)
      .uri("/api/jwt/refresh")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramRefreshRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<AccessToken, OAuthError> exchangeUserCredentialsForAccessToken(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6) {
    HashMap<Object, Object> hashMap = new HashMap<>();
    hashMap.put("username", Arrays.asList(new String[] { paramString1 }));
    hashMap.put("password", Arrays.asList(new String[] { paramString2 }));
    hashMap.put("client_id", Arrays.asList(new String[] { paramString3 }));
    hashMap.put("client_secret", Arrays.asList(new String[] { paramString4 }));
    hashMap.put("grant_type", Arrays.asList(new String[] { "password" }));
    hashMap.put("scope", Arrays.asList(new String[] { paramString5 }));
    hashMap.put("user_code", Arrays.asList(new String[] { paramString6 }));
    return startAnonymous((Class)AccessToken.class, (Class)OAuthError.class)
      .uri("/oauth2/token")
      .bodyHandler((RESTClient.BodyHandler)new FormDataBodyHandler(hashMap))
      .post()
      .go();
  }
  
  public ClientResponse<AccessToken, OAuthError> exchangeUserCredentialsForAccessTokenWithRequest(UserCredentialsAccessTokenRequest paramUserCredentialsAccessTokenRequest) {
    HashMap<Object, Object> hashMap = new HashMap<>();
    hashMap.put("client_id", Arrays.asList(new String[] { paramUserCredentialsAccessTokenRequest.client_id }));
    hashMap.put("client_secret", Arrays.asList(new String[] { paramUserCredentialsAccessTokenRequest.client_secret }));
    hashMap.put("grant_type", Arrays.asList(new String[] { paramUserCredentialsAccessTokenRequest.grant_type }));
    hashMap.put("password", Arrays.asList(new String[] { paramUserCredentialsAccessTokenRequest.password }));
    hashMap.put("scope", Arrays.asList(new String[] { paramUserCredentialsAccessTokenRequest.scope }));
    hashMap.put("tenantId", Arrays.asList(new String[] { paramUserCredentialsAccessTokenRequest.tenantId }));
    hashMap.put("user_code", Arrays.asList(new String[] { paramUserCredentialsAccessTokenRequest.user_code }));
    hashMap.put("username", Arrays.asList(new String[] { paramUserCredentialsAccessTokenRequest.username }));
    return startAnonymous((Class)AccessToken.class, (Class)OAuthError.class)
      .uri("/oauth2/token")
      .bodyHandler((RESTClient.BodyHandler)new FormDataBodyHandler(hashMap))
      .post()
      .go();
  }
  
  public ClientResponse<ForgotPasswordResponse, Errors> forgotPassword(ForgotPasswordRequest paramForgotPasswordRequest) {
    return start((Class)ForgotPasswordResponse.class, (Class)Errors.class)
      .uri("/api/user/forgot-password")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramForgotPasswordRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<VerifyEmailResponse, Void> generateEmailVerificationId(String paramString) {
    return start((Class)VerifyEmailResponse.class, (Class)void.class)
      .uri("/api/user/verify-email")
      .urlParameter("email", paramString)
      .urlParameter("sendVerifyEmail", Boolean.valueOf(false))
      .put()
      .go();
  }
  
  public ClientResponse<KeyResponse, Errors> generateKey(UUID paramUUID, KeyRequest paramKeyRequest) {
    return start((Class)KeyResponse.class, (Class)Errors.class)
      .uri("/api/key/generate")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramKeyRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<VerifyRegistrationResponse, Void> generateRegistrationVerificationId(String paramString, UUID paramUUID) {
    return start((Class)VerifyRegistrationResponse.class, (Class)void.class)
      .uri("/api/user/verify-registration")
      .urlParameter("email", paramString)
      .urlParameter("sendVerifyPasswordEmail", Boolean.valueOf(false))
      .urlParameter("applicationId", paramUUID)
      .put()
      .go();
  }
  
  public ClientResponse<TwoFactorRecoveryCodeResponse, Errors> generateTwoFactorRecoveryCodes(UUID paramUUID) {
    return start((Class)TwoFactorRecoveryCodeResponse.class, (Class)Errors.class)
      .uri("/api/user/two-factor/recovery-code")
      .urlSegment(paramUUID)
      .post()
      .go();
  }
  
  public ClientResponse<SecretResponse, Void> generateTwoFactorSecret() {
    return start((Class)SecretResponse.class, (Class)void.class)
      .uri("/api/two-factor/secret")
      .get()
      .go();
  }
  
  public ClientResponse<SecretResponse, Void> generateTwoFactorSecretUsingJWT(String paramString) {
    return startAnonymous((Class)SecretResponse.class, (Class)void.class)
      .uri("/api/two-factor/secret")
      .authorization("Bearer " + paramString)
      .get()
      .go();
  }
  
  public ClientResponse<LoginResponse, Errors> identityProviderLogin(IdentityProviderLoginRequest paramIdentityProviderLoginRequest) {
    return startAnonymous((Class)LoginResponse.class, (Class)Errors.class)
      .uri("/api/identity-provider/login")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramIdentityProviderLoginRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<KeyResponse, Errors> importKey(UUID paramUUID, KeyRequest paramKeyRequest) {
    return start((Class)KeyResponse.class, (Class)Errors.class)
      .uri("/api/key/import")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramKeyRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<Void, Errors> importRefreshTokens(RefreshTokenImportRequest paramRefreshTokenImportRequest) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/user/refresh-token/import")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramRefreshTokenImportRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<Void, Errors> importUsers(ImportRequest paramImportRequest) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/user/import")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramImportRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<Void, Errors> importWebAuthnCredential(WebAuthnCredentialImportRequest paramWebAuthnCredentialImportRequest) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/webauthn/import")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramWebAuthnCredentialImportRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<IntrospectResponse, OAuthError> introspectAccessToken(String paramString1, String paramString2) {
    HashMap<Object, Object> hashMap = new HashMap<>();
    hashMap.put("client_id", Arrays.asList(new String[] { paramString1 }));
    hashMap.put("token", Arrays.asList(new String[] { paramString2 }));
    return startAnonymous((Class)IntrospectResponse.class, (Class)OAuthError.class)
      .uri("/oauth2/introspect")
      .bodyHandler((RESTClient.BodyHandler)new FormDataBodyHandler(hashMap))
      .post()
      .go();
  }
  
  public ClientResponse<IntrospectResponse, OAuthError> introspectAccessTokenWithRequest(AccessTokenIntrospectRequest paramAccessTokenIntrospectRequest) {
    HashMap<Object, Object> hashMap = new HashMap<>();
    hashMap.put("client_id", Arrays.asList(new String[] { paramAccessTokenIntrospectRequest.client_id }));
    hashMap.put("tenantId", Arrays.asList(new String[] { paramAccessTokenIntrospectRequest.tenantId }));
    hashMap.put("token", Arrays.asList(new String[] { paramAccessTokenIntrospectRequest.token }));
    hashMap.put("token_type_hint", Arrays.asList(new String[] { paramAccessTokenIntrospectRequest.token_type_hint }));
    return startAnonymous((Class)IntrospectResponse.class, (Class)OAuthError.class)
      .uri("/oauth2/introspect")
      .bodyHandler((RESTClient.BodyHandler)new FormDataBodyHandler(hashMap))
      .post()
      .go();
  }
  
  public ClientResponse<IntrospectResponse, OAuthError> introspectClientCredentialsAccessToken(String paramString) {
    HashMap<Object, Object> hashMap = new HashMap<>();
    hashMap.put("token", Arrays.asList(new String[] { paramString }));
    return startAnonymous((Class)IntrospectResponse.class, (Class)OAuthError.class)
      .uri("/oauth2/introspect")
      .bodyHandler((RESTClient.BodyHandler)new FormDataBodyHandler(hashMap))
      .post()
      .go();
  }
  
  public ClientResponse<IntrospectResponse, OAuthError> introspectClientCredentialsAccessTokenWithRequest(ClientCredentialsAccessTokenIntrospectRequest paramClientCredentialsAccessTokenIntrospectRequest) {
    HashMap<Object, Object> hashMap = new HashMap<>();
    hashMap.put("tenantId", Arrays.asList(new String[] { paramClientCredentialsAccessTokenIntrospectRequest.tenantId }));
    hashMap.put("token", Arrays.asList(new String[] { paramClientCredentialsAccessTokenIntrospectRequest.token }));
    return startAnonymous((Class)IntrospectResponse.class, (Class)OAuthError.class)
      .uri("/oauth2/introspect")
      .bodyHandler((RESTClient.BodyHandler)new FormDataBodyHandler(hashMap))
      .post()
      .go();
  }
  
  public ClientResponse<IssueResponse, Errors> issueJWT(UUID paramUUID, String paramString1, String paramString2) {
    return startAnonymous((Class)IssueResponse.class, (Class)Errors.class)
      .uri("/api/jwt/issue")
      .authorization("Bearer " + paramString1)
      .urlParameter("applicationId", paramUUID)
      .urlParameter("refreshToken", paramString2)
      .get()
      .go();
  }
  
  public ClientResponse<LoginResponse, Errors> login(LoginRequest paramLoginRequest) {
    return start((Class)LoginResponse.class, (Class)Errors.class)
      .uri("/api/login")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramLoginRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<LoginResponse, Errors> loginPing(UUID paramUUID1, UUID paramUUID2, String paramString) {
    return start((Class)LoginResponse.class, (Class)Errors.class)
      .uri("/api/login")
      .urlSegment(paramUUID1)
      .urlSegment(paramUUID2)
      .urlParameter("ipAddress", paramString)
      .put()
      .go();
  }
  
  public ClientResponse<LoginResponse, Errors> loginPingWithRequest(LoginPingRequest paramLoginPingRequest) {
    return start((Class)LoginResponse.class, (Class)Errors.class)
      .uri("/api/login")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramLoginPingRequest, objectMapper()))
      .put()
      .go();
  }
  
  public ClientResponse<Void, Void> logout(boolean paramBoolean, String paramString) {
    return startAnonymous((Class)void.class, (Class)void.class)
      .uri("/api/logout")
      .urlParameter("global", Boolean.valueOf(paramBoolean))
      .urlParameter("refreshToken", paramString)
      .post()
      .go();
  }
  
  public ClientResponse<Void, Void> logoutWithRequest(LogoutRequest paramLogoutRequest) {
    return startAnonymous((Class)void.class, (Class)void.class)
      .uri("/api/logout")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramLogoutRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<LookupResponse, Void> lookupIdentityProvider(String paramString) {
    return start((Class)LookupResponse.class, (Class)void.class)
      .uri("/api/identity-provider/lookup")
      .urlParameter("domain", paramString)
      .get()
      .go();
  }
  
  public ClientResponse<LookupResponse, Void> lookupIdentityProviderByTenantId(String paramString, UUID paramUUID) {
    return start((Class)LookupResponse.class, (Class)void.class)
      .uri("/api/identity-provider/lookup")
      .urlParameter("domain", paramString)
      .urlParameter("tenantId", paramUUID)
      .get()
      .go();
  }
  
  public ClientResponse<ActionResponse, Errors> modifyAction(UUID paramUUID, ActionRequest paramActionRequest) {
    return start((Class)ActionResponse.class, (Class)Errors.class)
      .uri("/api/user/action")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramActionRequest, objectMapper()))
      .put()
      .go();
  }
  
  public ClientResponse<LoginResponse, Errors> passwordlessLogin(PasswordlessLoginRequest paramPasswordlessLoginRequest) {
    return startAnonymous((Class)LoginResponse.class, (Class)Errors.class)
      .uri("/api/passwordless/login")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramPasswordlessLoginRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<APIKeyResponse, Errors> patchAPIKey(UUID paramUUID, Map<String, Object> paramMap) {
    return start((Class)APIKeyResponse.class, (Class)Errors.class)
      .uri("/api/api-key")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramMap, objectMapper()))
      .patch()
      .go();
  }
  
  public ClientResponse<ApplicationResponse, Errors> patchApplication(UUID paramUUID, Map<String, Object> paramMap) {
    return start((Class)ApplicationResponse.class, (Class)Errors.class)
      .uri("/api/application")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramMap, objectMapper()))
      .patch()
      .go();
  }
  
  public ClientResponse<ApplicationResponse, Errors> patchApplicationRole(UUID paramUUID1, UUID paramUUID2, Map<String, Object> paramMap) {
    return start((Class)ApplicationResponse.class, (Class)Errors.class)
      .uri("/api/application")
      .urlSegment(paramUUID1)
      .urlSegment("role")
      .urlSegment(paramUUID2)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramMap, objectMapper()))
      .patch()
      .go();
  }
  
  public ClientResponse<ConnectorResponse, Errors> patchConnector(UUID paramUUID, Map<String, Object> paramMap) {
    return start((Class)ConnectorResponse.class, (Class)Errors.class)
      .uri("/api/connector")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramMap, objectMapper()))
      .patch()
      .go();
  }
  
  public ClientResponse<ConsentResponse, Errors> patchConsent(UUID paramUUID, Map<String, Object> paramMap) {
    return start((Class)ConsentResponse.class, (Class)Errors.class)
      .uri("/api/consent")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramMap, objectMapper()))
      .patch()
      .go();
  }
  
  public ClientResponse<EmailTemplateResponse, Errors> patchEmailTemplate(UUID paramUUID, Map<String, Object> paramMap) {
    return start((Class)EmailTemplateResponse.class, (Class)Errors.class)
      .uri("/api/email/template")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramMap, objectMapper()))
      .patch()
      .go();
  }
  
  public ClientResponse<EntityResponse, Errors> patchEntity(UUID paramUUID, Map<String, Object> paramMap) {
    return start((Class)EntityResponse.class, (Class)Errors.class)
      .uri("/api/entity")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramMap, objectMapper()))
      .patch()
      .go();
  }
  
  public ClientResponse<EntityTypeResponse, Errors> patchEntityType(UUID paramUUID, Map<String, Object> paramMap) {
    return start((Class)EntityTypeResponse.class, (Class)Errors.class)
      .uri("/api/entity/type")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramMap, objectMapper()))
      .patch()
      .go();
  }
  
  public ClientResponse<EntityTypeResponse, Errors> patchEntityTypePermission(UUID paramUUID1, UUID paramUUID2, Map<String, Object> paramMap) {
    return start((Class)EntityTypeResponse.class, (Class)Errors.class)
      .uri("/api/entity/type")
      .urlSegment(paramUUID1)
      .urlSegment("permission")
      .urlSegment(paramUUID2)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramMap, objectMapper()))
      .patch()
      .go();
  }
  
  public ClientResponse<FormResponse, Errors> patchForm(UUID paramUUID, Map<String, Object> paramMap) {
    return start((Class)FormResponse.class, (Class)Errors.class)
      .uri("/api/form")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramMap, objectMapper()))
      .patch()
      .go();
  }
  
  public ClientResponse<FormFieldResponse, Errors> patchFormField(UUID paramUUID, Map<String, Object> paramMap) {
    return start((Class)FormFieldResponse.class, (Class)Errors.class)
      .uri("/api/form/field")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramMap, objectMapper()))
      .patch()
      .go();
  }
  
  public ClientResponse<GroupResponse, Errors> patchGroup(UUID paramUUID, Map<String, Object> paramMap) {
    return start((Class)GroupResponse.class, (Class)Errors.class)
      .uri("/api/group")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramMap, objectMapper()))
      .patch()
      .go();
  }
  
  public ClientResponse<IPAccessControlListResponse, Errors> patchIPAccessControlList(UUID paramUUID, Map<String, Object> paramMap) {
    return start((Class)IPAccessControlListResponse.class, (Class)Errors.class)
      .uri("/api/ip-acl")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramMap, objectMapper()))
      .patch()
      .go();
  }
  
  public ClientResponse<IdentityProviderResponse, Errors> patchIdentityProvider(UUID paramUUID, Map<String, Object> paramMap) {
    return start((Class)IdentityProviderResponse.class, (Class)Errors.class)
      .uri("/api/identity-provider")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramMap, objectMapper()))
      .patch()
      .go();
  }
  
  public ClientResponse<IntegrationResponse, Errors> patchIntegrations(Map<String, Object> paramMap) {
    return start((Class)IntegrationResponse.class, (Class)Errors.class)
      .uri("/api/integration")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramMap, objectMapper()))
      .patch()
      .go();
  }
  
  public ClientResponse<LambdaResponse, Errors> patchLambda(UUID paramUUID, Map<String, Object> paramMap) {
    return start((Class)LambdaResponse.class, (Class)Errors.class)
      .uri("/api/lambda")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramMap, objectMapper()))
      .patch()
      .go();
  }
  
  public ClientResponse<MessageTemplateResponse, Errors> patchMessageTemplate(UUID paramUUID, Map<String, Object> paramMap) {
    return start((Class)MessageTemplateResponse.class, (Class)Errors.class)
      .uri("/api/message/template")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramMap, objectMapper()))
      .patch()
      .go();
  }
  
  public ClientResponse<MessengerResponse, Errors> patchMessenger(UUID paramUUID, Map<String, Object> paramMap) {
    return start((Class)MessengerResponse.class, (Class)Errors.class)
      .uri("/api/messenger")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramMap, objectMapper()))
      .patch()
      .go();
  }
  
  public ClientResponse<ApplicationOAuthScopeResponse, Errors> patchOAuthScope(UUID paramUUID1, UUID paramUUID2, Map<String, Object> paramMap) {
    return start((Class)ApplicationOAuthScopeResponse.class, (Class)Errors.class)
      .uri("/api/application")
      .urlSegment(paramUUID1)
      .urlSegment("scope")
      .urlSegment(paramUUID2)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramMap, objectMapper()))
      .patch()
      .go();
  }
  
  public ClientResponse<RegistrationResponse, Errors> patchRegistration(UUID paramUUID, Map<String, Object> paramMap) {
    return start((Class)RegistrationResponse.class, (Class)Errors.class)
      .uri("/api/user/registration")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramMap, objectMapper()))
      .patch()
      .go();
  }
  
  public ClientResponse<SystemConfigurationResponse, Errors> patchSystemConfiguration(Map<String, Object> paramMap) {
    return start((Class)SystemConfigurationResponse.class, (Class)Errors.class)
      .uri("/api/system-configuration")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramMap, objectMapper()))
      .patch()
      .go();
  }
  
  public ClientResponse<TenantResponse, Errors> patchTenant(UUID paramUUID, Map<String, Object> paramMap) {
    return start((Class)TenantResponse.class, (Class)Errors.class)
      .uri("/api/tenant")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramMap, objectMapper()))
      .patch()
      .go();
  }
  
  public ClientResponse<TenantManagerConfigurationResponse, Errors> patchTenantManagerConfiguration(Map<String, Object> paramMap) {
    return start((Class)TenantManagerConfigurationResponse.class, (Class)Errors.class)
      .uri("/api/tenant-manager")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramMap, objectMapper()))
      .patch()
      .go();
  }
  
  public ClientResponse<TenantManagerIdentityProviderTypeConfigurationResponse, Errors> patchTenantManagerIdentityProviderTypeConfiguration(IdentityProviderType paramIdentityProviderType, Map<String, Object> paramMap) {
    return start((Class)TenantManagerIdentityProviderTypeConfigurationResponse.class, (Class)Errors.class)
      .uri("/api/tenant-manager/identity-provider")
      .urlSegment(paramIdentityProviderType)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramMap, objectMapper()))
      .patch()
      .go();
  }
  
  public ClientResponse<ThemeResponse, Errors> patchTheme(UUID paramUUID, Map<String, Object> paramMap) {
    return start((Class)ThemeResponse.class, (Class)Errors.class)
      .uri("/api/theme")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramMap, objectMapper()))
      .patch()
      .go();
  }
  
  public ClientResponse<UserResponse, Errors> patchUser(UUID paramUUID, Map<String, Object> paramMap) {
    return start((Class)UserResponse.class, (Class)Errors.class)
      .uri("/api/user")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramMap, objectMapper()))
      .patch()
      .go();
  }
  
  public ClientResponse<UserActionResponse, Errors> patchUserAction(UUID paramUUID, Map<String, Object> paramMap) {
    return start((Class)UserActionResponse.class, (Class)Errors.class)
      .uri("/api/user-action")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramMap, objectMapper()))
      .patch()
      .go();
  }
  
  public ClientResponse<UserActionReasonResponse, Errors> patchUserActionReason(UUID paramUUID, Map<String, Object> paramMap) {
    return start((Class)UserActionReasonResponse.class, (Class)Errors.class)
      .uri("/api/user-action-reason")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramMap, objectMapper()))
      .patch()
      .go();
  }
  
  public ClientResponse<UserConsentResponse, Errors> patchUserConsent(UUID paramUUID, Map<String, Object> paramMap) {
    return start((Class)UserConsentResponse.class, (Class)Errors.class)
      .uri("/api/user/consent")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramMap, objectMapper()))
      .patch()
      .go();
  }
  
  public ClientResponse<WebhookResponse, Errors> patchWebhook(UUID paramUUID, Map<String, Object> paramMap) {
    return start((Class)WebhookResponse.class, (Class)Errors.class)
      .uri("/api/webhook")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramMap, objectMapper()))
      .patch()
      .go();
  }
  
  public ClientResponse<ApplicationResponse, Errors> reactivateApplication(UUID paramUUID) {
    return start((Class)ApplicationResponse.class, (Class)Errors.class)
      .uri("/api/application")
      .urlSegment(paramUUID)
      .urlParameter("reactivate", Boolean.valueOf(true))
      .put()
      .go();
  }
  
  public ClientResponse<UserResponse, Errors> reactivateUser(UUID paramUUID) {
    return start((Class)UserResponse.class, (Class)Errors.class)
      .uri("/api/user")
      .urlSegment(paramUUID)
      .urlParameter("reactivate", Boolean.valueOf(true))
      .put()
      .go();
  }
  
  public ClientResponse<UserActionResponse, Errors> reactivateUserAction(UUID paramUUID) {
    return start((Class)UserActionResponse.class, (Class)Errors.class)
      .uri("/api/user-action")
      .urlSegment(paramUUID)
      .urlParameter("reactivate", Boolean.valueOf(true))
      .put()
      .go();
  }
  
  public ClientResponse<LoginResponse, Errors> reconcileJWT(IdentityProviderLoginRequest paramIdentityProviderLoginRequest) {
    return startAnonymous((Class)LoginResponse.class, (Class)Errors.class)
      .uri("/api/jwt/reconcile")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramIdentityProviderLoginRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<Void, Void> refreshEntitySearchIndex() {
    return start((Class)void.class, (Class)void.class)
      .uri("/api/entity/search")
      .put()
      .go();
  }
  
  public ClientResponse<Void, Void> refreshUserSearchIndex() {
    return start((Class)void.class, (Class)void.class)
      .uri("/api/user/search")
      .put()
      .go();
  }
  
  public ClientResponse<Void, Void> regenerateReactorKeys() {
    return start((Class)void.class, (Class)void.class)
      .uri("/api/reactor")
      .put()
      .go();
  }
  
  public ClientResponse<RegistrationResponse, Errors> register(UUID paramUUID, RegistrationRequest paramRegistrationRequest) {
    return start((Class)RegistrationResponse.class, (Class)Errors.class)
      .uri("/api/user/registration")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramRegistrationRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<Void, Errors> reindex(ReindexRequest paramReindexRequest) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/system/reindex")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramReindexRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<Void, Errors> removeUserFromFamily(UUID paramUUID1, UUID paramUUID2) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/user/family")
      .urlSegment(paramUUID1)
      .urlSegment(paramUUID2)
      .delete()
      .go();
  }
  
  public ClientResponse<VerifyEmailResponse, Errors> resendEmailVerification(String paramString) {
    return start((Class)VerifyEmailResponse.class, (Class)Errors.class)
      .uri("/api/user/verify-email")
      .urlParameter("email", paramString)
      .put()
      .go();
  }
  
  public ClientResponse<VerifyEmailResponse, Errors> resendEmailVerificationWithApplicationTemplate(UUID paramUUID, String paramString) {
    return start((Class)VerifyEmailResponse.class, (Class)Errors.class)
      .uri("/api/user/verify-email")
      .urlParameter("applicationId", paramUUID)
      .urlParameter("email", paramString)
      .put()
      .go();
  }
  
  public ClientResponse<VerifyRegistrationResponse, Errors> resendRegistrationVerification(String paramString, UUID paramUUID) {
    return start((Class)VerifyRegistrationResponse.class, (Class)Errors.class)
      .uri("/api/user/verify-registration")
      .urlParameter("email", paramString)
      .urlParameter("applicationId", paramUUID)
      .put()
      .go();
  }
  
  public ClientResponse<APIKeyResponse, Errors> retrieveAPIKey(UUID paramUUID) {
    return start((Class)APIKeyResponse.class, (Class)Errors.class)
      .uri("/api/api-key")
      .urlSegment(paramUUID)
      .get()
      .go();
  }
  
  public ClientResponse<ActionResponse, Errors> retrieveAction(UUID paramUUID) {
    return start((Class)ActionResponse.class, (Class)Errors.class)
      .uri("/api/user/action")
      .urlSegment(paramUUID)
      .get()
      .go();
  }
  
  public ClientResponse<ActionResponse, Errors> retrieveActions(UUID paramUUID) {
    return start((Class)ActionResponse.class, (Class)Errors.class)
      .uri("/api/user/action")
      .urlParameter("userId", paramUUID)
      .get()
      .go();
  }
  
  public ClientResponse<ActionResponse, Errors> retrieveActionsPreventingLogin(UUID paramUUID) {
    return start((Class)ActionResponse.class, (Class)Errors.class)
      .uri("/api/user/action")
      .urlParameter("userId", paramUUID)
      .urlParameter("preventingLogin", Boolean.valueOf(true))
      .get()
      .go();
  }
  
  public ClientResponse<ActionResponse, Errors> retrieveActiveActions(UUID paramUUID) {
    return start((Class)ActionResponse.class, (Class)Errors.class)
      .uri("/api/user/action")
      .urlParameter("userId", paramUUID)
      .urlParameter("active", Boolean.valueOf(true))
      .get()
      .go();
  }
  
  public ClientResponse<ApplicationResponse, Void> retrieveApplication(UUID paramUUID) {
    return start((Class)ApplicationResponse.class, (Class)void.class)
      .uri("/api/application")
      .urlSegment(paramUUID)
      .get()
      .go();
  }
  
  public ClientResponse<ApplicationResponse, Void> retrieveApplications() {
    return start((Class)ApplicationResponse.class, (Class)void.class)
      .uri("/api/application")
      .get()
      .go();
  }
  
  public ClientResponse<AuditLogResponse, Errors> retrieveAuditLog(Integer paramInteger) {
    return start((Class)AuditLogResponse.class, (Class)Errors.class)
      .uri("/api/system/audit-log")
      .urlSegment(paramInteger)
      .get()
      .go();
  }
  
  public ClientResponse<ConnectorResponse, Void> retrieveConnector(UUID paramUUID) {
    return start((Class)ConnectorResponse.class, (Class)void.class)
      .uri("/api/connector")
      .urlSegment(paramUUID)
      .get()
      .go();
  }
  
  public ClientResponse<ConnectorResponse, Void> retrieveConnectors() {
    return start((Class)ConnectorResponse.class, (Class)void.class)
      .uri("/api/connector")
      .get()
      .go();
  }
  
  public ClientResponse<ConsentResponse, Void> retrieveConsent(UUID paramUUID) {
    return start((Class)ConsentResponse.class, (Class)void.class)
      .uri("/api/consent")
      .urlSegment(paramUUID)
      .get()
      .go();
  }
  
  public ClientResponse<ConsentResponse, Void> retrieveConsents() {
    return start((Class)ConsentResponse.class, (Class)void.class)
      .uri("/api/consent")
      .get()
      .go();
  }
  
  public ClientResponse<DailyActiveUserReportResponse, Errors> retrieveDailyActiveReport(UUID paramUUID, long paramLong1, long paramLong2) {
    return start((Class)DailyActiveUserReportResponse.class, (Class)Errors.class)
      .uri("/api/report/daily-active-user")
      .urlParameter("applicationId", paramUUID)
      .urlParameter("start", Long.valueOf(paramLong1))
      .urlParameter("end", Long.valueOf(paramLong2))
      .get()
      .go();
  }
  
  public ClientResponse<EmailTemplateResponse, Void> retrieveEmailTemplate(UUID paramUUID) {
    return start((Class)EmailTemplateResponse.class, (Class)void.class)
      .uri("/api/email/template")
      .urlSegment(paramUUID)
      .get()
      .go();
  }
  
  public ClientResponse<PreviewResponse, Errors> retrieveEmailTemplatePreview(PreviewRequest paramPreviewRequest) {
    return start((Class)PreviewResponse.class, (Class)Errors.class)
      .uri("/api/email/template/preview")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramPreviewRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<EmailTemplateResponse, Void> retrieveEmailTemplates() {
    return start((Class)EmailTemplateResponse.class, (Class)void.class)
      .uri("/api/email/template")
      .get()
      .go();
  }
  
  public ClientResponse<EntityResponse, Errors> retrieveEntity(UUID paramUUID) {
    return start((Class)EntityResponse.class, (Class)Errors.class)
      .uri("/api/entity")
      .urlSegment(paramUUID)
      .get()
      .go();
  }
  
  public ClientResponse<EntityGrantResponse, Errors> retrieveEntityGrant(UUID paramUUID1, UUID paramUUID2, UUID paramUUID3) {
    return start((Class)EntityGrantResponse.class, (Class)Errors.class)
      .uri("/api/entity")
      .urlSegment(paramUUID1)
      .urlSegment("grant")
      .urlParameter("recipientEntityId", paramUUID2)
      .urlParameter("userId", paramUUID3)
      .get()
      .go();
  }
  
  public ClientResponse<EntityTypeResponse, Errors> retrieveEntityType(UUID paramUUID) {
    return start((Class)EntityTypeResponse.class, (Class)Errors.class)
      .uri("/api/entity/type")
      .urlSegment(paramUUID)
      .get()
      .go();
  }
  
  public ClientResponse<EntityTypeResponse, Errors> retrieveEntityTypes() {
    return start((Class)EntityTypeResponse.class, (Class)Errors.class)
      .uri("/api/entity/type")
      .get()
      .go();
  }
  
  public ClientResponse<EventLogResponse, Errors> retrieveEventLog(Integer paramInteger) {
    return start((Class)EventLogResponse.class, (Class)Errors.class)
      .uri("/api/system/event-log")
      .urlSegment(paramInteger)
      .get()
      .go();
  }
  
  public ClientResponse<FamilyResponse, Void> retrieveFamilies(UUID paramUUID) {
    return start((Class)FamilyResponse.class, (Class)void.class)
      .uri("/api/user/family")
      .urlParameter("userId", paramUUID)
      .get()
      .go();
  }
  
  public ClientResponse<FamilyResponse, Void> retrieveFamilyMembersByFamilyId(UUID paramUUID) {
    return start((Class)FamilyResponse.class, (Class)void.class)
      .uri("/api/user/family")
      .urlSegment(paramUUID)
      .get()
      .go();
  }
  
  public ClientResponse<FormResponse, Void> retrieveForm(UUID paramUUID) {
    return start((Class)FormResponse.class, (Class)void.class)
      .uri("/api/form")
      .urlSegment(paramUUID)
      .get()
      .go();
  }
  
  public ClientResponse<FormFieldResponse, Void> retrieveFormField(UUID paramUUID) {
    return start((Class)FormFieldResponse.class, (Class)void.class)
      .uri("/api/form/field")
      .urlSegment(paramUUID)
      .get()
      .go();
  }
  
  public ClientResponse<FormFieldResponse, Void> retrieveFormFields() {
    return start((Class)FormFieldResponse.class, (Class)void.class)
      .uri("/api/form/field")
      .get()
      .go();
  }
  
  public ClientResponse<FormResponse, Void> retrieveForms() {
    return start((Class)FormResponse.class, (Class)void.class)
      .uri("/api/form")
      .get()
      .go();
  }
  
  public ClientResponse<GroupResponse, Errors> retrieveGroup(UUID paramUUID) {
    return start((Class)GroupResponse.class, (Class)Errors.class)
      .uri("/api/group")
      .urlSegment(paramUUID)
      .get()
      .go();
  }
  
  public ClientResponse<GroupResponse, Void> retrieveGroups() {
    return start((Class)GroupResponse.class, (Class)void.class)
      .uri("/api/group")
      .get()
      .go();
  }
  
  public ClientResponse<IPAccessControlListResponse, Void> retrieveIPAccessControlList(UUID paramUUID) {
    return start((Class)IPAccessControlListResponse.class, (Class)void.class)
      .uri("/api/ip-acl")
      .urlSegment(paramUUID)
      .get()
      .go();
  }
  
  public ClientResponse<IdentityProviderResponse, Errors> retrieveIdentityProvider(UUID paramUUID) {
    return start((Class)IdentityProviderResponse.class, (Class)Errors.class)
      .uri("/api/identity-provider")
      .urlSegment(paramUUID)
      .get()
      .go();
  }
  
  public ClientResponse<IdentityProviderResponse, Errors> retrieveIdentityProviderByType(IdentityProviderType paramIdentityProviderType) {
    return start((Class)IdentityProviderResponse.class, (Class)Errors.class)
      .uri("/api/identity-provider")
      .urlParameter("type", paramIdentityProviderType)
      .get()
      .go();
  }
  
  public ClientResponse<IdentityProviderConnectionTestResponse, Errors> retrieveIdentityProviderConnectionTestResults(String paramString) {
    return start((Class)IdentityProviderConnectionTestResponse.class, (Class)Errors.class)
      .uri("/api/identity-provider/test")
      .urlParameter("connectionTestId", paramString)
      .get()
      .go();
  }
  
  public ClientResponse<IdentityProviderResponse, Void> retrieveIdentityProviders() {
    return start((Class)IdentityProviderResponse.class, (Class)void.class)
      .uri("/api/identity-provider")
      .get()
      .go();
  }
  
  public ClientResponse<ActionResponse, Errors> retrieveInactiveActions(UUID paramUUID) {
    return start((Class)ActionResponse.class, (Class)Errors.class)
      .uri("/api/user/action")
      .urlParameter("userId", paramUUID)
      .urlParameter("active", Boolean.valueOf(false))
      .get()
      .go();
  }
  
  public ClientResponse<ApplicationResponse, Void> retrieveInactiveApplications() {
    return start((Class)ApplicationResponse.class, (Class)void.class)
      .uri("/api/application")
      .urlParameter("inactive", Boolean.valueOf(true))
      .get()
      .go();
  }
  
  public ClientResponse<UserActionResponse, Void> retrieveInactiveUserActions() {
    return start((Class)UserActionResponse.class, (Class)void.class)
      .uri("/api/user-action")
      .urlParameter("inactive", Boolean.valueOf(true))
      .get()
      .go();
  }
  
  public ClientResponse<IntegrationResponse, Void> retrieveIntegration() {
    return start((Class)IntegrationResponse.class, (Class)void.class)
      .uri("/api/integration")
      .get()
      .go();
  }
  
  public ClientResponse<PublicKeyResponse, Void> retrieveJWTPublicKey(String paramString) {
    return startAnonymous((Class)PublicKeyResponse.class, (Class)void.class)
      .uri("/api/jwt/public-key")
      .urlParameter("kid", paramString)
      .get()
      .go();
  }
  
  public ClientResponse<PublicKeyResponse, Void> retrieveJWTPublicKeyByApplicationId(String paramString) {
    return startAnonymous((Class)PublicKeyResponse.class, (Class)void.class)
      .uri("/api/jwt/public-key")
      .urlParameter("applicationId", paramString)
      .get()
      .go();
  }
  
  public ClientResponse<PublicKeyResponse, Void> retrieveJWTPublicKeys() {
    return startAnonymous((Class)PublicKeyResponse.class, (Class)void.class)
      .uri("/api/jwt/public-key")
      .get()
      .go();
  }
  
  public ClientResponse<JWKSResponse, Void> retrieveJsonWebKeySet() {
    return startAnonymous((Class)JWKSResponse.class, (Class)void.class)
      .uri("/.well-known/jwks.json")
      .get()
      .go();
  }
  
  public ClientResponse<KeyResponse, Errors> retrieveKey(UUID paramUUID) {
    return start((Class)KeyResponse.class, (Class)Errors.class)
      .uri("/api/key")
      .urlSegment(paramUUID)
      .get()
      .go();
  }
  
  public ClientResponse<KeyResponse, Void> retrieveKeys() {
    return start((Class)KeyResponse.class, (Class)void.class)
      .uri("/api/key")
      .get()
      .go();
  }
  
  public ClientResponse<LambdaResponse, Errors> retrieveLambda(UUID paramUUID) {
    return start((Class)LambdaResponse.class, (Class)Errors.class)
      .uri("/api/lambda")
      .urlSegment(paramUUID)
      .get()
      .go();
  }
  
  public ClientResponse<LambdaResponse, Void> retrieveLambdas() {
    return start((Class)LambdaResponse.class, (Class)void.class)
      .uri("/api/lambda")
      .get()
      .go();
  }
  
  public ClientResponse<LambdaResponse, Void> retrieveLambdasByType(LambdaType paramLambdaType) {
    return start((Class)LambdaResponse.class, (Class)void.class)
      .uri("/api/lambda")
      .urlParameter("type", paramLambdaType)
      .get()
      .go();
  }
  
  public ClientResponse<LoginReportResponse, Errors> retrieveLoginReport(UUID paramUUID, long paramLong1, long paramLong2) {
    return start((Class)LoginReportResponse.class, (Class)Errors.class)
      .uri("/api/report/login")
      .urlParameter("applicationId", paramUUID)
      .urlParameter("start", Long.valueOf(paramLong1))
      .urlParameter("end", Long.valueOf(paramLong2))
      .get()
      .go();
  }
  
  public ClientResponse<MessageTemplateResponse, Void> retrieveMessageTemplate(UUID paramUUID) {
    return start((Class)MessageTemplateResponse.class, (Class)void.class)
      .uri("/api/message/template")
      .urlSegment(paramUUID)
      .get()
      .go();
  }
  
  public ClientResponse<PreviewMessageTemplateResponse, Errors> retrieveMessageTemplatePreview(PreviewMessageTemplateRequest paramPreviewMessageTemplateRequest) {
    return start((Class)PreviewMessageTemplateResponse.class, (Class)Errors.class)
      .uri("/api/message/template/preview")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramPreviewMessageTemplateRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<MessageTemplateResponse, Void> retrieveMessageTemplates() {
    return start((Class)MessageTemplateResponse.class, (Class)void.class)
      .uri("/api/message/template")
      .get()
      .go();
  }
  
  public ClientResponse<MessengerResponse, Void> retrieveMessenger(UUID paramUUID) {
    return start((Class)MessengerResponse.class, (Class)void.class)
      .uri("/api/messenger")
      .urlSegment(paramUUID)
      .get()
      .go();
  }
  
  public ClientResponse<MessengerResponse, Void> retrieveMessengers() {
    return start((Class)MessengerResponse.class, (Class)void.class)
      .uri("/api/messenger")
      .get()
      .go();
  }
  
  public ClientResponse<MonthlyActiveUserReportResponse, Errors> retrieveMonthlyActiveReport(UUID paramUUID, long paramLong1, long paramLong2) {
    return start((Class)MonthlyActiveUserReportResponse.class, (Class)Errors.class)
      .uri("/api/report/monthly-active-user")
      .urlParameter("applicationId", paramUUID)
      .urlParameter("start", Long.valueOf(paramLong1))
      .urlParameter("end", Long.valueOf(paramLong2))
      .get()
      .go();
  }
  
  public ClientResponse<ApplicationOAuthScopeResponse, Errors> retrieveOAuthScope(UUID paramUUID1, UUID paramUUID2) {
    return start((Class)ApplicationOAuthScopeResponse.class, (Class)Errors.class)
      .uri("/api/application")
      .urlSegment(paramUUID1)
      .urlSegment("scope")
      .urlSegment(paramUUID2)
      .get()
      .go();
  }
  
  public ClientResponse<OAuthConfigurationResponse, Errors> retrieveOauthConfiguration(UUID paramUUID) {
    return start((Class)OAuthConfigurationResponse.class, (Class)Errors.class)
      .uri("/api/application")
      .urlSegment(paramUUID)
      .urlSegment("oauth-configuration")
      .get()
      .go();
  }
  
  public ClientResponse<OpenIdConfiguration, Void> retrieveOpenIdConfiguration() {
    return startAnonymous((Class)OpenIdConfiguration.class, (Class)void.class)
      .uri("/.well-known/openid-configuration")
      .get()
      .go();
  }
  
  public ClientResponse<PasswordValidationRulesResponse, Void> retrievePasswordValidationRules() {
    return startAnonymous((Class)PasswordValidationRulesResponse.class, (Class)void.class)
      .uri("/api/tenant/password-validation-rules")
      .get()
      .go();
  }
  
  public ClientResponse<PasswordValidationRulesResponse, Void> retrievePasswordValidationRulesWithTenantId(UUID paramUUID) {
    return startAnonymous((Class)PasswordValidationRulesResponse.class, (Class)void.class)
      .uri("/api/tenant/password-validation-rules")
      .urlSegment(paramUUID)
      .get()
      .go();
  }
  
  public ClientResponse<PendingResponse, Errors> retrievePendingChildren(String paramString) {
    return start((Class)PendingResponse.class, (Class)Errors.class)
      .uri("/api/user/family/pending")
      .urlParameter("parentEmail", paramString)
      .get()
      .go();
  }
  
  public ClientResponse<IdentityProviderPendingLinkResponse, Errors> retrievePendingLink(String paramString, UUID paramUUID) {
    return start((Class)IdentityProviderPendingLinkResponse.class, (Class)Errors.class)
      .uri("/api/identity-provider/link/pending")
      .urlSegment(paramString)
      .urlParameter("userId", paramUUID)
      .get()
      .go();
  }
  
  public ClientResponse<ReactorMetricsResponse, Void> retrieveReactorMetrics() {
    return start((Class)ReactorMetricsResponse.class, (Class)void.class)
      .uri("/api/reactor/metrics")
      .get()
      .go();
  }
  
  public ClientResponse<ReactorResponse, Void> retrieveReactorStatus() {
    return start((Class)ReactorResponse.class, (Class)void.class)
      .uri("/api/reactor")
      .get()
      .go();
  }
  
  public ClientResponse<RecentLoginResponse, Errors> retrieveRecentLogins(int paramInt, Integer paramInteger) {
    return start((Class)RecentLoginResponse.class, (Class)Errors.class)
      .uri("/api/user/recent-login")
      .urlParameter("offset", Integer.valueOf(paramInt))
      .urlParameter("limit", paramInteger)
      .get()
      .go();
  }
  
  public ClientResponse<RefreshTokenResponse, Errors> retrieveRefreshTokenById(UUID paramUUID) {
    return start((Class)RefreshTokenResponse.class, (Class)Errors.class)
      .uri("/api/jwt/refresh")
      .urlSegment(paramUUID)
      .get()
      .go();
  }
  
  public ClientResponse<RefreshTokenResponse, Errors> retrieveRefreshTokens(UUID paramUUID) {
    return start((Class)RefreshTokenResponse.class, (Class)Errors.class)
      .uri("/api/jwt/refresh")
      .urlParameter("userId", paramUUID)
      .get()
      .go();
  }
  
  public ClientResponse<RegistrationResponse, Errors> retrieveRegistration(UUID paramUUID1, UUID paramUUID2) {
    return start((Class)RegistrationResponse.class, (Class)Errors.class)
      .uri("/api/user/registration")
      .urlSegment(paramUUID1)
      .urlSegment(paramUUID2)
      .get()
      .go();
  }
  
  public ClientResponse<RegistrationReportResponse, Errors> retrieveRegistrationReport(UUID paramUUID, long paramLong1, long paramLong2) {
    return start((Class)RegistrationReportResponse.class, (Class)Errors.class)
      .uri("/api/report/registration")
      .urlParameter("applicationId", paramUUID)
      .urlParameter("start", Long.valueOf(paramLong1))
      .urlParameter("end", Long.valueOf(paramLong2))
      .get()
      .go();
  }
  
  public ClientResponse<Void, Errors> retrieveReindexStatus() {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/system/reindex")
      .get()
      .go();
  }
  
  public ClientResponse<SystemConfigurationResponse, Void> retrieveSystemConfiguration() {
    return start((Class)SystemConfigurationResponse.class, (Class)void.class)
      .uri("/api/system-configuration")
      .get()
      .go();
  }
  
  public ClientResponse<Void, Void> retrieveSystemHealth() {
    return startAnonymous((Class)void.class, (Class)void.class)
      .uri("/api/health")
      .get()
      .go();
  }
  
  public ClientResponse<StatusResponse, Void> retrieveSystemStatus() {
    return startAnonymous((Class)StatusResponse.class, (Class)void.class)
      .uri("/api/status")
      .get()
      .go();
  }
  
  public ClientResponse<StatusResponse, Void> retrieveSystemStatusUsingAPIKey() {
    return start((Class)StatusResponse.class, (Class)void.class)
      .uri("/api/status")
      .get()
      .go();
  }
  
  public ClientResponse<TenantResponse, Errors> retrieveTenant(UUID paramUUID) {
    return start((Class)TenantResponse.class, (Class)Errors.class)
      .uri("/api/tenant")
      .urlSegment(paramUUID)
      .get()
      .go();
  }
  
  public ClientResponse<TenantManagerConfigurationResponse, Void> retrieveTenantManagerConfiguration() {
    return start((Class)TenantManagerConfigurationResponse.class, (Class)void.class)
      .uri("/api/tenant-manager")
      .get()
      .go();
  }
  
  public ClientResponse<TenantResponse, Void> retrieveTenants() {
    return start((Class)TenantResponse.class, (Class)void.class)
      .uri("/api/tenant")
      .get()
      .go();
  }
  
  public ClientResponse<ThemeResponse, Errors> retrieveTheme(UUID paramUUID) {
    return start((Class)ThemeResponse.class, (Class)Errors.class)
      .uri("/api/theme")
      .urlSegment(paramUUID)
      .get()
      .go();
  }
  
  public ClientResponse<ThemeResponse, Void> retrieveThemes() {
    return start((Class)ThemeResponse.class, (Class)void.class)
      .uri("/api/theme")
      .get()
      .go();
  }
  
  public ClientResponse<TotalsReportResponse, Void> retrieveTotalReport() {
    return start((Class)TotalsReportResponse.class, (Class)void.class)
      .uri("/api/report/totals")
      .get()
      .go();
  }
  
  public ClientResponse<TotalsReportResponse, Void> retrieveTotalReportWithExcludes(List<String> paramList) {
    return start((Class)TotalsReportResponse.class, (Class)void.class)
      .uri("/api/report/totals")
      .urlParameter("excludes", paramList)
      .get()
      .go();
  }
  
  public ClientResponse<TwoFactorRecoveryCodeResponse, Errors> retrieveTwoFactorRecoveryCodes(UUID paramUUID) {
    return start((Class)TwoFactorRecoveryCodeResponse.class, (Class)Errors.class)
      .uri("/api/user/two-factor/recovery-code")
      .urlSegment(paramUUID)
      .get()
      .go();
  }
  
  public ClientResponse<TwoFactorStatusResponse, Errors> retrieveTwoFactorStatus(UUID paramUUID1, UUID paramUUID2, String paramString) {
    return start((Class)TwoFactorStatusResponse.class, (Class)Errors.class)
      .uri("/api/two-factor/status")
      .urlParameter("userId", paramUUID1)
      .urlParameter("applicationId", paramUUID2)
      .urlSegment(paramString)
      .get()
      .go();
  }
  
  public ClientResponse<TwoFactorStatusResponse, Errors> retrieveTwoFactorStatusWithRequest(TwoFactorStatusRequest paramTwoFactorStatusRequest) {
    return start((Class)TwoFactorStatusResponse.class, (Class)Errors.class)
      .uri("/api/two-factor/status")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramTwoFactorStatusRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<UserResponse, Errors> retrieveUser(UUID paramUUID) {
    return start((Class)UserResponse.class, (Class)Errors.class)
      .uri("/api/user")
      .urlSegment(paramUUID)
      .get()
      .go();
  }
  
  public ClientResponse<UserActionResponse, Void> retrieveUserAction(UUID paramUUID) {
    return start((Class)UserActionResponse.class, (Class)void.class)
      .uri("/api/user-action")
      .urlSegment(paramUUID)
      .get()
      .go();
  }
  
  public ClientResponse<UserActionReasonResponse, Void> retrieveUserActionReason(UUID paramUUID) {
    return start((Class)UserActionReasonResponse.class, (Class)void.class)
      .uri("/api/user-action-reason")
      .urlSegment(paramUUID)
      .get()
      .go();
  }
  
  public ClientResponse<UserActionReasonResponse, Void> retrieveUserActionReasons() {
    return start((Class)UserActionReasonResponse.class, (Class)void.class)
      .uri("/api/user-action-reason")
      .get()
      .go();
  }
  
  public ClientResponse<UserActionResponse, Void> retrieveUserActions() {
    return start((Class)UserActionResponse.class, (Class)void.class)
      .uri("/api/user-action")
      .get()
      .go();
  }
  
  public ClientResponse<UserResponse, Errors> retrieveUserByChangePasswordId(String paramString) {
    return start((Class)UserResponse.class, (Class)Errors.class)
      .uri("/api/user")
      .urlParameter("changePasswordId", paramString)
      .get()
      .go();
  }
  
  public ClientResponse<UserResponse, Errors> retrieveUserByEmail(String paramString) {
    return start((Class)UserResponse.class, (Class)Errors.class)
      .uri("/api/user")
      .urlParameter("email", paramString)
      .get()
      .go();
  }
  
  public ClientResponse<UserResponse, Errors> retrieveUserByLoginId(String paramString) {
    return start((Class)UserResponse.class, (Class)Errors.class)
      .uri("/api/user")
      .urlParameter("loginId", paramString)
      .get()
      .go();
  }
  
  public ClientResponse<UserResponse, Errors> retrieveUserByLoginIdWithLoginIdTypes(String paramString, List<String> paramList) {
    return start((Class)UserResponse.class, (Class)Errors.class)
      .uri("/api/user")
      .urlParameter("loginId", paramString)
      .urlParameter("loginIdTypes", paramList)
      .get()
      .go();
  }
  
  public ClientResponse<UserResponse, Errors> retrieveUserByUsername(String paramString) {
    return start((Class)UserResponse.class, (Class)Errors.class)
      .uri("/api/user")
      .urlParameter("username", paramString)
      .get()
      .go();
  }
  
  public ClientResponse<UserResponse, Errors> retrieveUserByVerificationId(String paramString) {
    return start((Class)UserResponse.class, (Class)Errors.class)
      .uri("/api/user")
      .urlParameter("verificationId", paramString)
      .get()
      .go();
  }
  
  public ClientResponse<Void, Void> retrieveUserCodeUsingAPIKeyWithRequest(RetrieveUserCodeUsingAPIKeyRequest paramRetrieveUserCodeUsingAPIKeyRequest) {
    HashMap<Object, Object> hashMap = new HashMap<>();
    if (paramRetrieveUserCodeUsingAPIKeyRequest.tenantId != null)
      hashMap.put("tenantId", Arrays.asList(new String[] { paramRetrieveUserCodeUsingAPIKeyRequest.tenantId.toString() })); 
    hashMap.put("user_code", Arrays.asList(new String[] { paramRetrieveUserCodeUsingAPIKeyRequest.user_code }));
    return startAnonymous((Class)void.class, (Class)void.class)
      .uri("/oauth2/device/user-code")
      .bodyHandler((RESTClient.BodyHandler)new FormDataBodyHandler(hashMap))
      .post()
      .go();
  }
  
  public ClientResponse<Void, Void> retrieveUserCodeWithRequest(RetrieveUserCodeRequest paramRetrieveUserCodeRequest) {
    HashMap<Object, Object> hashMap = new HashMap<>();
    hashMap.put("client_id", Arrays.asList(new String[] { paramRetrieveUserCodeRequest.client_id }));
    hashMap.put("client_secret", Arrays.asList(new String[] { paramRetrieveUserCodeRequest.client_secret }));
    if (paramRetrieveUserCodeRequest.tenantId != null)
      hashMap.put("tenantId", Arrays.asList(new String[] { paramRetrieveUserCodeRequest.tenantId.toString() })); 
    hashMap.put("user_code", Arrays.asList(new String[] { paramRetrieveUserCodeRequest.user_code }));
    return startAnonymous((Class)void.class, (Class)void.class)
      .uri("/oauth2/device/user-code")
      .bodyHandler((RESTClient.BodyHandler)new FormDataBodyHandler(hashMap))
      .post()
      .go();
  }
  
  public ClientResponse<UserCommentResponse, Errors> retrieveUserComments(UUID paramUUID) {
    return start((Class)UserCommentResponse.class, (Class)Errors.class)
      .uri("/api/user/comment")
      .urlSegment(paramUUID)
      .get()
      .go();
  }
  
  public ClientResponse<UserConsentResponse, Void> retrieveUserConsent(UUID paramUUID) {
    return start((Class)UserConsentResponse.class, (Class)void.class)
      .uri("/api/user/consent")
      .urlSegment(paramUUID)
      .get()
      .go();
  }
  
  public ClientResponse<UserConsentResponse, Void> retrieveUserConsents(UUID paramUUID) {
    return start((Class)UserConsentResponse.class, (Class)void.class)
      .uri("/api/user/consent")
      .urlParameter("userId", paramUUID)
      .get()
      .go();
  }
  
  public ClientResponse<UserinfoResponse, OAuthError> retrieveUserInfoFromAccessToken(String paramString) {
    return startAnonymous((Class)UserinfoResponse.class, (Class)OAuthError.class)
      .uri("/oauth2/userinfo")
      .authorization("Bearer " + paramString)
      .get()
      .go();
  }
  
  public ClientResponse<IdentityProviderLinkResponse, Errors> retrieveUserLink(UUID paramUUID1, String paramString, UUID paramUUID2) {
    return start((Class)IdentityProviderLinkResponse.class, (Class)Errors.class)
      .uri("/api/identity-provider/link")
      .urlParameter("identityProviderId", paramUUID1)
      .urlParameter("identityProviderUserId", paramString)
      .urlParameter("userId", paramUUID2)
      .get()
      .go();
  }
  
  public ClientResponse<IdentityProviderLinkResponse, Errors> retrieveUserLinksByUserId(UUID paramUUID1, UUID paramUUID2) {
    return start((Class)IdentityProviderLinkResponse.class, (Class)Errors.class)
      .uri("/api/identity-provider/link")
      .urlParameter("identityProviderId", paramUUID1)
      .urlParameter("userId", paramUUID2)
      .get()
      .go();
  }
  
  public ClientResponse<LoginReportResponse, Errors> retrieveUserLoginReport(UUID paramUUID1, UUID paramUUID2, long paramLong1, long paramLong2) {
    return start((Class)LoginReportResponse.class, (Class)Errors.class)
      .uri("/api/report/login")
      .urlParameter("applicationId", paramUUID1)
      .urlParameter("userId", paramUUID2)
      .urlParameter("start", Long.valueOf(paramLong1))
      .urlParameter("end", Long.valueOf(paramLong2))
      .get()
      .go();
  }
  
  public ClientResponse<LoginReportResponse, Errors> retrieveUserLoginReportByLoginId(UUID paramUUID, String paramString, long paramLong1, long paramLong2) {
    return start((Class)LoginReportResponse.class, (Class)Errors.class)
      .uri("/api/report/login")
      .urlParameter("applicationId", paramUUID)
      .urlParameter("loginId", paramString)
      .urlParameter("start", Long.valueOf(paramLong1))
      .urlParameter("end", Long.valueOf(paramLong2))
      .get()
      .go();
  }
  
  public ClientResponse<LoginReportResponse, Errors> retrieveUserLoginReportByLoginIdAndLoginIdTypes(UUID paramUUID, String paramString, long paramLong1, long paramLong2, List<String> paramList) {
    return start((Class)LoginReportResponse.class, (Class)Errors.class)
      .uri("/api/report/login")
      .urlParameter("applicationId", paramUUID)
      .urlParameter("loginId", paramString)
      .urlParameter("start", Long.valueOf(paramLong1))
      .urlParameter("end", Long.valueOf(paramLong2))
      .urlParameter("loginIdTypes", paramList)
      .get()
      .go();
  }
  
  public ClientResponse<RecentLoginResponse, Errors> retrieveUserRecentLogins(UUID paramUUID, int paramInt, Integer paramInteger) {
    return start((Class)RecentLoginResponse.class, (Class)Errors.class)
      .uri("/api/user/recent-login")
      .urlParameter("userId", paramUUID)
      .urlParameter("offset", Integer.valueOf(paramInt))
      .urlParameter("limit", paramInteger)
      .get()
      .go();
  }
  
  public ClientResponse<VersionResponse, Errors> retrieveVersion() {
    return start((Class)VersionResponse.class, (Class)Errors.class)
      .uri("/api/system/version")
      .get()
      .go();
  }
  
  public ClientResponse<WebAuthnCredentialResponse, Errors> retrieveWebAuthnCredential(UUID paramUUID) {
    return start((Class)WebAuthnCredentialResponse.class, (Class)Errors.class)
      .uri("/api/webauthn")
      .urlSegment(paramUUID)
      .get()
      .go();
  }
  
  public ClientResponse<WebAuthnCredentialResponse, Errors> retrieveWebAuthnCredentialsForUser(UUID paramUUID) {
    return start((Class)WebAuthnCredentialResponse.class, (Class)Errors.class)
      .uri("/api/webauthn")
      .urlParameter("userId", paramUUID)
      .get()
      .go();
  }
  
  public ClientResponse<WebhookResponse, Void> retrieveWebhook(UUID paramUUID) {
    return start((Class)WebhookResponse.class, (Class)void.class)
      .uri("/api/webhook")
      .urlSegment(paramUUID)
      .get()
      .go();
  }
  
  public ClientResponse<WebhookAttemptLogResponse, Errors> retrieveWebhookAttemptLog(UUID paramUUID) {
    return start((Class)WebhookAttemptLogResponse.class, (Class)Errors.class)
      .uri("/api/system/webhook-attempt-log")
      .urlSegment(paramUUID)
      .get()
      .go();
  }
  
  public ClientResponse<WebhookEventLogResponse, Errors> retrieveWebhookEventLog(UUID paramUUID) {
    return start((Class)WebhookEventLogResponse.class, (Class)Errors.class)
      .uri("/api/system/webhook-event-log")
      .urlSegment(paramUUID)
      .get()
      .go();
  }
  
  public ClientResponse<WebhookResponse, Void> retrieveWebhooks() {
    return start((Class)WebhookResponse.class, (Class)void.class)
      .uri("/api/webhook")
      .get()
      .go();
  }
  
  public ClientResponse<Void, Errors> revokeRefreshToken(String paramString, UUID paramUUID1, UUID paramUUID2) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/jwt/refresh")
      .urlParameter("token", paramString)
      .urlParameter("userId", paramUUID1)
      .urlParameter("applicationId", paramUUID2)
      .delete()
      .go();
  }
  
  public ClientResponse<Void, Errors> revokeRefreshTokenById(UUID paramUUID) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/jwt/refresh")
      .urlSegment(paramUUID)
      .delete()
      .go();
  }
  
  public ClientResponse<Void, Errors> revokeRefreshTokenByToken(String paramString) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/jwt/refresh")
      .urlParameter("token", paramString)
      .delete()
      .go();
  }
  
  public ClientResponse<Void, Errors> revokeRefreshTokensByApplicationId(UUID paramUUID) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/jwt/refresh")
      .urlParameter("applicationId", paramUUID)
      .delete()
      .go();
  }
  
  public ClientResponse<Void, Errors> revokeRefreshTokensByUserId(UUID paramUUID) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/jwt/refresh")
      .urlParameter("userId", paramUUID)
      .delete()
      .go();
  }
  
  public ClientResponse<Void, Errors> revokeRefreshTokensByUserIdForApplication(UUID paramUUID1, UUID paramUUID2) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/jwt/refresh")
      .urlParameter("userId", paramUUID1)
      .urlParameter("applicationId", paramUUID2)
      .delete()
      .go();
  }
  
  public ClientResponse<Void, Errors> revokeRefreshTokensWithRequest(RefreshTokenRevokeRequest paramRefreshTokenRevokeRequest) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/jwt/refresh")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramRefreshTokenRevokeRequest, objectMapper()))
      .delete()
      .go();
  }
  
  public ClientResponse<Void, Void> revokeUserConsent(UUID paramUUID) {
    return start((Class)void.class, (Class)void.class)
      .uri("/api/user/consent")
      .urlSegment(paramUUID)
      .delete()
      .go();
  }
  
  public ClientResponse<ApplicationSearchResponse, Errors> searchApplications(ApplicationSearchRequest paramApplicationSearchRequest) {
    return start((Class)ApplicationSearchResponse.class, (Class)Errors.class)
      .uri("/api/application/search")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramApplicationSearchRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<AuditLogSearchResponse, Errors> searchAuditLogs(AuditLogSearchRequest paramAuditLogSearchRequest) {
    return start((Class)AuditLogSearchResponse.class, (Class)Errors.class)
      .uri("/api/system/audit-log/search")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramAuditLogSearchRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<ConsentSearchResponse, Errors> searchConsents(ConsentSearchRequest paramConsentSearchRequest) {
    return start((Class)ConsentSearchResponse.class, (Class)Errors.class)
      .uri("/api/consent/search")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramConsentSearchRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<ConsentSearchResponse, Errors> searchConsentsByParameters(String paramString1, Integer paramInteger1, String paramString2, Integer paramInteger2) {
    return start((Class)ConsentSearchResponse.class, (Class)Errors.class)
      .uri("/api/consent/search")
      .urlParameter("name", paramString1)
      .urlParameter("numberOfResults", paramInteger1)
      .urlParameter("orderBy", paramString2)
      .urlParameter("startRow", paramInteger2)
      .get()
      .go();
  }
  
  public ClientResponse<EmailTemplateSearchResponse, Errors> searchEmailTemplates(EmailTemplateSearchRequest paramEmailTemplateSearchRequest) {
    return start((Class)EmailTemplateSearchResponse.class, (Class)Errors.class)
      .uri("/api/email/template/search")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramEmailTemplateSearchRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<EntitySearchResponse, Errors> searchEntities(EntitySearchRequest paramEntitySearchRequest) {
    return start((Class)EntitySearchResponse.class, (Class)Errors.class)
      .uri("/api/entity/search")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramEntitySearchRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<EntitySearchResponse, Errors> searchEntitiesByIds(Collection<UUID> paramCollection) {
    return start((Class)EntitySearchResponse.class, (Class)Errors.class)
      .uri("/api/entity/search")
      .urlParameter("ids", paramCollection)
      .get()
      .go();
  }
  
  public ClientResponse<EntityGrantSearchResponse, Errors> searchEntityGrants(EntityGrantSearchRequest paramEntityGrantSearchRequest) {
    return start((Class)EntityGrantSearchResponse.class, (Class)Errors.class)
      .uri("/api/entity/grant/search")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramEntityGrantSearchRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<EntityGrantSearchResponse, Errors> searchEntityGrantsByParameters(UUID paramUUID1, String paramString1, UUID paramUUID2, Integer paramInteger1, String paramString2, Integer paramInteger2) {
    return start((Class)EntityGrantSearchResponse.class, (Class)Errors.class)
      .uri("/api/entity/grant/search")
      .urlParameter("entityId", paramUUID1)
      .urlParameter("name", paramString1)
      .urlParameter("userId", paramUUID2)
      .urlParameter("numberOfResults", paramInteger1)
      .urlParameter("orderBy", paramString2)
      .urlParameter("startRow", paramInteger2)
      .get()
      .go();
  }
  
  public ClientResponse<EntityTypeSearchResponse, Errors> searchEntityTypes(EntityTypeSearchRequest paramEntityTypeSearchRequest) {
    return start((Class)EntityTypeSearchResponse.class, (Class)Errors.class)
      .uri("/api/entity/type/search")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramEntityTypeSearchRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<EntityTypeSearchResponse, Errors> searchEntityTypesByParameters(String paramString1, Integer paramInteger1, String paramString2, Integer paramInteger2) {
    return start((Class)EntityTypeSearchResponse.class, (Class)Errors.class)
      .uri("/api/entity/type/search")
      .urlParameter("name", paramString1)
      .urlParameter("numberOfResults", paramInteger1)
      .urlParameter("orderBy", paramString2)
      .urlParameter("startRow", paramInteger2)
      .get()
      .go();
  }
  
  public ClientResponse<EventLogSearchResponse, Errors> searchEventLogs(EventLogSearchRequest paramEventLogSearchRequest) {
    return start((Class)EventLogSearchResponse.class, (Class)Errors.class)
      .uri("/api/system/event-log/search")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramEventLogSearchRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<GroupMemberSearchResponse, Errors> searchGroupMembers(GroupMemberSearchRequest paramGroupMemberSearchRequest) {
    return start((Class)GroupMemberSearchResponse.class, (Class)Errors.class)
      .uri("/api/group/member/search")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramGroupMemberSearchRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<GroupSearchResponse, Errors> searchGroups(GroupSearchRequest paramGroupSearchRequest) {
    return start((Class)GroupSearchResponse.class, (Class)Errors.class)
      .uri("/api/group/search")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramGroupSearchRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<IPAccessControlListSearchResponse, Errors> searchIPAccessControlLists(IPAccessControlListSearchRequest paramIPAccessControlListSearchRequest) {
    return start((Class)IPAccessControlListSearchResponse.class, (Class)Errors.class)
      .uri("/api/ip-acl/search")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramIPAccessControlListSearchRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<IPAccessControlListSearchResponse, Errors> searchIPAccessControlListsByParameters(String paramString1, Integer paramInteger1, String paramString2, Integer paramInteger2) {
    return start((Class)IPAccessControlListSearchResponse.class, (Class)Errors.class)
      .uri("/api/ip-acl/search")
      .urlParameter("name", paramString1)
      .urlParameter("numberOfResults", paramInteger1)
      .urlParameter("orderBy", paramString2)
      .urlParameter("startRow", paramInteger2)
      .get()
      .go();
  }
  
  public ClientResponse<IdentityProviderSearchResponse, Errors> searchIdentityProviders(IdentityProviderSearchRequest paramIdentityProviderSearchRequest) {
    return start((Class)IdentityProviderSearchResponse.class, (Class)Errors.class)
      .uri("/api/identity-provider/search")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramIdentityProviderSearchRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<IdentityProviderSearchResponse, Errors> searchIdentityProvidersByParameters(UUID paramUUID1, String paramString1, Integer paramInteger1, String paramString2, Integer paramInteger2, UUID paramUUID2, String paramString3) {
    return start((Class)IdentityProviderSearchResponse.class, (Class)Errors.class)
      .uri("/api/identity-provider/search")
      .urlParameter("applicationId", paramUUID1)
      .urlParameter("name", paramString1)
      .urlParameter("numberOfResults", paramInteger1)
      .urlParameter("orderBy", paramString2)
      .urlParameter("startRow", paramInteger2)
      .urlParameter("tenantId", paramUUID2)
      .urlParameter("type", paramString3)
      .get()
      .go();
  }
  
  public ClientResponse<KeySearchResponse, Errors> searchKeys(KeySearchRequest paramKeySearchRequest) {
    return start((Class)KeySearchResponse.class, (Class)Errors.class)
      .uri("/api/key/search")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramKeySearchRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<KeySearchResponse, Errors> searchKeysByParameters(String paramString1, String paramString2, Integer paramInteger1, String paramString3, Integer paramInteger2, String paramString4) {
    return start((Class)KeySearchResponse.class, (Class)Errors.class)
      .uri("/api/key/search")
      .urlParameter("algorithm", paramString1)
      .urlParameter("name", paramString2)
      .urlParameter("numberOfResults", paramInteger1)
      .urlParameter("orderBy", paramString3)
      .urlParameter("startRow", paramInteger2)
      .urlParameter("type", paramString4)
      .get()
      .go();
  }
  
  public ClientResponse<LambdaSearchResponse, Errors> searchLambdas(LambdaSearchRequest paramLambdaSearchRequest) {
    return start((Class)LambdaSearchResponse.class, (Class)Errors.class)
      .uri("/api/lambda/search")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramLambdaSearchRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<LoginRecordSearchResponse, Errors> searchLoginRecords(LoginRecordSearchRequest paramLoginRecordSearchRequest) {
    return start((Class)LoginRecordSearchResponse.class, (Class)Errors.class)
      .uri("/api/system/login-record/search")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramLoginRecordSearchRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<TenantSearchResponse, Errors> searchTenants(TenantSearchRequest paramTenantSearchRequest) {
    return start((Class)TenantSearchResponse.class, (Class)Errors.class)
      .uri("/api/tenant/search")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramTenantSearchRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<ThemeSearchResponse, Errors> searchThemes(ThemeSearchRequest paramThemeSearchRequest) {
    return start((Class)ThemeSearchResponse.class, (Class)Errors.class)
      .uri("/api/theme/search")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramThemeSearchRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<UserCommentSearchResponse, Errors> searchUserComments(UserCommentSearchRequest paramUserCommentSearchRequest) {
    return start((Class)UserCommentSearchResponse.class, (Class)Errors.class)
      .uri("/api/user/comment/search")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramUserCommentSearchRequest, objectMapper()))
      .post()
      .go();
  }
  
  @Deprecated
  public ClientResponse<SearchResponse, Errors> searchUsers(Collection<UUID> paramCollection) {
    return start((Class)SearchResponse.class, (Class)Errors.class)
      .uri("/api/user/search")
      .urlParameter("ids", paramCollection)
      .get()
      .go();
  }
  
  public ClientResponse<SearchResponse, Errors> searchUsersByIds(Collection<UUID> paramCollection) {
    return start((Class)SearchResponse.class, (Class)Errors.class)
      .uri("/api/user/search")
      .urlParameter("ids", paramCollection)
      .get()
      .go();
  }
  
  public ClientResponse<SearchResponse, Errors> searchUsersByQuery(SearchRequest paramSearchRequest) {
    return start((Class)SearchResponse.class, (Class)Errors.class)
      .uri("/api/user/search")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramSearchRequest, objectMapper()))
      .post()
      .go();
  }
  
  @Deprecated
  public ClientResponse<SearchResponse, Errors> searchUsersByQueryString(SearchRequest paramSearchRequest) {
    return start((Class)SearchResponse.class, (Class)Errors.class)
      .uri("/api/user/search")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramSearchRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<WebhookEventLogSearchResponse, Errors> searchWebhookEventLogs(WebhookEventLogSearchRequest paramWebhookEventLogSearchRequest) {
    return start((Class)WebhookEventLogSearchResponse.class, (Class)Errors.class)
      .uri("/api/system/webhook-event-log/search")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramWebhookEventLogSearchRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<WebhookSearchResponse, Errors> searchWebhooks(WebhookSearchRequest paramWebhookSearchRequest) {
    return start((Class)WebhookSearchResponse.class, (Class)Errors.class)
      .uri("/api/webhook/search")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramWebhookSearchRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<WebhookSearchResponse, Errors> searchWebhooksByParameters(String paramString1, Integer paramInteger1, String paramString2, Integer paramInteger2, UUID paramUUID, String paramString3) {
    return start((Class)WebhookSearchResponse.class, (Class)Errors.class)
      .uri("/api/webhook/search")
      .urlParameter("description", paramString1)
      .urlParameter("numberOfResults", paramInteger1)
      .urlParameter("orderBy", paramString2)
      .urlParameter("startRow", paramInteger2)
      .urlParameter("tenantId", paramUUID)
      .urlParameter("url", paramString3)
      .get()
      .go();
  }
  
  public ClientResponse<SendResponse, Errors> sendEmail(UUID paramUUID, SendRequest paramSendRequest) {
    return start((Class)SendResponse.class, (Class)Errors.class)
      .uri("/api/email/send")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramSendRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<Void, Errors> sendFamilyRequestEmail(FamilyEmailRequest paramFamilyEmailRequest) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/user/family/request")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramFamilyEmailRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<Void, Errors> sendPasswordlessCode(PasswordlessSendRequest paramPasswordlessSendRequest) {
    return startAnonymous((Class)void.class, (Class)Errors.class)
      .uri("/api/passwordless/send")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramPasswordlessSendRequest, objectMapper()))
      .post()
      .go();
  }
  
  @Deprecated
  public ClientResponse<Void, Errors> sendTwoFactorCode(TwoFactorSendRequest paramTwoFactorSendRequest) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/two-factor/send")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramTwoFactorSendRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<Void, Errors> sendTwoFactorCodeForEnableDisable(TwoFactorSendRequest paramTwoFactorSendRequest) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/two-factor/send")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramTwoFactorSendRequest, objectMapper()))
      .post()
      .go();
  }
  
  @Deprecated
  public ClientResponse<Void, Errors> sendTwoFactorCodeForLogin(String paramString) {
    return startAnonymous((Class)void.class, (Class)Errors.class)
      .uri("/api/two-factor/send")
      .urlSegment(paramString)
      .post()
      .go();
  }
  
  public ClientResponse<Void, Errors> sendTwoFactorCodeForLoginUsingMethod(String paramString, TwoFactorSendRequest paramTwoFactorSendRequest) {
    return startAnonymous((Class)void.class, (Class)Errors.class)
      .uri("/api/two-factor/send")
      .urlSegment(paramString)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramTwoFactorSendRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<Void, Errors> sendVerifyIdentity(VerifySendRequest paramVerifySendRequest) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/identity/verify/send")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramVerifySendRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<IdentityProviderConnectionTestResponse, Errors> startIdentityProviderConnectionTest(IdentityProviderConnectionTestRequest paramIdentityProviderConnectionTestRequest) {
    return start((Class)IdentityProviderConnectionTestResponse.class, (Class)Errors.class)
      .uri("/api/identity-provider/test")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramIdentityProviderConnectionTestRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<IdentityProviderStartLoginResponse, Errors> startIdentityProviderLogin(IdentityProviderStartLoginRequest paramIdentityProviderStartLoginRequest) {
    return start((Class)IdentityProviderStartLoginResponse.class, (Class)Errors.class)
      .uri("/api/identity-provider/start")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramIdentityProviderStartLoginRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<PasswordlessStartResponse, Errors> startPasswordlessLogin(PasswordlessStartRequest paramPasswordlessStartRequest) {
    return start((Class)PasswordlessStartResponse.class, (Class)Errors.class)
      .uri("/api/passwordless/start")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramPasswordlessStartRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<TwoFactorStartResponse, Errors> startTwoFactorLogin(TwoFactorStartRequest paramTwoFactorStartRequest) {
    return start((Class)TwoFactorStartResponse.class, (Class)Errors.class)
      .uri("/api/two-factor/start")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramTwoFactorStartRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<VerifyStartResponse, Errors> startVerifyIdentity(VerifyStartRequest paramVerifyStartRequest) {
    return start((Class)VerifyStartResponse.class, (Class)Errors.class)
      .uri("/api/identity/verify/start")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramVerifyStartRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<WebAuthnStartResponse, Errors> startWebAuthnLogin(WebAuthnStartRequest paramWebAuthnStartRequest) {
    return start((Class)WebAuthnStartResponse.class, (Class)Errors.class)
      .uri("/api/webauthn/start")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramWebAuthnStartRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<WebAuthnRegisterStartResponse, Errors> startWebAuthnRegistration(WebAuthnRegisterStartRequest paramWebAuthnRegisterStartRequest) {
    return start((Class)WebAuthnRegisterStartResponse.class, (Class)Errors.class)
      .uri("/api/webauthn/register/start")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramWebAuthnRegisterStartRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<LoginResponse, Errors> twoFactorLogin(TwoFactorLoginRequest paramTwoFactorLoginRequest) {
    return startAnonymous((Class)LoginResponse.class, (Class)Errors.class)
      .uri("/api/two-factor/login")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramTwoFactorLoginRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<APIKeyResponse, Errors> updateAPIKey(UUID paramUUID, APIKeyRequest paramAPIKeyRequest) {
    return start((Class)APIKeyResponse.class, (Class)Errors.class)
      .uri("/api/api-key")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramAPIKeyRequest, objectMapper()))
      .put()
      .go();
  }
  
  public ClientResponse<ApplicationResponse, Errors> updateApplication(UUID paramUUID, ApplicationRequest paramApplicationRequest) {
    return start((Class)ApplicationResponse.class, (Class)Errors.class)
      .uri("/api/application")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramApplicationRequest, objectMapper()))
      .put()
      .go();
  }
  
  public ClientResponse<ApplicationResponse, Errors> updateApplicationRole(UUID paramUUID1, UUID paramUUID2, ApplicationRequest paramApplicationRequest) {
    return start((Class)ApplicationResponse.class, (Class)Errors.class)
      .uri("/api/application")
      .urlSegment(paramUUID1)
      .urlSegment("role")
      .urlSegment(paramUUID2)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramApplicationRequest, objectMapper()))
      .put()
      .go();
  }
  
  public ClientResponse<ConnectorResponse, Errors> updateConnector(UUID paramUUID, ConnectorRequest paramConnectorRequest) {
    return start((Class)ConnectorResponse.class, (Class)Errors.class)
      .uri("/api/connector")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramConnectorRequest, objectMapper()))
      .put()
      .go();
  }
  
  public ClientResponse<ConsentResponse, Errors> updateConsent(UUID paramUUID, ConsentRequest paramConsentRequest) {
    return start((Class)ConsentResponse.class, (Class)Errors.class)
      .uri("/api/consent")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramConsentRequest, objectMapper()))
      .put()
      .go();
  }
  
  public ClientResponse<EmailTemplateResponse, Errors> updateEmailTemplate(UUID paramUUID, EmailTemplateRequest paramEmailTemplateRequest) {
    return start((Class)EmailTemplateResponse.class, (Class)Errors.class)
      .uri("/api/email/template")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramEmailTemplateRequest, objectMapper()))
      .put()
      .go();
  }
  
  public ClientResponse<EntityResponse, Errors> updateEntity(UUID paramUUID, EntityRequest paramEntityRequest) {
    return start((Class)EntityResponse.class, (Class)Errors.class)
      .uri("/api/entity")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramEntityRequest, objectMapper()))
      .put()
      .go();
  }
  
  public ClientResponse<EntityTypeResponse, Errors> updateEntityType(UUID paramUUID, EntityTypeRequest paramEntityTypeRequest) {
    return start((Class)EntityTypeResponse.class, (Class)Errors.class)
      .uri("/api/entity/type")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramEntityTypeRequest, objectMapper()))
      .put()
      .go();
  }
  
  public ClientResponse<EntityTypeResponse, Errors> updateEntityTypePermission(UUID paramUUID1, UUID paramUUID2, EntityTypeRequest paramEntityTypeRequest) {
    return start((Class)EntityTypeResponse.class, (Class)Errors.class)
      .uri("/api/entity/type")
      .urlSegment(paramUUID1)
      .urlSegment("permission")
      .urlSegment(paramUUID2)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramEntityTypeRequest, objectMapper()))
      .put()
      .go();
  }
  
  public ClientResponse<FamilyResponse, Errors> updateFamily(UUID paramUUID, FamilyRequest paramFamilyRequest) {
    return start((Class)FamilyResponse.class, (Class)Errors.class)
      .uri("/api/user/family")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramFamilyRequest, objectMapper()))
      .put()
      .go();
  }
  
  public ClientResponse<FormResponse, Errors> updateForm(UUID paramUUID, FormRequest paramFormRequest) {
    return start((Class)FormResponse.class, (Class)Errors.class)
      .uri("/api/form")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramFormRequest, objectMapper()))
      .put()
      .go();
  }
  
  public ClientResponse<FormFieldResponse, Errors> updateFormField(UUID paramUUID, FormFieldRequest paramFormFieldRequest) {
    return start((Class)FormFieldResponse.class, (Class)Errors.class)
      .uri("/api/form/field")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramFormFieldRequest, objectMapper()))
      .put()
      .go();
  }
  
  public ClientResponse<GroupResponse, Errors> updateGroup(UUID paramUUID, GroupRequest paramGroupRequest) {
    return start((Class)GroupResponse.class, (Class)Errors.class)
      .uri("/api/group")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramGroupRequest, objectMapper()))
      .put()
      .go();
  }
  
  public ClientResponse<MemberResponse, Errors> updateGroupMembers(MemberRequest paramMemberRequest) {
    return start((Class)MemberResponse.class, (Class)Errors.class)
      .uri("/api/group/member")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramMemberRequest, objectMapper()))
      .put()
      .go();
  }
  
  public ClientResponse<IPAccessControlListResponse, Errors> updateIPAccessControlList(UUID paramUUID, IPAccessControlListRequest paramIPAccessControlListRequest) {
    return start((Class)IPAccessControlListResponse.class, (Class)Errors.class)
      .uri("/api/ip-acl")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramIPAccessControlListRequest, objectMapper()))
      .put()
      .go();
  }
  
  public ClientResponse<IdentityProviderResponse, Errors> updateIdentityProvider(UUID paramUUID, IdentityProviderRequest paramIdentityProviderRequest) {
    return start((Class)IdentityProviderResponse.class, (Class)Errors.class)
      .uri("/api/identity-provider")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramIdentityProviderRequest, objectMapper()))
      .put()
      .go();
  }
  
  public ClientResponse<IntegrationResponse, Errors> updateIntegrations(IntegrationRequest paramIntegrationRequest) {
    return start((Class)IntegrationResponse.class, (Class)Errors.class)
      .uri("/api/integration")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramIntegrationRequest, objectMapper()))
      .put()
      .go();
  }
  
  public ClientResponse<KeyResponse, Errors> updateKey(UUID paramUUID, KeyRequest paramKeyRequest) {
    return start((Class)KeyResponse.class, (Class)Errors.class)
      .uri("/api/key")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramKeyRequest, objectMapper()))
      .put()
      .go();
  }
  
  public ClientResponse<LambdaResponse, Errors> updateLambda(UUID paramUUID, LambdaRequest paramLambdaRequest) {
    return start((Class)LambdaResponse.class, (Class)Errors.class)
      .uri("/api/lambda")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramLambdaRequest, objectMapper()))
      .put()
      .go();
  }
  
  public ClientResponse<MessageTemplateResponse, Errors> updateMessageTemplate(UUID paramUUID, MessageTemplateRequest paramMessageTemplateRequest) {
    return start((Class)MessageTemplateResponse.class, (Class)Errors.class)
      .uri("/api/message/template")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramMessageTemplateRequest, objectMapper()))
      .put()
      .go();
  }
  
  public ClientResponse<MessengerResponse, Errors> updateMessenger(UUID paramUUID, MessengerRequest paramMessengerRequest) {
    return start((Class)MessengerResponse.class, (Class)Errors.class)
      .uri("/api/messenger")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramMessengerRequest, objectMapper()))
      .put()
      .go();
  }
  
  public ClientResponse<ApplicationOAuthScopeResponse, Errors> updateOAuthScope(UUID paramUUID1, UUID paramUUID2, ApplicationOAuthScopeRequest paramApplicationOAuthScopeRequest) {
    return start((Class)ApplicationOAuthScopeResponse.class, (Class)Errors.class)
      .uri("/api/application")
      .urlSegment(paramUUID1)
      .urlSegment("scope")
      .urlSegment(paramUUID2)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramApplicationOAuthScopeRequest, objectMapper()))
      .put()
      .go();
  }
  
  public ClientResponse<RegistrationResponse, Errors> updateRegistration(UUID paramUUID, RegistrationRequest paramRegistrationRequest) {
    return start((Class)RegistrationResponse.class, (Class)Errors.class)
      .uri("/api/user/registration")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramRegistrationRequest, objectMapper()))
      .put()
      .go();
  }
  
  public ClientResponse<SystemConfigurationResponse, Errors> updateSystemConfiguration(SystemConfigurationRequest paramSystemConfigurationRequest) {
    return start((Class)SystemConfigurationResponse.class, (Class)Errors.class)
      .uri("/api/system-configuration")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramSystemConfigurationRequest, objectMapper()))
      .put()
      .go();
  }
  
  public ClientResponse<TenantResponse, Errors> updateTenant(UUID paramUUID, TenantRequest paramTenantRequest) {
    return start((Class)TenantResponse.class, (Class)Errors.class)
      .uri("/api/tenant")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramTenantRequest, objectMapper()))
      .put()
      .go();
  }
  
  public ClientResponse<TenantManagerConfigurationResponse, Errors> updateTenantManagerConfiguration(TenantManagerConfigurationRequest paramTenantManagerConfigurationRequest) {
    return start((Class)TenantManagerConfigurationResponse.class, (Class)Errors.class)
      .uri("/api/tenant-manager")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramTenantManagerConfigurationRequest, objectMapper()))
      .put()
      .go();
  }
  
  public ClientResponse<TenantManagerIdentityProviderTypeConfigurationResponse, Errors> updateTenantManagerIdentityProviderTypeConfiguration(IdentityProviderType paramIdentityProviderType, TenantManagerIdentityProviderTypeConfigurationRequest paramTenantManagerIdentityProviderTypeConfigurationRequest) {
    return start((Class)TenantManagerIdentityProviderTypeConfigurationResponse.class, (Class)Errors.class)
      .uri("/api/tenant-manager/identity-provider")
      .urlSegment(paramIdentityProviderType)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramTenantManagerIdentityProviderTypeConfigurationRequest, objectMapper()))
      .put()
      .go();
  }
  
  public ClientResponse<ThemeResponse, Errors> updateTheme(UUID paramUUID, ThemeRequest paramThemeRequest) {
    return start((Class)ThemeResponse.class, (Class)Errors.class)
      .uri("/api/theme")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramThemeRequest, objectMapper()))
      .put()
      .go();
  }
  
  public ClientResponse<Void, Errors> updateTwoFactor(UUID paramUUID, TwoFactorUpdateRequest paramTwoFactorUpdateRequest) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/user/two-factor")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramTwoFactorUpdateRequest, objectMapper()))
      .put()
      .go();
  }
  
  public ClientResponse<UserResponse, Errors> updateUser(UUID paramUUID, UserRequest paramUserRequest) {
    return start((Class)UserResponse.class, (Class)Errors.class)
      .uri("/api/user")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramUserRequest, objectMapper()))
      .put()
      .go();
  }
  
  public ClientResponse<UserActionResponse, Errors> updateUserAction(UUID paramUUID, UserActionRequest paramUserActionRequest) {
    return start((Class)UserActionResponse.class, (Class)Errors.class)
      .uri("/api/user-action")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramUserActionRequest, objectMapper()))
      .put()
      .go();
  }
  
  public ClientResponse<UserActionReasonResponse, Errors> updateUserActionReason(UUID paramUUID, UserActionReasonRequest paramUserActionReasonRequest) {
    return start((Class)UserActionReasonResponse.class, (Class)Errors.class)
      .uri("/api/user-action-reason")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramUserActionReasonRequest, objectMapper()))
      .put()
      .go();
  }
  
  public ClientResponse<UserConsentResponse, Errors> updateUserConsent(UUID paramUUID, UserConsentRequest paramUserConsentRequest) {
    return start((Class)UserConsentResponse.class, (Class)Errors.class)
      .uri("/api/user/consent")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramUserConsentRequest, objectMapper()))
      .put()
      .go();
  }
  
  public ClientResponse<WebhookResponse, Errors> updateWebhook(UUID paramUUID, WebhookRequest paramWebhookRequest) {
    return start((Class)WebhookResponse.class, (Class)Errors.class)
      .uri("/api/webhook")
      .urlSegment(paramUUID)
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramWebhookRequest, objectMapper()))
      .put()
      .go();
  }
  
  public ClientResponse<Void, Errors> upsertEntityGrant(UUID paramUUID, EntityGrantRequest paramEntityGrantRequest) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/entity")
      .urlSegment(paramUUID)
      .urlSegment("grant")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramEntityGrantRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<Void, Void> validateDevice(String paramString1, String paramString2) {
    return startAnonymous((Class)void.class, (Class)void.class)
      .uri("/oauth2/device/validate")
      .urlParameter("user_code", paramString1)
      .urlParameter("client_id", paramString2)
      .get()
      .go();
  }
  
  public ClientResponse<Void, Void> validateDeviceWithRequest(ValidateDeviceRequest paramValidateDeviceRequest) {
    return startAnonymous((Class)void.class, (Class)void.class)
      .uri("/oauth2/device/validate")
      .urlParameter("client_id", paramValidateDeviceRequest.client_id)
      .urlParameter("tenantId", (paramValidateDeviceRequest.tenantId != null) ? paramValidateDeviceRequest.tenantId.toString() : null)
      .urlParameter("user_code", paramValidateDeviceRequest.user_code)
      .get()
      .go();
  }
  
  public ClientResponse<ValidateResponse, Void> validateJWT(String paramString) {
    return startAnonymous((Class)ValidateResponse.class, (Class)void.class)
      .uri("/api/jwt/validate")
      .authorization("Bearer " + paramString)
      .get()
      .go();
  }
  
  public ClientResponse<JWTVendResponse, Errors> vendJWT(JWTVendRequest paramJWTVendRequest) {
    return start((Class)JWTVendResponse.class, (Class)Errors.class)
      .uri("/api/jwt/vend")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramJWTVendRequest, objectMapper()))
      .post()
      .go();
  }
  
  @Deprecated
  public ClientResponse<Void, Errors> verifyEmail(String paramString) {
    return startAnonymous((Class)void.class, (Class)Errors.class)
      .uri("/api/user/verify-email")
      .urlSegment(paramString)
      .post()
      .go();
  }
  
  public ClientResponse<Void, Errors> verifyEmailAddress(VerifyEmailRequest paramVerifyEmailRequest) {
    return startAnonymous((Class)void.class, (Class)Errors.class)
      .uri("/api/user/verify-email")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramVerifyEmailRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<Void, Errors> verifyEmailAddressByUserId(VerifyEmailRequest paramVerifyEmailRequest) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/user/verify-email")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramVerifyEmailRequest, objectMapper()))
      .post()
      .go();
  }
  
  public ClientResponse<Void, Errors> verifyIdentity(VerifyRequest paramVerifyRequest) {
    return start((Class)void.class, (Class)Errors.class)
      .uri("/api/identity/verify")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramVerifyRequest, objectMapper()))
      .post()
      .go();
  }
  
  @Deprecated
  public ClientResponse<Void, Errors> verifyRegistration(String paramString) {
    return startAnonymous((Class)void.class, (Class)Errors.class)
      .uri("/api/user/verify-registration")
      .urlSegment(paramString)
      .post()
      .go();
  }
  
  public ClientResponse<Void, Errors> verifyUserRegistration(VerifyRegistrationRequest paramVerifyRegistrationRequest) {
    return startAnonymous((Class)void.class, (Class)Errors.class)
      .uri("/api/user/verify-registration")
      .bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(paramVerifyRegistrationRequest, objectMapper()))
      .post()
      .go();
  }
  
  protected <T, U> RESTClient<T, U> start(Class<T> paramClass, Class<U> paramClass1) {
    return startAnonymous(paramClass, paramClass1).authorization(this.apiKey);
  }
  
  protected <T, U> RESTClient<T, U> startAnonymous(Class<T> paramClass, Class<U> paramClass1) {
    RESTClient<T, U> rESTClient = (new RESTClient(paramClass, paramClass1)).successResponseHandler((paramClass != void.class) ? (RESTClient.ResponseHandler)new JSONResponseHandler(paramClass, objectMapper()) : null).errorResponseHandler((paramClass1 != void.class) ? (RESTClient.ResponseHandler)new JSONResponseHandler(paramClass1, objectMapper()) : null).url(this.baseURL).connectTimeout(this.connectTimeout).readTimeout(this.readTimeout);
    if (this.tenantId != null)
      rESTClient.header(TENANT_ID_HEADER, this.tenantId); 
    if (this.retryConfiguration != null)
      rESTClient.retry(this.retryConfiguration); 
    return rESTClient;
  }
  
  private ObjectMapper objectMapper() {
    return (this.customMapper != null) ? this.customMapper : objectMapper;
  }
}
