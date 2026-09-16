package io.fusionauth.app.action.ajax.tenantManager.identityProvider;

import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.api.tenantManager.TenantManagerIdentityProviderTypeConfigurationRequest;
import io.fusionauth.domain.api.tenantManager.TenantManagerIdentityProviderTypeConfigurationResponse;
import io.fusionauth.domain.tenantManager.TenantManagerIdentityProviderTypeConfiguration;
import javax.inject.Inject;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(requiresAuthentication = true, constraints = {"admin", "system_manager"})
public class AddAction extends BaseFormAction {
  @Inject
  protected AddAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    return "render";
  }
  
  public String post() {
    TenantManagerIdentityProviderTypeConfigurationRequest tenantManagerIdentityProviderTypeConfigurationRequest = new TenantManagerIdentityProviderTypeConfigurationRequest();
    this.typeConfiguration.enabled = true;
    tenantManagerIdentityProviderTypeConfigurationRequest.typeConfiguration = this.typeConfiguration;
    TenantManagerIdentityProviderTypeConfigurationResponse tenantManagerIdentityProviderTypeConfigurationResponse = superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.createTenantManagerIdentityProviderTypeConfiguration(this.typeConfiguration.type, paramTenantManagerIdentityProviderTypeConfigurationRequest));
    writeAuditLog("Added Tenant Manager IdP Type Configuration [" + String.valueOf(tenantManagerIdentityProviderTypeConfigurationResponse.typeConfiguration.type) + "]");
    return "success";
  }
  
  @ValidationMethod
  public void validate() {
    if (this.typeConfiguration.type == null)
      this.frontEndSupport.addFieldError("typeConfiguration.type", "[missing]typeConfiguration.type", new Object[0]); 
  }
  
  protected void prepareForm() {
    this.typeConfiguration = new TenantManagerIdentityProviderTypeConfiguration();
  }
}
