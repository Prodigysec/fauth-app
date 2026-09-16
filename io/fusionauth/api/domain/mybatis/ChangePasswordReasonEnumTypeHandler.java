package io.fusionauth.api.domain.mybatis;

import io.fusionauth.domain.ChangePasswordReason;
import org.apache.ibatis.type.EnumOrdinalTypeHandler;

public class ChangePasswordReasonEnumTypeHandler extends EnumOrdinalTypeHandler<ChangePasswordReason> {
  public ChangePasswordReasonEnumTypeHandler() {
    super(ChangePasswordReason.class);
  }
}
