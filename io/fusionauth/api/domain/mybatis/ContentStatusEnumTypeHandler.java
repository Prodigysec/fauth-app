package io.fusionauth.api.domain.mybatis;

import io.fusionauth.domain.ContentStatus;
import org.apache.ibatis.type.EnumOrdinalTypeHandler;

public class ContentStatusEnumTypeHandler extends EnumOrdinalTypeHandler<ContentStatus> {
  public ContentStatusEnumTypeHandler() {
    super(ContentStatus.class);
  }
}
