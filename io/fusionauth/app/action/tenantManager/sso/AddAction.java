package io.fusionauth.app.action.tenantManager.sso;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.domain.guice.FusionAuthClientProvider;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.app.action.tenantManager.BaseIdentityProviderAction;
import io.fusionauth.app.guice.TenantManagerApplicationId;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.IdentityProviderRequest;
import io.fusionauth.domain.api.IdentityProviderResponse;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import java.util.List;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Forward;
import org.primeframework.mvc.security.UserLoginSecurityContext;
import org.primeframework.mvc.security.csrf.CSRFProvider;

@Action(value = "{type}", requiresAuthentication = true, scheme = {"tenant-manager"}, constraints = {"admin"})
@Forward(code = "input", page = "/tenant-manager/sso/add.ftl")
public class AddAction extends BaseIdentityProviderAction {
  @Inject
  public AddAction(@Named("TenantManagerSecurityContext") UserLoginSecurityContext paramUserLoginSecurityContext, FrontEndSupport paramFrontEndSupport, @Named("TenantManagerFusionAuthClientProvider") FusionAuthClientProvider paramFusionAuthClientProvider, ReactorStatusService paramReactorStatusService, @Named("TenantManagerCSRFProvider") CSRFProvider paramCSRFProvider, @TenantManagerApplicationId UUID paramUUID) {
    super(paramUserLoginSecurityContext, paramFrontEndSupport, paramFusionAuthClientProvider, paramReactorStatusService, paramCSRFProvider, paramUUID);
  }
  
  public String get() {
    return "input";
  }
  
  public String post() {
    List<UUID> list = postInitialize();
    if (this.frontEndSupport.hasErrorMessages()) {
      cleanupKeys(list);
      return "input";
    } 
    ClientResponse<IdentityProviderResponse, Errors> clientResponse = this.client.createIdentityProvider(this.identityProviderId, new IdentityProviderRequest(this.identityProvider));
    if (!clientResponse.wasSuccessful()) {
      this.frontEndSupport.transfer((Errors)clientResponse.errorResponse);
      cleanupKeys(list);
      return "input";
    } 
    BaseIdentityProvider<?> baseIdentityProvider = ((IdentityProviderResponse)clientResponse.successResponse).identityProvider;
    writeAuditLog("Created the identity provider with Id [" + String.valueOf(baseIdentityProvider.id) + "] and name [" + this.identityProvider.name + "]");
    if (this.testConfiguration) {
      this.identityProviderId = baseIdentityProvider.id;
      return "test";
    } 
    return "success";
  }
}
