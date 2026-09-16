package io.fusionauth.api.domain.mybatis;

import io.fusionauth.domain.message.MessageType;
import org.apache.ibatis.type.EnumOrdinalTypeHandler;

public class MessageTypeHandler extends EnumOrdinalTypeHandler<MessageType> {
  public MessageTypeHandler() {
    super(MessageType.class);
  }
}
