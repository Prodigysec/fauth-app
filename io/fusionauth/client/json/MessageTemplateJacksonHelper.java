package io.fusionauth.client.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import io.fusionauth.domain.connector.BaseConnectorConfiguration;
import io.fusionauth.domain.message.MessageTemplate;
import io.fusionauth.domain.message.MessageType;
import io.fusionauth.domain.message.sms.SMSMessageTemplate;
import io.fusionauth.domain.message.voice.VoiceMessageTemplate;
import java.io.IOException;
import java.util.Arrays;

public class MessageTemplateJacksonHelper {
  public static MessageType extractType(DeserializationContext paramDeserializationContext, JsonParser paramJsonParser, JsonNode paramJsonNode) throws IOException {
    JsonNode jsonNode = paramJsonNode.at("/type");
    String str = jsonNode.asText();
    MessageType messageType = MessageType.safeValueOf(str);
    if (messageType == null)
      return (MessageType)paramDeserializationContext.handleUnexpectedToken(BaseConnectorConfiguration.class, jsonNode.asToken(), paramJsonParser, "Expected the type field to be one of " + 
          
          String.join(",", new CharSequence[] { Arrays.toString((Object[])MessageType.values()) + ", but found [" + Arrays.toString((Object[])MessageType.values()) + "]" }), new Object[0]); 
    return messageType;
  }
  
  public static MessageTemplate newMessageTemplate(MessageType paramMessageType) {
    switch (paramMessageType) {
      case SMS:
        return new SMSMessageTemplate();
      case Voice:
        return new VoiceMessageTemplate();
    } 
    throw new IllegalStateException("Unexpected type [" + String.valueOf(paramMessageType) + "]. This is a FusionAuth bug, someone forgot to add a case statement for a new type.");
  }
}
