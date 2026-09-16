package io.fusionauth.app.action.ajax.tenantManager.identityProvider;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.provider.IdentityProviderType;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{identityProviderType}", requiresAuthentication = true, constraints = {"admin", "system_manager"})
public class DeleteAction extends BaseAJAXAction {
  public String identityProviderType;
  
  @Inject
  public DeleteAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    return "render";
  }
  
  public String post() {
    superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.deleteTenantManagerIdentityProviderTypeConfiguration(IdentityProviderType.valueOf(this.identityProviderType)));
    writeAuditLog("Deleted the Tenant Manager IdP Configuration with IdP Type[" + this.identityProviderType + "]");
    return "success";
  }
  
  @ValidationMethod
  public void validate() {
    if (this.identityProviderType == null) {
      this.frontEndSupport.addFieldError("identityProviderType", "[missing]identityProviderType", new Object[0]);
      return;
    } 
    try {
      IdentityProviderType.valueOf(this.identityProviderType);
    } catch (IllegalArgumentException illegalArgumentException) {
      this.frontEndSupport.addFieldError("identityProviderType", "[invalid]identityProviderType", new Object[0]);
    } 
  }
}
