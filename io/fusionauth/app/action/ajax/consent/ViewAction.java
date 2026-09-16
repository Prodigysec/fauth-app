package io.fusionauth.app.action.ajax.consent;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Consent;
import io.fusionauth.domain.api.ConsentResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;

@Action(value = "{consentId}", requiresAuthentication = true, constraints = {"admin", "consent_manager"})
public class ViewAction extends BaseAJAXAction {
  public Consent consent;
  
  public UUID consentId;
  
  @Inject
  public ViewAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.consent = ((ConsentResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveConsent(this.consentId))).consent;
    return "render";
  }
}
