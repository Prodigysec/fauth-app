package io.fusionauth.api.service.reindex;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import com.inversoft.error.Errors;
import com.inversoft.validator.Validator;
import io.fusionauth.api.configuration.FusionAuthConfiguration;
import io.fusionauth.api.service.lock.ReindexDistributedLock;
import io.fusionauth.domain.Entity;
import io.fusionauth.domain.User;
import org.apache.ibatis.session.SqlSessionFactory;

public class DefaultReindexService implements ReindexService {
  private final SqlSessionFactory backgroundSQLSessionFactory;
  
  private final FusionAuthConfiguration configuration;
  
  private final Injector injector;
  
  private final ReindexDistributedLock reindexDistributedLock;
  
  @Inject
  public DefaultReindexService(@Named("background") SqlSessionFactory paramSqlSessionFactory, FusionAuthConfiguration paramFusionAuthConfiguration, Injector paramInjector, ReindexDistributedLock paramReindexDistributedLock) {
    this.backgroundSQLSessionFactory = paramSqlSessionFactory;
    this.configuration = paramFusionAuthConfiguration;
    this.injector = paramInjector;
    this.reindexDistributedLock = paramReindexDistributedLock;
  }
  
  public boolean inProgress() {
    return this.reindexDistributedLock.isLocked();
  }
  
  public Thread reindexEntities() {
    Thread thread = new Thread(new ReindexRunner<>(this.configuration, this.backgroundSQLSessionFactory, this.injector, this.reindexDistributedLock, Entity.class));
    thread.setDaemon(true);
    thread.start();
    return thread;
  }
  
  public Thread reindexUsers() {
    Thread thread = new Thread(new ReindexRunner<>(this.configuration, this.backgroundSQLSessionFactory, this.injector, this.reindexDistributedLock, User.class));
    thread.setDaemon(true);
    thread.start();
    return thread;
  }
  
  public Errors validate(String paramString) {
    return (new Validator())
      .notBlank(paramString, "index", new Object[0])
      .ifLastCheckHadNoError(paramValidator -> paramValidator.ensure((paramString.equals("fusionauth_user") || paramString.equals("fusionauth_entity")), "index", "[invalid]", new Object[0]))
      .ifNoFieldErrors("index", paramValidator -> paramValidator.ensure(!this.reindexDistributedLock.isLocked(), "index", "[inProgress]", new Object[0]))
      .done();
  }
}
