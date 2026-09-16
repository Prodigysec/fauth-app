package io.fusionauth.app.action.tenantManager.ajax.sso;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.domain.guice.FusionAuthClientProvider;
import io.fusionauth.api.service.NotFoundException;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.app.action.tenantManager.BaseTenantManagerAction;
import io.fusionauth.app.guice.TenantManagerApplicationId;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.api.IdentityProviderResponse;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.domain.provider.SAMLv2IdentityProvider;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Forward;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;
import org.primeframework.mvc.security.UserLoginSecurityContext;
import org.primeframework.mvc.security.csrf.CSRFProvider;

@Action(value = "{identityProviderId}", requiresAuthentication = true, scheme = {"tenant-manager"}, constraints = {"admin"})
@Forward(code = "input", page = "/tenant-manager/ajax/sso/delete.ftl")
@List({@Redirect(code = "success", uri = "/tenant-manager/sso/?tenantId=${tenantId}"), @Redirect(code = "missing", uri = "/tenant-manager/sso/?tenantId=${tenantId}")})
public class DeleteAction extends BaseTenantManagerAction {
  public UUID identityProviderId;
  
  @Inject
  public DeleteAction(@Named("TenantManagerSecurityContext") UserLoginSecurityContext paramUserLoginSecurityContext, FrontEndSupport paramFrontEndSupport, @Named("TenantManagerFusionAuthClientProvider") FusionAuthClientProvider paramFusionAuthClientProvider, ReactorStatusService paramReactorStatusService, @Named("TenantManagerCSRFProvider") CSRFProvider paramCSRFProvider, @TenantManagerApplicationId UUID paramUUID) {
    super(paramUserLoginSecurityContext, paramFrontEndSupport, paramFusionAuthClientProvider, paramReactorStatusService, paramCSRFProvider, paramUUID);
  }
  
  public String get() {
    return "input";
  }
  
  public String post() {
    BaseIdentityProvider<?> baseIdentityProvider = ((IdentityProviderResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveIdentityProvider(this.identityProviderId))).identityProvider;
    if (baseIdentityProvider == null || !"Tenant Manager".equals(baseIdentityProvider.source))
      throw new NotFoundException(); 
    this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.deleteIdentityProvider(this.identityProviderId));
    writeAuditLog("Deleted the Tenant Manager IdP Configuration with IdP Id [" + String.valueOf(this.identityProviderId) + "]");
    if (baseIdentityProvider instanceof SAMLv2IdentityProvider) {
      SAMLv2IdentityProvider sAMLv2IdentityProvider = (SAMLv2IdentityProvider)baseIdentityProvider;
      if (sAMLv2IdentityProvider.keyId != null)
        superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.deleteKey(paramSAMLv2IdentityProvider.keyId)); 
      if (sAMLv2IdentityProvider.requestSigningKeyId != null)
        superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.deleteKey(paramSAMLv2IdentityProvider.requestSigningKeyId)); 
    } 
    return "success";
  }
}
