package io.fusionauth.api.domain.mybatis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.inversoft.mybatis.BaseJSONTypeHandler;
import io.fusionauth.domain.HTTPHeaders;

public class HTTPHeadersTypeHandler extends BaseJSONTypeHandler<HTTPHeaders> {
  @Inject
  public HTTPHeadersTypeHandler(@Named("DatabaseObjectMapper") ObjectMapper paramObjectMapper) {
    super(paramObjectMapper, HTTPHeaders.class);
  }
}
