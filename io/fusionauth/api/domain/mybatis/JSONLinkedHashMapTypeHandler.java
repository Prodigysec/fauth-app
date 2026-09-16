package io.fusionauth.api.domain.mybatis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.inversoft.mybatis.BaseJSONTypeHandler;
import java.util.LinkedHashMap;

public class JSONLinkedHashMapTypeHandler extends BaseJSONTypeHandler<LinkedHashMap> {
  @Inject
  public JSONLinkedHashMapTypeHandler(@Named("DatabaseObjectMapper") ObjectMapper paramObjectMapper) {
    super(paramObjectMapper, LinkedHashMap.class);
  }
}
