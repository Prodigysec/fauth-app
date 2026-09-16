package io.fusionauth.app.action.ajax.user.webauthn;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.WebAuthnCredential;
import io.fusionauth.domain.api.WebAuthnCredentialResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;

@Action(value = "{id}", requiresAuthentication = true, constraints = {"admin", "user_manager", "user_support_manager", "user_support_viewer"})
public class ViewAction extends BaseAJAXAction {
  public WebAuthnCredential credential;
  
  public UUID id;
  
  @Inject
  public ViewAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.credential = ((WebAuthnCredentialResponse)this.delegate.execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveWebAuthnCredential(this.id))).credential;
    return "render";
  }
}
