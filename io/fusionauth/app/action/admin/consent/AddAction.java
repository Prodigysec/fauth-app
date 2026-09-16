package io.fusionauth.app.action.admin.consent;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Consent;
import io.fusionauth.domain.api.ConsentRequest;
import io.fusionauth.domain.api.ConsentResponse;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;

@Action(requiresAuthentication = true, constraints = {"admin", "consent_manager"})
@Redirect(code = "success", uri = "/admin/consent/")
public class AddAction extends BaseFormAction {
  @Inject
  public AddAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    return "input";
  }
  
  public String post() {
    Consent consent = ((ConsentResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.createConsent(this.consentId, new ConsentRequest(this.consent)))).consent;
    writeAuditLog("Created user consent with Id [" + String.valueOf(consent.id) + "] and name [" + consent.name + "]");
    return "success";
  }
}
