package io.fusionauth.api.domain.mybatis;

import io.fusionauth.domain.Key;
import org.apache.ibatis.type.EnumTypeHandler;

public class KeyTypeTypeHandler extends EnumTypeHandler<Key.KeyType> {
  public KeyTypeTypeHandler() {
    super(Key.KeyType.class);
  }
}
