package io.fusionauth.api.domain.mybatis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.inversoft.mybatis.BaseJSONTypeHandler;
import io.fusionauth.domain.LocalizedStrings;

public class LocalizedStringsTypeHandler extends BaseJSONTypeHandler<LocalizedStrings> {
  @Inject
  public LocalizedStringsTypeHandler(@Named("DatabaseObjectMapper") ObjectMapper paramObjectMapper) {
    super(paramObjectMapper, LocalizedStrings.class);
  }
}
