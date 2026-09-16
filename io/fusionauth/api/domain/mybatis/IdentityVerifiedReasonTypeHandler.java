package io.fusionauth.api.domain.mybatis;

import io.fusionauth.domain.IdentityVerifiedReason;
import org.apache.ibatis.type.EnumOrdinalTypeHandler;

public class IdentityVerifiedReasonTypeHandler extends EnumOrdinalTypeHandler<IdentityVerifiedReason> {
  public IdentityVerifiedReasonTypeHandler() {
    super(IdentityVerifiedReason.class);
  }
}
