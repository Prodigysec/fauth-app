package io.fusionauth.api.domain.mybatis;

import io.fusionauth.domain.provider.IdentityProviderType;
import org.apache.ibatis.type.EnumTypeHandler;

public class IdentityProviderTypeHandler extends EnumTypeHandler<IdentityProviderType> {
  public IdentityProviderTypeHandler() {
    super(IdentityProviderType.class);
  }
}
