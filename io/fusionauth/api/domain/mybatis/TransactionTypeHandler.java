package io.fusionauth.api.domain.mybatis;

import io.fusionauth.domain.TransactionType;
import org.apache.ibatis.type.EnumOrdinalTypeHandler;

public class TransactionTypeHandler extends EnumOrdinalTypeHandler<TransactionType> {
  public TransactionTypeHandler() {
    super(TransactionType.class);
  }
}
