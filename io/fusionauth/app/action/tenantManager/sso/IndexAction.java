package io.fusionauth.app.action.tenantManager.sso;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.domain.guice.FusionAuthClientProvider;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.app.action.tenantManager.BaseTenantManagerAction;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.guice.TenantManagerApplicationId;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.api.IdentityProviderSearchRequest;
import io.fusionauth.domain.api.IdentityProviderSearchResponse;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.domain.provider.IdentityProviderType;
import io.fusionauth.domain.search.IdentityProviderSearchCriteria;
import io.fusionauth.domain.tenantManager.TenantManagerIdentityProviderTypeConfiguration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Forward;
import org.primeframework.mvc.security.UserLoginSecurityContext;
import org.primeframework.mvc.security.csrf.CSRFProvider;

@Forward(page = "/tenant-manager/sso/index.ftl")
@Action(requiresAuthentication = true, scheme = {"tenant-manager"})
public class IndexAction extends BaseTenantManagerAction {
  @FTLVariable
  public List<IdentityProviderType> allowedIdpTypes = new ArrayList<>();
  
  @FTLVariable
  public List<BaseIdentityProvider<?>> identityProviders;
  
  @Inject
  public IndexAction(@Named("TenantManagerSecurityContext") UserLoginSecurityContext paramUserLoginSecurityContext, FrontEndSupport paramFrontEndSupport, @Named("TenantManagerFusionAuthClientProvider") FusionAuthClientProvider paramFusionAuthClientProvider, ReactorStatusService paramReactorStatusService, @Named("TenantManagerCSRFProvider") CSRFProvider paramCSRFProvider, @TenantManagerApplicationId UUID paramUUID) {
    super(paramUserLoginSecurityContext, paramFrontEndSupport, paramFusionAuthClientProvider, paramReactorStatusService, paramCSRFProvider, paramUUID);
  }
  
  public String get() {
    IdentityProviderSearchCriteria identityProviderSearchCriteria = new IdentityProviderSearchCriteria();
    identityProviderSearchCriteria.source = "Tenant Manager";
    identityProviderSearchCriteria.tenantId = this.tenantId;
    identityProviderSearchCriteria.numberOfResults = 1000;
    this


      
      .identityProviders = ((IdentityProviderSearchResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.searchIdentityProviders(new IdentityProviderSearchRequest(paramIdentityProviderSearchCriteria)))).identityProviders.stream().filter(paramBaseIdentityProvider -> Objects.equals(paramBaseIdentityProvider.source, "Tenant Manager")).toList();
    this.tenantManagerConfiguration.identityProviderTypeConfigurations.forEach((paramString, paramTenantManagerIdentityProviderTypeConfiguration) -> this.allowedIdpTypes.add(IdentityProviderType.valueOf(paramString)));
    return "success";
  }
}
