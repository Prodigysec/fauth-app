package io.fusionauth.api.domain.mybatis;

import io.fusionauth.api.domain.ExternalIdentifier;
import org.apache.ibatis.type.EnumOrdinalTypeHandler;

public class ExternalIdTypeHandler extends EnumOrdinalTypeHandler<ExternalIdentifier.ExternalIdType> {
  public ExternalIdTypeHandler() {
    super(ExternalIdentifier.ExternalIdType.class);
  }
}
