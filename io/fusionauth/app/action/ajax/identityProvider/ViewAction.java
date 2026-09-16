package io.fusionauth.app.action.ajax.identityProvider;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.service.samlv2.SAMLv2Helper;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.Key;
import io.fusionauth.domain.Lambda;
import io.fusionauth.domain.api.ApplicationResponse;
import io.fusionauth.domain.api.IdentityProviderResponse;
import io.fusionauth.domain.api.KeyResponse;
import io.fusionauth.domain.api.LambdaResponse;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.domain.provider.BaseSAMLv2IdentityProvider;
import io.fusionauth.domain.provider.IdentityProviderTenantConfiguration;
import io.fusionauth.domain.provider.IdentityProviderType;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;

@Action(value = "{identityProviderId}", requiresAuthentication = true, constraints = {"admin", "system_manager"})
public class ViewAction extends BaseAJAXAction {
  public Map<UUID, Application> applications = new HashMap<>();
  
  public Key decryptionKey;
  
  @FTLVariable
  public IdentityProviderTenantConfiguration defaultIdentityProviderTenantConfiguration = new IdentityProviderTenantConfiguration();
  
  public BaseIdentityProvider<?> identityProvider;
  
  public UUID identityProviderId;
  
  public Key key;
  
  public Lambda lambda;
  
  public String samlIssuerValue;
  
  @Inject
  public ViewAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.identityProvider = ((IdentityProviderResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveIdentityProvider(this.identityProviderId))).identityProvider;
    ((ApplicationResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.setTenantId(this.identityProvider.tenantId).retrieveApplications())).applications


      
      .forEach(paramApplication -> this.applications.put(paramApplication.id, paramApplication));
    if (this.identityProvider.lambdaConfiguration.reconcileId != null)
      this.lambda = ((LambdaResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveLambda(this.identityProvider.lambdaConfiguration.reconcileId))).lambda; 
    switch (this.identityProvider.getType()) {
      case Apple:
      
      case SAMLv2:
      
      case SAMLv2IdPInitiated:
      
      default:
        break;
    } 
    Object object1 = 


      
      null;
    this.key = (object1 != null) ? ((KeyResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveKey(paramUUID))).key : null;
    switch (this.identityProvider.getType()) {
      case SAMLv2:
      
      case SAMLv2IdPInitiated:
      
      default:
        break;
    } 
    Object object2 = 

      
      null;
    this.decryptionKey = (object2 != null) ? ((KeyResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveKey(paramUUID))).key : null;
    BaseIdentityProvider<?> baseIdentityProvider = this.identityProvider;
    if (baseIdentityProvider instanceof BaseSAMLv2IdentityProvider) {
      BaseSAMLv2IdentityProvider<?> baseSAMLv2IdentityProvider = (BaseSAMLv2IdentityProvider)baseIdentityProvider;
      this.samlIssuerValue = SAMLv2Helper.getServiceProviderEntityId(this.frontEndSupport.request.getBaseURL(), baseSAMLv2IdentityProvider);
    } 
    return "render";
  }
}
