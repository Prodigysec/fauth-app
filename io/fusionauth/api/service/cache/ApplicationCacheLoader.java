package io.fusionauth.api.service.cache;

import com.google.inject.Inject;
import com.inversoft.cache.BaseCacheLoader;
import com.inversoft.cache.Cache;
import io.fusionauth.api.service.system.ApplicationReaderService;
import io.fusionauth.domain.Application;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ApplicationCacheLoader extends BaseCacheLoader<UUID, Application> implements Runnable {
  private final ApplicationReaderService applicationReader;
  
  private final ApplicationCache cache;
  
  @Inject
  public ApplicationCacheLoader(ApplicationReaderService paramApplicationReaderService, ApplicationCache paramApplicationCache) {
    super(Collections.emptyList());
    this.applicationReader = paramApplicationReaderService;
    this.cache = paramApplicationCache;
  }
  
  public void run() {
    load();
  }
  
  protected void internalLoad(Cache<UUID, Application> paramCache) {
    paramCache.replace((Map)this.applicationReader.retrieveAll(null, ApplicationReaderService.ApplicationExpansion.all()).stream().collect(Collectors.toMap(paramApplication -> paramApplication.id, paramApplication -> paramApplication)));
  }
  
  protected Cache<UUID, Application> resolveCache() {
    return (Cache<UUID, Application>)this.cache;
  }
}
