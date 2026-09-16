package io.fusionauth.app.action.tenantManager.sso;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import com.inversoft.util.CollectionTools;
import io.fusionauth.api.domain.guice.FusionAuthClientProvider;
import io.fusionauth.api.service.NotFoundException;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.app.action.tenantManager.BaseIdentityProviderAction;
import io.fusionauth.app.guice.TenantManagerApplicationId;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.api.IdentityProviderRequest;
import io.fusionauth.domain.api.IdentityProviderResponse;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.domain.provider.DomainBasedIdentityProvider;
import io.fusionauth.domain.provider.IdentityProviderType;
import io.fusionauth.domain.provider.SAMLv2IdentityProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Forward;
import org.primeframework.mvc.security.UserLoginSecurityContext;
import org.primeframework.mvc.security.csrf.CSRFProvider;

@Action(value = "{type}/{identityProviderId}", requiresAuthentication = true, scheme = {"tenant-manager"}, constraints = {"admin"})
@Forward(code = "input", page = "/tenant-manager/sso/edit.ftl")
public class EditAction extends BaseIdentityProviderAction {
  @Inject
  public EditAction(@Named("TenantManagerSecurityContext") UserLoginSecurityContext paramUserLoginSecurityContext, FrontEndSupport paramFrontEndSupport, @Named("TenantManagerFusionAuthClientProvider") FusionAuthClientProvider paramFusionAuthClientProvider, ReactorStatusService paramReactorStatusService, @Named("TenantManagerCSRFProvider") CSRFProvider paramCSRFProvider, @TenantManagerApplicationId UUID paramUUID) {
    super(paramUserLoginSecurityContext, paramFrontEndSupport, paramFusionAuthClientProvider, paramReactorStatusService, paramCSRFProvider, paramUUID);
  }
  
  public String get() throws Exception {
    this.identityProvider = ((IdentityProviderResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveIdentityProvider(this.identityProviderId))).identityProvider;
    if (this.identityProvider == null || !"Tenant Manager".equals(this.identityProvider.source))
      throw new NotFoundException(); 
    if (this.identityProvider instanceof DomainBasedIdentityProvider)
      this.domains = CollectionTools.collectionToString(((DomainBasedIdentityProvider)this.identityProvider).getDomains()); 
    return "input";
  }
  
  public String post() throws Exception {
    BaseIdentityProvider<?> baseIdentityProvider1 = ((IdentityProviderResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveIdentityProvider(this.identityProviderId))).identityProvider;
    List<UUID> list = postInitialize(baseIdentityProvider1);
    if (this.frontEndSupport.hasErrorMessages()) {
      cleanupKeys(list);
      return "input";
    } 
    ClientResponse<IdentityProviderResponse, Errors> clientResponse = this.client.updateIdentityProvider(this.identityProviderId, new IdentityProviderRequest(this.identityProvider));
    if (!clientResponse.wasSuccessful()) {
      this.frontEndSupport.transfer((Errors)clientResponse.errorResponse);
      cleanupKeys(list);
      return "input";
    } 
    BaseIdentityProvider<?> baseIdentityProvider2 = ((IdentityProviderResponse)clientResponse.successResponse).identityProvider;
    writeAuditLogForUpdate("Updated identity provider with Id [" + String.valueOf(this.identityProviderId) + "] and name [" + baseIdentityProvider2.name + "]", baseIdentityProvider1, baseIdentityProvider2);
    if (IdentityProviderType.SAMLv2.equals(this.type)) {
      SAMLv2IdentityProvider sAMLv2IdentityProvider1 = (SAMLv2IdentityProvider)baseIdentityProvider1;
      SAMLv2IdentityProvider sAMLv2IdentityProvider2 = (SAMLv2IdentityProvider)baseIdentityProvider2;
      ArrayList<UUID> arrayList = new ArrayList();
      if (!Objects.equals(sAMLv2IdentityProvider2.keyId, sAMLv2IdentityProvider1.keyId) && 
        sAMLv2IdentityProvider1.keyId != null)
        arrayList.add(sAMLv2IdentityProvider1.keyId); 
      if (!sAMLv2IdentityProvider2.signRequest && sAMLv2IdentityProvider1.requestSigningKeyId != null)
        arrayList.add(sAMLv2IdentityProvider1.requestSigningKeyId); 
      cleanupKeys(arrayList);
    } 
    if (this.testConfiguration)
      return "test"; 
    return "success";
  }
}
