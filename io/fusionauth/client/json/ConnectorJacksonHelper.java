package io.fusionauth.client.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import io.fusionauth.domain.connector.BaseConnectorConfiguration;
import io.fusionauth.domain.connector.ConnectorType;
import io.fusionauth.domain.connector.FusionAuthConnectorConfiguration;
import io.fusionauth.domain.connector.GenericConnectorConfiguration;
import io.fusionauth.domain.connector.LDAPConnectorConfiguration;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ConnectorJacksonHelper {
  public static ConnectorType extractType(DeserializationContext paramDeserializationContext, JsonParser paramJsonParser, JsonNode paramJsonNode) throws IOException {
    JsonNode jsonNode = paramJsonNode.at("/type");
    String str = jsonNode.asText(ConnectorType.Generic.name());
    ConnectorType connectorType = ConnectorType.safeValueOf(str);
    if (connectorType == null) {
      String str1 = Arrays.<ConnectorType>stream(ConnectorType.values()).map(Enum::name).sorted().collect(Collectors.joining(", "));
      return (ConnectorType)paramDeserializationContext.handleUnexpectedToken(BaseConnectorConfiguration.class, jsonNode.asToken(), paramJsonParser, "Expected the type field to be one of [" + str1 + "], but found [" + jsonNode
          .asText() + "]", new Object[0]);
    } 
    return connectorType;
  }
  
  public static BaseConnectorConfiguration newConnector(ConnectorType paramConnectorType) {
    switch (paramConnectorType) {
      case FusionAuth:
        return new FusionAuthConnectorConfiguration();
      case Generic:
        return new GenericConnectorConfiguration();
      case LDAP:
        return new LDAPConnectorConfiguration();
    } 
    throw new IllegalStateException("Unexpected type [" + String.valueOf(paramConnectorType) + "]. This is a FusionAuth bug, someone forgot to add a case statement for a new type.");
  }
}
