package io.fusionauth.api.domain.mybatis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.inversoft.mybatis.BaseJSONTypeHandler;
import io.fusionauth.domain.Integrations;

public class IntegrationsTypeHandler extends BaseJSONTypeHandler<Integrations> {
  @Inject
  public IntegrationsTypeHandler(@Named("DatabaseObjectMapper") ObjectMapper paramObjectMapper) {
    super(paramObjectMapper, Integrations.class);
  }
}
