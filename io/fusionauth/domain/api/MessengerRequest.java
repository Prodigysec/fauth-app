package io.fusionauth.domain.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.inversoft.json.JacksonConstructor;
import io.fusionauth.client.json.MessengerRequestDeserializer;
import io.fusionauth.domain.messenger.BaseMessengerConfiguration;

@JsonDeserialize(using = MessengerRequestDeserializer.class)
public class MessengerRequest {
  public BaseMessengerConfiguration messenger;
  
  @JacksonConstructor
  public MessengerRequest() {}
  
  public MessengerRequest(BaseMessengerConfiguration paramBaseMessengerConfiguration) {
    this.messenger = paramBaseMessengerConfiguration;
  }
}
