package io.fusionauth.app.action.ajax.messenger.twilio;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import com.inversoft.validator.Validator;
import io.fusionauth.api.service.messenger.MessengerTestService;
import io.fusionauth.app.action.admin.messenger.BaseFormAction;
import io.fusionauth.app.action.ajax.messenger.BaseTestAJAXAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.api.MessengerResponse;
import io.fusionauth.domain.messenger.BaseMessengerConfiguration;
import io.fusionauth.domain.messenger.TwilioMessengerConfiguration;
import java.util.Objects;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(requiresAuthentication = true, constraints = {"admin", "system_manager"})
public class TestAction extends BaseTestAJAXAction {
  public final TwilioMessengerConfiguration messenger = new TwilioMessengerConfiguration();
  
  private final MessengerTestService messengerTestService;
  
  public BaseFormAction.EditAuthTokenOption editAuthTokenOption;
  
  @JSONResponse
  public MessengerTestService.MessengerTestResult response;
  
  public String testNumber;
  
  @Inject
  public TestAction(FrontEndSupport paramFrontEndSupport, MessengerTestService paramMessengerTestService) {
    super(paramFrontEndSupport);
    this.messengerTestService = paramMessengerTestService;
  }
  
  public String post() {
    this.messenger.id = this.messengerId;
    this.response = this.messengerTestService.testTwilio(this.messenger, this.testNumber);
    return "render-json";
  }
  
  @ValidationMethod
  public void validate() {
    if (this.editAuthTokenOption == BaseFormAction.EditAuthTokenOption.useExisting) {
      BaseMessengerConfiguration baseMessengerConfiguration = ((MessengerResponse)superDelegate().execute(paramFusionAuthClient -> paramFusionAuthClient.retrieveMessenger(this.messengerId))).messenger;
      if (baseMessengerConfiguration instanceof TwilioMessengerConfiguration) {
        TwilioMessengerConfiguration twilioMessengerConfiguration = (TwilioMessengerConfiguration)baseMessengerConfiguration;
        this.messenger.authToken = twilioMessengerConfiguration.authToken;
      } 
    } 
    Objects.requireNonNull(this.frontEndSupport);
    (new Validator()).notMissing(this.messenger.url, "messenger.url", new Object[0]).notMissing(this.messenger.accountSID, "messenger.accountSID", new Object[0]).notMissing(this.messenger.authToken, "messenger.authToken", new Object[0]).ifTrue((this.messenger.messagingServiceSid == null), paramValidator -> paramValidator.notMissing(this.messenger.fromPhoneNumber, "messenger.fromPhoneNumber", new Object[0])).notBlank(this.testNumber, "testNumber", new Object[0]).done(this.frontEndSupport::transfer);
  }
}
