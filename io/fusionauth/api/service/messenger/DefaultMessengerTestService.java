package io.fusionauth.api.service.messenger;

import com.google.inject.Inject;
import io.fusionauth.api.domain.message.SendMessageResult;
import io.fusionauth.api.service.messenger.generic.GenericMessenger;
import io.fusionauth.api.service.messenger.kafka.KafkaMessenger;
import io.fusionauth.api.service.messenger.twilio.TwilioMessenger;
import io.fusionauth.domain.message.Message;
import io.fusionauth.domain.message.MessageType;
import io.fusionauth.domain.message.sms.SMSMessage;
import io.fusionauth.domain.message.voice.VoiceMessage;
import io.fusionauth.domain.messenger.BaseMessengerConfiguration;
import io.fusionauth.domain.messenger.GenericMessengerConfiguration;
import io.fusionauth.domain.messenger.KafkaMessengerConfiguration;
import io.fusionauth.domain.messenger.MessengerType;
import io.fusionauth.domain.messenger.TwilioMessengerConfiguration;
import java.util.UUID;
import java.util.function.Supplier;

public class DefaultMessengerTestService implements MessengerTestService {
  private final MessengerProvider messengerProvider;
  
  @Inject
  public DefaultMessengerTestService(MessengerProvider paramMessengerProvider) {
    this.messengerProvider = paramMessengerProvider;
  }
  
  private static Message createTestMessage(BaseMessengerConfiguration paramBaseMessengerConfiguration, String paramString1, String paramString2) {
    if (paramBaseMessengerConfiguration.messageTypes.isEmpty() || paramBaseMessengerConfiguration.messageTypes.contains(MessageType.SMS)) {
      SMSMessage sMSMessage = new SMSMessage();
      sMSMessage.textMessage = paramString2;
      sMSMessage.phoneNumber = paramString1;
      sMSMessage.code = "123456";
      sMSMessage.userId = UUID.fromString("00000000-0000-0000-0000-000000000001");
      return sMSMessage;
    } 
    if (paramBaseMessengerConfiguration.messageTypes.contains(MessageType.Voice)) {
      VoiceMessage voiceMessage = new VoiceMessage();
      voiceMessage.message = paramString2;
      voiceMessage.phoneNumber = paramString1;
      voiceMessage.code = "123456";
      voiceMessage.userId = UUID.fromString("00000000-0000-0000-0000-000000000001");
      return voiceMessage;
    } 
    throw new IllegalStateException("Invalid messenger configuration.");
  }
  
  private static MessengerTestService.MessengerTestResult handleSend(Supplier<SendMessageResult> paramSupplier) {
    MessengerTestService.MessengerTestResult messengerTestResult = new MessengerTestService.MessengerTestResult();
    try {
      SendMessageResult sendMessageResult = paramSupplier.get();
      messengerTestResult.status = Integer.valueOf(sendMessageResult.httpStatus);
      messengerTestResult.success = true;
    } catch (MessengerException messengerException) {
      messengerTestResult.exception = messengerException.getCause();
      messengerTestResult.status = messengerException.httpStatus;
      messengerTestResult.message = messengerException.errorResponse;
      messengerTestResult.success = false;
    } 
    return messengerTestResult;
  }
  
  public MessengerTestService.MessengerTestResult testGeneric(GenericMessengerConfiguration paramGenericMessengerConfiguration) {
    Message message = createTestMessage(paramGenericMessengerConfiguration, "(555) 555-5555", "Testing");
    GenericMessenger genericMessenger = (GenericMessenger)this.messengerProvider.get(MessengerType.Generic);
    return handleSend(() -> paramGenericMessenger.send(paramMessage, paramGenericMessengerConfiguration))

      
      .with(paramMessengerTestResult -> paramMessengerTestResult.message = paramMessengerTestResult.success ? null : "Messenger %s [%s] returned response code [%d]\n".formatted(new Object[] { paramGenericMessengerConfiguration.name, paramGenericMessengerConfiguration.url, paramMessengerTestResult.status }));
  }
  
  public MessengerTestService.MessengerTestResult testKafka(KafkaMessengerConfiguration paramKafkaMessengerConfiguration) {
    SMSMessage sMSMessage = new SMSMessage();
    sMSMessage.textMessage = "You have configured Kafka successfully.";
    sMSMessage.phoneNumber = "(555) 555-5555";
    KafkaMessenger kafkaMessenger = (KafkaMessenger)this.messengerProvider.get(MessengerType.Kafka);
    return handleSend(() -> paramKafkaMessenger.send(paramSMSMessage, paramKafkaMessengerConfiguration));
  }
  
  public MessengerTestService.MessengerTestResult testTwilio(TwilioMessengerConfiguration paramTwilioMessengerConfiguration, String paramString) {
    Message message = createTestMessage(paramTwilioMessengerConfiguration, paramString, "You have configured Twilio successfully.");
    TwilioMessenger twilioMessenger = (TwilioMessenger)this.messengerProvider.get(MessengerType.Twilio);
    return handleSend(() -> paramTwilioMessenger.send(paramMessage, paramTwilioMessengerConfiguration));
  }
}
