package io.fusionauth.app.action.admin.messenger;

import com.google.inject.Inject;
import io.fusionauth.app.action.BaseAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.api.MessengerResponse;
import io.fusionauth.domain.messenger.BaseMessengerConfiguration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.primeframework.mvc.action.annotation.Action;

@Action(requiresAuthentication = true, constraints = {"admin", "messenger_manager"})
public class IndexAction extends BaseAction {
  public List<BaseMessengerConfiguration> messengers;
  
  @Inject
  public IndexAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.messengers = (List<BaseMessengerConfiguration>)Objects.requireNonNullElseGet(((MessengerResponse)superDelegate().execute(FusionAuthClient::retrieveMessengers)).messengers, Collections::emptyList);
    return "input";
  }
}
