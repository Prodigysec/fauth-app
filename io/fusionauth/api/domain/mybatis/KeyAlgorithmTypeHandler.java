package io.fusionauth.api.domain.mybatis;

import io.fusionauth.domain.Key;
import org.apache.ibatis.type.EnumTypeHandler;

public class KeyAlgorithmTypeHandler extends EnumTypeHandler<Key.KeyAlgorithm> {
  public KeyAlgorithmTypeHandler() {
    super(Key.KeyAlgorithm.class);
  }
}
