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
import org.primeframework.mvc.action.result.annotation.Redirect.List;

@Action(requiresAuthentication = true, value = "{consentId}", constraints = {"admin", "consent_manager"})
@List({@Redirect(code = "missing", uri = "/admin/consent/"), @Redirect(code = "success", uri = "/admin/consent/")})
public class EditAction extends BaseFormAction {
  @Inject
  public EditAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.consent = ((ConsentResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveConsent(this.consentId))).consent;
    return "input";
  }
  
  public String post() {
    Consent consent1 = ((ConsentResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveConsent(this.consentId))).consent;
    this.consent.data.clear();
    this.consent.data.putAll(consent1.data);
    Consent consent2 = ((ConsentResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.updateConsent(this.consentId, new ConsentRequest(this.consent)))).consent;
    writeAuditLogForUpdate("Updated consent with Id [" + String.valueOf(this.consentId) + "] and name [" + consent2.name + "]", consent1, consent2);
    return "success";
  }
}
