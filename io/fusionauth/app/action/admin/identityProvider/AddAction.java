package io.fusionauth.app.action.admin.identityProvider;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.api.IdentityProviderRequest;
import io.fusionauth.domain.api.IdentityProviderResponse;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import java.util.ArrayList;
import java.util.List;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;
import org.primeframework.mvc.control.form.annotation.FormPrepareMethod;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{type}", requiresAuthentication = true, constraints = {"admin", "system_manager"})
@List({@Redirect(code = "success", uri = "/admin/identity-provider/"), @Redirect(code = "api-error", uri = "/admin/identity-provider/")})
public class AddAction extends BaseFormAction {
  @FTLVariable
  public Boolean global;
  
  @FTLVariable
  public List<TenantOption> tenantOptions;
  
  @Inject
  public AddAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  @FormPrepareMethod
  public void formPrepare() {
    this.tenantOptions = new ArrayList<>();
    this.tenantOptions.add(new TenantOption("global", this.frontEndSupport.messageProvider.getMessage("global-name", new Object[0])));
    this.tenantOptions.addAll(this.tenants.values().stream().map(paramTenant -> new TenantOption(paramTenant.id.toString(), paramTenant.name)).toList());
  }
  
  public String get() {
    return "input";
  }
  
  public String post() {
    BaseIdentityProvider<?> baseIdentityProvider = ((IdentityProviderResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.createIdentityProvider(this.identityProviderId, new IdentityProviderRequest(this.identityProvider)))).identityProvider;
    writeAuditLog("Created the identity provider with Id [" + String.valueOf(baseIdentityProvider.id) + "] and name [" + this.identityProvider.name + "]");
    return "success";
  }
  
  @ValidationMethod(httpMethods = {"POST"})
  public void validatePost() {
    if (this.identityProvider.name == null)
      this.frontEndSupport.addFieldError("identityProvider.name", "[blank]identityProvider.name", new Object[0]); 
  }
  
  public static class TenantOption {
    public String id;
    
    public String name;
    
    public TenantOption(String param1String1, String param1String2) {
      this.id = param1String1;
      this.name = param1String2;
    }
  }
}
