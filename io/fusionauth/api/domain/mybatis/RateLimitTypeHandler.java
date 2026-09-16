package io.fusionauth.api.domain.mybatis;

import io.fusionauth.domain.RateLimitedRequestType;
import org.apache.ibatis.type.EnumOrdinalTypeHandler;

public class RateLimitTypeHandler extends EnumOrdinalTypeHandler<RateLimitedRequestType> {
  public RateLimitTypeHandler() {
    super(RateLimitedRequestType.class);
  }
}
