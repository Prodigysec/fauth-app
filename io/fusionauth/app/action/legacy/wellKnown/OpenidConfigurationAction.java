package io.fusionauth.app.action.legacy.wellKnown;

import com.google.inject.Inject;
import io.fusionauth.api.configuration.FusionAuthConfiguration;
import io.fusionauth.api.domain.guice.FusionAuthTenantId;
import io.fusionauth.api.service.cache.TenantCache;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.api.service.system.TenantReaderService;
import io.fusionauth.domain.OpenIdConfiguration;
import io.fusionauth.domain.Tenant;
import io.fusionauth.http.server.HTTPRequest;
import java.util.Objects;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.JSON;
import org.primeframework.mvc.action.result.annotation.JSON.List;
import org.primeframework.mvc.action.result.annotation.Status;
import org.primeframework.mvc.action.result.annotation.Status.List;
import org.primeframework.mvc.content.json.annotation.JSONResponse;

@Action("{tenantId}")
@List({@JSON(code = "render", status = 200), @JSON(code = "input", status = 400)})
@List({@Status(code = "disabled", status = 404), @Status(code = "missing", status = 404)})
public class OpenidConfigurationAction extends BaseLegacyWellKnownAction {
  private final UUID fusionauthTenantId;
  
  private final HTTPRequest request;
  
  private final TenantCache tenantCache;
  
  private final TenantReaderService tenantReader;
  
  @JSONResponse(prettyPrint = true)
  public Object response;
  
  @Inject
  public OpenidConfigurationAction(FusionAuthConfiguration paramFusionAuthConfiguration, @FusionAuthTenantId UUID paramUUID, HTTPRequest paramHTTPRequest, ReactorStatusService paramReactorStatusService, TenantCache paramTenantCache, TenantReaderService paramTenantReaderService) {
    super(paramFusionAuthConfiguration, paramReactorStatusService);
    this.fusionauthTenantId = paramUUID;
    this.request = paramHTTPRequest;
    this.tenantCache = paramTenantCache;
    this.tenantReader = paramTenantReaderService;
  }
  
  public String get() {
    if (isDisabled())
      return "disabled"; 
    if (isNotLicensed()) {
      this.response = buildNotLicensedError();
      return "input";
    } 
    String str = this.request.getBaseURL();
    Objects.requireNonNull(this.tenantReader);
    Objects.requireNonNull(this.tenantReader);
    Tenant tenant = (this.tenantId == null) ? this.tenantCache.get(this.fusionauthTenantId, this.tenantReader::retrieveById) : this.tenantCache.get(this.tenantId, this.tenantReader::retrieveById);
    if (tenant == null)
      return "missing"; 
    this





      
      .response = (new OpenIdConfiguration()).with(paramOpenIdConfiguration -> paramOpenIdConfiguration.authorization_endpoint = String.format(paramOpenIdConfiguration.authorization_endpoint, new Object[] { paramString })).with(paramOpenIdConfiguration -> paramOpenIdConfiguration.device_authorization_endpoint = String.format(paramOpenIdConfiguration.device_authorization_endpoint, new Object[] { paramString })).with(paramOpenIdConfiguration -> paramOpenIdConfiguration.end_session_endpoint = String.format(paramOpenIdConfiguration.end_session_endpoint, new Object[] { paramString })).with(paramOpenIdConfiguration -> paramOpenIdConfiguration.issuer = paramTenant.issuer).with(paramOpenIdConfiguration -> paramOpenIdConfiguration.jwks_uri = paramString + "/legacy/.well-known/jwks.json").with(paramOpenIdConfiguration -> paramOpenIdConfiguration.token_endpoint = paramString + "/legacy/oauth2/token").with(paramOpenIdConfiguration -> paramOpenIdConfiguration.userinfo_endpoint = paramString + "/legacy/oauth2/userinfo");
    return "render";
  }
}
