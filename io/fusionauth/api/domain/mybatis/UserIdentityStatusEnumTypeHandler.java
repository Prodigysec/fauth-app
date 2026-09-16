package io.fusionauth.api.domain.mybatis;

import io.fusionauth.api.domain.UserIdentityStatus;
import org.apache.ibatis.type.EnumOrdinalTypeHandler;

public class UserIdentityStatusEnumTypeHandler extends EnumOrdinalTypeHandler<UserIdentityStatus> {
  public UserIdentityStatusEnumTypeHandler() {
    super(UserIdentityStatus.class);
  }
}
