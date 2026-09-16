package io.fusionauth.api.domain.mybatis;

import io.fusionauth.domain.SecureGeneratorType;
import org.apache.ibatis.type.EnumTypeHandler;

public class SecureGeneratorTypeTypeHandler extends EnumTypeHandler<SecureGeneratorType> {
  public SecureGeneratorTypeTypeHandler() {
    super(SecureGeneratorType.class);
  }
}
