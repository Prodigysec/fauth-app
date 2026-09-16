package io.fusionauth.api.domain.mybatis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.inversoft.mybatis.BaseJSONTypeHandler;
import io.fusionauth.usagestats.shared.domain.UsageStats;

public class UsageStatsStatsTypeHandler extends BaseJSONTypeHandler<UsageStats.Stats> {
  @Inject
  public UsageStatsStatsTypeHandler(@Named("DatabaseObjectMapper") ObjectMapper paramObjectMapper) {
    super(paramObjectMapper, UsageStats.Stats.class);
  }
}
