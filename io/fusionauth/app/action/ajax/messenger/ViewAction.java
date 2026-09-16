package io.fusionauth.app.action.ajax.messenger;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.action.ajax.BaseAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.api.MessengerResponse;
import io.fusionauth.domain.messenger.BaseMessengerConfiguration;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;

@Action(value = "{messengerId}", requiresAuthentication = true, constraints = {"admin", "messenger_manager"})
public class ViewAction extends BaseAJAXAction {
  public BaseMessengerConfiguration messenger;
  
  public UUID messengerId;
  
  @Inject
  public ViewAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.messenger = ((MessengerResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveMessenger(this.messengerId))).messenger;
    return "render";
  }
}
