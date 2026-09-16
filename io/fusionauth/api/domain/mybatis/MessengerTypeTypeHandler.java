package io.fusionauth.api.domain.mybatis;

import io.fusionauth.domain.messenger.MessengerType;
import org.apache.ibatis.type.EnumOrdinalTypeHandler;

public class MessengerTypeTypeHandler extends EnumOrdinalTypeHandler<MessengerType> {
  public MessengerTypeTypeHandler() {
    super(MessengerType.class);
  }
}
