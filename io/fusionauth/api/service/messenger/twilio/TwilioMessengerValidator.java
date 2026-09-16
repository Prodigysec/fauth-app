package io.fusionauth.api.service.messenger.twilio;

import com.inversoft.error.Errors;
import com.inversoft.validator.Validator;
import io.fusionauth.api.service.messenger.MessengerValidator;
import io.fusionauth.domain.message.MessageType;
import io.fusionauth.domain.messenger.BaseMessengerConfiguration;
import io.fusionauth.domain.messenger.TwilioMessengerConfiguration;

public class TwilioMessengerValidator implements MessengerValidator {
  public Errors validate(BaseMessengerConfiguration paramBaseMessengerConfiguration) {
    TwilioMessengerConfiguration twilioMessengerConfiguration = (TwilioMessengerConfiguration)paramBaseMessengerConfiguration;
    boolean bool1 = (twilioMessengerConfiguration.messageTypes != null && twilioMessengerConfiguration.messageTypes.contains(MessageType.Voice)) ? true : false;
    boolean bool2 = (twilioMessengerConfiguration.fromPhoneNumber != null || twilioMessengerConfiguration.messagingServiceSid != null) ? true : false;
    return (new Validator())
      
      .notMissing(twilioMessengerConfiguration.url, "messenger.url", new Object[0])
      .ifLastCheckHadNoError(paramValidator -> paramValidator.validAbsoluteHttpURL(paramTwilioMessengerConfiguration.url, "messenger.url", new Object[] { paramTwilioMessengerConfiguration.url })).notBlank(twilioMessengerConfiguration.accountSID, "messenger.accountSID", new Object[0])

      
      .notBlank(twilioMessengerConfiguration.authToken, "messenger.authToken", new Object[0])

      
      .ifFalse(bool1, paramValidator -> paramValidator.ensure(paramBoolean, "messenger.fromPhoneNumber", "[blank]", new Object[0]).ensure(paramBoolean, "messenger.messagingServiceSid", "[blank]", new Object[0]))



      
      .ifTrue(bool1, paramValidator -> paramValidator.notBlank(paramTwilioMessengerConfiguration.fromPhoneNumber, "messenger.fromPhoneNumber", new Object[0]))


      
      .notEmpty(twilioMessengerConfiguration.messageTypes, "messenger.messageTypes", new Object[0])
      
      .done();
  }
}
