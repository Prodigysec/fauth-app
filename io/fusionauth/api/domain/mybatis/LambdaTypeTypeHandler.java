package io.fusionauth.api.domain.mybatis;

import io.fusionauth.domain.LambdaType;
import org.apache.ibatis.type.EnumOrdinalTypeHandler;

public class LambdaTypeTypeHandler extends EnumOrdinalTypeHandler<LambdaType> {
  public LambdaTypeTypeHandler() {
    super(LambdaType.class);
  }
}
