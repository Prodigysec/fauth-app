package io.fusionauth.api.domain.mybatis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.inversoft.mybatis.BaseJSONTypeHandler;
import io.fusionauth.usagestats.shared.domain.UsageStats;

public class UsageStatsTimingsTypeHandler extends BaseJSONTypeHandler<UsageStats.Timings> {
  @Inject
  public UsageStatsTimingsTypeHandler(@Named("DatabaseObjectMapper") ObjectMapper paramObjectMapper) {
    super(paramObjectMapper, UsageStats.Timings.class);
  }
}
