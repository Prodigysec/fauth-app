package io.fusionauth.app.action.ajax.identityProvider.claim;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import io.fusionauth.api.service.identity.IdentityProviderService;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, constraints = {"admin", "system_manager"})
public class AddAction extends BaseAJAXAction {
  private final IdentityProviderService identityProviderService;
  
  public String fusionAuthClaim;
  
  public BaseIdentityProvider<?> identityProvider;
  
  public UUID identityProviderId;
  
  public String incomingClaim;
  
  @Inject
  public AddAction(FrontEndSupport paramFrontEndSupport, IdentityProviderService paramIdentityProviderService) {
    super(paramFrontEndSupport);
    this.identityProviderService = paramIdentityProviderService;
  }
  
  public String get() {
    return "render";
  }
  
  public String post() {
    Errors errors = this.identityProviderService.validateClaim(this.identityProviderId, this.incomingClaim, this.fusionAuthClaim);
    if (errors.size() > 0) {
      this.frontEndSupport.transfer(errors);
      return "input";
    } 
    return "success";
  }
}
