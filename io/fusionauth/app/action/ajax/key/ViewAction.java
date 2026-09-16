package io.fusionauth.app.action.ajax.key;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.Key;
import io.fusionauth.domain.api.KeyResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;

@Action(value = "{keyId}", requiresAuthentication = true, constraints = {"admin", "key_manager"})
public class ViewAction extends BaseAJAXAction {
  public Key key;
  
  public UUID keyId;
  
  @Inject
  public ViewAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.key = ((KeyResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveKey(this.keyId))).key;
    return "render";
  }
}
