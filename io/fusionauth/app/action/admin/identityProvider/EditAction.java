package io.fusionauth.app.action.admin.identityProvider;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import com.inversoft.util.CollectionTools;
import io.fusionauth.api.service.NotFoundException;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.api.IdentityProviderRequest;
import io.fusionauth.domain.api.IdentityProviderResponse;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.domain.provider.DomainBasedIdentityProvider;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{type}/{identityProviderId}", requiresAuthentication = true, constraints = {"admin", "system_manager"})
@List({@Redirect(code = "success", uri = "/admin/identity-provider/"), @Redirect(code = "api-error", uri = "/admin/identity-provider/"), @Redirect(code = "missing", uri = "/admin/identity-provider/")})
public class EditAction extends BaseFormAction {
  @Inject
  public EditAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.identityProvider = ((IdentityProviderResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveIdentityProvider(this.identityProviderId))).identityProvider;
    if (this.identityProvider == null)
      throw new NotFoundException(); 
    this.type = this.identityProvider.getType();
    if (this.identityProvider instanceof DomainBasedIdentityProvider)
      this.domains = CollectionTools.collectionToString(((DomainBasedIdentityProvider)this.identityProvider).getDomains()); 
    return "input";
  }
  
  public String post() {
    BaseIdentityProvider<?> baseIdentityProvider1 = ((IdentityProviderResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveIdentityProvider(this.identityProviderId))).identityProvider;
    this.identityProvider.attributeMappings.clear();
    this.identityProvider.attributeMappings.putAll(baseIdentityProvider1.attributeMappings);
    this.identityProvider.data.clear();
    this.identityProvider.data.putAll(baseIdentityProvider1.data);
    if (this.identityProvider.enabled)
      this.identityProvider.linkingStrategy = baseIdentityProvider1.linkingStrategy; 
    BaseIdentityProvider<?> baseIdentityProvider2 = ((IdentityProviderResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.updateIdentityProvider(this.identityProviderId, new IdentityProviderRequest(this.identityProvider)))).identityProvider;
    writeAuditLogForUpdate("Updated identity provider with Id [" + String.valueOf(this.identityProviderId) + "] and name [" + baseIdentityProvider2.name + "]", baseIdentityProvider1, baseIdentityProvider2);
    return "success";
  }
  
  @ValidationMethod(httpMethods = {"POST"})
  public void validatePost() {
    if (this.identityProvider.name == null)
      this.frontEndSupport.addFieldError("identityProvider.name", "[blank]identityProvider.name", new Object[0]); 
  }
}
