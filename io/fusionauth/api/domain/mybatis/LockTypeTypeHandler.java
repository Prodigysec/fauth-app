package io.fusionauth.api.domain.mybatis;

import io.fusionauth.api.domain.LockType;
import org.apache.ibatis.type.EnumTypeHandler;

public class LockTypeTypeHandler extends EnumTypeHandler<LockType> {
  public LockTypeTypeHandler() {
    super(LockType.class);
  }
}
