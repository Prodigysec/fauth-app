package io.fusionauth.api.domain.mybatis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.inversoft.mybatis.BaseJSONTypeHandler;
import io.fusionauth.domain.LogHistory;

public class LogHistoryTypeHandler extends BaseJSONTypeHandler<LogHistory> {
  @Inject
  public LogHistoryTypeHandler(@Named("DatabaseObjectMapper") ObjectMapper paramObjectMapper) {
    super(paramObjectMapper, LogHistory.class);
  }
}
