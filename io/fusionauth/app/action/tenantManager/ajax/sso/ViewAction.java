package io.fusionauth.app.action.tenantManager.ajax.sso;

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
import io.fusionauth.domain.Key;
import io.fusionauth.domain.api.IdentityProviderResponse;
import io.fusionauth.domain.api.KeyResponse;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.domain.provider.IdentityProviderType;
import io.fusionauth.domain.provider.SAMLv2IdentityProvider;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Forward;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;
import org.primeframework.mvc.security.UserLoginSecurityContext;
import org.primeframework.mvc.security.csrf.CSRFProvider;

@Action(value = "{identityProviderId}", requiresAuthentication = true, scheme = {"tenant-manager"})
@Forward(code = "input", page = "/tenant-manager/ajax/sso/view.ftl")
@List({@Redirect(code = "success", uri = "/tenant-manager/sso/?tenantId=${tenantId}"), @Redirect(code = "missing", uri = "/tenant-manager/sso/?tenantId=${tenantId}")})
public class ViewAction extends BaseTenantManagerAction {
  @FTLVariable
  public BaseIdentityProvider<?> identityProvider;
  
  public UUID identityProviderId;
  
  @FTLVariable
  public String requestSigningPublicKey;
  
  @FTLVariable
  public String verificationKey;
  
  @Inject
  public ViewAction(@Named("TenantManagerSecurityContext") UserLoginSecurityContext paramUserLoginSecurityContext, FrontEndSupport paramFrontEndSupport, @Named("TenantManagerFusionAuthClientProvider") FusionAuthClientProvider paramFusionAuthClientProvider, ReactorStatusService paramReactorStatusService, @Named("TenantManagerCSRFProvider") CSRFProvider paramCSRFProvider, @TenantManagerApplicationId UUID paramUUID) {
    super(paramUserLoginSecurityContext, paramFrontEndSupport, paramFusionAuthClientProvider, paramReactorStatusService, paramCSRFProvider, paramUUID);
  }
  
  public String get() {
    this.identityProvider = ((IdentityProviderResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveIdentityProvider(this.identityProviderId))).identityProvider;
    if (this.identityProvider == null || !"Tenant Manager".equals(this.identityProvider.source))
      throw new NotFoundException(); 
    if (IdentityProviderType.SAMLv2.equals(this.identityProvider.getType()) && this.identityProviderId != null) {
      SAMLv2IdentityProvider sAMLv2IdentityProvider = (SAMLv2IdentityProvider)this.identityProvider;
      if (sAMLv2IdentityProvider.keyId != null) {
        Key key = ((KeyResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveKey(paramSAMLv2IdentityProvider.keyId))).key;
        this.verificationKey = (key.certificate != null) ? key.certificate : key.publicKey;
      } 
      if (sAMLv2IdentityProvider.signRequest && sAMLv2IdentityProvider.requestSigningKeyId != null) {
        Key key = ((KeyResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveKey(paramSAMLv2IdentityProvider.requestSigningKeyId))).key;
        this.requestSigningPublicKey = (key.certificate != null) ? key.certificate : key.publicKey;
      } 
    } 
    return "input";
  }
}
