package io.fusionauth.api.domain.mybatis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.inversoft.mybatis.BaseJSONTypeHandler;
import io.fusionauth.domain.jwt.RefreshToken;

public class RefreshTokenMetaDataTypeHandler extends BaseJSONTypeHandler<RefreshToken.MetaData> {
  @Inject
  public RefreshTokenMetaDataTypeHandler(@Named("DatabaseObjectMapper") ObjectMapper paramObjectMapper) {
    super(paramObjectMapper, RefreshToken.MetaData.class);
  }
}
