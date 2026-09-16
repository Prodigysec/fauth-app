package io.fusionauth.app.action.tenantManager.sso.test;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.domain.guice.FusionAuthClientProvider;
import io.fusionauth.api.service.NotFoundException;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.app.action.tenantManager.BaseTenantManagerAction;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.guice.TenantManagerApplicationId;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.api.IdentityProviderResponse;
import io.fusionauth.domain.api.identityProvider.IdentityProviderConnectionTestResponse;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.domain.provider.IdentityProviderConnectionTestResult;
import io.fusionauth.domain.provider.IdentityProviderType;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Forward;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;
import org.primeframework.mvc.security.UserLoginSecurityContext;
import org.primeframework.mvc.security.csrf.CSRFProvider;

@Action(requiresAuthentication = true, scheme = {"tenant-manager"}, constraints = {"admin"})
@Forward(code = "input", page = "/tenant-manager/sso/test/complete.ftl")
@List({@Redirect(code = "missing", uri = "/tenant-manager/sso/?tenantId=${tenantId}")})
public class CompleteAction extends BaseTenantManagerAction {
  public String client_id;
  
  public String connectionTestId;
  
  @FTLVariable
  public String error;
  
  @FTLVariable
  public String error_description;
  
  @FTLVariable
  public String error_reason;
  
  @FTLVariable
  public BaseIdentityProvider<?> identityProvider;
  
  public UUID identityProviderId;
  
  @FTLVariable
  public IdentityProviderConnectionTestResult testResult;
  
  @FTLVariable
  public IdentityProviderType type;
  
  @Inject
  public CompleteAction(@Named("TenantManagerSecurityContext") UserLoginSecurityContext paramUserLoginSecurityContext, FrontEndSupport paramFrontEndSupport, @Named("TenantManagerFusionAuthClientProvider") FusionAuthClientProvider paramFusionAuthClientProvider, ReactorStatusService paramReactorStatusService, @Named("TenantManagerCSRFProvider") CSRFProvider paramCSRFProvider, @TenantManagerApplicationId UUID paramUUID) {
    super(paramUserLoginSecurityContext, paramFrontEndSupport, paramFusionAuthClientProvider, paramReactorStatusService, paramCSRFProvider, paramUUID);
  }
  
  public String get() throws Exception {
    return "input";
  }
  
  @PostParameterMethod
  public void postParameterMethod() throws Exception {
    if (this.error != null)
      return; 
    this.identityProvider = ((IdentityProviderResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveIdentityProvider(this.identityProviderId))).identityProvider;
    if (this.identityProvider == null || !"Tenant Manager".equals(this.identityProvider.source))
      throw new NotFoundException(); 
    this.type = this.identityProvider.getType();
    this.testResult = ((IdentityProviderConnectionTestResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveIdentityProviderConnectionTestResults(this.connectionTestId))).result;
    if (this.testResult == null)
      throw new NotFoundException(); 
    if (this.testResult.identityProviderId == null || !this.testResult.identityProviderId.equals(this.identityProvider.id))
      throw new NotFoundException(); 
  }
}
