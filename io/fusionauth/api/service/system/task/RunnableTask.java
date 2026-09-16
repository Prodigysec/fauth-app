package io.fusionauth.api.service.system.task;

import io.fusionauth.api.domain.AsyncTask;

public interface RunnableTask {
  Runnable get(AsyncTask paramAsyncTask);
  
  boolean isComplete(AsyncTask paramAsyncTask);
  
  void resetEntityState(AsyncTask paramAsyncTask);
}
