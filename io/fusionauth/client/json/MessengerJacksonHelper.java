package io.fusionauth.client.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import io.fusionauth.domain.connector.BaseConnectorConfiguration;
import io.fusionauth.domain.messenger.BaseMessengerConfiguration;
import io.fusionauth.domain.messenger.GenericMessengerConfiguration;
import io.fusionauth.domain.messenger.KafkaMessengerConfiguration;
import io.fusionauth.domain.messenger.MessengerType;
import io.fusionauth.domain.messenger.TwilioMessengerConfiguration;
import java.io.IOException;
import java.util.Arrays;

public class MessengerJacksonHelper {
  public static MessengerType extractType(DeserializationContext paramDeserializationContext, JsonParser paramJsonParser, JsonNode paramJsonNode) throws IOException {
    JsonNode jsonNode = paramJsonNode.at("/type");
    String str = jsonNode.asText(MessengerType.Twilio.name());
    MessengerType messengerType = MessengerType.safeValueOf(str);
    if (messengerType == null)
      return (MessengerType)paramDeserializationContext.handleUnexpectedToken(BaseConnectorConfiguration.class, jsonNode.asToken(), paramJsonParser, "Expected the type field to be one of " + 
          
          String.join(",", new CharSequence[] { Arrays.toString((Object[])MessengerType.values()) + ", but found [" + Arrays.toString((Object[])MessengerType.values()) + "]" }), new Object[0]); 
    return messengerType;
  }
  
  public static BaseMessengerConfiguration newMessenger(MessengerType paramMessengerType) {
    switch (paramMessengerType) {
      case Generic:
        return new GenericMessengerConfiguration();
      case Twilio:
        return new TwilioMessengerConfiguration();
      case Kafka:
        return new KafkaMessengerConfiguration();
    } 
    throw new IllegalStateException("Unexpected type [" + String.valueOf(paramMessengerType) + "]. This is a FusionAuth bug, someone forgot to add a case statement for a new type.");
  }
}
