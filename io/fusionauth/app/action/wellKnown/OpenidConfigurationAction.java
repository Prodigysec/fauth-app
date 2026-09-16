package io.fusionauth.app.action.wellKnown;

import com.google.inject.Inject;
import io.fusionauth.api.domain.guice.FusionAuthTenantId;
import io.fusionauth.api.service.cache.TenantCache;
import io.fusionauth.api.service.system.TenantReaderService;
import io.fusionauth.app.action.BaseFasterAction;
import io.fusionauth.domain.OpenIdConfiguration;
import io.fusionauth.domain.Tenant;
import io.fusionauth.http.server.HTTPRequest;
import java.util.Objects;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.JSON;
import org.primeframework.mvc.content.json.annotation.JSONResponse;

@Action("{tenantId}")
@JSON(code = "render", status = 200)
public class OpenidConfigurationAction extends BaseFasterAction {
  private final UUID fusionauthTenantId;
  
  private final HTTPRequest request;
  
  private final TenantCache tenantCache;
  
  private final TenantReaderService tenantReader;
  
  @JSONResponse(prettyPrint = true)
  public OpenIdConfiguration response;
  
  @Inject
  public OpenidConfigurationAction(@FusionAuthTenantId UUID paramUUID, HTTPRequest paramHTTPRequest, TenantCache paramTenantCache, TenantReaderService paramTenantReaderService) {
    this.fusionauthTenantId = paramUUID;
    this.tenantCache = paramTenantCache;
    this.tenantReader = paramTenantReaderService;
    this.request = paramHTTPRequest;
  }
  
  public String get() {
    String str = this.request.getBaseURL();
    Objects.requireNonNull(this.tenantReader);
    Objects.requireNonNull(this.tenantReader);
    Tenant tenant = (this.tenantId == null) ? this.tenantCache.get(this.fusionauthTenantId, this.tenantReader::retrieveById) : this.tenantCache.get(this.tenantId, this.tenantReader::retrieveById);
    if (tenant == null)
      return "missing"; 
    this





      
      .response = (new OpenIdConfiguration()).with(paramOpenIdConfiguration -> paramOpenIdConfiguration.authorization_endpoint = String.format(paramOpenIdConfiguration.authorization_endpoint, new Object[] { paramString })).with(paramOpenIdConfiguration -> paramOpenIdConfiguration.device_authorization_endpoint = String.format(paramOpenIdConfiguration.device_authorization_endpoint, new Object[] { paramString })).with(paramOpenIdConfiguration -> paramOpenIdConfiguration.end_session_endpoint = String.format(paramOpenIdConfiguration.end_session_endpoint, new Object[] { paramString })).with(paramOpenIdConfiguration -> paramOpenIdConfiguration.issuer = paramTenant.issuer).with(paramOpenIdConfiguration -> paramOpenIdConfiguration.jwks_uri = String.format(paramOpenIdConfiguration.jwks_uri, new Object[] { paramString })).with(paramOpenIdConfiguration -> paramOpenIdConfiguration.token_endpoint = String.format(paramOpenIdConfiguration.token_endpoint, new Object[] { paramString })).with(paramOpenIdConfiguration -> paramOpenIdConfiguration.userinfo_endpoint = String.format(paramOpenIdConfiguration.userinfo_endpoint, new Object[] { paramString }));
    return "render";
  }
}
