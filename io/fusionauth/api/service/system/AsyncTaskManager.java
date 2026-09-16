package io.fusionauth.api.service.system;

import io.fusionauth.api.domain.AsyncTask;

public interface AsyncTaskManager {
  boolean offer(AsyncTask paramAsyncTask);
}
