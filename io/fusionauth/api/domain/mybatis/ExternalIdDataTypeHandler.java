package io.fusionauth.api.domain.mybatis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.inversoft.mybatis.BaseJSONTypeHandler;
import io.fusionauth.api.domain.ExternalIdentifier;

public class ExternalIdDataTypeHandler extends BaseJSONTypeHandler<ExternalIdentifier.ExternalIdData> {
  @Inject
  public ExternalIdDataTypeHandler(@Named("DatabaseObjectMapper") ObjectMapper paramObjectMapper) {
    super(paramObjectMapper, ExternalIdentifier.ExternalIdData.class);
  }
}
