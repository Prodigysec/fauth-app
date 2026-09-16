package io.fusionauth.api.domain.mybatis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.inversoft.mybatis.BaseJSONTypeHandler;
import io.fusionauth.domain.CleanSpeakConfiguration;

public class CleanSpeakConfigurationTypeHandler extends BaseJSONTypeHandler<CleanSpeakConfiguration> {
  @Inject
  public CleanSpeakConfigurationTypeHandler(@Named("DatabaseObjectMapper") ObjectMapper paramObjectMapper) {
    super(paramObjectMapper, CleanSpeakConfiguration.class);
  }
}
