package io.fusionauth.app.action.ajax.user.consent;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Consent;
import io.fusionauth.domain.UserConsent;
import io.fusionauth.domain.api.UserConsentRequest;
import io.fusionauth.domain.api.UserConsentResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;

@Action(value = "{userConsentId}", requiresAuthentication = true, constraints = {"admin", "user_manager", "user_support_manager"})
public class EditAction extends BaseAJAXAction {
  public Consent consent = new Consent();
  
  public UserConsent userConsent = new UserConsent();
  
  public UUID userConsentId;
  
  @Inject
  public EditAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.userConsent = ((UserConsentResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveUserConsent(this.userConsentId))).userConsent;
    this.consent = this.userConsent.consent;
    return "render";
  }
  
  public String post() {
    UserConsent userConsent1 = ((UserConsentResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveUserConsent(this.userConsentId))).userConsent;
    UserConsent userConsent2 = new UserConsent(userConsent1);
    userConsent2.values = this.userConsent.values;
    UserConsent userConsent3 = ((UserConsentResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.updateUserConsent(this.userConsentId, new UserConsentRequest(paramUserConsent)))).userConsent;
    writeAuditLogForUpdate("Updated user consent with Id [" + String.valueOf(this.userConsentId) + "] for User with Id [" + String.valueOf(userConsent3.userId) + "]", userConsent1, userConsent3);
    return "success";
  }
}
