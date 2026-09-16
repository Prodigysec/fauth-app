package io.fusionauth.domain.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.inversoft.json.JacksonConstructor;
import io.fusionauth.client.json.ConnectorRequestDeserializer;
import io.fusionauth.domain.connector.BaseConnectorConfiguration;

@JsonDeserialize(using = ConnectorRequestDeserializer.class)
public class ConnectorRequest {
  public BaseConnectorConfiguration connector;
  
  @JacksonConstructor
  public ConnectorRequest() {}
  
  public ConnectorRequest(BaseConnectorConfiguration paramBaseConnectorConfiguration) {
    this.connector = paramBaseConnectorConfiguration;
  }
}
