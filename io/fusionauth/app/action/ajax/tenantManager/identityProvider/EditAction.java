package io.fusionauth.app.action.ajax.tenantManager.identityProvider;

import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.api.tenantManager.TenantManagerConfigurationResponse;
import io.fusionauth.domain.api.tenantManager.TenantManagerIdentityProviderTypeConfigurationRequest;
import io.fusionauth.domain.api.tenantManager.TenantManagerIdentityProviderTypeConfigurationResponse;
import io.fusionauth.domain.tenantManager.TenantManagerConfiguration;
import io.fusionauth.domain.tenantManager.TenantManagerIdentityProviderTypeConfiguration;
import javax.inject.Inject;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, value = "{identityProviderType}", constraints = {"admin", "system_manager"})
public class EditAction extends BaseFormAction {
  public String identityProviderType;
  
  @Inject
  protected EditAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    return "render";
  }
  
  public String post() {
    TenantManagerConfiguration tenantManagerConfiguration = ((TenantManagerConfigurationResponse)superDelegate().execute(FusionAuthClient::retrieveTenantManagerConfiguration)).tenantManagerConfiguration;
    TenantManagerIdentityProviderTypeConfiguration tenantManagerIdentityProviderTypeConfiguration = tenantManagerConfiguration.identityProviderTypeConfigurations.get(this.typeConfiguration.type.name());
    TenantManagerIdentityProviderTypeConfigurationRequest tenantManagerIdentityProviderTypeConfigurationRequest = new TenantManagerIdentityProviderTypeConfigurationRequest();
    tenantManagerIdentityProviderTypeConfigurationRequest.typeConfiguration = this.typeConfiguration;
    TenantManagerIdentityProviderTypeConfigurationResponse tenantManagerIdentityProviderTypeConfigurationResponse = superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.updateTenantManagerIdentityProviderTypeConfiguration(this.typeConfiguration.type, paramTenantManagerIdentityProviderTypeConfigurationRequest));
    writeAuditLogForUpdate("Updated Tenant Manager IdP Type Configuration [" + String.valueOf(tenantManagerIdentityProviderTypeConfigurationResponse.typeConfiguration.type) + "]", tenantManagerIdentityProviderTypeConfiguration, tenantManagerIdentityProviderTypeConfigurationResponse.typeConfiguration);
    return "success";
  }
  
  protected void prepareForm() {
    this.typeConfiguration = this.existingConfigurations.get(this.identityProviderType);
  }
}
