package io.fusionauth.api.domain.mybatis;

import io.fusionauth.domain.connector.ConnectorType;
import org.apache.ibatis.type.EnumOrdinalTypeHandler;

public class ConnectorTypeTypeHandler extends EnumOrdinalTypeHandler<ConnectorType> {
  public ConnectorTypeTypeHandler() {
    super(ConnectorType.class);
  }
}
