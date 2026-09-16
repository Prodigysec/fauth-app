package io.fusionauth.domain.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.inversoft.json.JacksonConstructor;
import io.fusionauth.client.json.MessengerResponseDeserializer;
import io.fusionauth.domain.messenger.BaseMessengerConfiguration;
import java.util.List;

@JsonDeserialize(using = MessengerResponseDeserializer.class)
public class MessengerResponse {
  public BaseMessengerConfiguration messenger;
  
  public List<BaseMessengerConfiguration> messengers;
  
  @JacksonConstructor
  public MessengerResponse() {}
  
  public MessengerResponse(BaseMessengerConfiguration paramBaseMessengerConfiguration) {
    this.messenger = paramBaseMessengerConfiguration;
  }
  
  public MessengerResponse(List<BaseMessengerConfiguration> paramList) {
    this.messengers = paramList;
  }
}
