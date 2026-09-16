package io.fusionauth.api.domain.mybatis;

import io.fusionauth.domain.EventLogType;
import org.apache.ibatis.type.EnumOrdinalTypeHandler;

public class EventLogTypeTypeHandler extends EnumOrdinalTypeHandler<EventLogType> {
  public EventLogTypeTypeHandler() {
    super(EventLogType.class);
  }
}
