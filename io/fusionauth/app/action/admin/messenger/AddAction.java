package io.fusionauth.app.action.admin.messenger;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import com.inversoft.util.CollectionTools;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.api.MessengerRequest;
import io.fusionauth.domain.api.MessengerResponse;
import io.fusionauth.domain.messenger.BaseMessengerConfiguration;
import io.fusionauth.domain.messenger.KafkaMessengerConfiguration;
import io.fusionauth.domain.messenger.MessengerType;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;

@Action(value = "{type}", requiresAuthentication = true, constraints = {"admin", "messenger_manager"})
@List({@Redirect(code = "success", uri = "/admin/messenger/"), @Redirect(code = "api-error", uri = "/admin/messenger/"), @Redirect(code = "unsupported-messenger-type", uri = "/admin/messenger/")})
public class AddAction extends BaseFormAction {
  @Inject
  public AddAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    if (this.type == MessengerType.Kafka) {
      this.messenger.normalize();
      this.producerConfiguration = CollectionTools.mapToString(((KafkaMessengerConfiguration)this.messenger).producer);
    } 
    if (this.messengerId != null) {
      this.messenger = ((MessengerResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveMessenger(this.messengerId))).messenger;
      this.messenger.id = null;
      this.messengerId = null;
      this.messenger.name += " - copy";
    } 
    return "input";
  }
  
  public String post() {
    BaseMessengerConfiguration baseMessengerConfiguration = ((MessengerResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.createMessenger(this.messengerId, new MessengerRequest(this.messenger)))).messenger;
    writeAuditLog("Created the messenger with Id [" + String.valueOf(baseMessengerConfiguration.id) + "] and name [" + this.messenger.name + "]");
    return "success";
  }
}
