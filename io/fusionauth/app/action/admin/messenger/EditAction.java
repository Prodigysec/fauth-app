package io.fusionauth.app.action.admin.messenger;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.api.MessengerRequest;
import io.fusionauth.domain.api.MessengerResponse;
import io.fusionauth.domain.messenger.BaseMessengerConfiguration;
import io.fusionauth.domain.messenger.MessengerType;
import io.fusionauth.domain.messenger.TwilioMessengerConfiguration;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;

@Action(value = "{type}/{messengerId}", requiresAuthentication = true, constraints = {"admin", "messenger_manager"})
@List({@Redirect(code = "success", uri = "/admin/messenger/"), @Redirect(code = "api-error", uri = "/admin/messenger/"), @Redirect(code = "unsupported-messenger-type", uri = "/admin/messenger/")})
public class EditAction extends BaseFormAction {
  @Inject
  public EditAction(FrontEndSupport paramFrontEndSupport) {
    super(paramFrontEndSupport);
  }
  
  public String get() {
    this.messenger = ((MessengerResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveMessenger(this.messengerId))).messenger;
    if (this.type == MessengerType.Twilio) {
      BaseMessengerConfiguration baseMessengerConfiguration = this.messenger;
      if (baseMessengerConfiguration instanceof TwilioMessengerConfiguration) {
        TwilioMessengerConfiguration twilioMessengerConfiguration = (TwilioMessengerConfiguration)baseMessengerConfiguration;
        twilioMessengerConfiguration.authToken = null;
      } 
    } 
    return "input";
  }
  
  public String post() {
    BaseMessengerConfiguration baseMessengerConfiguration1 = ((MessengerResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveMessenger(this.messengerId))).messenger;
    this.messenger.data.clear();
    this.messenger.data.putAll(baseMessengerConfiguration1.data);
    if (this.type == MessengerType.Twilio && this.editAuthTokenOption == BaseFormAction.EditAuthTokenOption.useExisting) {
      BaseMessengerConfiguration baseMessengerConfiguration = this.messenger;
      if (baseMessengerConfiguration instanceof TwilioMessengerConfiguration) {
        TwilioMessengerConfiguration twilioMessengerConfiguration = (TwilioMessengerConfiguration)baseMessengerConfiguration;
        twilioMessengerConfiguration.authToken = ((TwilioMessengerConfiguration)baseMessengerConfiguration1).authToken;
      } 
    } 
    BaseMessengerConfiguration baseMessengerConfiguration2 = ((MessengerResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.updateMessenger(this.messengerId, new MessengerRequest(this.messenger)))).messenger;
    writeAuditLogForUpdate("Updated the messenger with Id [" + String.valueOf(this.messengerId) + "] and name [" + baseMessengerConfiguration2.name + "]", baseMessengerConfiguration1, baseMessengerConfiguration2);
    return "success";
  }
}
