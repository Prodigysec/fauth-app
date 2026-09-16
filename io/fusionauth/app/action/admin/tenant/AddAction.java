package io.fusionauth.app.action.admin.tenant;

import com.google.inject.Inject;
import com.inversoft.cache.Cache;
import com.inversoft.rest.ClientResponse;
import com.inversoft.util.CollectionTools;
import io.fusionauth.api.domain.CachedTheme;
import io.fusionauth.api.domain.guice.FusionAuthTenantId;
import io.fusionauth.api.security.PasswordEncryptorLibrary;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.license.FrontEndLicensedFeaturesService;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.EmailHeader;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.api.TenantRequest;
import io.fusionauth.domain.api.TenantResponse;
import java.util.UUID;
import java.util.stream.Collectors;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;
import org.primeframework.mvc.validation.annotation.PostValidationMethod;

@Action(requiresAuthentication = true, constraints = {"admin", "tenant_manager"})
@List({@Redirect(code = "success", uri = "/admin/tenant/"), @Redirect(code = "api-error", uri = "/admin/tenant/")})
public class AddAction extends BaseFormAction {
  private final UUID fusionAuthTenantId;
  
  private final FrontEndLicensedFeaturesService licensedFeaturesService;
  
  @Inject
  public AddAction(FrontEndSupport paramFrontEndSupport, @FusionAuthTenantId UUID paramUUID, FrontEndLicensedFeaturesService paramFrontEndLicensedFeaturesService, PasswordEncryptorLibrary paramPasswordEncryptorLibrary, Cache<UUID, CachedTheme> paramCache) {
    super(paramFrontEndSupport, paramPasswordEncryptorLibrary, paramCache);
    this.fusionAuthTenantId = paramUUID;
    this.licensedFeaturesService = paramFrontEndLicensedFeaturesService;
  }
  
  public String get() {
    this.tenant = ((TenantResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveTenant((this.tenantId == null) ? this.fusionAuthTenantId : this.tenantId))).tenant;
    this.tenant.id = null;
    this.tenant.name = (this.tenantId == null) ? null : (this.tenant.name + " - copy");
    this.tenantId = null;
    this.licensedFeaturesService.disableLicensedFeatures(this.tenant);
    this.blockedDomains = CollectionTools.collectionToString(this.tenant.registrationConfiguration.blockedDomains);
    this.additionalEmailHeaders = this.tenant.emailConfiguration.additionalHeaders.stream().map(paramEmailHeader -> paramEmailHeader.name + "=" + paramEmailHeader.name).collect(Collectors.joining("\n"));
    this.scimSchemas = (this.scimSchemas != null) ? this.scimSchemas : "";
    return "input";
  }
  
  public String post() {
    Tenant tenant = ((TenantResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.createTenant(this.tenantId, new TenantRequest(this.frontEndSupport.buildEventInfo(null), this.tenant, this.webhookIds)))).tenant;
    writeAuditLog("Created the tenant with Id [" + String.valueOf(tenant.id) + "] and name [" + this.tenant.name + "]");
    if (tenant.scimServerConfiguration.enabled)
      setupInitialSCIMEntities(tenant); 
    return "success";
  }
  
  @PostValidationMethod
  public void postValidate() {
    validateAndNormalizeBlockedDomains();
    validateAndNormalizeEmailHeaders();
    validateAndNormalizeSCIMSchemas();
  }
}
