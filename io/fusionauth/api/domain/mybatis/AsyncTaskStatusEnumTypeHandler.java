package io.fusionauth.api.domain.mybatis;

import com.google.inject.Inject;
import io.fusionauth.api.domain.AsyncTask;
import org.apache.ibatis.type.EnumOrdinalTypeHandler;

public class AsyncTaskStatusEnumTypeHandler extends EnumOrdinalTypeHandler<AsyncTask.AsyncTaskStatus> {
  @Inject
  public AsyncTaskStatusEnumTypeHandler() {
    super(AsyncTask.AsyncTaskStatus.class);
  }
}
