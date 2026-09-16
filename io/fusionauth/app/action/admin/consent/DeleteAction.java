package io.fusionauth.app.action.admin.consent;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Consent;
import io.fusionauth.domain.api.ConsentResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(requiresAuthentication = true, value = "{consentId}", constraints = {"admin", "consent_deleter"})
@List({@Redirect(code = "missing", uri = "/admin/consent/"), @Redirect(code = "success", uri = "/admin/consent/")})
public class DeleteAction extends BaseAction {
  public String confirm;
  
  public Consent consent;
  
  public UUID consentId;
  
  @Inject
  public DeleteAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
    paramFrontEndSupport.customPreTransferErrorConsumer = (paramErrors -> paramFrontEndSupport.moveFieldErrorToGeneral(paramErrors, "consentId", "[inUse]consentId"));
  }
  
  public String get() {
    return "input";
  }
  
  public String post() {
    superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.deleteConsent(this.consentId));
    writeAuditLog("Deleted consent with Id [" + String.valueOf(this.consentId) + "]");
    return "success";
  }
  
  @PostParameterMethod
  public void retrieveConsent() {
    this.consent = ((ConsentResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveConsent(this.consentId))).consent;
  }
  
  @ValidationMethod
  public void validate() {
    if (this.confirm == null) {
      this.frontEndSupport.addFieldError("confirm", "[missing]confirm", new Object[0]);
    } else if (!this.confirm.equals("DELETE")) {
      this.frontEndSupport.addFieldError("confirm", "[invalid]confirm", new Object[0]);
    } 
  }
}
