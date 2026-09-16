package io.fusionauth.app.action.tenantManager.sso.test;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.inversoft.rest.ClientResponse;
import com.inversoft.rest.ProxyInfoSupplier;
import io.fusionauth.api.domain.guice.FusionAuthClientProvider;
import io.fusionauth.api.service.NotFoundException;
import io.fusionauth.api.service.identity.OpenIdConnectIdentityProviderHelper;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.api.util.PKCETools;
import io.fusionauth.app.action.tenantManager.BaseTenantManagerAction;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.guice.TenantManagerApplicationId;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.api.IdentityProviderResponse;
import io.fusionauth.domain.api.identityProvider.IdentityProviderConnectionTestRequest;
import io.fusionauth.domain.api.identityProvider.IdentityProviderConnectionTestResponse;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.domain.provider.BaseIdentityProviderApplicationConfiguration;
import io.fusionauth.domain.provider.IdentityProviderType;
import io.fusionauth.domain.provider.OpenIdConnectIdentityProvider;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Forward;
import org.primeframework.mvc.action.result.annotation.Forward.List;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;
import org.primeframework.mvc.security.UserLoginSecurityContext;
import org.primeframework.mvc.security.csrf.CSRFProvider;
import org.primeframework.mvc.util.QueryStringBuilder;

@Action(value = "{identityProviderId}", requiresAuthentication = true, scheme = {"tenant-manager"}, constraints = {"admin"})
@List({@Forward(code = "input", page = "/tenant-manager/sso/test/start.ftl"), @Forward(code = "meta-refresh", page = "/tenant-manager/sso/test/meta-refresh.ftl")})
@Redirect(code = "missing", uri = "/tenant-manager/sso/?tenantId=${tenantId}")
public class StartAction extends BaseTenantManagerAction {
  private final ProxyInfoSupplier proxyInfoSupplier;
  
  public String authorizedRedirectURI;
  
  @FTLVariable
  public BaseIdentityProvider<?> identityProvider;
  
  public UUID identityProviderId;
  
  @FTLVariable
  public IdentityProviderType type;
  
  @Inject
  public StartAction(@Named("TenantManagerSecurityContext") UserLoginSecurityContext paramUserLoginSecurityContext, FrontEndSupport paramFrontEndSupport, @Named("TenantManagerFusionAuthClientProvider") FusionAuthClientProvider paramFusionAuthClientProvider, ReactorStatusService paramReactorStatusService, @Named("TenantManagerCSRFProvider") CSRFProvider paramCSRFProvider, @TenantManagerApplicationId UUID paramUUID, ProxyInfoSupplier paramProxyInfoSupplier) {
    super(paramUserLoginSecurityContext, paramFrontEndSupport, paramFusionAuthClientProvider, paramReactorStatusService, paramCSRFProvider, paramUUID);
    this.proxyInfoSupplier = paramProxyInfoSupplier;
  }
  
  public String get() throws Exception {
    return "input";
  }
  
  public String post() throws Exception {
    Optional optional = this.identityProvider.applicationConfiguration.entrySet().stream().filter(paramEntry -> ((BaseIdentityProviderApplicationConfiguration)paramEntry.getValue()).enabled).map(Map.Entry::getKey).findFirst();
    if (optional.isEmpty()) {
      this.frontEndSupport.addGeneralError("[InvalidApplicationConfiguration]", new Object[0]);
      return "input";
    } 
    IdentityProviderConnectionTestRequest identityProviderConnectionTestRequest = new IdentityProviderConnectionTestRequest();
    identityProviderConnectionTestRequest.identityProviderId = this.identityProvider.id;
    identityProviderConnectionTestRequest.tenantId = this.tenantId;
    String str1 = ((IdentityProviderConnectionTestResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.startIdentityProviderConnectionTest(paramIdentityProviderConnectionTestRequest))).connectionTestId;
    String str2 = PKCETools.generateCodeVerifier();
    String str3 = PKCETools.generateCodeChallenge(str2);
    this









      
      .authorizedRedirectURI = QueryStringBuilder.builder("/oauth2/authorize").with("client_id", optional.get()).with("code_challenge", str3).with("code_challenge_method", "S256").with("connectionTestId", str1).with("identityProviderId", this.identityProvider.id).with("prompt", "login").with("redirect_uri", "/tenant-manager/sso/test/complete?client_id=" + String.valueOf(optional.get()) + "&tenantId=" + String.valueOf(this.tenantId)).with("response_type", "code").with("scope", "openid").with("tenantId", this.tenantId).build();
    return "meta-refresh";
  }
  
  @PostParameterMethod
  public void postParameterMethod() throws Exception {
    this.identityProvider = ((IdentityProviderResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveIdentityProvider(this.identityProviderId))).identityProvider;
    if (this.identityProvider == null || !"Tenant Manager".equals(this.identityProvider.source))
      throw new NotFoundException(); 
    BaseIdentityProvider<?> baseIdentityProvider = this.identityProvider;
    if (baseIdentityProvider instanceof OpenIdConnectIdentityProvider) {
      OpenIdConnectIdentityProvider openIdConnectIdentityProvider = (OpenIdConnectIdentityProvider)baseIdentityProvider;
      if (openIdConnectIdentityProvider.oauth2 != null && openIdConnectIdentityProvider.oauth2.issuer != null)
        OpenIdConnectIdentityProviderHelper.resolveOpenIDConnectEndpoints(this.proxyInfoSupplier, openIdConnectIdentityProvider); 
    } 
    this.type = this.identityProvider.getType();
  }
}
