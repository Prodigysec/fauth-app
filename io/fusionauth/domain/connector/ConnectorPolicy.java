package io.fusionauth.domain.connector;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonMerge;
import com.fasterxml.jackson.annotation.OptBoolean;
import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import com.inversoft.mybatis.JSONColumn;
import com.inversoft.mybatis.JSONColumnable;
import io.fusionauth.domain.Buildable;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class ConnectorPolicy implements Buildable<ConnectorPolicy>, JSONColumnable {
  public UUID connectorId = BaseConnectorConfiguration.FUSIONAUTH_CONNECTOR_ID;
  
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public Map<String, Object> data = new LinkedHashMap<>();
  
  @JSONColumn
  @JsonMerge(OptBoolean.FALSE)
  public Set<String> domains = new HashSet<>(
      
      Collections.singletonList("*"));
  
  @JSONColumn
  public boolean migrate;
  
  @JacksonConstructor
  public ConnectorPolicy() {}
  
  public ConnectorPolicy(ConnectorPolicy paramConnectorPolicy) {
    this.migrate = paramConnectorPolicy.migrate;
    this.connectorId = paramConnectorPolicy.connectorId;
    this.data.putAll(paramConnectorPolicy.data);
    this.domains = new HashSet<>(paramConnectorPolicy.domains);
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof ConnectorPolicy))
      return false; 
    ConnectorPolicy connectorPolicy = (ConnectorPolicy)paramObject;
    return (this.migrate == connectorPolicy.migrate && 
      Objects.equals(this.connectorId, connectorPolicy.connectorId) && 
      Objects.equals(this.data, connectorPolicy.data) && 
      Objects.equals(this.domains, connectorPolicy.domains));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Boolean.valueOf(this.migrate), this.connectorId, this.data, this.domains });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
