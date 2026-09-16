package io.fusionauth.app.action.admin.identityProvider;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.api.IdentityProviderResponse;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(requiresAuthentication = true, constraints = {"admin", "system_manager"}, value = "{identityProviderId}")
@List({@Redirect(code = "success", uri = "/admin/identity-provider/"), @Redirect(code = "api-error", uri = "/admin/identity-provider/"), @Redirect(code = "missing", uri = "/admin/identity-provider/")})
public class DeleteAction extends BaseAction {
  public String confirm;
  
  public BaseIdentityProvider<?> identityProvider;
  
  public UUID identityProviderId;
  
  @Inject
  public DeleteAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    return "input";
  }
  
  public String post() {
    this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.deleteIdentityProvider(this.identityProviderId));
    writeAuditLog("Deleted the identity provider with Id [" + String.valueOf(this.identityProviderId) + "] and name [" + this.identityProvider.name + "]");
    return "success";
  }
  
  @PostParameterMethod
  public void retrieveIdentityProvider() {
    this.identityProvider = ((IdentityProviderResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveIdentityProvider(this.identityProviderId))).identityProvider;
  }
  
  @ValidationMethod
  public void validate() {
    if (this.confirm == null) {
      this.frontEndSupport.addFieldError("confirm", "[missing]confirm", new Object[0]);
    } else if (!this.confirm.equals("DELETE")) {
      this.frontEndSupport.addFieldError("confirm", "[invalid]confirm", new Object[0]);
    } 
  }
}
