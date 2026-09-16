package io.fusionauth.api.service.system.task;

import com.google.inject.Injector;
import io.fusionauth.api.domain.AsyncTask;
import io.fusionauth.api.service.system.EventLogHelper;
import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.EventLogType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FailSafeRunnable implements Runnable {
  private static final Logger logger = LoggerFactory.getLogger(FailSafeRunnable.class);
  
  private final Injector injector;
  
  private final Runnable runnable;
  
  private final AsyncTask task;
  
  public FailSafeRunnable(Injector paramInjector, Runnable paramRunnable, AsyncTask paramAsyncTask) {
    this.injector = paramInjector;
    this.runnable = paramRunnable;
    this.task = paramAsyncTask;
  }
  
  public void run() {
    try {
      this.runnable.run();
    } catch (Throwable throwable) {
      try {
        String str = "A [" + this.task.type.name() + "] async task threw an exception.";
        logger.error(str, throwable);
        switch (this.task.type) {
          case DeleteTenant:
            ((DeleteTenantTask)this.injector.getInstance(DeleteTenantTask.class)).resetEntityState(this.task);
            break;
        } 
        EventLogHelper.create(new EventLog(EventLogType.Error, str, throwable));
      } catch (Throwable throwable1) {
        logger.error("Failed to create an event log to indicate why an async task failed.", throwable1);
      } 
    } 
  }
}
