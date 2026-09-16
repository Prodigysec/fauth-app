package io.fusionauth.domain.connector;

import com.inversoft.json.ToString;
import io.fusionauth.domain.Buildable;

public class FusionAuthConnectorConfiguration extends BaseConnectorConfiguration implements Buildable<FusionAuthConnectorConfiguration> {
  public ConnectorType getType() {
    return ConnectorType.FusionAuth;
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
