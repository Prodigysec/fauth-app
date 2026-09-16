package io.fusionauth.api.domain.mybatis;

import io.fusionauth.domain.FamilyMember;
import org.apache.ibatis.type.EnumOrdinalTypeHandler;

public class FamilyRoleTypeHandler extends EnumOrdinalTypeHandler<FamilyMember.FamilyRole> {
  public FamilyRoleTypeHandler() {
    super(FamilyMember.FamilyRole.class);
  }
}
