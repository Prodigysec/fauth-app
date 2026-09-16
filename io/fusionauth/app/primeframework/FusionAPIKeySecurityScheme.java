package io.fusionauth.app.primeframework;

import com.google.inject.Inject;
import com.inversoft.authentication.api.domain.AuthenticationKey;
import com.inversoft.authentication.api.domain.LocalAuthenticationKey;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import com.inversoft.authentication.api.service.AuthenticationKeySecurityScheme;
import io.fusionauth.api.configuration.FusionAuthConfiguration;
import io.fusionauth.api.domain.TenantRequest;
import io.fusionauth.api.domain.guice.FusionAuthLocalURLProvider;
import io.fusionauth.api.service.cache.IPAccessControlListCache;
import io.fusionauth.api.service.cache.SystemConfigurationCache;
import io.fusionauth.api.service.system.APIKeyService;
import io.fusionauth.api.util.NetworkTools;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.http.server.HTTPRequest;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.primeframework.mvc.action.ActionInvocationStore;
import org.primeframework.mvc.security.UnauthorizedException;

public class FusionAPIKeySecurityScheme extends AuthenticationKeySecurityScheme {
  private final FusionAuthConfiguration configuration;
  
  private final FusionAuthLocalURLProvider fusionAuthLocalURLProvider;
  
  private final IPAccessControlListCache ipAccessControlListCache;
  
  private final SystemConfigurationCache systemConfigurationCache;
  
  @Inject
  public FusionAPIKeySecurityScheme(ActionInvocationStore paramActionInvocationStore, HTTPRequest paramHTTPRequest, AuthenticationKeyCache paramAuthenticationKeyCache, FusionAuthConfiguration paramFusionAuthConfiguration, FusionAuthLocalURLProvider paramFusionAuthLocalURLProvider, IPAccessControlListCache paramIPAccessControlListCache, SystemConfigurationCache paramSystemConfigurationCache) {
    super(paramActionInvocationStore, paramHTTPRequest, paramAuthenticationKeyCache);
    this.configuration = paramFusionAuthConfiguration;
    this.fusionAuthLocalURLProvider = paramFusionAuthLocalURLProvider;
    this.ipAccessControlListCache = paramIPAccessControlListCache;
    this.systemConfigurationCache = paramSystemConfigurationCache;
  }
  
  public void handle(String[] paramArrayOfString) {
    super.handle(paramArrayOfString);
    String str1 = authenticationKey();
    AuthenticationKey authenticationKey = getAuthenticationKey(str1);
    if (authenticationKey instanceof LocalAuthenticationKey) {
      if (this.request.getHeader("X-Forwarded-Host") != null || this.request.getHeader("X-Forwarded-Port") != null)
        throw new UnauthorizedException(); 
      Objects.requireNonNull(this.fusionAuthLocalURLProvider);
      String str3 = "localhost";
      int i = this.fusionAuthLocalURLProvider.port;
      if (!str3.equals(this.request.getRawHost()) || this.request.getRawPort() != i)
        throw new UnauthorizedException(); 
      String str4 = this.request.getRawIPAddress();
      if (!NetworkTools.isLoopbackAddress(str4))
        throw new UnauthorizedException(); 
    } 
    String str2 = NetworkTools.getTrustedClientIPAddress(this.request, this.configuration, this.systemConfigurationCache.get());
    if (this.ipAccessControlListCache.isBlocked(authenticationKey.ipAccessControlListId, str2))
      throw new UnauthorizedException(); 
    UUID uUID = getTenantIdFromRequestHeader();
    if (authenticationKey.tenantId != null && uUID != null && 
      !authenticationKey.tenantId.equals(uUID))
      throw new UnauthorizedException(); 
    if (uUID != null) {
      this.request.setAttribute(TenantRequest.KEY, uUID);
    } else if (authenticationKey.tenantId != null) {
      this.request.setAttribute(TenantRequest.KEY, authenticationKey.tenantId);
    } 
  }
  
  protected String apiKeyEndpoint() {
    return "/api/api-key";
  }
  
  protected AuthenticationKey getAuthenticationKey(String paramString) {
    if (paramString.equals(APIKeyService.TENANT_MANAGER_LOCAL_KEY))
      return (AuthenticationKey)((AuthenticationKey)(new LocalAuthenticationKey(paramString, false))
        .with(paramAuthenticationKey -> paramAuthenticationKey.permissions = new AuthenticationKey.AuthenticationPermissions()))
        .with(paramAuthenticationKey -> paramAuthenticationKey.permissions.endpoints.putAll(Map.ofEntries(new Map.Entry[] { 
                Map.entry("/api/form", Set.of("GET")), Map.entry("/api/form/field", Set.of("GET")), Map.entry("/api/system-configuration", Set.of("GET")), Map.entry("/api/application", Set.of("GET")), Map.entry("/api/system/audit-log", Set.of("POST")), Map.entry("/api/tenant", Set.of("GET")), Map.entry("/api/theme", Set.of("GET")), Map.entry("/api/user", Set.of("DELETE", "POST", "GET", "PUT", "PATCH")), Map.entry("/api/user/change-password", Set.of("DELETE", "POST", "GET", "PUT", "PATCH")), Map.entry("/api/user/forgot-password", Set.of("DELETE", "POST", "GET", "PUT", "PATCH")), 
                Map.entry("/api/user/search", Set.of("DELETE", "POST", "GET", "PUT", "PATCH")), Map.entry("/api/tenant-manager", Set.of("GET")), Map.entry("/api/identity-provider/search", Set.of("POST")), Map.entry("/api/identity-provider/test", Set.of("GET", "POST")), Map.entry("/api/identity-provider", Set.of("DELETE", "POST", "GET", "PUT", "PATCH")), Map.entry("/api/key", Set.of("GET", "DELETE")), Map.entry("/api/key/generate", Set.of("POST")), Map.entry("/api/key/import", Set.of("POST")) }))); 
    return super.getAuthenticationKey(paramString);
  }
  
  private UUID getTenantIdFromRequestHeader() {
    String str = this.request.getHeader(FusionAuthClient.TENANT_ID_HEADER);
    if (str == null)
      return null; 
    try {
      return UUID.fromString(str);
    } catch (Exception exception) {
      return null;
    } 
  }
}
