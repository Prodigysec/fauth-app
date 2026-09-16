package io.fusionauth.api.domain.mybatis;

import io.fusionauth.domain.BreachedPasswordStatus;
import org.apache.ibatis.type.EnumOrdinalTypeHandler;

public class BreachedPasswordStatusEnumTypeHandler extends EnumOrdinalTypeHandler<BreachedPasswordStatus> {
  public BreachedPasswordStatusEnumTypeHandler() {
    super(BreachedPasswordStatus.class);
  }
}
