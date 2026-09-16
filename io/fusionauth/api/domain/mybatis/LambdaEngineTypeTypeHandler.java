package io.fusionauth.api.domain.mybatis;

import io.fusionauth.domain.LambdaEngineType;
import org.apache.ibatis.type.EnumTypeHandler;

public class LambdaEngineTypeTypeHandler extends EnumTypeHandler<LambdaEngineType> {
  public LambdaEngineTypeTypeHandler() {
    super(LambdaEngineType.class);
  }
}
