package io.fusionauth.api.domain.mybatis;

import com.google.inject.Inject;
import io.fusionauth.domain.ObjectState;
import org.apache.ibatis.type.EnumTypeHandler;

public class ObjectStateEnumTypeHandler extends EnumTypeHandler<ObjectState> {
  @Inject
  public ObjectStateEnumTypeHandler() {
    super(ObjectState.class);
  }
}
