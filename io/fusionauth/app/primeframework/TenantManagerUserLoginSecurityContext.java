package io.fusionauth.app.primeframework;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.inversoft.util.StringTools;
import io.fusionauth.api.domain.guice.FusionAuthClientProvider;
import io.fusionauth.api.domain.guice.FusionAuthLocalClientURL;
import io.fusionauth.api.service.cache.ApplicationCache;
import io.fusionauth.api.service.cache.TenantCache;
import io.fusionauth.api.service.jwt.JWTClaimValidator;
import io.fusionauth.api.service.jwt.RefreshTokenService;
import io.fusionauth.api.service.system.ApplicationReaderService;
import io.fusionauth.api.service.system.TenantReaderService;
import io.fusionauth.api.util.ActionTools;
import io.fusionauth.app.guice.TenantManagerApplicationId;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.http.server.HTTPRequest;
import io.fusionauth.http.server.HTTPResponse;
import java.util.UUID;
import org.primeframework.mvc.security.VerifierProvider;

public class TenantManagerUserLoginSecurityContext extends FusionAuthUserLoginSecurityContext {
  private final FusionAuthClientProvider clientProvider;
  
  private final UUID tenantManagerApplicationId;
  
  private UUID tenantId;
  
  @Inject
  protected TenantManagerUserLoginSecurityContext(HTTPRequest paramHTTPRequest, HTTPResponse paramHTTPResponse, VerifierProvider paramVerifierProvider, @Named("TenantManagerFusionAuthClientProvider") FusionAuthClientProvider paramFusionAuthClientProvider, FusionAuthClient paramFusionAuthClient, ApplicationCache paramApplicationCache, ApplicationReaderService paramApplicationReaderService, @FusionAuthLocalClientURL String paramString, @TenantManagerApplicationId UUID paramUUID, TenantCache paramTenantCache, TenantReaderService paramTenantReaderService, RefreshTokenService paramRefreshTokenService, JWTClaimValidator paramJWTClaimValidator) {
    super(paramHTTPRequest, paramHTTPResponse, paramVerifierProvider, paramApplicationCache, paramApplicationReaderService, paramFusionAuthClient, paramString, paramRefreshTokenService, paramTenantCache, paramTenantReaderService, (UUID)null, paramJWTClaimValidator);
    this.clientProvider = paramFusionAuthClientProvider;
    this.tenantManagerApplicationId = paramUUID;
  }
  
  protected UUID getApplicationId() {
    return this.tenantManagerApplicationId;
  }
  
  protected FusionAuthClient getClient() {
    return this.clientProvider.get(this.tenantId);
  }
  
  protected UUID getTenantId() {
    if (this.tenantId == null)
      this
        .tenantId = ActionTools.resolveTenantIdFromHeader(this.request).orElse(StringTools.parseUUID(this.request.getParameter("tenantId"))); 
    return this.tenantId;
  }
  
  protected boolean isUniversalApplication() {
    return true;
  }
  
  protected String jwtCookieName() {
    return "tm.at";
  }
  
  protected String refreshTokenCookieName() {
    return "tm.rt";
  }
}
