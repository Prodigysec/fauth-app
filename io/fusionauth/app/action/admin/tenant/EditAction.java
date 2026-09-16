package io.fusionauth.app.action.admin.tenant;

import com.google.inject.Inject;
import com.inversoft.cache.Cache;
import com.inversoft.rest.ClientResponse;
import com.inversoft.util.CollectionTools;
import com.inversoft.util.StringTools;
import io.fusionauth.api.domain.CachedTheme;
import io.fusionauth.api.security.PasswordEncryptorLibrary;
import io.fusionauth.app.service.FrontEndSupport;
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

@Action(value = "{tenantId}", requiresAuthentication = true, constraints = {"admin", "tenant_manager"})
@List({@Redirect(code = "success", uri = "/admin/tenant/"), @Redirect(code = "api-error", uri = "/admin/tenant/"), @Redirect(code = "missing", uri = "/admin/tenant/")})
public class EditAction extends BaseFormAction {
  @Inject
  public EditAction(FrontEndSupport paramFrontEndSupport, PasswordEncryptorLibrary paramPasswordEncryptorLibrary, Cache<UUID, CachedTheme> paramCache) {
    super(paramFrontEndSupport, paramPasswordEncryptorLibrary, paramCache);
  }
  
  public String get() {
    this.tenant = ((TenantResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveTenant(this.tenantId))).tenant;
    if (this.tenant == null)
      return "missing"; 
    this.tenant.emailConfiguration.password = null;
    this.additionalEmailHeaders = this.tenant.emailConfiguration.additionalHeaders.stream().map(paramEmailHeader -> paramEmailHeader.name + "=" + paramEmailHeader.name).collect(Collectors.joining("\n"));
    this.blockedDomains = CollectionTools.collectionToString(this.tenant.registrationConfiguration.blockedDomains);
    this.scimSchemas = StringTools.defaultIfNull(this.frontEndSupport.prettyPrint(this.tenant.scimServerConfiguration.schemas), "");
    return "input";
  }
  
  public String post() {
    Tenant tenant1 = ((TenantResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveTenant(this.tenantId))).tenant;
    this.tenant.data.clear();
    this.tenant.data.putAll(tenant1.data);
    if (this.editPasswordOption == BaseFormAction.EditPasswordOption.useExisting)
      this.tenant.emailConfiguration.password = tenant1.emailConfiguration.password; 
    Tenant tenant2 = ((TenantResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.updateTenant(this.tenantId, new TenantRequest(this.frontEndSupport.buildEventInfo(null), this.tenant, this.webhookIds)))).tenant;
    writeAuditLogForUpdate("Updated the tenant with Id [" + String.valueOf(this.tenantId) + "] and name [" + tenant2.name + "]", tenant1, tenant2);
    return "success";
  }
  
  @PostValidationMethod
  public void postValidate() {
    validateAndNormalizeBlockedDomains();
    validateAndNormalizeEmailHeaders();
    validateAndNormalizeSCIMSchemas();
  }
}
