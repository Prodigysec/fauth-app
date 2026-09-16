package io.fusionauth.api.domain.mybatis;

import io.fusionauth.domain.form.FormStepType;
import org.apache.ibatis.type.EnumOrdinalTypeHandler;

public class FormStepTypeEnumHandler extends EnumOrdinalTypeHandler<FormStepType> {
  public FormStepTypeEnumHandler() {
    super(FormStepType.class);
  }
}
