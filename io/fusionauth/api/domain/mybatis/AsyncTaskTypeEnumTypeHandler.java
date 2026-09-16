package io.fusionauth.api.domain.mybatis;

import com.google.inject.Inject;
import io.fusionauth.api.domain.AsyncTask;
import org.apache.ibatis.type.EnumOrdinalTypeHandler;

public class AsyncTaskTypeEnumTypeHandler extends EnumOrdinalTypeHandler<AsyncTask.AsyncTaskType> {
  @Inject
  public AsyncTaskTypeEnumTypeHandler() {
    super(AsyncTask.AsyncTaskType.class);
  }
}
