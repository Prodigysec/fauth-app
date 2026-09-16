package io.fusionauth.api.domain.mybatis;

import io.fusionauth.domain.form.FormType;
import org.apache.ibatis.type.EnumOrdinalTypeHandler;

public class FormTypeEnumTypeHandler extends EnumOrdinalTypeHandler<FormType> {
  public FormTypeEnumTypeHandler() {
    super(FormType.class);
  }
}
