package io.fusionauth.app.action.ajax.user.consent;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.ConsentStatus;
import io.fusionauth.domain.UserConsent;
import io.fusionauth.domain.api.UserConsentRequest;
import io.fusionauth.domain.api.UserConsentResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;

@Action(value = "{userConsentId}", requiresAuthentication = true, constraints = {"admin", "user_manager", "user_support_manager"})
public class UnrevokeAction extends BaseAJAXAction {
  public UUID userConsentId;
  
  @Inject
  public UnrevokeAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    return "render";
  }
  
  public String post() {
    UserConsent userConsent = ((UserConsentResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveUserConsent(this.userConsentId))).userConsent;
    userConsent.status = ConsentStatus.Active;
    this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.updateUserConsent(this.userConsentId, new UserConsentRequest(paramUserConsent)));
    writeAuditLog("Unrevoked consent with Id [" + String.valueOf(this.userConsentId) + "] for User with Id [" + String.valueOf(userConsent.userId) + "] new status is [" + String.valueOf(userConsent.status) + "]");
    return "success";
  }
}
