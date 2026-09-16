package io.fusionauth.domain.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.inversoft.json.JacksonConstructor;
import io.fusionauth.client.json.ConnectorResponseDeserializer;
import io.fusionauth.domain.connector.BaseConnectorConfiguration;
import java.util.List;

@JsonDeserialize(using = ConnectorResponseDeserializer.class)
public class ConnectorResponse {
  public BaseConnectorConfiguration connector;
  
  public List<BaseConnectorConfiguration> connectors;
  
  @JacksonConstructor
  public ConnectorResponse() {}
  
  public ConnectorResponse(BaseConnectorConfiguration paramBaseConnectorConfiguration) {
    this.connector = paramBaseConnectorConfiguration;
  }
  
  public ConnectorResponse(List<BaseConnectorConfiguration> paramList) {
    this.connectors = paramList;
  }
}
